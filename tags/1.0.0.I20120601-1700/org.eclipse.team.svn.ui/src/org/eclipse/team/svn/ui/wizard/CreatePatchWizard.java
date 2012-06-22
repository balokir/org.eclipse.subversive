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

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.wizard.createpatch.PatchOptionsPage;
import org.eclipse.team.svn.ui.wizard.createpatch.SelectPatchFilePage;

/**
 * Create patch file wizard
 * 
 * @author Alexander Gurov
 */
public class CreatePatchWizard extends AbstractSVNWizard {
	public static final int WRITE_TO_CLIPBOARD = 0;
	public static final int WRITE_TO_EXTERNAL_FILE = 1;
	public static final int WRITE_TO_WORKSPACE_FILE = 2;
	
	protected String targetName;
	protected boolean showIgnoreAncestry;
	
	protected SelectPatchFilePage selectFile;
	protected PatchOptionsPage options;
	protected IResource []roots;

	public CreatePatchWizard(String targetName) {
		this(targetName, null);
	}
	
	public CreatePatchWizard(String targetName, IResource []roots) {
		this(targetName, roots, false);
	}
	
	public CreatePatchWizard(String targetName, IResource []roots, boolean showIgnoreAncestry) {
		super();
		this.setWindowTitle(SVNUIMessages.CreatePatchWizard_Title);
		this.targetName = targetName;
		this.roots = roots;
		this.showIgnoreAncestry = showIgnoreAncestry;
	}
	
	public int getRootPoint() {
		return this.options.getRootPoint();
	}
	
	public IResource getTargetFolder() {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(this.getFileName())).getParent();
	}
	
	public String getFileName() {
		return this.selectFile.getFileName();
	}
	
	public int getWriteMode() {
		return this.selectFile.getWriteMode();
	}

	public boolean isIgnoreDeleted() {
		return this.options.isIgnoreDeleted();
	}

	public boolean isProcessBinary() {
		return this.options.isProcessBinary();
	}

	public boolean isProcessUnversioned() {
		return this.options.isProcessUnversioned();
	}

	public boolean isRecursive() {
		return this.options.isRecursive() & this.selectFile.isRecursive();
	}
	
	public IResource []getSelection() {
		return this.selectFile.isRecursive() ? this.roots : this.selectFile.getSelection();
	}
	
	public boolean isIgnoreAncestry() {
		return this.options.isIgnoreAncestry();
	}

	public void addPages() {
		this.addPage(this.selectFile = new SelectPatchFilePage(this.targetName, this.roots));
		this.addPage(this.options = new PatchOptionsPage(this.roots != null, this.showIgnoreAncestry));
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		if (this.roots != null) {
			this.options.setMultiSelect(SVNUtility.splitWorkingCopies(this.getSelection()).size() > 1);
		}
		return super.getNextPage(page);
	}
	
	public boolean performFinish() {
		return true;
	}
	
}
