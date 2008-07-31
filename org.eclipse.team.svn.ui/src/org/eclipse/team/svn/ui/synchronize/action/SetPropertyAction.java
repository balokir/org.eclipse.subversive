/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import java.util.Iterator;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.properties.ResourcePropertyEditPanel;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set property action implementation for Synchronize view
 * 
 * @author Alexei Goncharov
 */
public class SetPropertyAction extends AbstractSynchronizeModelAction {
	public SetPropertyAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		for (Iterator<?> it = selection.iterator(); it.hasNext(); ) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)it.next();
			if (IStateFilter.SF_VERSIONED.accept(SVNRemoteStorage.instance().asLocalResource(element.getResource()))) {
				return true;
			}
		}
	    return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource [] resources = SetPropertyAction.this.treeNodeSelector.getSelectedResourcesRecursive(IStateFilter.SF_VERSIONED);
		ResourcePropertyEditPanel panel = new ResourcePropertyEditPanel(null, resources, true);
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		if (dialog.open() == Dialog.OK) {
			org.eclipse.team.svn.ui.action.local.SetPropertyAction.doSetProperty(resources, panel, null);
		}
		return null;
	}

}