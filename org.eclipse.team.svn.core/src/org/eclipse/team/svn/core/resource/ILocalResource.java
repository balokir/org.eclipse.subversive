/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor;

/**
 * Checked out resource representation
 * 
 * For more details how external definitions are handled see <code>SVNRemoteStorage</code> comments
 *   
 * @author Alexander Gurov
 */
public interface ILocalResource {	
	public static final int NO_MODIFICATION = 0x00;
	
	/*
	 * Can be used ONLY for backward compatibility.
	 * For other cases see 'getTextStatus' and 'getPropStatus' methods
	 */
	public static final int TEXT_MODIFIED = 0x01; 
	public static final int PROP_MODIFIED = 0x02;
	
	public static final int IS_COPIED = 0x04;
	public static final int IS_SWITCHED = 0x08;
	public static final int IS_LOCKED = 0x10;
	public static final int TREE_CONFLICT_UNKNOWN_NODE_KIND = 0x20;
	/**
	 * @deprecated due to mixed semantics it is replaced with IS_FORBIDDEN which is required to prevent SVN actions with resources, 
	 * while unversioned resources, produced by svn:externals, will be marked as ST_IGNORED + IS_SVN_EXTERNALS
	 */
	public static final int IS_UNVERSIONED_EXTERNAL = 0x40;
	public static final int IS_FORBIDDEN = 0x40;
	public static final int IS_SVN_EXTERNALS = 0x80;
	public static final int IS_SYMLINK = 0x100;
	
	public IResource getResource();
	
	public String getName();
	
	public long getRevision();
	
	public long getBaseRevision();
	
	public String getTextStatus();
	
	public String getPropStatus();
	
	/**
	 * Return a compound status from text and property statuses
	 * @return
	 */
	public String getStatus();
	
	public boolean hasTreeConflict();
	
	public SVNConflictDescriptor getTreeConflictDescriptor();
	
	public int getChangeMask();
	
	public boolean isCopied();
	
	public String getAuthor();
	
	public long getLastCommitDate();
	
	public boolean isLocked();
}
