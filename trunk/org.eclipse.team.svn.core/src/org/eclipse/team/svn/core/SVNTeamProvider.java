/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.history.SVNFileHistoryProvider;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.HiddenException;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.management.DisconnectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * IProject attachement, that allows us to associate a project with a repository
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProvider extends RepositoryProvider implements IConnectedProjectInformation {
	public final static QualifiedName RESOURCE_PROPERTY = new QualifiedName("org.eclipse.team.svn", "resource");
	public final static QualifiedName LOCATION_PROPERTY = new QualifiedName("org.eclipse.team.svn", "location");

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
		if (this.state != 0) {
			SVNTeamProvider.setRepositoryLocation(this.getProject(), resource.getRepositoryLocation());
			this.state = 0;
		}
	}
	
	public synchronized void relocateResource() throws CoreException {
		if (this.state != 0) {
			SVNTeamProvider.setRepositoryLocation(this.getProject(), this.location);
			this.state = 0;
		}
	}
	
	public static void map(IProject project, IRepositoryResource resource) throws CoreException {
		SVNTeamProvider.setRepositoryLocation(project, resource.getRepositoryLocation());
		RepositoryProvider.map(project, SVNTeamPlugin.NATURE_ID);
	}
	
	public String getID() {
		return SVNTeamPlugin.NATURE_ID;
	}

	public void configureProject() {
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(new IResource[] {this.getProject()}, IResource.DEPTH_ZERO));		
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
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(new IResource[] {this.getProject()}, IResource.DEPTH_ZERO));		
		super.deconfigured();
	}
	
	protected static void setRepositoryLocation(IProject project, IRepositoryLocation location) throws CoreException {
		project.setPersistentProperty(SVNTeamProvider.LOCATION_PROPERTY, SVNRemoteStorage.instance().repositoryLocationAsReference(location));
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
	
	protected void connectToProject() throws HiddenException {
		// serialize initialization
		synchronized (SVNTeamProvider.class) {
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
			else if (this.errorCode == ErrorDescription.CANNOT_READ_PROJECT_METAINFORMATION) {
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
    		else {
    			return this.errorCode;
    		}
		}
	}

	protected void performDisconnect() {
    	this.state = -1;
    	String opName = "Operation.OpenProject";
    	CompositeOperation op = new CompositeOperation(opName);
    	op.add(new DisconnectOperation(new IProject[] {this.getProject()}, false));
    	// notify user about the problem is happened
    	op.add(new AbstractNonLockingOperation(opName) {
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
		String errMessage = SVNTeamPlugin.instance().getResource("Error.AutoDisconnect");
		return MessageFormat.format(errMessage, new String[] {this.getProject().getName()});
	}
	
	protected int uploadRepositoryResource() {
		int errorCode = this.uploadRepositoryLocation();
		
		IProject project = this.getProject();
		
		SVNEntryStatus st = SVNUtility.getSVNInfoForNotConnected(project);
		if (st != null) {
			this.relocatedTo = SVNUtility.decodeURL(st.url);
			if (this.location != null) {
				this.resource = this.location.asRepositoryContainer(this.relocatedTo, true);
				if (this.resource == null) {
					return ErrorDescription.PROJECT_IS_RELOCATED_OUTSIDE_PLUGIN;
				}
			}
		}
		else {
			return ErrorDescription.CANNOT_READ_PROJECT_METAINFORMATION;
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
			String []data = resourceData.split(";");
			if (data.length >= 2) {
				return data[1];
			}
		}
		return null;
	}
	
}
