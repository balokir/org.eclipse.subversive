/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Compare input for comparison remote resources' SVN properties.
 * 
 * @author Alexei Goncharov
 */
public class TwoWayPropertyCompareInput extends PropertyCompareInput {

	public TwoWayPropertyCompareInput(CompareConfiguration configuration,
									  SVNEntryRevisionReference left,
									  SVNEntryRevisionReference right,
									  IRepositoryLocation location) {
		super(configuration, left, right, null, location);
	}

	protected void fillMenu(IMenuManager manager, TreeSelection selection) {
		// is menu needed???
	}
	
	public String getTitle() {
		return super.getTitle();
	}

}
