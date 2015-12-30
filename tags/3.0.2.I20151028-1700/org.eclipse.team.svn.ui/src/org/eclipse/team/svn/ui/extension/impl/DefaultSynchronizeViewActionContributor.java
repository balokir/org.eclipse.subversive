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

package org.eclipse.team.svn.ui.extension.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.team.svn.ui.extension.factory.ISynchronizeViewActionContributor;
import org.eclipse.team.svn.ui.extension.impl.synchronize.MergeActionGroup;
import org.eclipse.team.svn.ui.extension.impl.synchronize.OptionsActionGroup;
import org.eclipse.team.svn.ui.extension.impl.synchronize.UpdateActionGroup;

/**
 * Default implementation of the ISynchronizeViewActionContributor
 * 
 * @author Alexander Gurov
 */
public class DefaultSynchronizeViewActionContributor implements ISynchronizeViewActionContributor {

	public DefaultSynchronizeViewActionContributor() {

	}

	public Collection getUpdateContributions() {
		ArrayList actionGroups = new ArrayList();
		actionGroups.add(new UpdateActionGroup());
		actionGroups.add(new OptionsActionGroup());
		return actionGroups;
	}

	public Collection getMergeContributions() {
		ArrayList actionGroups = new ArrayList();
		actionGroups.add(new MergeActionGroup());
		return actionGroups;
	}

	
}
