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

import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.extension.factory.IHistoryViewFactory;


/**
 * Default team history components factory
 * 
 * @author Andrej Zachar
 */
public class DefaultHistoryViewFactory implements IHistoryViewFactory {
	
	public ICommentView getCommentView() {
		return new DefaultCommentView();
	}

}
