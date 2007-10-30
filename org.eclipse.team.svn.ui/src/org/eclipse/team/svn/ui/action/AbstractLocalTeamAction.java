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

package org.eclipse.team.svn.ui.action;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;


/**
 * Local SVN team actions ancestor
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractLocalTeamAction extends AbstractSVNTeamAction {
	private static IStructuredSelection selection;
	
	public AbstractLocalTeamAction() {
		super();
	}
	
	protected IStructuredSelection getSelection() {
		if (AbstractLocalTeamAction.selection == null) {
			AbstractLocalTeamAction.selection = StructuredSelection.EMPTY;
		}
		return AbstractLocalTeamAction.selection;
	}
	
	protected void checkSelection(IStructuredSelection selection) {
		HashSet oldSel = new HashSet(Arrays.asList(this.getSelectedResources()));
		IStructuredSelection oldSelection = this.getSelection();
		AbstractLocalTeamAction.selection = selection;
		HashSet newSel = new HashSet(Arrays.asList(this.getSelectedResources()));
		AbstractLocalTeamAction.selection = oldSelection;
		if (!newSel.equals(oldSel)) {
			AbstractLocalTeamAction.selection = (IStructuredSelection)selection;
			FilterManager.instance().clear();
		}
	}
	
	protected boolean checkForResourcesPresence(IStateFilter filter) {
		return FilterManager.instance().checkForResourcesPresence(this.getSelectedResources(), filter, false);
	}
	
	protected boolean checkForResourcesPresenceRecursive(IStateFilter filter) {
		return FilterManager.instance().checkForResourcesPresenceRecursive(this.getSelectedResources(), filter);
	}

}
