/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alexei Goncharov (Polarion Software) - Closing project with file in editor and reopening project generates NPE (bug 246147)
 *******************************************************************************/

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNErrorCodes;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.history.SVNFileHistoryProvider;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.HiddenException;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.management.DisconnectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation.LocationReferenceTypeEnum;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * IProject attachment, that allows us to associate a project with a repository
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProvider extends RepositoryProvider implements IConnectedProjectInformation {
	public final static QualifiedName RESOURCE_PROPERTY = new QualifiedName("org.eclipse.team.svn", "resource"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static QualifiedName LOCATION_PROPERTY = new QualifiedName("org.eclipse.team.svn", "location"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static QualifiedName VERIFY_TAG_ON_COMMIT_PROPERTY = new QualifiedName("org.eclipse.team.svn", "verifyTagOnCommit"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public final static boolean DEFAULT_VERIFY_TAG_ON_COMMIT = true;

	protected IRepositoryLocation location;
	protected IRepositoryResource resource;
	protected String relocatedTo;
	protected String locationId;
	protected int errorCode;
	protected int state;

	public SVNTeamProvider() {
		super();
		this.state = 0;
	}
	
	public synchronized IRepositoryLocation getRepositoryLocation() throws HiddenException {
		if (this.state != 1) {
			this.restoreLocation();
		}
		return this.location;
	}
	
	public synchronized IRepositoryResource getRepositoryResource() throws HiddenException {
		if (this.state != 1) {
			this.connectToProject();
		}
		return this.resource;
	}
	
	public synchronized void switchResource(IRepositoryResource resource) throws CoreException {
		//does not affect finite automate state
		this.resource = SVNUtility.copyOf(resource);
		this.location = resource.getRepositoryLocation();
		SVNTeamProvider.setRepositoryLocation(this.getProject(), this.location);
	}
	
	public synchronized void relocateResource() throws CoreException {
		//does not affect finite automate state
		if (this.state != 1) {
			this.restoreLocation();
		}
		SVNTeamProvider.setRepositoryLocation(this.getProject(), this.location);
	}
	
	public static void map(IProject project, IRepositoryResource resource) throws CoreException {
		SVNTeamProvider.setRepositoryLocation(project, resource.getRepositoryLocation());
		RepositoryProvider.map(project, SVNTeamPlugin.NATURE_ID);
	}
	
	public String getID() {
		return SVNTeamPlugin.NATURE_ID;
	}

	public void configureProject() {
		this.connectToProject();
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(new IResource[] {this.getProject()}, IResource.DEPTH_ZERO, ResourceStatesChangedEvent.CHANGED_NODES));		
	}

	public void deconfigure() throws CoreException {

	}

	public IMoveDeleteHook getMoveDeleteHook() {
		return new SVNTeamMoveDeleteHook();
	}
	
    public FileModificationValidator getFileModificationValidator2() {
        return SVNTeamPlugin.instance().getOptionProvider().getFileModificationValidator();
    }
	
    public boolean canHandleLinkedResources() {
    	// deprecated in Eclipse 3.2
        return this.canHandleLinkedResourceURI();
    }
    
	public boolean canHandleLinkedResourceURI() {
		// since Eclipse 3.2
        return true;
	}
	
    public IResourceRuleFactory getRuleFactory() {
    	return SVNResourceRuleFactory.INSTANCE;
    }
    
    public IFileHistoryProvider getFileHistoryProvider() {
    	return new SVNFileHistoryProvider();
    }
    
	protected void deconfigured() {
		IProject project = this.getProject();
		if (project != null) {
			try {project.setPersistentProperty(SVNTeamProvider.LOCATION_PROPERTY, null);} catch (Exception ex) {}
			// compatibility with previous versions
			try {project.setPersistentProperty(SVNTeamProvider.RESOURCE_PROPERTY, null);} catch (Exception ex) {}
		}
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(new IResource[] {this.getProject()}, IResource.DEPTH_ZERO, ResourceStatesChangedEvent.CHANGED_NODES));		
		super.deconfigured();
	}
	
	protected static void setRepositoryLocation(IProject project, IRepositoryLocation location) throws CoreException {
		//as property length is limited by size, we save only required data
		project.setPersistentProperty(SVNTeamProvider.LOCATION_PROPERTY, SVNRemoteStorage.instance().repositoryLocationAsReference(location, LocationReferenceTypeEnum.ONLY_REQUIRED_DATA));
	}
	
	protected void restoreLocation() throws HiddenException {
		if (this.state == 0) {
			this.location = null;
			this.locationId = null;
			if ((this.errorCode = this.uploadRepositoryLocation()) == ErrorDescription.SUCCESS ||
				(this.errorCode = this.acquireResolution(false)) == ErrorDescription.SUCCESS) {
    			return;
			}
			
			this.performDisconnect();
		}
		this.breakThreadExecution();
	}
	
	protected synchronized void connectToProject() throws HiddenException {				
		if (this.state == 0) {
			this.location = null;
			this.locationId = null;
			this.resource = null;
			this.relocatedTo = null;
			if ((this.errorCode = this.uploadRepositoryResource()) == ErrorDescription.SUCCESS ||
				(this.errorCode = this.acquireResolution(true)) == ErrorDescription.SUCCESS) {
    			this.state = 1;
    			return;
			}
			
			this.performDisconnect();
		}
		this.breakThreadExecution();
	}
	
	protected int acquireResolution(boolean full) {
		while (true) {
			Object context = null;
			if (this.errorCode == ErrorDescription.REPOSITORY_LOCATION_IS_DISCARDED) {
				context = new Object[] {this.getProject(), this.location};
			}
			else if (this.errorCode == ErrorDescription.PROJECT_IS_RELOCATED_OUTSIDE_PLUGIN) {
				context = new Object[] {this.getProject(), this.relocatedTo, this.location};
			}
			else if (this.errorCode == ErrorDescription.CANNOT_READ_LOCATION_DATA) {
				context = new Object[] {this.getProject(), this.relocatedTo, this.locationId};
			}
			else {
				context = this.getProject();
			}
				
    		if (SVNTeamPlugin.instance().getErrorHandlingFacility().acquireResolution(new ErrorDescription(this.errorCode, context))) {
    			int newError = full ? this.uploadRepositoryResource() : this.uploadRepositoryLocation();
				if (newError != ErrorDescription.SUCCESS) {
					if (newError != this.errorCode) {
						this.errorCode = newError;
						continue;
					}
					return newError;
				}
    			return ErrorDescription.SUCCESS;
    		}
			return this.errorCode;
		}
	}

	protected void performDisconnect() {
    	this.state = -1;
    	CompositeOperation op = new CompositeOperation("Operation_OpenProject", SVNMessages.class); //$NON-NLS-1$
    	op.add(new DisconnectOperation(new IProject[] {this.getProject()}, false));
    	// notify user about the problem is happened
    	op.add(new AbstractActionOperation(op.getId(), op.getMessagesClass()) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new UnreportableException(SVNTeamProvider.this.getAutoDisconnectMessage());
			}
		});
    	ProgressMonitorUtility.doTaskScheduled(op);
	}
	
	protected void breakThreadExecution() {
		throw new HiddenException(this.getAutoDisconnectMessage());
	}
	
	protected String getAutoDisconnectMessage() {
		return SVNMessages.formatErrorString("Error_AutoDisconnect", new String[] {this.getProject().getName()}); //$NON-NLS-1$
	}
	
	public static boolean requiresUpgrade(IProject project) {
		IPath location = FileUtility.getResourcePath(project);
		location = location.append(SVNUtility.getSVNFolderName());
		return !SVNUtility.isPriorToSVN17() && location.toFile().exists() && !location.append("pristine").toFile().exists();
	}
	
	protected int uploadRepositoryResource() {
		int errorCode = this.uploadRepositoryLocation();
		
		IProject project = this.getProject();
		IPath location = FileUtility.getResourcePath(project);
		if (SVNUtility.isPriorToSVN17() && !location.append(SVNUtility.getSVNFolderName()).toFile().exists()) {
			return ErrorDescription.CANNOT_READ_PROJECT_METAINFORMATION;
		}
		ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			SVNChangeStatus []sts = SVNUtility.status(proxy, location.toString(), Depth.IMMEDIATES, ISVNConnector.Options.INCLUDE_UNCHANGED, new SVNNullProgressMonitor());
			if (sts != null && sts.length > 0) {
				SVNUtility.reorder(sts, true);
				SVNChangeStatus st = sts[0];
				this.relocatedTo = SVNUtility.decodeURL(st.url);
				if (this.location != null) {
					this.resource = this.location.asRepositoryContainer(this.relocatedTo, true);
					if (this.resource == null) {
						return ErrorDescription.PROJECT_IS_RELOCATED_OUTSIDE_PLUGIN;
					}
				}
			}
		}		
		catch (SVNConnectorException ex) {
			if (ex.getErrorId() == SVNErrorCodes.wcCleanupRequired) {
				// no way to read statuses, return some fake for now...
				return ErrorDescription.WORKING_COPY_REQUIRES_CLEANUP;
			}
			if (ex.getErrorId() == SVNErrorCodes.wcOldFormat) {
				return ErrorDescription.WORKING_COPY_REQUIRES_UPGRADE;
			}
			return ErrorDescription.CANNOT_READ_PROJECT_METAINFORMATION;
		}
		finally {
			proxy.dispose();
		}
		return errorCode;
	}
	
	protected int uploadRepositoryLocation() {
		try {
			IProject project = this.getProject();
			
			String data = project.getPersistentProperty(SVNTeamProvider.LOCATION_PROPERTY);
			if (data != null) {
				this.location = SVNRemoteStorage.instance().newRepositoryLocation(data);
				this.locationId = this.location.getId();
				if (SVNRemoteStorage.instance().getRepositoryLocation(this.location.getId()) == null) {
					return ErrorDescription.REPOSITORY_LOCATION_IS_DISCARDED;
				}
			}
			else {
				// compatibility with previous versions
				data = project.getPersistentProperty(SVNTeamProvider.RESOURCE_PROPERTY);
				if (data != null) {
					this.locationId = SVNTeamProvider.extractLocationId(data);
					if (this.locationId != null) {
						this.location = SVNRemoteStorage.instance().getRepositoryLocation(this.locationId);
						if (this.location != null) {
							SVNTeamProvider.setRepositoryLocation(project, this.location);
						}
					}
				}
			}
			return this.location == null ? ErrorDescription.CANNOT_READ_LOCATION_DATA : ErrorDescription.SUCCESS;
		}
		catch (CoreException ex) {
			return ErrorDescription.CANNOT_READ_LOCATION_DATA;
		}
	}
	
	// compatibility with old plugin versions
	protected static String extractLocationId(String resourceData) {
		if (resourceData != null) {
			String []data = resourceData.split(";"); //$NON-NLS-1$
			if (data.length >= 2) {
				return data[1];
			}
		}
		return null;
	}

	public boolean isVerifyTagOnCommit() {
		try {
			String strProp = this.getProject().getPersistentProperty(SVNTeamProvider.VERIFY_TAG_ON_COMMIT_PROPERTY);	
			if (strProp == null) {
				return SVNTeamProvider.DEFAULT_VERIFY_TAG_ON_COMMIT;
			}
			return Boolean.valueOf(strProp).booleanValue();	
		} catch (CoreException e) {
			//ignore and return default value
			return SVNTeamProvider.DEFAULT_VERIFY_TAG_ON_COMMIT;
		}		
	}

	public void setVerifyTagOnCommit(boolean isVerifyTagOnCommit) throws CoreException {
		this.getProject().setPersistentProperty(SVNTeamProvider.VERIFY_TAG_ON_COMMIT_PROPERTY, String.valueOf(isVerifyTagOnCommit));		
	}
	
}
