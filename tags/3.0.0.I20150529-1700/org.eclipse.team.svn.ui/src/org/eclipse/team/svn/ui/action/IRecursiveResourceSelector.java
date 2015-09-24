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

package org.eclipse.team.svn.ui.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.IStateFilter;

/**
 * Conventional interface that allow us to implement generic algorythms with resource selection
 * 
 * @author Alexander Gurov
 */
public interface IRecursiveResourceSelector {
    public IResource []getSelectedResourcesRecursive(IStateFilter filter, int depth);
    public IResource []getSelectedResourcesRecursive(IStateFilter filter);
}
