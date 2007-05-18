/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Panagiotis Korros - bug fix: .svn folder is deleted by subversive
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import java.io.File;
import java.text.MessageFormat;
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
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.StatusKind;
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
import org.eclipse.team.svn.core.resource.ISVNStorage;
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
	public static final String STATE_INFO_FILE_NAME = ".svnRepositories";
	
	private static SVNRemoteStorage instance = new SVNRemoteStorage();
	
	protected Map localResources;
	protected Map switchedToUrls;
	protected Map externalsLocations;
	protected Map parent2Children;
	protected Map resourceStateListeners;
	protected LinkedList fetchQueue;

	public static SVNRemoteStorage instance() {
		return SVNRemoteStorage.instance;
	}
	
    public void addResourceStatesListener(Class eventClass, IResourceStatesListener listener) {
    	synchronized (this.resourceStateListeners) {
    		List listenersList = (List)this.resourceStateListeners.get(eventClass);
    		if (listenersList == null) {
    			this.resourceStateListeners.put(eventClass, listenersList = new ArrayList());
    		}
    		if (!listenersList.contains(listener)) {
    			listenersList.add(listener);
    		}
    	}
    }
    
    public void removeResourceStatesListener(Class eventClass, IResourceStatesListener listener) {
    	synchronized (this.resourceStateListeners) {
    		List listenersList = (List)this.resourceStateListeners.get(eventClass);
    		if (listenersList != null) {
    			listenersList.remove(listener);
        		if (listenersList.size() == 0) {
        			this.resourceStateListeners.remove(eventClass);
        		}
    		}
    	}
    }
    
    public void fireResourceStatesChangedEvent(ResourceStatesChangedEvent event) {
    	IResourceStatesListener []listeners = null;
    	synchronized (this.resourceStateListeners) {
    		List listenersArray = (List)this.resourceStateListeners.get(event.getClass());
    		if (listenersArray == null) {
    			return;
    		}
        	listeners = (IResourceStatesListener [])listenersArray.toArray(new IResourceStatesListener[listenersArray.size()]);
    	}
    	for (int i = 0; i < listeners.length; i++) {
    		listeners[i].resourcesStateChanged(event);
    	}
    }
    
	public void initialize(IPath stateInfoLocation) throws Exception {
		this.initializeImpl(stateInfoLocation, SVNRemoteStorage.STATE_INFO_FILE_NAME);
	}
	
	public IResourceChange asResourceChange(IChangeStateProvider changeState) {
		IResource resource = null;
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath location = new Path(changeState.getLocalPath());
		int nodeKind = changeState.getNodeKind();
		int propKind = changeState.getPropertiesChangeType();
		int textKind = changeState.getTextChangeType();
		boolean isCopied = changeState.isCopied();
		boolean isSwitched = changeState.isSwitched();
	    Revision.Number remoteRevision = changeState.getChangeRevision();
		long revision = remoteRevision != null ? remoteRevision.getNumber() : Revision.SVN_INVALID_REVNUM;
		// repositoryTextStatus can be StatusKind::none in two cases: resource not modified and non versioned
		// in the second case we should ignore repository status calculation
		String statusStr = revision == Revision.SVN_INVALID_REVNUM ? IStateFilter.ST_NOTEXISTS : this.getStatusString(propKind, textKind, true);
		if (nodeKind == NodeKind.dir) {
		    if ((resource = root.getContainerForLocation(location)) == null) {
		    	return null;
		    }
		    int changeMask = this.getChangeMask(textKind, propKind, isCopied, isSwitched);
		    if (IStateFilter.SF_NOTEXISTS.accept(resource, statusStr, changeMask)) {
				revision = Revision.SVN_INVALID_REVNUM;
			}
			return new SVNFolderChange(resource, revision, statusStr, changeMask, changeState.getChangeAuthor(), changeState.getChangeDate(), null, changeState.getComment());
		}
		else {
		    if ((resource = root.getFileForLocation(location)) == null) {
		    	return null;
		    }
		    int changeMask = this.getChangeMask(textKind, propKind, isCopied, isSwitched);
		    if (IStateFilter.SF_NOTEXISTS.accept(resource, statusStr, changeMask)) {
				revision = Revision.SVN_INVALID_REVNUM;
			}			    
			return new SVNFileChange(resource, revision, statusStr, changeMask, changeState.getChangeAuthor(), changeState.getChangeDate(), null, changeState.getComment());
		}
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
	/*0*/	String.valueOf(resource instanceof ILocalFolder) + ";" + 
	/*1*/	new String(Base64.encode(FileUtility.getWorkingCopyPath(resource.getResource()).getBytes())) + ";" + 
	/*2*/	resource.getRevision() + ";" + 
	/*3*/	resource.getStatus() + ";" +
	/*4*/	resource.getAuthor() + ";" + 
	/*5*/	(lastCommitDate == 0 ? "null" : String.valueOf(lastCommitDate)) + ";" +
	/*6*/	String.valueOf(kind) + ";" + 
	/*7*/	(kind == Revision.Kind.number ? String.valueOf(((Revision.Number)resource.getPegRevision()).getNumber()) : String.valueOf(kind)) + ";" +
	/*8*/	(originatorData != null ? originatorData : "null") + ";" +
	/*9*/	(comment == null ? "null" : new String(Base64.encode(comment.getBytes()))) + ";" +
	/*10*/	resource.getChangeMask();
		return retVal.getBytes();
	}
	
	public IResourceChange resourceChangeFromBytes(byte []bytes) {
		if (bytes == null) {
			return null;
		}
		String []data = new String(bytes).split(";");
		boolean isFolder = "true".equals(data[0]);
		String name = new String(Base64.decode(data[1].getBytes()));
		long revision = Long.parseLong(data[2]);
		String status = this.deserializeStatus(data[3]);
		String author = "null".equals(data[4]) ? null : data[4];
		long lastCommitDate = "null".equals(data[5]) ? 0 : Long.parseLong(data[5]);
		int revisionKind = Integer.parseInt(data[6]);
		Revision pegRevision = null;
		if (revisionKind == Revision.Kind.number) {
		    long pegNum = Long.parseLong(data[7]);
		    pegRevision = pegNum == revision ? null : Revision.getInstance(pegNum);
		}
		else {
		    pegRevision = new ISVNStorage.KindBasedRevision(revisionKind);
		}
		String comment = "null".equals(data[9]) ? null : new String(Base64.decode(data[9].getBytes()));
		int changeMask = "null".equals(data[10]) ? ILocalResource.NO_MODIFICATION : Integer.parseInt(data[10]);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResourceChange change = 
			isFolder ? 
			(IResourceChange)new SVNFolderChange(root.getContainerForLocation(new Path(name)), revision, status, changeMask, author, lastCommitDate, pegRevision, comment) : 
			new SVNFileChange(root.getFileForLocation(new Path(name)), revision, status, changeMask, author, lastCommitDate, pegRevision, comment);

		if (!"null".equals(data[8])) {
			byte []originatorData = Base64.decode(data[8].getBytes());
			change.setOriginator(this.repositoryResourceFromBytes(originatorData));
		}
		
		return change;
	}
	
	public synchronized IResource []getRegisteredChildren(IContainer container) throws Exception {
		// for null, inaccessible resource and workspace root members shouldn't be provided
		if (container == null || !container.isAccessible() || container.getProject() == null) {
			return null;
		}
		IResource []members = FileUtility.resourceMembers(container, false);
		
		Set retVal = (Set)this.parent2Children.get(container.getFullPath());
		if (retVal == null) {
			ILocalResource local = this.asLocalResource(container);
			if (local == null || !SVNRemoteStorage.SF_NONSVN.accept(container, local.getStatus(), local.getChangeMask())) {
				this.loadLocalResourcesSubTree(container, !CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled());
				retVal = (Set)this.parent2Children.get(container.getFullPath());
			}
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
			return null;
		}
		ILocalResource retVal = (ILocalResource)this.localResources.get(resource.getFullPath());
		if (retVal == null) {
			ILocalResource parent = this.getFirstExistingParentLocal(resource);
			int mask = parent == null ? 0 : (parent.getChangeMask() & ILocalResource.IS_SWITCHED);
			retVal = 
				resource.getType() == IResource.FILE ? 
				(ILocalResource)new SVNLocalFile(resource, Revision.SVN_INVALID_REVNUM, IStateFilter.ST_NOTEXISTS, mask, null, -1) :
				new SVNLocalFolder(resource, Revision.SVN_INVALID_REVNUM, IStateFilter.ST_NOTEXISTS, mask, null, -1);
		}
		return retVal;
	}
	
	public ILocalResource asLocalResource(IResource resource) {
		// null resource and workspace root shouldn't be provided
		if (resource == null || resource.getProject() == null) {
			return null;
		}
		ILocalResource local = (ILocalResource)this.localResources.get(resource.getFullPath());
		if (local == null) {
			synchronized (this) {
				local = (ILocalResource)this.localResources.get(resource.getFullPath());
				if (local == null) {
					if (resource.getProject().isAccessible()) {
						try {
							local = this.loadLocalResourcesSubTree(resource, !CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled());
						} 
						catch (RuntimeException ex) {
							throw ex;
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
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
				if (url.startsWith(cachedUrl)) {
					IPath target = ((IPath)entry.getKey()).append(url.substring(cachedUrl.length())).removeFirstSegments(1);
					return this.asLocalResource(kind == IResource.FOLDER ? (IResource)project.getFolder(target) : project.getFile(target));
				}
			}
		}
		String projectUrl = this.asRepositoryResource(project).getUrl();
		if (url.length() < projectUrl.length()) {
			return null;
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
				
				url = (String)this.switchedToUrls.get(parentPath) + "/" + resource.getFullPath().removeFirstSegments(parentPath.segmentCount()).toString();
			}
		}
		
		if (url == null) {
			url = this.makeUrl(resource, baseResource);
		}
		else if (!url.startsWith(location.getRepositoryRootUrl())) {
			location = this.wrapLocationIfRequired(location, url, resource.getType() == IResource.FILE);
		}
		
		return resource instanceof IContainer ? (IRepositoryResource)location.asRepositoryContainer(url, false) : location.asRepositoryFile(url, false);
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
	
	protected String makeUrl(IResource resource, IRepositoryResource baseResource) {
		if (resource.getType() == IResource.PROJECT) {
			return baseResource.getUrl();
		}
		IProject project = resource.getProject();
		String url = resource.getFullPath().toString();
		//truncating of the project name allow us to remap a content of project with name 'A' to the remote folder called 'B'
		return baseResource.getUrl() + "/" + url.substring(project.getFullPath().toString().length() + 1);
	}
	
	protected IConnectedProjectInformation getConnectedProjectInformation(IProject project) {
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if (provider == null) {
			String errMessage = SVNTeamPlugin.instance().getResource("Error.NotConnectedProject");
			throw new UnreportableException(MessageFormat.format(errMessage, new String[] {project.getName()}));
		}
		if (!(provider instanceof IConnectedProjectInformation)) {
			String errMessage = SVNTeamPlugin.instance().getResource("Error.AnotherProvider");
			throw new UnreportableException(MessageFormat.format(errMessage, new String[] {project.getName(), provider.getID()}));
		}
		
		return (IConnectedProjectInformation)provider;
	}
	
	protected void refreshLocalResourceImpl(IResource resource, int depth) {	   
	    ArrayList removed = new ArrayList();
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
		    IPath currentPath = (IPath)current.getFullPath();
	        if (resource.getFullPath().isPrefixOf(currentPath) || IStateFilter.SF_NOTEXISTS.accept(current, local.getStatus(), local.getChangeMask())) {
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
	
	protected ILocalResource loadLocalResourcesSubTree(final IResource resource, boolean noCache) throws Exception {
		IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID);
		if (provider == null || FileUtility.isSVNInternals(resource)) {
			return null;
		}
		
		if (noCache) {
			this.localResources.clear();
		}
		
		ILocalResource retVal = null;
	    boolean isLinked = FileUtility.isLinked(resource);
		IResource parent = resource.getParent();
		boolean parentExists = parent != null && parent.isAccessible();
		if (parentExists && !isLinked) {
			ILocalResource parentLocal = this.getFirstExistingParentLocal(resource);
			if (parentLocal == null || !SVNRemoteStorage.SF_NONSVN.accept(parentLocal.getResource(), parentLocal.getStatus(), parentLocal.getChangeMask())) {
			    retVal = this.loadLocalResourcesSubTreeSVNImpl(provider, resource, noCache);
			}
		}

		return retVal == null ? this.loadUnversionedSubtree(resource, isLinked, noCache) : retVal;
	}
	
	protected ILocalResource loadUnversionedSubtree(final IResource resource, boolean isLinked, boolean noCache) throws CoreException {
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
            	ILocalResource retVal = SVNRemoteStorage.this.registerResource(child, Revision.SVN_INVALID_REVNUM, state, changeMask, null, -1);
                if (tmp[0] == null) {
                	tmp[0] = retVal;
                }
                return true;
            }
        }, noCache ? IResource.DEPTH_ONE : IResource.DEPTH_INFINITE);
        
		return tmp[0];
	}
	
	protected String calculateUnversionedStatus(final IResource resource, boolean isLinked) {
		String status = IStateFilter.ST_NOTEXISTS;
		if (isLinked) {
		    status = IStateFilter.ST_LINKED;
		}
		else {
			IPath location = resource.getLocation();
			if (location != null && new File(location.toString()).exists()) {
			    // may be ignored ?
				status = IStateFilter.ST_NONE;
				if (!SVNUtility.isIgnored(resource)) {
				    status = this.getTopLevelStatus(resource, IStateFilter.ST_NEW, 0);
				}
			}
		}
		return status;
	}
	
	protected ILocalResource loadLocalResourcesSubTreeSVNImpl(IConnectedProjectInformation provider, IResource resource, boolean noCache) throws Exception {
		IProject project = resource.getProject();
		IResource target = null;
		boolean recursively = false;
		if (resource.getType() == IResource.FILE) {
			target = resource.getParent();
		}
		else {
			target = resource;
			recursively = target.getFullPath().segmentCount() > 2 & !noCache;
		}

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
			recursively = false;
			resourcePath = resourcePath.removeLastSegments(1);
			target = target.getParent();
		}
		IRepositoryResource baseResource = provider.getRepositoryResource();
		Status []statuses = this.getStatuses(baseResource.getRepositoryLocation(), resourcePath.toString(), recursively);
		String desiredUrl = this.makeUrl(target, baseResource);
		ILocalResource retVal = this.fillCache(statuses, desiredUrl, resource, subPathStart, requestedPath);
		
		if (statuses.length == 1 || statuses.length > 1 && this.parent2Children.get(target.getFullPath()) == null) {
			// the caching is done for the folder if it is empty 
			this.parent2Children.put(target.getFullPath(), new HashSet());
		}
		
		if (retVal != null && hasSVNMeta && !recursively && statuses.length > 1 && !noCache) {
			this.scheduleStatusesFetch(statuses, target);
		}
		
		return retVal;
	}
	
	protected void scheduleStatusesFetch(final Status []st, IResource target) {
		synchronized (this.fetchQueue) {
			this.fetchQueue.add(new Object[] {st, target});
			if (this.fetchQueue.size() == 1) {
				ProgressMonitorUtility.doTaskScheduledDefault(new AbstractActionOperation("Operation.UpdateSVNCache") {
					public ISchedulingRule getSchedulingRule() {
						return null;
					}
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
						while (CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled()) {
							Status [] st;
							IResource target;
							synchronized (SVNRemoteStorage.this.fetchQueue) {
								if (SVNRemoteStorage.this.fetchQueue.size() == 0) {
									break;
								}
								Object []entry = (Object [])SVNRemoteStorage.this.fetchQueue.get(0);
								st = (Status [])entry[0];
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
					protected void processEntry(IProgressMonitor monitor, Status []st, IResource target) {
						IProject prj = target.getProject();
						IPath location = prj.getLocation();
						if (location != null) {
							SVNUtility.reorder(st, false);
							int projectEnd = location.toString().length();
							for (int i = 0; i < st.length && !monitor.isCanceled() && CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled(); i++) {
								ProgressMonitorUtility.progress(monitor, i, IProgressMonitor.UNKNOWN);
								
								if (st[i].nodeKind == NodeKind.dir && st[i].path.length() > projectEnd) {
									IResource folder = prj.getFolder(new Path(st[i].path.substring(projectEnd)));
									ProgressMonitorUtility.setTaskInfo(monitor, this, folder.getFullPath().toString());
									ILocalFolder local = (ILocalFolder)SVNRemoteStorage.this.asLocalResource(folder);
									if (local != null) {
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
	
	protected Status []getStatuses(IRepositoryLocation location, String path, boolean recursively) throws Exception {
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			Status []statuses = proxy.status(path, recursively, false, true, true, true, false, new SVNNullProgressMonitor());
			SVNUtility.reorder(statuses, true);
			return statuses;
		}
		// FIXME The only discovered reason of SBV-3557 defect is that some
		// folders under the ignored path contains completely or partially
		// empty .svn folders. May be due to using external tools.
		// So, suppress throwing "Path is not a working copy directory" exception.
		// The magic number 155007 equals to "Path is not a working copy directory"
		// error code.
		catch (ClientWrapperException cwe) {
			if (cwe.getErrorId() != 155007) {
				throw cwe;
			}
			return new Status[0];
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected ILocalResource fillCache(Status []statuses, String desiredUrl, IResource resource, int subPathStart, IPath requestedPath) {
		IProject project = resource.getProject();
		
		ILocalResource retVal = null;
		
		for (int i = 0; i < statuses.length; i++) {
			int nodeKind = SVNUtility.getNodeKind(statuses[i].path, statuses[i].nodeKind, true);
			// ignore files absent in the WC base and WC working. But what is the reason why it is reported ?
			if (nodeKind == NodeKind.none) {
				continue;
			}
			
			String nodePath = statuses[i].path;
			
			nodePath = nodePath.length() >= subPathStart ? nodePath.substring(subPathStart) : "";
			if (nodePath.length() > 0 && nodePath.charAt(nodePath.length() - 1) == '/') {
				nodePath = nodePath.substring(0, nodePath.length() - 1);
			}
			else if (i > 0 && nodePath.trim().length() == 0) {
				continue;
				// Debug code is switched off. We already have all information about the JavaSVN bug.
//			    throw new RuntimeException(
//			        "Current [" + i + "] path '" + statuses[i].getPath() + 
//			        "' with status '" + statuses[i].getTextStatusDescription() + ":" + statuses[i].getPropStatusDescription() + 
//			        "' and root [0] path '" + statuses[0].getPath() + 
//			        "' with status '" + statuses[0].getTextStatusDescription() + ":" + statuses[0].getPropStatusDescription() + "' reported by JavaSVN." + 
//			        "Statuses requested for '" + path + "' that is related to project '" + projectPath + "'.");
			}
			
			IResource tRes = null;
			if (new Path(statuses[i].path).equals(requestedPath) && (resource.getType() > IResource.FOLDER || resource.getType() == nodeKind)) {//if nodekind not equals do not use default resource
			    tRes = resource;
			}
			else {
				if (nodeKind == NodeKind.dir) {
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
				int externalMask = statuses[i].textStatus == StatusKind.external ? ILocalResource.IS_EXTERNAL : 0;
				if (externalMask != 0) {
					statuses[i] = SVNUtility.getSVNInfoForNotConnected(tRes);
					if (statuses[i] == null) {
						continue;
					}
				}
				else {
					ILocalResource parent = this.getFirstExistingParentLocal(tRes);
					if (parent != null && (parent.getChangeMask() & ILocalResource.IS_EXTERNAL) != 0) {
						externalMask = ILocalResource.IS_EXTERNAL;
					}
				}
				
				int changeMask = this.getChangeMask(statuses[i].textStatus, statuses[i].propStatus, statuses[i].isCopied, statuses[i].isSwitched);
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
				
				if (status == IStateFilter.ST_DELETED && nodeKind == NodeKind.file && new File(statuses[i].path).exists()) {
				    status = IStateFilter.ST_PREREPLACED;
				}
				
				if (FileUtility.isLinked(tRes)) {
				    status = IStateFilter.ST_LINKED;
				}
				else if (status != IStateFilter.ST_OBSTRUCTED && statuses[i].textStatus == Status.Kind.unversioned) {
				    status = this.getDelegatedStatus(tRes, IStateFilter.ST_NEW, changeMask);
				}
				
				if (status == IStateFilter.ST_NEW && this.canFetchStatuses(tRes.getLocation())) {
					status = IStateFilter.ST_OBSTRUCTED;
				}

				local = this.registerResource(tRes, statuses[i].lastChangedRevision, status, changeMask, statuses[i].lastCommitAuthor, statuses[i].lastChangedDate);
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
	        return IStateFilter.ST_NONE;
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
	
	protected ILocalResource registerResource(IResource current, long revision, String status, int changeMask, String author, long date) {
	    SVNLocalResource local = null;
	    
	    if (IStateFilter.SF_OBSTRUCTED.accept(current, status, changeMask)) {
	    	try {
	        	IIgnoreRecommendations []ignores = CoreExtensionsManager.instance().getIgnoreRecommendations();
	        	for (int i = 0; i < ignores.length; i++) {
	        		if (ignores[i].isAcceptableNature(current) && ignores[i].isOutput(current)) {
				    	String projectPath = current.getProject().getLocation().removeLastSegments(1).toString();
				    	File checkedResource = new File(projectPath + current.getFullPath().toString());
				    	status = !checkedResource.exists() ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NONE;
				    	// Bug fix: incorrect path calculation; thanks to Panagiotis Korros
			    		IPath invalidMetaPath = new Path(projectPath + current.getFullPath().toString()).append(SVNUtility.getSVNFolderName());
			    		FileUtility.deleteRecursive(invalidMetaPath.toFile());
			    		break;
	        		}
	        	}
			} 
	    	catch (CoreException e1) {
	    		// Cannot detect output folder due to some reasons. So, do not process the resource. 
			}
	    }
	    
	    if (IStateFilter.SF_NONVERSIONED.accept(current, status, changeMask) && 
	    	!(IStateFilter.SF_PREREPLACEDREPLACED.accept(current, status, changeMask) || IStateFilter.SF_DELETED.accept(current, status, changeMask)) ||
	        IStateFilter.SF_LINKED.accept(current, status, changeMask)) {
	        revision = Revision.SVN_INVALID_REVNUM;
	        author = null;
	        date = -1;
	    }

	    local = 
	    	current instanceof IContainer ?
	    	(SVNLocalResource)new SVNLocalFolder(current, revision, status, changeMask, author, date) : 
	    	new SVNLocalFile(current, revision, status, changeMask, author, date);

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
		if (local != null) {
			return local;
		}
		return this.getFirstExistingParentLocal(parent);
	}
	
	protected String deserializeStatus(String status) {
		if ("null".equals(status)) {
			return IStateFilter.ST_NOTEXISTS;
		}
		else if (IStateFilter.ST_NONE.equals(status)) {
			return IStateFilter.ST_NONE;
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
		throw new RuntimeException(SVNTeamPlugin.instance().getResource("Error.UnknownStatus"));
	}
	
	protected String getStatusString(int propKind, int textKind, boolean isRemoteStatus) {
		String status = IStateFilter.ST_NORMAL;
		
		switch (textKind) {
			case Status.Kind.ignored: {
				status = IStateFilter.ST_NONE;
				break;
			}
			case Status.Kind.unversioned: {
				status = isRemoteStatus ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NEW;
				break;
			}
			case Status.Kind.added: {
				status = IStateFilter.ST_ADDED;
				break;
			}
			case Status.Kind.deleted: {
				status = IStateFilter.ST_DELETED;
				break;
			}
			case Status.Kind.missing: {
				status = IStateFilter.ST_MISSING;
				break;
			}
			case Status.Kind.conflicted: {
				status = isRemoteStatus ? IStateFilter.ST_MODIFIED : IStateFilter.ST_CONFLICTING;
				break;
			}
			case Status.Kind.modified: {
				status = IStateFilter.ST_MODIFIED;
				break;
			}
			case Status.Kind.obstructed: {
				status = IStateFilter.ST_OBSTRUCTED;
				break;
			}
			case Status.Kind.replaced: {
				status = IStateFilter.ST_REPLACED;
				break;
			}
			case Status.Kind.none: {
				if (!isRemoteStatus) {
					status = IStateFilter.ST_NOTEXISTS;
				}
				break;
			}
		}
		if (status == IStateFilter.ST_NORMAL &&
			(propKind == Status.Kind.modified || propKind == Status.Kind.conflicted)) {
			status = IStateFilter.ST_MODIFIED;
		}
		
		return status;
	}
	
	protected int getChangeMask(int textStatus, int propKind, boolean isCopied, boolean isSwitched) {
		int changeMask = ILocalResource.NO_MODIFICATION;
		if (propKind == Status.Kind.modified || propKind == Status.Kind.conflicted) {
			changeMask |= ILocalResource.PROP_MODIFIED; 
		}
		if (textStatus != Status.Kind.normal) {
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
		this.resourceStateListeners = new HashMap();
		this.fetchQueue = new LinkedList();
	}

	private static final IStateFilter SF_NONSVN = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return 
				state == IStateFilter.ST_PREREPLACED || state == IStateFilter.ST_NEW || 
				state == IStateFilter.ST_NONE || state == IStateFilter.ST_NOTEXISTS || 
				state == IStateFilter.ST_LINKED || state == IStateFilter.ST_OBSTRUCTED;
		}
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return true;
		}
	};
	
}
