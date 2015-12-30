/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.team.svn.ui.composite.BranchTagSelectionComposite;

/**
 * Compare with tag action implementation
 * 
 * @author Alexander Gurov
 */
public class CompareWithTagAction extends CompareWithBranchTagAction {

	public CompareWithTagAction() {
		super(BranchTagSelectionComposite.TAG_OPERATED);
	}

}
