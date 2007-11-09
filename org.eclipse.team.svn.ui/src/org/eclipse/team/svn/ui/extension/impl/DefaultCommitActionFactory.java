/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrej Zachar - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Default team commit components factory
 * 
 * @author Andrej Zachar
 */
public class DefaultCommitActionFactory implements ICommitActionFactory {

	public ICommitDialog getCommitDialog(final Shell shell, Collection allFilesToCommit, final ICommentDialogPanel commentPanel) {
		return new ICommitDialog(){
			
			public String getMessage() {
				return commentPanel.getMessage();
			}
			
			public int open() {
				DefaultDialog dialog = new DefaultDialog(shell, commentPanel);
				return dialog.open();
			}
			
		};
	}

	//OVERRIDE
	public void performAfterCommitTasks(CompositeOperation operation, IRevisionProvider revisionProvider, IActionOperation[] dependsOn, IWorkbenchPart part) {
			// default implementation do nothing more
	}

	

}
