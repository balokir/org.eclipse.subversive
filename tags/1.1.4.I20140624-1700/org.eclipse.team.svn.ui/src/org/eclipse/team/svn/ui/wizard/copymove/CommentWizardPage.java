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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * Comment page for Copy To or Move To wizard.
 * 
 * @author Alexei Goncharov
 */
public class CommentWizardPage extends AbstractVerifiedWizardPage {
	protected CommentComposite commentComposite;
	
	public CommentWizardPage(boolean isMove) {
		super(CommentWizardPage.class.getName(),
				isMove ?
				SVNUIMessages.MoveToAction_Comment_Title :
				SVNUIMessages.CopyToAction_Comment_Title,
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.setDescription(SVNUIMessages.CopyMove_Comment_Message);
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);		
		this.commentComposite = new CommentComposite(composite, this);
		this.commentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	public String getComment() {
		this.commentComposite.saveChanges();
		return this.commentComposite.getMessage();
	}

}
