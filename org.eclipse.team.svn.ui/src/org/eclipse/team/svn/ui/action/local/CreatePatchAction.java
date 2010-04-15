/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.FileToClipboardOperation;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;

/**
 * Create patch file action
 * 
 * @author Alexander Gurov
 */
public class CreatePatchAction extends AbstractWorkingCopyAction {
	public CreatePatchAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []targets = FileUtility.filterResources(this.getSelectedResources(), IStateFilter.SF_ANY_CHANGE);
		IActionOperation op = CreatePatchAction.getCreatePatchOperation(targets, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}
	
	public static IActionOperation getCreatePatchOperation(IResource []targets, Shell shell) {
		if (targets.length > 0) {
			CreatePatchWizard wizard = new CreatePatchWizard(targets[0].getName(), targets);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == 0) {
				CreatePatchOperation mainOp = new CreatePatchOperation(wizard.getSelection(), wizard.getFileName(), wizard.isRecursive(), wizard.isIgnoreDeleted(), wizard.isProcessBinary(), wizard.isProcessUnversioned(), wizard.getRootPoint());
				CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
				op.add(mainOp);
				switch (wizard.getWriteMode()) {
					case CreatePatchWizard.WRITE_TO_WORKSPACE_FILE: {
						op.add(new RefreshResourcesOperation(new IResource[] {wizard.getTargetFolder()}, IResource.DEPTH_ONE, RefreshResourcesOperation.REFRESH_CHANGES), new IActionOperation[] {mainOp});
						break;
					}
					case CreatePatchWizard.WRITE_TO_CLIPBOARD: {
						op.add(new FileToClipboardOperation(wizard.getFileName()), new IActionOperation[] {mainOp});
						break;
					}
				}
				return op;
			}
		}
		return null;
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresenceRecursive(IStateFilter.SF_ANY_CHANGE);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
