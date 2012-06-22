/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrej Zachar - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;


/**
 * Generic dialog for standard and commit office implementation.
 * This interface is not intended to be used in classes in team svn, except DefaultDialog.
 * @see org.eclipse.team.svn.ui.dialog.DefaultDialog
 * 
 * @todo Later this interface should be used as a child interface of {@link org.eclipse.team.svn.ui.panel.IDialogPanel}
 * 
 * @author Andrej Zachar
 */
public interface ICommitDialog {

	/**
	 * @return message Message for commit
	 */
	public String getMessage();
	
	
	/**
	 * return code from dialog.open  
	 * @return code
	 */
	public int open();
	
}
