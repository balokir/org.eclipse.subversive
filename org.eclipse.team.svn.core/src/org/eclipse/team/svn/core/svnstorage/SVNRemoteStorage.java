/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Panagiotis Korros - [patch] bug fix: .svn folder is deleted by subversive
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor;
import org.eclipse.team.svn.core.connector.SVNConflictVersion;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.options.IIgnoreRecommendations;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVN based representation of IRemoteStorage
 * 
 * @author Alexander Gurov
 */
public class SVNRemoteStorage extends AbstractSVNStorage implements IRemoteStorage {
	
	/**
	 * The name of the preferences node in the Subversive Core preferences that contains
	 * the known repositories as its children.
	 */
	public static final String PREF_REPOSITORIES_NODE = "repositories"; //$NON-NLS-1$
	
	/**
	 * The name of file containing the SVN repository locations information.
	 * Deprecated since Subversive 0.7.0 v20080404 - must not be used. The valid information
	 * is stored in preferences.
	 * @see SVNRemoteStorage.PREF_REPOSITORIES_NODE
	 */
	public static final String STATE_INFO_FILE_NAME = ".svnRepositories"; //$NON-NLS-1$
	
	private static SVNRemoteStorage instance = new SVNRemoteStorage();
	
	protected Map localResources;
	protected Map switchedToUrls;
	protected Map externalsLocations;
	protected Map parent2Children;
	protected Map<Class, List<IResourceStatesListener>> resourceStateListeners;
	protected LinkedList fetchQueue;
	
	protected ISchedulingRule notifyLock = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	public static SVNRemoteStorage instance() {
		return SVNRemoteStorage.instance;
	}

    public void addResourceStatesListener(Class eventClass, IResourceStatesListener listener) {
    	synchronized (this.resourceStateListeners) {
    		List<IResourceStatesListener> listenersList = this.resourceStateListeners.get(eventClass);
    		if (listenersList == null) {
    			this.resourceStateListeners.put(eventClass, listenersList = new ArrayList<IResourceStatesListener>());
    		}
    		if (!listenersList.contains(listener)) {
    			listenersList.add(listener);
    		}
    	}
    }
    
    public void removeResourceStatesListener(Class eventClass, IResourceStatesListener listener) {
    	synchronized (this.resourceStateListeners) {
    		List<IResourceStatesListener> listenersList = this.resourceStateListeners.get(eventClass);
    		if (listenersList != null) {
    			listenersList.remove(listener);
        		if (listenersList.size() == 0) {
        			this.resourceStateListeners.remove(eventClass);
        		}
    		}
    	}
    }
    
    public void fireResourceStatesChangedEvent(final ResourceStatesChangedEvent event) {
		if (event.resources.length > 0) {
		    // events should be serialized and called asynchronous to caller thread
	    	ProgressMonitorUtility.doTaskScheduled(new AbstractActionOperation("Operation_SendNotifications") { //$NON-NLS-1$
	    		protected void runImpl(IProgressMonitor monitor) throws Exception {
	    	    	IResourceStatesListener []listeners = null;
	    	    	synchronized (SVNRemoteStorage.this.resourceStateListeners) {
	    	    		List<IResourceStatesListener> listenersArray = SVNRemoteStorage.this.resourceStateListeners.get(event.getClass());
	    	    		if (listenersArray == null) {
	    	    			return;
	    	    		}
	    	        	listeners = listenersArray.toArray(new IResourceStatesListener[listenersArray.size()]);
	    	    	}
	    	    	for (int i = 0; i < listeners.length; i++) {
	    	    		listeners[i].resourcesStateChanged(event);
	    	    	}
	    		}
	    		public ISchedulingRule getSchedulingRule() {
	    			return SVNRemoteStorage.this.notifyLock;
	    		}
	    	}, true);
		}
    }
    
	public void initialize(IPath stateInfoLocation) throws Exception {
		this.setStateInfoFile(stateInfoLocation, SVNRemoteStorage.STATE_INFO_FILE_NAME);
		this.initializeImpl(SVNRemoteStorage.PREF_REPOSITORIES_NODE);
	}
	
	public IResourceChange asResourceChange(IChangeStateProvider changeState, boolean update) {
		IResource resource = null;
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath location = new Path(changeState.getLocalPath());
		int nodeKind = changeState.getNodeKind();
		int propKind = changeState.getPropertiesChangeType();
		int textKind = changeState.getTextChangeType();
		boolean isCopied = changeState.isCopied();
		boolean isSwitched = changeState.isSwitched();
	    SVNRevision.Number remoteRevision = changeState.getChangeRevision();
		long revision = remoteRevision != null ? remoteRevision.getNumber() : SVNRevision.INVALID_REVISION_NUMBER;
		// repositoryTextStatus can be StatusKind::none in two cases: resource not modified and non versioned
		// in the second case we should ignore repository status calculation
		
		SVNConflictDescriptor treeConflictDescriptor = changeState.getTreeConflictDescriptor();		
		String statusStr = /*revision == SVNRevision.INVALID_REVISION_NUMBER ? IStateFilter.ST_NOTEXISTS : */this.getStatusString(propKind, textKind, update);
		if (nodeKind == SVNEntry.Kind.DIR) {
		    if ((resource = changeState.getExact(root.findContainersForLocation(location))) == null) {
		    	return null;
		    }
		    int changeMask = this.getChangeMask(textKind, propKind, isCopied, isSwitched);
		    if (IStateFilter.SF_NOTEXISTS.accept(resource, statusStr, changeMask)) {
				revision = SVNRevision.INVALID_REVISION_NUMBER;
			}
			return new SVNFolderChange(resource, revision, statusStr, changeMask, changeState.getChangeAuthor(), changeState.getChangeDate(), treeConflictDescriptor, null, changeState.getComment());
		}
	    if ((resource = changeState.getExact(root.findFilesForLocation(location))) == null) {
	    	return null;
	    }
	    int changeMask = this.getChangeMask(textKind, propKind, isCopied, isSwitched);
	    if (IStateFilter.SF_NOTEXISTS.accept(resource, statusStr, changeMask)) {
			revision = SVNRevision.INVALID_REVISION_NUMBER;
		}			    
		return new SVNFileChange(resource, revision, statusStr, changeMask, changeState.getChangeAuthor(), changeState.getChangeDate(), treeConflictDescriptor, null, changeState.getComment());
	}
	
	public byte []resourceChangeAsBytes(IResourceChange resource) {
		if (resource == null) {
			return null;
		}
		int kind = resource.getPegRevision().getKind();
		String originatorData = null;
		if (resource.getOriginator() != null) {
			byte []data = this.repositoryResourceAsBytes(resource.getOriginator());
			originatorData = new String(Base64.encode(data));
		}
		long lastCommitDate = resource.getLastCommitDate();
		String comment = resource.getComment();
		String retVal = 
	/*0*/	String.valueOf(resource instanceof ILocalFolder) + ";" +  //$NON-NLS-1$
	/*1*/	new String(Base64.encode(FileUtility.getWorkingCopyPath(resource.getResource()).getBytes())) + ";" +  //$NON-NLS-1$
	/*2*/	resource.getRevision() + ";" +  //$NON-NLS-1$
	/*3*/	resource.getStatus() + ";" + //$NON-NLS-1$
	/*4*/	resource.getAuthor() + ";" +  //$NON-NLS-1$
	/*5*/	(lastCommitDate == 0 ? "null" : String.valueOf(lastCommitDate)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*6*/	String.valueOf(kind) + ";" +  //$NON-NLS-1$
	/*7*/	(kind == SVNRevision.Kind.NUMBER ? String.valueOf(((SVNRevision.Number)resource.getPegRevision()).getNumber()) : String.valueOf(kind)) + ";" + //$NON-NLS-1$
	/*8*/	(originatorData != null ? originatorData : "null") + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*9*/	(comment == null ? "null" : new String(Base64.encode(comment.getBytes()))) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*10*/	resource.getChangeMask() + ";" +  //$NON-NLS-1$
		this.getTreeConflictDescriptorAsString(resource.getTreeConflictDescriptor());								
								
		return retVal.getBytes();
	}
	
	protected String getTreeConflictDescriptorAsString(SVNConflictDescriptor conflictDescriptor) {
		String retVal = 
	/*11*/	(conflictDescriptor == null ? "null" : String.valueOf(conflictDescriptor.action)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*12*/	(conflictDescriptor == null ? "null" : String.valueOf(conflictDescriptor.reason)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*13*/  (conflictDescriptor == null ? "null" : String.valueOf(conflictDescriptor.operation)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
		this.getSVNConflictVersionAsString(conflictDescriptor == null ? null : conflictDescriptor.srcLeftVersion) + ";" + //$NON-NLS-1$
		this.getSVNConflictVersionAsString(conflictDescriptor == null ? null : conflictDescriptor.srcRightVersion);
		return retVal;
	}
	
	protected String getSVNConflictVersionAsString(SVNConflictVersion conflictVersion) {
		String retVal = 
	/*14 or 18*/    (conflictVersion == null ? "null" : String.valueOf(conflictVersion.nodeKind)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*15 or 19*/	(conflictVersion == null ? "null" : new String(Base64.encode(conflictVersion.pathInRepos.getBytes()))) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*16 or 20*/	(conflictVersion == null ? "null" : String.valueOf(conflictVersion.pegRevision)) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
	/*17 or 21*/	(conflictVersion == null ? "null" : new String(Base64.encode(conflictVersion.reposURL.getBytes()))); //$NON-NLS-1$
		return retVal;
	}
	
	public IResourceChange resourceChangeFromBytes(byte []bytes) {
		if (bytes == null) {
			return null;
		}
		String []data = new String(bytes).split(";"); //$NON-NLS-1$
		boolean isFolder = "true".equals(data[0]); //$NON-NLS-1$
		String name = new String(Base64.decode(data[1].getBytes()));
		long revision = Long.parseLong(data[2]);
		String status = this.deserializeStatus(data[3]);
		String author = "null".equals(data[4]) ? null : data[4]; //$NON-NLS-1$
		long lastCommitDate = "null".equals(data[5]) ? 0 : Long.parseLong(data[5]); //$NON-NLS-1$
		int revisionKind = Integer.parseInt(data[6]);
		SVNRevision pegRevision = null;
		if (revisionKind == SVNRevision.Kind.NUMBER) {
		    long pegNum = Long.parseLong(data[7]);
		    pegRevision = pegNum == revision || revision == SVNRevision.INVALID_REVISION_NUMBER ? null : SVNRevision.fromNumber(pegNum);
		}
		else {
		    pegRevision = SVNRevision.fromKind(revisionKind);
		}
		String comment = "null".equals(data[9]) ? null : new String(Base64.decode(data[9].getBytes())); //$NON-NLS-1$
		int changeMask = "null".equals(data[10]) ? ILocalResource.NO_MODIFICATION : Integer.parseInt(data[10]); //$NON-NLS-1$
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		SVNConflictDescriptor treeConflict = this.getTreeConflictDescriptorFromString(name, data);				
		IResourceChange change = 
			isFolder ? 
			(IResourceChange)new SVNFolderChange(root.getContainerForLocation(new Path(name)), revision, status, changeMask, author, lastCommitDate, treeConflict, pegRevision, comment) : 
			new SVNFileChange(root.getFileForLocation(new Path(name)), revision, status, changeMask, author, lastCommitDate, treeConflict, pegRevision, comment);

		if (!"null".equals(data[8])) { //$NON-NLS-1$
			byte []originatorData = Base64.decode(data[8].getBytes());
			change.setOriginator(this.repositoryResourceFromBytes(originatorData));
		}
		
		return change;
	}
	
	protected SVNConflictDescriptor getTreeConflictDescriptorFromString(String path, String[] data) {
		SVNConflictDescriptor conflictDescriptor = null;		
		if (data.length >= 22) {
			int action = "null".equals(data[11]) ? 0 : Integer.parseInt(data[11]); //$NON-NLS-1$
			int reason = "null".equals(data[12]) ? 0 : Integer.parseInt(data[12]); //$NON-NLS-1$
			int operation = "null".equals(data[13]) ? 0 : Integer.parseInt(data[13]); //$NON-NLS-1$
			conflictDescriptor = new SVNConflictDescriptor(path, action, reason, operation, this.getSVNConflictVersionFromString(data, true), this.getSVNConflictVersionFromString(data, false));	
		}			
		return conflictDescriptor;
	}
	
	protected SVNConflictVersion getSVNConflictVersionFromString(String[] data, boolean isLeft) {
		int indexShift = isLeft ? 0 : 4;
		int nodeKind = "null".equals(data[14 + indexShift]) ? 0 :  Integer.parseInt(data[14 + indexShift]); //$NON-NLS-1$
		String pathInRepos = "null".equals(data[15 + indexShift]) ? null : new String(Base64.decode(data[15 + indexShift].getBytes())); //$NON-NLS-1$
		long pegRevision = "null".equals(data[16 + indexShift]) ? 0 :  Long.parseLong(data[16 + indexShift]); //$NON-NLS-1$
		String reposUrl = "null".equals(data[17 + indexShift]) ? null : new String(Base64.decode(data[17 + indexShift].getBytes()));		 //$NON-NLS-1$
		return new SVNConflictVersion(reposUrl, pegRevision, pathInRepos, nodeKind);
	}
	
	public synchronized IResource []getRegisteredChildren(IContainer container) throws Exception {
		// for null, inaccessible resource and workspace root members shouldn't be provided
		if (container == null || !container.isAccessible() || container.getProject() == null) {
			return null;
		}
		IResource []members = FileUtility.resourceMembers(container, false);
		
		Set retVal = (Set)this.parent2Children.get(container.getFullPath());
		if (retVal == null) {
			this.loadLocalResourcesSubTree(container, false);
			retVal = (Set)this.parent2Children.get(container.getFullPath());
		}
		if (retVal != null) {
			retVal = new HashSet(retVal);
			retVal.addAll(Arrays.asList(members));
		}
		
		return retVal == null ? members : (IResource [])retVal.toArray(new IResource[retVal.size()]);
	}
	
	public ILocalResource asLocalResourceDirty(IResource resource) {
		if (!CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled()) {
			return this.asLocalResource(resource);
		}
		// null resource and workspace root shouldn't be provided
		if (resource == null || resource.getProject() == null) {
			return this.wrapUnexistingResource(resource, IStateFilter.ST_INTERNAL_INVALID, 0);
		}
		ILocalResource retVal = (ILocalResource)this.localResources.get(resource.getFullPath());
		if (retVal == null) {
			ILocalResource parent = this.getFirstExistingParentLocal(resource);
			int mask = parent == null ? 0 : (parent.getChangeMask() & ILocalResource.IS_SWITCHED);
			retVal = this.wrapUnexistingResource(resource, IStateFilter.ST_NOTEXISTS, mask);
		}
		return retVal;
	}
	
	public ILocalResource asLocalResourceAccessible(IResource resource) {
		ILocalResource retVal = this.asLocalResource(resource);
		if (IStateFilter.SF_INTERNAL_INVALID.accept(retVal)) {
			throw new UnreportableException(SVNMessages.formatErrorString("Error_InaccessibleResource", new String[]{FileUtility.getWorkingCopyPath(resource)})); //$NON-NLS-1$
		}
		return retVal;
	}
	
	public ILocalResource asLocalResource(IResource resource) {
		// null resource and workspace root shouldn't be provided
		if (resource == null || resource.getProject() == null || !resource.getProject().isAccessible()) {
			return this.wrapUnexistingResource(resource, IStateFilter.ST_INTERNAL_INVALID, 0);
		}
		ILocalResource local = (ILocalResource)this.localResources.get(resource.getFullPath());
		if (local == null) {
			synchronized (this) {
				local = (ILocalResource)this.localResources.get(resource.getFullPath());
				if (local == null) {
					try {
						local = this.loadLocalResourcesSubTree(resource, true);
					} 
					catch (RuntimeException ex) {
						throw ex;
					}
					catch (SVNConnectorException ex) {
						return this.wrapUnexistingResource(resource, IStateFilter.ST_INTERNAL_INVALID, 0);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return local;
	}
	
	public synchronized void refreshLocalResources(IResource []resources, int depth) {
		if (depth == IResource.DEPTH_INFINITE) {
			resources = FileUtility.shrinkChildNodes(resources);
		}
		for (int i = 0; i < resources.length; i++) {
			this.refreshLocalResourceImpl(resources[i], depth);
		}
	}
	
	public ILocalResource asLocalResource(IProject project, String url, int kind) {
		synchronized (this.switchedToUrls) {
			for (Iterator it = this.switchedToUrls.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				String cachedUrl = (String)entry.getValue();
				if (new Path(cachedUrl).isPrefixOf(new Path(url))) {
					IPath target = ((IPath)entry.getKey()).append(url.substring(cachedUrl.length())).removeFirstSegments(1);
					return this.asLocalResource(kind == IResource.FOLDER ? (IResource)project.getFolder(target) : project.getFile(target));
				}
			}
		}
		String projectUrl = this.asRepositoryResource(project).getUrl();
		if (url.length() < projectUrl.length()) {
			return this.wrapUnexistingResource(null, IStateFilter.ST_INTERNAL_INVALID, 0);
		}
		String pathInProject = url.substring(projectUrl.length());
		if (pathInProject.length() == 0) {
			return this.asLocalResource(project);
		}
		return this.asLocalResource(kind == IResource.FOLDER ? (IResource)project.getFolder(pathInProject) : project.getFile(pathInProject));
	}
	
	public IRepositoryResource asRepositoryResource(IResource resource) {
		IProject project = resource.getProject();
		IRepositoryResource baseResource = this.getConnectedProjectInformation(project).getRepositoryResource();
		
		if (resource.equals(project)) {
			return SVNUtility.copyOf(baseResource);
		}
		
		IRepositoryLocation location = baseResource.getRepositoryLocation();
		
		String url = (String)this.switchedToUrls.get(resource.getFullPath());
		if (url == null) {
			ILocalResource parent = this.getFirstExistingParentLocal(resource);
			if (parent != null && (parent.getChangeMask() & ILocalResource.IS_SWITCHED) != 0) {
				IPath parentPath = parent.getResource().getFullPath();
				
				url = (String)this.switchedToUrls.get(parentPath) + "/" + resource.getFullPath().removeFirstSegments(parentPath.segmentCount()).toString(); //$NON-NLS-1$
			}
		}
		
		if (url == null) {
			url = this.makeUrl(resource, baseResource);
		}
		else if (!new Path(location.getRepositoryRootUrl()).isPrefixOf(new Path(url))) {
			location = this.wrapLocationIfRequired(location, url, resource.getType() == IResource.FILE);
		}
		
		return resource instanceof IContainer ? (IRepositoryResource)location.asRepositoryContainer(url, false) : location.asRepositoryFile(url, false);
	}
	
	public IRepositoryResource asRepositoryResource(IRepositoryLocation location, SVNEntryRevisionReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		IRepositoryResource res = null;
		
		String url = reference.path;	
		//re-create SVNEntryRevisionReference because SVNUtility.info throws exception if peg revision or revision are null
		reference = new SVNEntryRevisionReference(SVNUtility.encodeURL(url),
				reference.pegRevision == null ? SVNRevision.HEAD : reference.pegRevision,
				reference.revision == null ? SVNRevision.HEAD : reference.revision);
		
		if (!new Path(location.getRepositoryRootUrl()).isPrefixOf(new Path(url))) {
			boolean isFile = false;
			location = this.wrapLocationIfRequired(location, url, isFile);
		}
		
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			//detect if resource is a file or directory
			SVNEntryInfo[] entriesInfo = SVNUtility.info(proxy, reference, Depth.EMPTY, monitor);
			if (entriesInfo.length > 0) {
				SVNEntryInfo info = entriesInfo[0];
				if (info.kind == Kind.FILE) {
					res = location.asRepositoryFile(url, false);
				} else if (info.kind == Kind.DIR) {
					res = location.asRepositoryContainer(url, false);
				}
			}				
		} finally {
			location.releaseSVNProxy(proxy);	
		}
						
		return res;
	}
	
	public IRepositoryResource asRepositoryResource(IRepositoryLocation location, String url, boolean isFile) {
		if (!new Path(location.getRepositoryRootUrl()).isPrefixOf(new Path(url))) {
			location = this.wrapLocationIfRequired(location, url, isFile);
		}
		
		return !isFile ? (IRepositoryResource)location.asRepositoryContainer(url, false) : location.asRepositoryFile(url, false);
	}
	
	public IRepositoryLocation getRepositoryLocation(IResource resource) {
		return this.getConnectedProjectInformation(resource.getProject()).getRepositoryLocation();
	}
	
	protected IRepositoryLocation wrapLocationIfRequired(IRepositoryLocation location, String url, boolean isFile) {
		if (!new Path(location.getRepositoryRootUrl()).isPrefixOf(new Path(url))) {
			synchronized (this.externalsLocations) {
				List locations = (List)this.externalsLocations.get(location);
				if (locations == null) {
					this.externalsLocations.put(location, locations = new ArrayList());
				}
				boolean found = false;
				for (Iterator it = locations.iterator(); it.hasNext(); ) {
					IRepositoryLocation tmp = (IRepositoryLocation)it.next();
					if (url.startsWith(tmp.getUrl()) || tmp.getUrl().startsWith(url)) {
						location = tmp;
						found = true;
						break;
					}
				}
				if (!found) {
					location = new SVNRepositoryLocationWrapper(location, isFile ? url.substring(0, url.lastIndexOf('/')) : url);
					locations.add(location);
				}
			}
		}
		return location;
	}
	
	protected ILocalResource wrapUnexistingResource(IResource resource, String state, int mask) {
		return resource == null || resource.getType() == IResource.FILE ? 
				(ILocalResource)new SVNLocalFile(resource, SVNRevision.INVALID_REVISION_NUMBER, SVNRevision.INVALID_REVISION_NUMBER, state, mask, null, 0, null) :
				new SVNLocalFolder(resource, SVNRevision.INVALID_REVISION_NUMBER, SVNRevision.INVALID_REVISION_NUMBER, state, mask, null, 0, null);
	}
	
	protected String makeUrl(IResource resource, IRepositoryResource baseResource) {
		if (resource.getType() == IResource.PROJECT) {
			return baseResource.getUrl();
		}
		IProject project = resource.getProject();
		String url = resource.getFullPath().toString();
		//truncating of the project name allow us to remap a content of project with name 'A' to the remote folder called 'B'
		return baseResource.getUrl() + "/" + url.substring(project.getFullPath().toString().length() + 1); //$NON-NLS-1$
	}
	
	protected IConnectedProjectInformation getConnectedProjectInformation(IProject project) {
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if (provider == null) {
			String errMessage = SVNMessages.formatErrorString("Error_NotConnectedProject", new String[] {project.getName()}); //$NON-NLS-1$
			throw new UnreportableException(errMessage);
		}
		if (!(provider instanceof IConnectedProjectInformation)) {
			String errMessage = SVNMessages.formatErrorString("Error_AnotherProvider", new String[] {project.getName(), provider.getID()}); //$NON-NLS-1$
			throw new UnreportableException(errMessage);
		}
		
		return (IConnectedProjectInformation)provider;
	}
	
	protected void refreshLocalResourceImpl(IResource resource, int depth) {	   
	    ArrayList<IResource> removed = new ArrayList<IResource>();
	    removed.add(resource);
	    if (resource.getType() == IResource.PROJECT) {
	    	IConnectedProjectInformation info = (IConnectedProjectInformation)RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID);
	    	if (info != null) {
	    		try {
					info.relocateResource();
				}
				catch (CoreException ex) {
					throw new RuntimeException(ex);
				}
	    	}
	    }
		for (Iterator it = this.localResources.values().iterator(); it.hasNext(); ) {
			ILocalResource local = (ILocalResource)it.next();
		    IResource current = local.getResource();
		    IPath currentPath = current.getFullPath();
	        if (resource.getFullPath().isPrefixOf(currentPath) || IStateFilter.SF_NOTEXISTS.accept(local)) {
	            int cachedSegmentsCount = currentPath.segmentCount();
	            int matchingSegmentsCount = resource.getFullPath().matchingFirstSegments(currentPath);
	            int difference = cachedSegmentsCount - matchingSegmentsCount;
	            if (difference >= 0 && depth == IResource.DEPTH_INFINITE ? true : depth >= difference) {
	                removed.add(current);
	            }
	        }
		}
		for (Iterator it = removed.iterator(); it.hasNext(); ) {
			IResource forRemove = (IResource)it.next();
			this.localResources.remove(forRemove.getFullPath());
			this.switchedToUrls.remove(forRemove.getFullPath());
            this.parent2Children.remove(forRemove.getFullPath());
        	this.parent2Children.remove(forRemove.getParent().getFullPath());
		}
	}
	
	protected ILocalResource loadLocalResourcesSubTree(final IResource resource, boolean recurse) throws Exception {
		IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID);
		if (provider == null || FileUtility.isSVNInternals(resource)) {
			return this.wrapUnexistingResource(resource, IStateFilter.ST_INTERNAL_INVALID, 0);
		}
		
		boolean isCacheEnabled = CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled();
		if (!isCacheEnabled) {
			this.localResources.clear();
		}
		recurse &= isCacheEnabled;
		
		ILocalResource retVal = null;
	    boolean isLinked = FileUtility.isLinked(resource);
		IResource parent = resource.getParent();
		boolean parentExists = parent != null && parent.isAccessible();
		if (parentExists && !isLinked) {
			ILocalResource parentLocal = this.getFirstExistingParentLocal(resource);
			if (parentLocal == null || !SVNRemoteStorage.SF_NONSVN.accept(parentLocal) || 
				(parentLocal.getChangeMask() & ILocalResource.IS_EXTERNAL) != 0 && IStateFilter.SF_IGNORED.accept(parentLocal)) {
			    retVal = this.loadLocalResourcesSubTreeSVNImpl(provider, resource, recurse);
			}
		}

		return retVal == null || IStateFilter.SF_UNVERSIONED.accept(retVal) ? this.loadUnversionedSubtree(resource, isLinked, recurse) : retVal;
	}
	
	protected ILocalResource loadUnversionedSubtree(final IResource resource, boolean isLinked, boolean recurse) throws Exception {
	    // if resource has unversioned parents it cannot be wrapped directly and it status should be calculated in other way
		String status = this.calculateUnversionedStatus(resource, isLinked);
		
		// delegate status
		final String fStatus = status;
		ILocalResource parent = this.getFirstExistingParentLocal(resource);
		final int parentCM = parent != null ? (parent.getChangeMask() & (ILocalResource.IS_EXTERNAL | ILocalResource.IS_SWITCHED)) : 0;
		final ILocalResource []tmp = new ILocalResource[1];
		FileUtility.visitNodes(resource, new IResourceVisitor() {
            public boolean visit(IResource child) throws CoreException {
            	if (FileUtility.isSVNInternals(child)) {
            		return false;
            	}
            	String state = child == resource ? fStatus : SVNRemoteStorage.this.getDelegatedStatus(child, fStatus, 0);
            	int changeMask = (state == IStateFilter.ST_OBSTRUCTED || state == IStateFilter.ST_NEW) ? ILocalResource.TEXT_MODIFIED : ILocalResource.NO_MODIFICATION;
            	changeMask |= parentCM;
            	String path = FileUtility.getWorkingCopyPath(child);
            	if (new File(path + "/" + SVNUtility.getSVNFolderName()).exists() && SVNUtility.getSVNInfoForNotConnected(child) != null) { //$NON-NLS-1$
            		return false;
            	}
            	ILocalResource retVal = SVNRemoteStorage.this.registerResource(child, SVNRevision.INVALID_REVISION_NUMBER, SVNRevision.INVALID_REVISION_NUMBER, state, changeMask, null, -1, null);
                if (tmp[0] == null) {
                	tmp[0] = retVal;
                }
                return true;
            }
        }, recurse ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE, false);
        
		return tmp[0] != null ? tmp[0] : this.wrapUnexistingResource(resource, IStateFilter.ST_INTERNAL_INVALID, 0);
	}
	
	protected String calculateUnversionedStatus(final IResource resource, boolean isLinked) {
		String status = IStateFilter.ST_NOTEXISTS;
		if (isLinked) {
		    status = IStateFilter.ST_LINKED;
		}
		else {
			IPath location = resource.getLocation();
			if (location != null && this.isFileExists(location)) {
			    // may be ignored ?
				status = IStateFilter.ST_IGNORED;
				if (!SVNUtility.isIgnored(resource)) {
					ILocalResource local = (ILocalResource)this.localResources.get(resource.getFullPath());
					if (local != null) {
						return local.getStatus();
					}
				    status = this.getTopLevelStatus(resource, IStateFilter.ST_NEW, 0);
				}
			}
		}
		return status;
	}
	
	protected boolean isFileExists(IPath location) {
		if (new File(location.toString()).exists()) {
			if (FileUtility.isWindows()) {
				// should be case sensitive on Windows OS also
				String []names = new File(location.removeLastSegments(1).toString()).list();
				if (names != null) {
					String name = location.lastSegment();
					for (int i = 0; i < names.length; i++) {
						if (names[i].equals(name)) {
							return true;
						}
					}
				}
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	protected ILocalResource loadLocalResourcesSubTreeSVNImpl(IConnectedProjectInformation provider, IResource resource, boolean recurse) throws Exception {
		IProject project = resource.getProject();
		IResource target = resource.getType() == IResource.FILE ? resource.getParent() : resource;

		IPath wcPath = project.getLocation();
		IPath resourcePath = target.getLocation();
		IPath requestedPath = resource.getLocation();
		if (wcPath == null || resourcePath == null || requestedPath == null) {
			return null;
		}
		String projectPath = wcPath.toString();
		int subPathStart = projectPath.length();

		// search for first parent which contains .svn folder
		//	only folders due to condition above - check for presence of SVN meta directly at the path...
		boolean hasSVNMeta = true;
		while (!this.canFetchStatuses(resourcePath)) {
			if (target == null || target.getType() == IResource.PROJECT) {
				return null;
			}
			hasSVNMeta = false;
			// load statuses non-recursively for the found parent
			resourcePath = resourcePath.removeLastSegments(1);
			target = target.getParent();
		}
		IRepositoryResource baseResource = provider.getRepositoryResource();
		SVNChangeStatus []statuses = this.getStatuses(baseResource.getRepositoryLocation(), resourcePath.toString());
		String desiredUrl = this.makeUrl(target, baseResource);
		ILocalResource retVal = this.fillCache(statuses, desiredUrl, resource, subPathStart, requestedPath);
		
		if (statuses.length == 1 || statuses.length > 1 && this.parent2Children.get(target.getFullPath()) == null) {
			// the caching is done for the folder if it is empty 
			this.parent2Children.put(target.getFullPath(), new HashSet());
		}
		
		if (retVal != null && hasSVNMeta && statuses.length > 1 && recurse) {
			this.scheduleStatusesFetch(statuses, target);
		}
		
		return retVal;
	}
	
	protected void scheduleStatusesFetch(SVNChangeStatus []st, IResource target) {
		synchronized (this.fetchQueue) {
			this.fetchQueue.add(new Object[] {st, target});
			if (this.fetchQueue.size() == 1) {
				ProgressMonitorUtility.doTaskScheduledDefault(new AbstractActionOperation("Operation_UpdateSVNCache") { //$NON-NLS-1$
					public ISchedulingRule getSchedulingRule() {
						return null;
					}
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
						while (CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled() && !monitor.isCanceled()) {
							SVNChangeStatus [] st;
							IResource target;
							synchronized (SVNRemoteStorage.this.fetchQueue) {
								if (SVNRemoteStorage.this.fetchQueue.size() == 0) {
									break;
								}
								Object []entry = (Object [])SVNRemoteStorage.this.fetchQueue.get(0);
								st = (SVNChangeStatus [])entry[0];
								target = (IResource)entry[1];
							}
							this.processEntry(monitor, st, target);
							synchronized (SVNRemoteStorage.this.fetchQueue) {
								SVNRemoteStorage.this.fetchQueue.remove(0);
							}
						}
						if (!CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled()) {
							synchronized (SVNRemoteStorage.this.fetchQueue) {
								SVNRemoteStorage.this.fetchQueue.clear();
							}
						}
					}
					protected void processEntry(IProgressMonitor monitor, SVNChangeStatus []st, IResource target) {
						IProject prj = target.getProject();
						IPath location = prj.getLocation();
						if (location != null) {
							SVNUtility.reorder(st, false);
							int projectEnd = location.toString().length();
							for (int i = 0; i < st.length && !monitor.isCanceled() && CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled(); i++) {
								ProgressMonitorUtility.progress(monitor, i, IProgressMonitor.UNKNOWN);
								
								if (st[i].nodeKind == SVNEntry.Kind.DIR && st[i].path.length() > projectEnd) {
									IResource folder = prj.getFolder(new Path(st[i].path.substring(projectEnd)));
									ProgressMonitorUtility.setTaskInfo(monitor, this, folder.getFullPath().toString());
									ILocalFolder local = (ILocalFolder)SVNRemoteStorage.this.asLocalResource(folder);
									if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
										local.getChildren();
									}
								}
							}
						}
					}
				}, false).setPriority(Job.DECORATE);
			}
		}
	}
	
	protected boolean canFetchStatuses(IPath path) {
		return path.append(SVNUtility.getSVNFolderName()).toFile().exists();
	}
	
	protected SVNChangeStatus []getStatuses(IRepositoryLocation location, String path) throws Exception {
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			SVNChangeStatus []statuses = SVNUtility.status(proxy, path, Depth.IMMEDIATES, ISVNConnector.Options.INCLUDE_UNCHANGED | ISVNConnector.Options.INCLUDE_IGNORED, new SVNNullProgressMonitor());
			SVNUtility.reorder(statuses, true);
			return statuses;
		}
		// FIXME The only discovered reason of SBV-3557 defect is that some
		// folders under the ignored path contains completely or partially
		// empty .svn folders. May be due to using external tools.
		// So, suppress throwing "Path is not a working copy directory" exception.
		// 155007 is error code for "Path is not a working copy directory".
		catch (SVNConnectorException cwe) {
			if (cwe.getErrorId() != 155007) {
				throw cwe;
			}
			return new SVNChangeStatus[0];
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected ILocalResource fillCache(SVNChangeStatus []statuses, String desiredUrl, IResource resource, int subPathStart, IPath requestedPath) {
		IProject project = resource.getProject();
		
		ILocalResource retVal = null;
		
		for (int i = 0; i < statuses.length; i++) {
			int nodeKind = SVNUtility.getNodeKind(statuses[i].path, statuses[i].nodeKind, true);
			// ignore files absent in the WC base and WC working. But what is the reason why it is reported ?
			//try to create local resource for file which has tree conflict but doesn't exist
			if (nodeKind == SVNEntry.Kind.NONE && !statuses[i].hasTreeConflict) {
				continue;
			}
			
			String fsNodePath = statuses[i].path;
			String nodePath = statuses[i].path;
			
			nodePath = nodePath.length() >= subPathStart ? nodePath.substring(subPathStart) : ""; //$NON-NLS-1$
			if (nodePath.length() > 0 && nodePath.charAt(nodePath.length() - 1) == '/') {
				nodePath = nodePath.substring(0, nodePath.length() - 1);
			}
			else if (i > 0 && nodePath.trim().length() == 0) {
				// Debug code was removed. We already have all information about the JavaSVN bug.
				continue;
			}
			
			IResource tRes = null;
			if (new Path(statuses[i].path).equals(requestedPath) && (resource.getType() > IResource.FOLDER || resource.getType() == nodeKind)) {//if nodekind not equals do not use default resource
			    tRes = resource;
			}
			else {
				if (nodeKind == SVNEntry.Kind.DIR) {
					if (nodePath.length() == 0) {
						tRes = project;
					}
					else {
						tRes = project.getFolder(new Path(nodePath));
					}
				}
				else {
				    tRes = project.getFile(new Path(nodePath));
				}
			}
			
			ILocalResource local = (ILocalResource)this.localResources.get(tRes.getFullPath());
			if (local == null) {
				ILocalResource parent = this.getFirstExistingParentLocal(tRes);
				int externalMask = statuses[i].textStatus == SVNEntryStatus.Kind.EXTERNAL ? ILocalResource.IS_EXTERNAL : 0;
				if (externalMask != 0) {
					statuses[i] = SVNUtility.getSVNInfoForNotConnected(tRes);
					if (statuses[i] == null) {
						local = this.registerResource(tRes, SVNRevision.INVALID_REVISION_NUMBER, SVNRevision.INVALID_REVISION_NUMBER, IStateFilter.ST_IGNORED, externalMask, null, 0, null);
						if (tRes == resource) {
							retVal = local;
						}
						continue;
					}
				}
				else if (parent != null && (parent.getChangeMask() & ILocalResource.IS_EXTERNAL) != 0) {
					externalMask = ILocalResource.IS_EXTERNAL;
				}
				
				//check file external
				if (statuses[i].isFileExternal) {
					externalMask = ILocalResource.IS_EXTERNAL;
				}
				
				 // get the IS_COPIED flag by parent node (it is not fetched for deletions)
				boolean forceCopied = parent != null && parent.isCopied();
				int changeMask = this.getChangeMask(statuses[i].textStatus, statuses[i].propStatus, forceCopied | statuses[i].isCopied, statuses[i].isSwitched);
				if (statuses[i].lockToken != null) {
					changeMask |= ILocalResource.IS_LOCKED;
				}
				changeMask |= externalMask;
				
				String status = this.getStatusString(statuses[i].propStatus, statuses[i].textStatus, false);
				if (!statuses[i].isSwitched && statuses[i].url != null && !SVNUtility.decodeURL(statuses[i].url).startsWith(desiredUrl)) {
					changeMask |= ILocalResource.IS_SWITCHED;
				}
				
				if ((changeMask & ILocalResource.IS_SWITCHED) != 0) {
					this.switchedToUrls.put(tRes.getFullPath(), SVNUtility.decodeURL(statuses[i].url));
				}
				
				if (status == IStateFilter.ST_DELETED && nodeKind == SVNEntry.Kind.FILE && new File(statuses[i].path).exists()) {
				    status = IStateFilter.ST_PREREPLACED;
				}
				
				if (FileUtility.isLinked(tRes)) {
				    status = IStateFilter.ST_LINKED;
				}
				else if (status != IStateFilter.ST_OBSTRUCTED && statuses[i].textStatus == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.UNVERSIONED) {
				    status = this.getDelegatedStatus(tRes, IStateFilter.ST_NEW, changeMask);
				}
				
				if (status == IStateFilter.ST_NEW && this.canFetchStatuses(new Path(fsNodePath))) {
					status = IStateFilter.ST_OBSTRUCTED;
				}

				// fetch revision for "copied from"
				long revision = statuses[i].lastChangedRevision == SVNRevision.INVALID_REVISION_NUMBER && (changeMask & ILocalResource.IS_COPIED) != 0 ? statuses[i].revision : statuses[i].lastChangedRevision;
				local = this.registerResource(tRes, revision, statuses[i].revision, status, changeMask, statuses[i].lastCommitAuthor, statuses[i].lastChangedDate, statuses[i].treeConflictDescriptor);
			}
			else {
				this.writeChild(local.getResource(), local.getStatus(), local.getChangeMask());
			}

			if (tRes == resource) {
				retVal = local;
			}
		}
		
		return retVal;
	}
	
	protected String getDelegatedStatus(IResource resource, String status, int mask) {
	    // calculate applicability of delegated status
	    if (IStateFilter.SF_LINKED.accept(resource, status, mask) ||
	        FileUtility.isLinked(resource)) {
	        return IStateFilter.ST_LINKED;
	    }
	    if (IStateFilter.SF_OBSTRUCTED.accept(resource, status, mask)) {
	        return IStateFilter.ST_OBSTRUCTED;
	    }
	    if (IStateFilter.SF_IGNORED.accept(resource, status, mask) || 
		    SVNUtility.isIgnored(resource)) {
	        return IStateFilter.ST_IGNORED;
	    }
	    return this.getTopLevelStatus(resource, status, mask);
	}
	
	protected String getTopLevelStatus(IResource resource, String status, int mask) {
		ILocalResource topLevel = this.getFirstExistingParentLocal(resource);
		if (topLevel != null) {
		    String topLevelStatus = topLevel.getStatus();
		    IResource topLevelResource = topLevel.getResource();
		    if (IStateFilter.SF_OBSTRUCTED.accept(topLevelResource, topLevelStatus, mask) ||
		        IStateFilter.SF_LINKED.accept(topLevelResource, topLevelStatus, mask) ||
		        IStateFilter.SF_IGNORED.accept(topLevelResource, topLevelStatus, mask)) {
			    return topLevelStatus;
		    }
		}
	    return status;
	}
	
	protected ILocalResource registerResource(IResource current, long revision, long baseRevision, String status, int changeMask, String author, long date, SVNConflictDescriptor treeConflictDescriptor) {
	    SVNLocalResource local = null;
	    
	    if (IStateFilter.SF_OBSTRUCTED.accept(current, status, changeMask)) {
	    	try {
	        	IIgnoreRecommendations []ignores = CoreExtensionsManager.instance().getIgnoreRecommendations();
	        	for (int i = 0; i < ignores.length; i++) {
	        		if (ignores[i].isAcceptableNature(current) && ignores[i].isOutput(current)) {
	        			IPath location = current.getProject().getLocation();
	        			if (location != null) {
					    	String projectPath = location.removeLastSegments(1).toString();
					    	File checkedResource = new File(projectPath + current.getFullPath().toString());
					    	status = !checkedResource.exists() ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_IGNORED;
					    	// Bug fix: incorrect path calculation; thanks to Panagiotis Korros
				    		IPath invalidMetaPath = new Path(projectPath + current.getFullPath().toString()).append(SVNUtility.getSVNFolderName());
				    		FileUtility.deleteRecursive(invalidMetaPath.toFile());
	        			}
			    		break;
	        		}
	        	}
			} 
	    	catch (CoreException e1) {
	    		// Cannot detect output folder due to some reasons. So, do not process the resource. 
			}
	    }
	    
	    if (IStateFilter.SF_UNVERSIONED.accept(current, status, changeMask) && 
	    	!(IStateFilter.SF_PREREPLACEDREPLACED.accept(current, status, changeMask) || IStateFilter.SF_DELETED.accept(current, status, changeMask)) ||
	        IStateFilter.SF_LINKED.accept(current, status, changeMask)) {
	        revision = SVNRevision.INVALID_REVISION_NUMBER;
	        author = null;
	        date = -1;
	    }

	    local = 
	    	current instanceof IContainer ?
	    	(SVNLocalResource)new SVNLocalFolder(current, revision, baseRevision, status, changeMask, author, date, treeConflictDescriptor) : 
	    	new SVNLocalFile(current, revision, baseRevision, status, changeMask, author, date, treeConflictDescriptor);

		//  handle parent-to-child relations
	    this.writeChild(current, status, changeMask);
		
		this.localResources.put(current.getFullPath(), local);

		return local;
	}
	
	protected void writeChild(IResource current, String status, int mask) {
	    if (!SVNRemoteStorage.SF_NONSVN.accept(current, status, mask)) {
			IResource parent = current.getParent();
			Set children = (Set)this.parent2Children.get(parent.getFullPath());
			if (children == null) {
				this.parent2Children.put(parent.getFullPath(), children = new HashSet());
			}
			children.add(current);
	    }
	}
	
	protected ILocalResource getFirstExistingParentLocal(IResource node) {
		IResource parent = node.getParent();
		if (parent == null) {
			return null;
		}
		ILocalResource local = (ILocalResource)this.localResources.get(parent.getFullPath());
		if (local != null
				/* && 
				!IStateFilter.SF_INTERNAL_INVALID.accept(local) &&
				!IStateFilter.SF_IGNORED.accept(local)
				*/
				) {
			return local;
		}
		return this.getFirstExistingParentLocal(parent);
	}
	
	protected String deserializeStatus(String status) {
		if ("null".equals(status)) { //$NON-NLS-1$
			return IStateFilter.ST_NOTEXISTS;
		}
		else if (IStateFilter.ST_IGNORED.equals(status)) {
			return IStateFilter.ST_IGNORED;
		}
		else if (IStateFilter.ST_NEW.equals(status)) {
			return IStateFilter.ST_NEW;
		}
		else if (IStateFilter.ST_ADDED.equals(status)) {
			return IStateFilter.ST_ADDED;
		}
		else if (IStateFilter.ST_NORMAL.equals(status)) {
			return IStateFilter.ST_NORMAL;
		}
		else if (IStateFilter.ST_MODIFIED.equals(status)) {
			return IStateFilter.ST_MODIFIED;
		}
		else if (IStateFilter.ST_CONFLICTING.equals(status)) {
			return IStateFilter.ST_CONFLICTING;
		}
		else if (IStateFilter.ST_DELETED.equals(status)) {
			return IStateFilter.ST_DELETED;
		}
		else if (IStateFilter.ST_MISSING.equals(status)) {
			return IStateFilter.ST_MISSING;
		}
		else if (IStateFilter.ST_OBSTRUCTED.equals(status)) {
			return IStateFilter.ST_OBSTRUCTED;
		}
		else if (IStateFilter.ST_PREREPLACED.equals(status)) {
			return IStateFilter.ST_PREREPLACED;
		}
		else if (IStateFilter.ST_REPLACED.equals(status)) {
			return IStateFilter.ST_REPLACED;
		} 
		throw new RuntimeException(SVNMessages.getErrorString("Error_UnknownStatus")); //$NON-NLS-1$
	}
	
	public String getStatusString(int propKind, int textKind, boolean isRemoteStatus) {
		String status = IStateFilter.ST_NORMAL;
				
		switch (textKind) {
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.IGNORED: {
				status = IStateFilter.ST_IGNORED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.UNVERSIONED: {
				status = isRemoteStatus ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NEW;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.ADDED: {
				status = IStateFilter.ST_ADDED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.DELETED: {
				status = IStateFilter.ST_DELETED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MISSING: {
				status = IStateFilter.ST_MISSING;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED: {
				status = isRemoteStatus ? IStateFilter.ST_MODIFIED : IStateFilter.ST_CONFLICTING;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED: {
				status = IStateFilter.ST_MODIFIED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.OBSTRUCTED: {
				status = IStateFilter.ST_OBSTRUCTED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.REPLACED: {
				status = IStateFilter.ST_REPLACED;
				break;
			}
			case org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE: {
				if (!isRemoteStatus && propKind == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE) {
					status = IStateFilter.ST_NOTEXISTS;
				}
				break;
			}
		}
		if (status == IStateFilter.ST_NORMAL) {
			if (propKind == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED) {
				status = IStateFilter.ST_CONFLICTING;
			}
			else if (propKind == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED) {
				status = IStateFilter.ST_MODIFIED;
			}
		}
		
		return status;
	}
	
	protected int getChangeMask(int textStatus, int propKind, boolean isCopied, boolean isSwitched) {
		int changeMask = ILocalResource.NO_MODIFICATION;
		if (propKind == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED || propKind == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED) {
			changeMask |= ILocalResource.PROP_MODIFIED; 
		}
		if (textStatus != org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NORMAL) {
			changeMask |= ILocalResource.TEXT_MODIFIED;
		}
		if (isCopied) {
			changeMask |= ILocalResource.IS_COPIED;
		}
		if (isSwitched) {
			changeMask |= ILocalResource.IS_SWITCHED;
		}
		return changeMask;
	}
	
	private SVNRemoteStorage() {
		super();
		this.localResources = Collections.synchronizedMap(new HashMap());
		this.switchedToUrls = Collections.synchronizedMap(new LinkedHashMap());
		this.parent2Children = new HashMap();
		this.externalsLocations = new HashMap();
		this.resourceStateListeners = new HashMap<Class, List<IResourceStatesListener>>();
		this.fetchQueue = new LinkedList();
	}

	private static final IStateFilter SF_NONSVN = new IStateFilter.AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_IGNORED || state == IStateFilter.ST_NOTEXISTS || 
				state == IStateFilter.ST_LINKED || state == IStateFilter.ST_OBSTRUCTED;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};
	
}
