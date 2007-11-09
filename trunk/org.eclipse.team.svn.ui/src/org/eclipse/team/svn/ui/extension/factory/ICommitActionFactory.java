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

package org.eclipse.team.svn.ui.extension.factory;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Visual and operation components factory. Used to extend Subversive without direct dependencies.
 * 
 * @author Andrej Zachar
 */
public interface ICommitActionFactory {

	/**
	 * The method provide abilities in extending of the standard Subversive Commit Dialog to more powerful
	 * @param shell Shell instance which will be used to interact with user
	 * @param allFilesToCommit full set of files which will be committed
	 * @param panel the default Subversive Commit Panel implementation 
	 * @return enahanced Commit Dialog
	 */
	public ICommitDialog getCommitDialog(Shell shell, Collection allFilesToCommit, ICommentDialogPanel panel);

	/**
	 * The method allows customizing of the Commit Operation
	 * @param operation prepared Commit operation
	 * @param revisionProvider committed revision provider
	 * @param dependsOn dependencies which can prevent commit operation execution in case of failure
	 * @param part workbench part which will be used to interact with user
	 */
	public void performAfterCommitTasks(CompositeOperation operation, IRevisionProvider revisionProvider, 
		IActionOperation[] dependsOn, IWorkbenchPart part);
	
}
