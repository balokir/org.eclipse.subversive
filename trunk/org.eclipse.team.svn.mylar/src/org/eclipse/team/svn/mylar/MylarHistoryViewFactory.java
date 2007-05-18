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

package org.eclipse.team.svn.mylar;

import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.extension.factory.IHistoryViewFactory;
import org.eclipse.team.svn.ui.extension.impl.DefaultCommentView;

/**
 * Recognize URL's in the comment body
 * 
 * @author Alexander Gurov
 */
public class MylarHistoryViewFactory implements IHistoryViewFactory {

	public ICommentView getCommentView() {
		return new DefaultCommentView();
	}

}
