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

package org.eclipse.team.svn.ui.wizard.copymove;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.wizard.AbstractSVNWizard;

/**
 * Wizard for 'Copy To...' and 'Move To...' refactoring actions on repository.
 * 
 * @author Alexei Goncharov
 */
public class CopyMoveWizard extends AbstractSVNWizard {
	protected SelectDestinationPage destinationPage;
	protected CommentWizardPage commentPage;
	
	protected IRepositoryResource[] selectedResources;
	protected boolean isMove;
	
	protected IRepositoryResource destination;
	protected String comment;
	protected String newName;

	public CopyMoveWizard(IRepositoryResource[] selectedResources, boolean isMove) {
		super();
		this.selectedResources = selectedResources;
		this.setWindowTitle((this.isMove = isMove) ? SVNTeamUIPlugin.instance().getResource("MoveToAction.Select.Title") : SVNTeamUIPlugin.instance().getResource(
				"CopyToAction.Select.Title"));
	}

	public void addPages() {
		this.addPage(this.destinationPage = new SelectDestinationPage(this.selectedResources));
		this.addPage(this.commentPage = new CommentWizardPage(this.isMove));
	}

	public boolean canFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		if (currentPage instanceof SelectDestinationPage) {
			return false;
		}
		return super.canFinish();
	}

	public boolean performFinish() {
		this.destination = this.destinationPage.getDestination();
		this.newName = this.destinationPage.getNewResourceName();
		this.comment = this.commentPage.getComment();
		return true;
	}

	public String getComment() {
		return this.comment;
	}

	public String getNewName() {
		return this.newName;
	}

	public IRepositoryResource getDestination() {
		return this.destination;
	}

}
