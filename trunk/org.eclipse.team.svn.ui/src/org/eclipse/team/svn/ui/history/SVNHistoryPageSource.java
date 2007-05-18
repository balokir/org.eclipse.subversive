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

package org.eclipse.team.svn.ui.history;

import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

/**
 * Instantiates HistoryView's
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryPageSource extends HistoryPageSource {

	public boolean canShowHistoryFor(Object object) {
		return SVNHistoryPage.isValidData(object);
	}

	public Page createPage(Object object) {
		return new SVNHistoryPage(object);
	}

}
