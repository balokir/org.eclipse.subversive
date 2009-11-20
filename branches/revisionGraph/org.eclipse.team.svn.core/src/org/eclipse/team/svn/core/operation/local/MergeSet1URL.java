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
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * 1URL-based merge set
 * 
 * @author Alexander Gurov
 */
public class MergeSet1URL extends AbstractMergeSetURL {
    public final IRepositoryResource []from;
    public final SVNRevisionRange []revisions;

	public MergeSet1URL(IResource[] to, IRepositoryResource []from, SVNRevisionRange []revisions, boolean ignoreAncestry, int depth) {
		super(to, ignoreAncestry, depth);
		this.from = from;
		this.revisions = revisions;
	}

}
