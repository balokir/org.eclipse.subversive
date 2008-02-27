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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Provides information about the view
 * 
 * @author Alexander Gurov
 */
public interface IViewInfoProvider {
	public IWorkbenchPartSite getPartSite();
	public IActionBars getActionBars();
}
