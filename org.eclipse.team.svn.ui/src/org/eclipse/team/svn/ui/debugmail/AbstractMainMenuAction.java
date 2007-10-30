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

package org.eclipse.team.svn.ui.debugmail;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Abstract action implementation that can be inserted into Eclipse IDE main menu. 
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractMainMenuAction implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow window;

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void dispose() {
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return this.window;
	}
	
	protected Shell getShell() {
		return this.window.getShell();
	}
	
}
