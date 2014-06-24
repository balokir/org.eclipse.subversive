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

package org.eclipse.team.svn.ui.panel.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Select resource revision panel implementation
 * 
 * @author Alexander Gurov
 */
public class SelectRevisionPanel extends SVNHistoryPanel {
	
	protected SVNLogEntry[] selectedLogMessages;	
		 
	public SelectRevisionPanel(GetLogMessagesOperation msgOp, boolean multiSelect, boolean useCheckboxes, long currentRevision) {
		super(SVNUIMessages.SelectRevisionPanel_Title, SVNUIMessages.SelectRevisionPanel_Description, SVNUIMessages.SelectRevisionPanel_Message, msgOp, multiSelect, useCheckboxes, currentRevision);		
	}
	
	public long getSelectedRevision() {
		return this.selectedLogMessages[0].revision;
	}

	public SVNLogEntry[] getSelectedLogMessages() {
	    return this.selectedLogMessages;
	}

	public SVNRevisionRange[] getSelectedRevisions() {
		ArrayList<SVNRevisionRange> revisions = new ArrayList<SVNRevisionRange>();
	
		List<SVNLogEntry> selected = Arrays.asList(this.selectedLogMessages);
		long startRev = SVNRevision.INVALID_REVISION_NUMBER;
		long lastRev = SVNRevision.INVALID_REVISION_NUMBER;
		for (int i = 0; i < this.logMessages.length ; i ++ ) {
			SVNLogEntry entry = this.logMessages[i];
			if (selected.indexOf(entry) != -1) {
				startRev = entry.revision;
				if (lastRev == SVNRevision.INVALID_REVISION_NUMBER) {
					lastRev = entry.revision;
				}				
				//add the last element to result list
				if (i == this.logMessages.length - 1) {
					revisions.add(new SVNRevisionRange(startRev == lastRev || startRev == 0 ? startRev : startRev - 1, lastRev));	
				}
			}
			else {
				if (lastRev != SVNRevision.INVALID_REVISION_NUMBER) {
					revisions.add(new SVNRevisionRange(startRev == lastRev || startRev == 0 ? startRev : startRev - 1, lastRev));
					lastRev = SVNRevision.INVALID_REVISION_NUMBER;
				}
			}
		}
		
	    return revisions.toArray(new SVNRevisionRange[revisions.size()]);
	}
	
	public void postInit() {
		this.manager.setButtonEnabled(0, false);
	}
	
	protected void initTableViewerListener() {
		this.tableViewerListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (SelectRevisionPanel.this.manager != null) {
					SVNLogEntry []messages = SelectRevisionPanel.this.history.getSelectedLogMessages();
					SelectRevisionPanel.this.manager.setButtonEnabled(0, messages != null && messages.length > 0);
				}
			}
		};		
		this.history.getTreeViewer().addSelectionChangedListener(this.tableViewerListener);
	}
	
    protected void saveChangesImpl() {
		this.selectedLogMessages = this.history.getSelectedLogMessages();
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.revisionLinkDialogContext"; //$NON-NLS-1$
	}
}
