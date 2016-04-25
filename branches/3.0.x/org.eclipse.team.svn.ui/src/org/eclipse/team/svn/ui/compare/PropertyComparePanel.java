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

package org.eclipse.team.svn.ui.compare;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Panel for properties comparison in a dialog
 * 
 * @author Alexei Goncharov
 */
public class PropertyComparePanel extends AbstractDialogPanel {

	protected PropertyCompareInput input;
	
	public PropertyComparePanel(PropertyCompareInput input, boolean local) {
		super(new String[] {IDialogConstants.CLOSE_LABEL});
		this.input = input;
		this.dialogDescription = SVNUIMessages.ComparePropsPanel_Description;
		if (local) {
			this.defaultMessage = SVNUIMessages.ComparePropsPanel_Local_Message;
		}
		else {
			this.defaultMessage = SVNUIMessages.ComparePropsPanel_Remote_Message;
		}
	}

	protected void createControlsImpl(Composite parent) {
		Control control = this.input.createContents(parent);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		Shell shell= control.getShell();
		shell.setText(this.input.getTitle());
		shell.setImage(this.input.getTitleImage());
	}
	
	public String getHelpId() {
		return "org.eclipse.team.svn.help.comparePropsDialogContext"; //$NON-NLS-1$
	}
	
	public Point getPrefferedSizeImpl() {
        return new Point(650, 500);
    }

	protected void saveChangesImpl() {
	}
	
	protected void cancelChangesImpl() {
	}

}
