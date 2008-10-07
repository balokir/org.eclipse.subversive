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

import org.eclipse.team.internal.ui.synchronize.GlobalRefreshResourceSelectionPage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.ShowUpdateViewOperation;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSubscriber;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Synchronize set selection wizard
 * 
 * @author Alexander Gurov
 */
public class SynchronizeWizard extends AbstractSVNWizard {
	
	protected GlobalRefreshResourceSelectionPage selection;

	public SynchronizeWizard() {
		super();
		this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("SynchronizeWizard.Title"));
	}
	
	public void addPages() {
		this.selection = new GlobalRefreshResourceSelectionPage(UpdateSubscriber.instance().roots());
		this.selection.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.addPage(this.selection);
	}

	public boolean performFinish() {
		UIMonitorUtility.doTaskBusyDefault(new ShowUpdateViewOperation(this.selection.getSynchronizeScope(), null));
		return true;
	}

//TODO rework this class as inherited from SubscriberParticipantWizard
//	protected IResource []getRootResources() {
//		return SynchronizeSubscriber.instance().roots();
//	}
//
//	protected SubscriberParticipant createParticipant(ISynchronizeScope scope) {
//		IResource []roots = scope.getRoots();
//		if (roots == null) {
//			roots = this.getRootResources();
//		}
//		
//		SynchronizeParticipant participant = 
//			(SynchronizeParticipant)SubscriberParticipant.getMatchingParticipant(SynchronizeParticipant.PARTICIPANT_ID, roots);
//		
//		return participant == null ? new SynchronizeParticipant(scope) : participant;
//	}
//
//	protected String getName() {
//		ISynchronizeParticipantDescriptor desc = TeamUI.getSynchronizeManager().getParticipantDescriptor(SynchronizeParticipant.PARTICIPANT_ID);
//		return desc == null ? "Unknown" : desc.getName();
//	}
//
//	protected IWizard getImportWizard() {
//		return new CheckoutWizard();
//	}
//
}
