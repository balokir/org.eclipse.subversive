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

/**
 * Abstract URL-based merge set
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractMergeSetURL extends AbstractMergeSet {
    public final boolean ignoreAncestry;
    public final int depth;

	public AbstractMergeSetURL(IResource[] to, boolean ignoreAncestry, int depth) {
		super(to);
		
    	this.ignoreAncestry = ignoreAncestry;
    	this.depth = depth;
	}

}
