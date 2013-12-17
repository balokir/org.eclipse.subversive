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

package org.eclipse.team.svn.ui.startup;

import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.ui.IStartup;

/**
 * Initiates plug-in startup if it is loaded in the IDE scope (details in bug #336689).
 * 
 * @author Alexander Gurov
 */
public class SVNCoreStartup implements IStartup {

	public void earlyStartup() {
		// touch the core plug-in to trigger its load
		SVNTeamPlugin.instance();
	}

}
