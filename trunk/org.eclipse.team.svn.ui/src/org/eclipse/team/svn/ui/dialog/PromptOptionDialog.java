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

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Prompt any option with toggle
 * 
 * @author Alexander Gurov
 */
public class PromptOptionDialog extends MessageDialogWithToggle {
	protected IOptionManager optionManager;
	
	public interface IOptionManager {
		public String []getButtons();
		public void buttonPressed(IPreferenceStore store, int idx, boolean toggle);
	}
	
	public static abstract class AbstractOptionManager implements IOptionManager {
		public String[] getButtons() {
			return new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL};
		}
	}

	public PromptOptionDialog(Shell parentShell, String title, String message, String toggleMessage, IOptionManager optionManager) {
		super(parentShell, title, null, message, MessageDialog.QUESTION, optionManager.getButtons(), 0, toggleMessage, false);
		this.optionManager = optionManager;
		this.setPrefStore(SVNTeamUIPlugin.instance().getPreferenceStore());
	}
	
	protected void buttonPressed(int buttonId) {
		int idx = -1;
		for (int i = 0; i < this.getButtonLabels().length; i++) {
			int id = ((Integer)((Button)this.getButton(i)).getData()).intValue();
			if (id == buttonId) {
				idx = i;
				break;
			}
		}
		
		super.buttonPressed(buttonId);
		
		this.optionManager.buttonPressed(this.getPrefStore(), idx, this.getToggleState());
	}
	
}
