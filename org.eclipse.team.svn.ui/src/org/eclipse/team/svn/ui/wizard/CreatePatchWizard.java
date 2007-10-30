/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
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
	protected boolean localMode;
	protected boolean showIgnoreAncestry;
	
	protected SelectPatchFilePage selectFile;
	protected PatchOptionsPage options;

	public CreatePatchWizard(String targetName) {
		this(targetName, true);
	}
	
	public CreatePatchWizard(String targetName, boolean localMode) {
		this(targetName, localMode, false);
	}
	
	public CreatePatchWizard(String targetName, boolean localMode, boolean showIgnoreAncestry) {
		super();
		this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("CreatePatchWizard.Title"));
		this.targetName = targetName;
		this.localMode = localMode;
		this.showIgnoreAncestry = showIgnoreAncestry;
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
		return this.options.isRecursive();
	}
	
	public boolean isIgnoreAncestry() {
		return this.options.isIgnoreAncestry();
	}

	public void addPages() {
		this.addPage(this.selectFile = new SelectPatchFilePage(this.targetName));
		this.addPage(this.options = new PatchOptionsPage(this.localMode, this.showIgnoreAncestry));
	}
	
	public boolean performFinish() {
		return true;
	}
	
}
