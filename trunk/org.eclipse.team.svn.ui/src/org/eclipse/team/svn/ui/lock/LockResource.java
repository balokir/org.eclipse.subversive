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

package org.eclipse.team.svn.ui.lock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Model element for LocksView
 * 
 * @author Igor Burilo
 */
public class LockResource implements IAdaptable {

	public enum LockStatusEnum {
		LOCALLY_LOCKED, OTHER_LOCKED, BROKEN, STOLEN		
	}
	
	protected final boolean isFile;
	protected final LockStatusEnum lockStatus;	
	protected String name;
	protected final String owner;
	protected final Date creationDate;
	protected final String comment;
	
	protected LockResource parent;	
	protected Set<LockResource> children; 
	
	protected String url;
	protected String fullFileSystemPath;
	
	public interface ILockResourceVisitor {
		public void visit(LockResource lockResource);
	}
	
	//for folders
	public LockResource(String directoryName) {
		this(directoryName, null, false, null, null, null, null, null);
	}
	
	public LockResource(String name, String owner, boolean isFile, LockStatusEnum lockStatus, Date creationDate, String comment, String fullFileSystemPath, String url) {
		this.name = name;
		this.owner = owner;
		this.isFile = isFile;
		this.lockStatus = lockStatus;
		this.creationDate = creationDate;
		this.comment = comment;
		this.fullFileSystemPath = fullFileSystemPath;
		this.url = url;		
		if (!this.isFile) {
			this.children = new HashSet<LockResource>();
		}					
	}
	
//	public boolean hasChildren() {
//		return !this.isFile && !this.children.isEmpty();		
//	}
	
	public void addChild(LockResource lockResource) {
		if (!this.isFile) {
			if (this.children.add(lockResource)) {
				lockResource.parent = this;
			}
		}
	}
	
	public void addChildren(LockResource[] lockResources) {
		if (!this.isFile) {
			for (LockResource lockResource : lockResources) {
				this.addChild(lockResource);
			}
		}
	}
	
	public LockResource[] getChildren() {
		return this.isFile ? new  LockResource[0] : this.children.toArray(new LockResource[0]);
	}
	
	public void removeChild(LockResource lockResource) {
		if (!this.isFile) {
			if (this.children.remove(lockResource)) {
				lockResource.parent = null;
			}
		} 
	}
	
	public LockResource getChildByName(String childName) {
		if (this.isFile) {
			return null;
		}
		for (LockResource child : this.children) {
			if (child.getName().equals(childName)) {
				return child;
			}
		}
		return null;
	}
	
	public boolean isFile() {
		return this.isFile;
	}
	
	/*
	 * path to resource relative to root.
	 * Includes also resource name  
	 */
	public String getPath() {
		return this.isRoot() ? this.name  : (this.parent.getPath() + "/" + this.name); //$NON-NLS-1$
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
		
	public LockStatusEnum getLockStatus() {
		return this.lockStatus;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public LockResource getParent() {
		return parent;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public String toString() {
		return this.getPath();
	}

	public void accept(ILockResourceVisitor visitor) {
		visitor.visit(this);		
	}
	
	public LockResource[] getAllChildFiles() {
		final List<LockResource> res = new ArrayList<LockResource>();
		this.accept(new ILockResourceVisitor() {			
			public void visit(LockResource lockResource) {
				if (lockResource.isFile()) {
					res.add(lockResource);
				} else {
					LockResource[] children = lockResource.getChildren();
					for (LockResource child : children) {
						this.visit(child);
					}
				}
			}
		});
		return res.toArray(new LockResource[0]);
	}
	
	//only for DEBUG
	public void showResourcesTree() {
		this.accept(new ILockResourceVisitor() {
			protected int indent;
			public void visit(LockResource lockResource) {
				String str = this.getStrIndent() + "Name: " + lockResource.getName() + ", path: " + lockResource.getPath(); //$NON-NLS-1$ //$NON-NLS-2$
				if (lockResource.isFile()) {
					str += ", lock status: " + lockResource.getLockStatus().toString();  //$NON-NLS-1$
				}
				System.out.println(str);				
				LockResource[] children = lockResource.getChildren();
				for (LockResource child : children) {
					this.indent ++;
					this.visit(child);
					this.indent --;
				}
			}	
			protected String getStrIndent() {
				StringBuffer res = new StringBuffer();
				for (int i = 0; i < this.indent; i ++) {
					res.append("\t"); //$NON-NLS-1$
				} 				
				return res.toString();
			}
		});
	}

	/*
	 * Current implementation is applied only for files
	 */
	public Object getAdapter(Class adapter) {
		if (this.isFile()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile file = root.getFileForLocation(new Path(this.fullFileSystemPath));			
			if (adapter == IResource.class) {
				return file;
			} else if (adapter == IRepositoryResource.class && file != null) {
				return SVNRemoteStorage.instance().asRepositoryResource(SVNRemoteStorage.instance().getRepositoryLocation(file), this.url, true);
			} 
		}		
		return null;
	}
}
