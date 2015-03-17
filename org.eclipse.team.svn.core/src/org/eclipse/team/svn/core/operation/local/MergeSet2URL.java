/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * 2URLs-based merge set
 * 
 * @author Alexander Gurov
 */
public class MergeSet2URL extends AbstractMergeSetURL {
    public final IRepositoryResource []fromStart;
    public final IRepositoryResource []fromEnd;

	public MergeSet2URL(IResource[] to, IRepositoryResource[] fromStart, IRepositoryResource[] fromEnd, boolean ignoreAncestry, boolean recordOnly, SVNDepth depth) {
		super(to, ignoreAncestry, recordOnly, depth);
    	this.fromStart = fromStart;
    	this.fromEnd = fromEnd;
	}

}
