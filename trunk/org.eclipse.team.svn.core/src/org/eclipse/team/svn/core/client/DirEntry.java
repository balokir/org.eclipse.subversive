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

package org.eclipse.team.svn.core.client;

import java.util.Date;

/**
 * Client dir entry presentation
 * 
 * @author Alexander Gurov
 */
public class DirEntry {
	public final Date lastChanged;
	public final long lastChangedRevision;
	public final boolean hasProps;
	public final String lastAuthor;
	public final int nodeKind;
	public final long size;
	public final String path;
	public final Lock lock;

	public DirEntry(Date lastChanged, long lastChangedRevision, boolean hasProps, String lastAuthor, int nodeKind, long size, String path, Lock lock) {
		this.lastChanged = lastChanged;
		this.lastChangedRevision = lastChangedRevision;
		this.hasProps = hasProps;
		this.lastAuthor = lastAuthor;
		this.nodeKind = nodeKind;
		this.size = size;
		this.path = path;
		this.lock = lock;
	}
	
}
