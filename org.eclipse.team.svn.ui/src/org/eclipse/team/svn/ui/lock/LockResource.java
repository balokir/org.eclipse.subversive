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

/**
 * Model element for LocksView
 * 
 * @author Igor Burilo
 */
public class LockResource {

	protected final boolean isFile;
	protected final boolean isLocalLock;	
	protected String name;
	protected final String owner;
	protected final Date creationDate;
	protected final String comment;
	
	protected LockResource parent;	
	protected Set<LockResource> children; 
	
	public interface ILockResourceVisitor {
		public void visit(LockResource lockResource);
	}
	
	public LockResource(String directoryName) {
		this(directoryName, null, false, false, null, null);
	}
	
	public LockResource(String name, String owner, boolean isFile, boolean isLocalLock, Date creationDate, String comment) {
		this.name = name;
		this.owner = owner;
		this.isFile = isFile;
		this.isLocalLock = isLocalLock;
		this.creationDate = creationDate;
		this.comment = comment;		
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
		
	public boolean isLocalLock() {
		return isLocalLock;
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
					str += ", is local state: " + lockResource.isLocalLock();  //$NON-NLS-1$
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
}
