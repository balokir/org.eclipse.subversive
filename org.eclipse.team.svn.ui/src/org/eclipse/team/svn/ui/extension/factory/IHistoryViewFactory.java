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


/**
 * Add additional function to a commit message history view Such as click on a link.
 * 
 * @author Andrej Zachar
 */
public interface IHistoryViewFactory {
	/**
	 * Returns project-specific multi-line comment view implementation 
	 * @return project-specific multi-line comment view implementation
	 */
	public ICommentView getCommentView();
}
