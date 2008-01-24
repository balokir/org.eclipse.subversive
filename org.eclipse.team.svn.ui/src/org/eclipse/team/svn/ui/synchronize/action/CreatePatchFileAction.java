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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Create patch file action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class CreatePatchFileAction extends AbstractSynchronizeModelAction {
	public CreatePatchFileAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(element.getResource());
			// null for change set nodes
			return local != null && IStateFilter.SF_ONREPOSITORY.accept(local);
		}
	    return false;
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IActionOperation [] op = new IActionOperation[1];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
			    IResource resource = operation.getSelectedResource();
			    FileDialog dlg = new FileDialog(UIMonitorUtility.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SavePatchAs"));
				dlg.setFileName(resource.getName() + ".patch");
				dlg.setFilterExtensions(new String[] {"patch", "*.*"});
				String file = dlg.open();
				if (file != null) {
					op[0] = new CreatePatchOperation(resource, file, true, true, true, true, true);
				}
			}
		});
		return op[0];
	}

}
