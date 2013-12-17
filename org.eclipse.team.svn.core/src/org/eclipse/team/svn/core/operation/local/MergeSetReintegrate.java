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
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Reintegrate-based merge set
 * 
 * @author Alexander Gurov
 */
public class MergeSetReintegrate extends AbstractMergeSet {
    public final IRepositoryResource []from;

	public MergeSetReintegrate(IResource[] to, IRepositoryResource []from) {
		super(to);
		this.from = from;
	}

}
