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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.ui.history.IHistoryPageSource;

/**
 * SVN HistoryPage adapter factory
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryAdapterFactory implements IAdapterFactory {
	private static final Class []ADAPTED_TYPES = new Class[] {IHistoryPageSource.class};
	
	private SVNHistoryPageSource pageSource = new SVNHistoryPageSource();
	
	public Class[] getAdapterList() {
		return SVNHistoryAdapterFactory.ADAPTED_TYPES;
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IHistoryPageSource.class.equals(adapterType)) {
			return this.pageSource;
		}
		return null;
	}

}
