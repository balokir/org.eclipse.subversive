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

import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.panel.IDialogPanel;

/**
 * Interface for communication with comment module in commit office.
 * 
 * @author Andrej Zachar
 */
public interface ICommentDialogPanel extends IDialogPanel {
	
	/**
	 * Returns the comment message from the editor. The call returns valid data outside of the UI thread context too.
	 * @return 
	 */
	public String getMessage();
	
	/**
	 * Adds resources selection change listener
	 */
	public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener);
	
	/**
	 * Removes resources selection change listener
	 */
	public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener);

}
