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

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.participant.BasePaneParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.ui.synchronize.ResourceScope;

/**
 * Override and commit/update panel implementation
 * 
 * @author Alexander Gurov
 */
public class OverrideResourcesPanel extends AbstractResourceSelectionPanel {
	public static final int MSG_COMMIT = 0;
	public static final int MSG_UPDATE = 1;
	
	protected static final String []MESSAGES = new String[] {
		"OverrideResourcesPanel_Description_Commit",
		"OverrideResourcesPanel_Description_Update"
	};
	
    public OverrideResourcesPanel(IResource []resources, IResource[] userSelectedResources, int msgId) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
        this.dialogTitle = SVNUIMessages.OverrideResourcesPanel_Title;
        this.dialogDescription = SVNUIMessages.getString(OverrideResourcesPanel.MESSAGES[msgId]);
        boolean isParticipantPane = this.paneParticipantHelper.isParticipantPane();
        this.defaultMessage = isParticipantPane ? SVNUIMessages.OverrideResourcesPanel_Pane_Message : SVNUIMessages.OverrideResourcesPanel_Message;
    }
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.overrideDialogContext";
    }
	
	protected BasePaneParticipant createPaneParticipant() {
		return new BasePaneParticipant(new ResourceScope(this.resources), this) {
			protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
				Collection<AbstractSynchronizeActionGroup> actionGroups = new ArrayList<AbstractSynchronizeActionGroup>();
				actionGroups.add(new BasePaneActionGroup(this.validationManager));
		    	return actionGroups;
			}
		};
	}
}
