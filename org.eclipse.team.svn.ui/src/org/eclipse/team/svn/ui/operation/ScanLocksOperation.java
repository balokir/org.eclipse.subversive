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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;

/**
 * Scan locks operation
 * 
 * @author Igor Burilo
 */
public class ScanLocksOperation extends AbstractActionOperation {

	protected IResource wcResource;	
	
	protected LockResource lockResourceRoot;
	
	public ScanLocksOperation(IResource wcResource) {
		super("Operation_ScanLocks"); //$NON-NLS-1$
		this.wcResource = wcResource;		
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final List<SVNChangeStatus> lockStatuses = new ArrayList<SVNChangeStatus>();
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.wcResource);
		ISVNConnector proxy = location.acquireSVNProxy();
		try {																			
			SVNChangeStatus[] changeStatuses = SVNUtility.status(proxy, FileUtility.getWorkingCopyPath(this.wcResource), ISVNConnector.Depth.INFINITY, ISVNConnector.Options.SERVER_SIDE, new SVNProgressMonitor(this, monitor, null));
			//filter out resources which don't have locks
			for (SVNChangeStatus status : changeStatuses) {				
				if (status.lockToken != null || status.reposLock != null) {
					lockStatuses.add(status);
				}
			}
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
				
		if (!lockStatuses.isEmpty()) {
			this.lockResourceRoot = this.mapChangeStatusesToLockResource(this.wcResource, lockStatuses.toArray(new SVNChangeStatus[0]));
			this.compressLockResources(this.lockResourceRoot);			
			//only debug
			//root.showResourcesTree();				
		}							
	}		
	
	public LockResource getLockResourceRoot() {
		return this.lockResourceRoot;
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
	
	protected LockResource mapChangeStatusesToLockResource(IResource resource, SVNChangeStatus[] lockStatuses) {
		if (resource.getType() == IResource.FILE) {
			resource = resource.getParent();
		}
		String rootPath = resource.getFullPath().toString();		
		if (rootPath.startsWith("/")) { //$NON-NLS-1$
			rootPath = rootPath.substring(1);
		}		 
		LockResource root = new LockResource(rootPath);
		
		String fullRootPath = FileUtility.getWorkingCopyPath(resource);
		for (SVNChangeStatus status : lockStatuses) {
			String path = status.path;
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
							child = this.createLockFile(status, subPath);
						} else {
							//folder
							child = new LockResource(subPath);
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
	
	protected LockResource createLockFile(SVNChangeStatus status, String resourceName) {		
		LockStatusEnum lockStatus = null;
		String owner = null;
		Date creationDate = null;
		String comment = null;
				
		if (status.lockToken != null && status.reposLock != null && status.lockOwner.equals(status.reposLock.owner) && status.lockToken.equals(status.reposLock.token)) {
			//locally locked
			lockStatus = LockStatusEnum.LOCALLY_LOCKED;
			owner = status.lockOwner;
			creationDate = new Date(status.lockCreationDate);
			comment = status.lockComment;
		} else if (status.lockToken == null && status.reposLock != null) {
			//other locked
			lockStatus = LockStatusEnum.OTHER_LOCKED;
			owner = status.reposLock.owner;
			creationDate = new Date(status.reposLock.creationDate);
			comment = status.reposLock.comment;
		} else if (status.lockToken != null && status.reposLock == null) {
			//broken
			lockStatus = LockStatusEnum.BROKEN;
			owner = status.lockOwner;
			creationDate = new Date(status.lockCreationDate);
			comment = status.lockComment;
		} else if (status.lockToken != null && status.reposLock != null && !status.lockToken.equals(status.reposLock.token)) {
			//stolen
			lockStatus = LockStatusEnum.STOLEN;
			owner = status.reposLock.owner;
			creationDate = new Date(status.reposLock.creationDate);
			comment = status.reposLock.comment;
		}		
		return lockStatus != null ? new LockResource(resourceName, owner, true, lockStatus, creationDate, comment, status.path, status.url) : null;			
	}
}
