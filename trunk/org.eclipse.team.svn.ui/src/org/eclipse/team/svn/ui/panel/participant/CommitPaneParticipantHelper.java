/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.participant;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * @author Igor Burilo
 */
public class CommitPaneParticipantHelper extends PaneParticipantHelper {

	public CommitPaneParticipantHelper() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.panel.participant.PaneParticipantHelper#getPaneSyncInfoSet()
	 */
	protected SyncInfoSet getPaneSyncInfoSetToProcess() {
		SyncInfoSet resultSet = new SyncInfoSet();
		
		SyncInfoSet syncSet = super.getPaneSyncInfoSetToProcess();
		SyncInfo[] syncInfos = syncSet.getSyncInfos();
		for (SyncInfo syncInfo : syncInfos) {
			int direction = syncInfo.getKind() & SyncInfo.DIRECTION_MASK;
			if (direction == SyncInfo.OUTGOING || direction == SyncInfo.CONFLICTING) {
				resultSet.add(syncInfo);             	
			}    
		}
		return resultSet;
	}
	
	public class CommitPaneVerifier extends PaneVerifier {
			
		protected String getErrorMessage(Control input) {
			String errorMessage = super.getErrorMessage(input);
			if (errorMessage == null) {
				IResource[] resourcesToProcess = CommitPaneParticipantHelper.this.getSelectedResources();
				if (FileUtility.checkForResourcesPresenceRecursive(resourcesToProcess, IStateFilter.SF_CONFLICTING)) {
					return SVNUIMessages.CommitPanel_Pane_Conflicting_Error;
				}
				return null;
			}
			return errorMessage;
		}
	}
}
