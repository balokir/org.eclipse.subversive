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

package org.eclipse.team.svn.ui.debugmail;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.core.SVNTeamPlugin;

/**
 * Plugin ID-based status visitor
 * 
 * @author Alexander Gurov
 */
public class PluginIDVisitor implements Reporter.IStatusVisitor {
	public boolean visit(IStatus status) {
		return status.getPlugin().equals(SVNTeamPlugin.NATURE_ID);
	}
}
