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

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set keywords action implementation for Synchronize view
 * 
 * @author Alexei Goncharov
 */
public class SetKeywordsAction extends AbstractSynchronizeModelAction {

	public SetKeywordsAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public SetKeywordsAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		Object [] selectionArr = selection.toArray();
		if (selection.size() > 0) {
			for (int i = 0; i < selection.size(); i++) {
				if (!(selectionArr[i] instanceof SyncInfoModelElement)) {
					return false;
				}
				SyncInfoModelElement element = (SyncInfoModelElement)selectionArr[i];
				IResource[] resource = {element.getResource()};
				if (FileUtility.checkForResourcesPresence(resource, IStateFilter.SF_UNVERSIONED, IResource.DEPTH_ZERO)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				org.eclipse.team.svn.ui.action.local.SetKeywordsAction.doSetKeywords(operation.getSelectedResources());
			}
		});
		return null;
	}

}
