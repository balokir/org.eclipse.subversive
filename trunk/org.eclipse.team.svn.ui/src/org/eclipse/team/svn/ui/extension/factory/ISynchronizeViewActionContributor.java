/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jiri Walek - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;

import java.util.Collection;

import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;

/**
 * Synchronize action contributor
 * 
 * @author Jiri Walek
 */
public interface ISynchronizeViewActionContributor {
    /**
     * This method returns synchronize view action contributions for update mode
     * @return collection of AbstractSynchronizeActionGroup
     */
	public Collection<AbstractSynchronizeActionGroup> getUpdateContributions();
    /**
     * This method returns synchronize view action contributions for merge mode
     * @return collection of AbstractSynchronizeActionGroup
     */
	public Collection<AbstractSynchronizeActionGroup> getMergeContributions();
}
