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

/**
 * Any repository root (branches, tags, repository location)
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryRoot extends IRepositoryContainer {
	
	public static final int KIND_ROOT = 4;
	
	public static final int KIND_LOCATION_ROOT = 0;
	
	public static final int KIND_TRUNK = 1;
	
	public static final int KIND_BRANCHES = 2;
	
	public static final int KIND_TAGS = 3;
	
	public int getKind();
	
}
