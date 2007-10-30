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

package org.eclipse.team.svn.ui.synchronize;

import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Abstract synchronize view action contribution implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSynchronizeActionGroup extends SynchronizePageActionGroup {
	protected ISynchronizePageConfiguration configuration;
	
	public AbstractSynchronizeActionGroup() {
		super();
	}

	public final void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(this.configuration = configuration);
		this.configureActions(configuration);
	}
	
    public ISynchronizePageConfiguration getConfiguration() {
        return this.configuration;
    }

    public abstract void configureMenuGroups(ISynchronizePageConfiguration configuration);
	protected abstract void configureActions(ISynchronizePageConfiguration configuration);
}
