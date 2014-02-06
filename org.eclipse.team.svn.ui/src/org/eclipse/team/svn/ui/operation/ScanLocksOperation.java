/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;

/**
 * Scan locks operation
 * 
 * @author Igor Burilo
 */
public class ScanLocksOperation extends AbstractActionOperation {

	protected IResource[] resources;
	protected int depth;
	
	protected Map<IResource, List<LockResource>> lockResources = new HashMap<IResource, List<LockResource>>();
	
	public ScanLocksOperation(IResource[] resources) {
		this(resources, ISVNConnector.Depth.INFINITY);
	}
	
	public ScanLocksOperation(IResource[] resources, int depth) {
		super("Operation_ScanLocks", SVNUIMessages.class); //$NON-NLS-1$
		this.resources = resources;		
		this.depth = depth;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource[] shrinkedResources = FileUtility.shrinkChildNodes(this.resources);						
		for (int i = 0; i < shrinkedResources.length && !monitor.isCanceled(); i ++) {
			final IResource resource = shrinkedResources[i];
			this.protectStep(new IUnprotectedOperation() {				
				public void run(IProgressMonitor monitor) throws Exception {
					List<SVNChangeStatus> lockStatuses = new ArrayList<SVNChangeStatus>();
					
					IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
					ISVNConnector proxy = location.acquireSVNProxy();
					try {																			
						SVNChangeStatus[] changeStatuses = SVNUtility.status(proxy, FileUtility.getWorkingCopyPath(resource), ScanLocksOperation.this.depth, ISVNConnector.Options.SERVER_SIDE, new SVNProgressMonitor(ScanLocksOperation.this, monitor, null));
						//filter out resources which don't have locks
						for (SVNChangeStatus status : changeStatuses) {				
							if (status.wcLock != null || status.reposLock != null) {
								lockStatuses.add(status);
							}
						}
					}
					finally {
					    location.releaseSVNProxy(proxy);
					}	
					
					if (!lockStatuses.isEmpty()) {
						List<LockResource> lockResourcesList = new ArrayList<LockResource>();
						ScanLocksOperation.this.lockResources.put(resource, lockResourcesList);						
						for (SVNChangeStatus status : lockStatuses) {
							Path path = new Path(status.path);
							String name = path.lastSegment();
							LockResource lockResource = ScanLocksOperation.this.createLockFile(status, name);
							lockResourcesList.add(lockResource);				
						}						
					}					
				}
			}, monitor, shrinkedResources.length);						
		}																			
	}		
	
	public Map<IResource, List<LockResource>> getLockResourcesMap() {
		return this.lockResources;
	}	
	
	public LockResource[] getLockResources() {
		List<LockResource> res = new ArrayList<LockResource>();
		for (List<LockResource> list : this.lockResources.values()) {
			res.addAll(list);
		}
		return res.toArray(new LockResource[0]);
	}
	
	protected LockResource createLockFile(SVNChangeStatus status, String resourceName) {		
		LockStatusEnum lockStatus = null;
		String owner = null;
		Date creationDate = null;
		String comment = null;
				
		if (status.wcLock != null && status.reposLock != null && status.wcLock.owner.equals(status.reposLock.owner) && status.wcLock.token.equals(status.reposLock.token)) {
			//locally locked
			lockStatus = LockStatusEnum.LOCALLY_LOCKED;
			owner = status.wcLock.owner;
			creationDate = new Date(status.wcLock.creationDate);
			comment = status.wcLock.comment;
		} else if ((status.wcLock == null || status.wcLock.token == null) && status.reposLock != null) {
			//other locked
			lockStatus = LockStatusEnum.OTHER_LOCKED;
			owner = status.reposLock.owner;
			creationDate = new Date(status.reposLock.creationDate);
			comment = status.reposLock.comment;
		} else if (status.wcLock != null && status.wcLock.token != null && status.reposLock == null) {
			//broken
			lockStatus = LockStatusEnum.BROKEN;
			owner = status.wcLock.owner;
			creationDate = new Date(status.wcLock.creationDate);
			comment = status.wcLock.comment;
		} else if (status.wcLock != null && status.wcLock.token != null && status.reposLock != null && !status.wcLock.token.equals(status.reposLock.token)) {
			//stolen
			lockStatus = LockStatusEnum.STOLEN;
			owner = status.reposLock.owner;
			creationDate = new Date(status.reposLock.creationDate);
			comment = status.reposLock.comment;
		}		
		return lockStatus != null ? new LockResource(resourceName, owner, true, lockStatus, creationDate, comment, status.path, status.url) : null;			
	}
	
	public static class CreateLockResourcesHierarchyOperation extends AbstractActionOperation {

		protected ScanLocksOperation scanOp;
		
		protected LockResource lockResourceRoot;
		
		public CreateLockResourcesHierarchyOperation(ScanLocksOperation scanOp) {			
			super("Operation_CreateLockResourcesHierarchy", SVNUIMessages.class); //$NON-NLS-1$
			this.scanOp = scanOp;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {			
			//get first resource
			Map<IResource, List<LockResource>> lockResources = this.scanOp.getLockResourcesMap();
			if (!lockResources.isEmpty()) {
				Set<IResource> keys = lockResources.keySet();
				IResource resource = keys.iterator().next();							
				List<LockResource> lockResourcesList = lockResources.get(resource);				
				
				this.lockResourceRoot = this.mapChangeStatusesToLockResource(resource, lockResourcesList.toArray(new LockResource[0]));
				this.compressLockResources(this.lockResourceRoot);	
			}																
		}
		
		protected LockResource mapChangeStatusesToLockResource(IResource resource, LockResource[] resources) {
			if (resource.getType() == IResource.FILE) {
				resource = resource.getParent();
			}
			String rootPath = resource.getFullPath().toString();		
			if (rootPath.startsWith("/")) { //$NON-NLS-1$
				rootPath = rootPath.substring(1);
			}		 
			LockResource root = LockResource.createDirectory(rootPath);
						
			String fullRootPath = FileUtility.getWorkingCopyPath(resource);
			for (LockResource lockResource : resources) {
				String path = lockResource.getFullFileSystemPath();
				
				if (path.startsWith(fullRootPath)) {
					String relativePath = path.substring(fullRootPath.length());
					if (relativePath.startsWith("/")) { //$NON-NLS-1$
						relativePath = relativePath.substring(1);
					}
					
					String[] subPaths = relativePath.split("/"); //$NON-NLS-1$
					LockResource parent = root;
					for (int i = 0; i < subPaths.length; i ++) {
						String subPath = subPaths[i];
						LockResource child = parent.getChildByName(subPath);
						if (child == null) {
							//last element is always a file
							if (i == subPaths.length - 1) {
								child = lockResource;								
							} else {
								//folder
								child = LockResource.createDirectory(subPath);
							}						
							parent.addChild(child);
						}
						if (child != null) {
							parent = child;	
						}					
					}
				}		
			}
			return root;
		}
		
		protected void compressLockResources(LockResource resource) {
			if (resource.isFile()) {
				return;
			}
			
			LockResource[] children = resource.getChildren();
			for (LockResource child : children) {
				if (!child.isFile()) {
					this.compressLockResources(child);	
				}				
			}						
			
			/*
			 * src + com + Foo.java should be compressed to src/com + Foo.java
			 */
			if (!resource.isRoot() && children.length == 1 && !children[0].isFile()) {
				resource.setName(resource.getName() + "/" + children[0].getName()); //$NON-NLS-1$
				resource.removeChild(children[0]);
				resource.addChildren(children[0].getChildren());				
			}
		}		
		
		public LockResource getLockResourceRoot() {
			return this.lockResourceRoot;
		}		
	}
}
