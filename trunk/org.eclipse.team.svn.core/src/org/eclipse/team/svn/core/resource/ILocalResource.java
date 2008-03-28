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

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.resources.IResource;

/**
 * Checked out resource representation
 * 
 * @author Alexander Gurov
 */
public interface ILocalResource {	
	public static final int NO_MODIFICATION = 0x00; 
	public static final int TEXT_MODIFIED = 0x01; 
	public static final int PROP_MODIFIED = 0x02;
	public static final int IS_COPIED = 0x04;
	public static final int IS_SWITCHED = 0x08;
	public static final int IS_LOCKED = 0x10;
	public static final int IS_EXTERNAL = 0x20;
	
	public IResource getResource();
	
	public String getName();
	
	public long getRevision();
	
	public long getBaseRevision();
	
	public String getStatus();
	
	public int getChangeMask();
	
	public boolean isCopied();
	
	public String getAuthor();
	
	public long getLastCommitDate();
	
	public boolean isLocked();
}
