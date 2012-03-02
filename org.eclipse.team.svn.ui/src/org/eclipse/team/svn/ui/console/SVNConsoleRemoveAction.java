/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Action that removes the SVN console from the console view. The console
 * can be re-added via the console view "Open Console" drop-down.
 * 
 * @author Igor Burilo
 */
public class SVNConsoleRemoveAction extends Action {

	public SVNConsoleRemoveAction() {
		super(SVNUIMessages.SVNConsoleRemoveAction_CloseConsole);
		this.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/remove.gif")); //$NON-NLS-1$
	}
	
	public void run() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		SVNConsole console = SVNConsoleFactory.getConsole();
		if (console != null) {
			manager.removeConsoles(new IConsole[] {console});
			ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(console.new SVNConsoleListener());
		}
	}
	
}
