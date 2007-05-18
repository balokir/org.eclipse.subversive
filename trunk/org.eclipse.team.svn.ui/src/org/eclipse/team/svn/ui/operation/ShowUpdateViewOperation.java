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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.synchronize.update.UpdateParticipant;

/**
 * Synchronize workspace resources operation
 * 
 * @author Alexander Gurov
 */
public class ShowUpdateViewOperation extends AbstractWorkingCopyOperation {
	protected IWorkbenchPart part;

	public ShowUpdateViewOperation(IResource []resources, IWorkbenchPart part) {
		super("Operation.ShowUpdateView", resources);
		this.part = part;
	}

	public ShowUpdateViewOperation(IResourceProvider provider, IWorkbenchPart part) {
		super("Operation.ShowUpdateView", provider);
		this.part = part;
	}

	public ISchedulingRule getSchedulingRule() {
		return AbstractNonLockingOperation.NON_LOCKING_RULE;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		UpdateParticipant participant = 
			(UpdateParticipant)SubscriberParticipant.getMatchingParticipant(UpdateParticipant.PARTICIPANT_ID, resources);
		if (participant == null) {
			participant = new UpdateParticipant(new ResourceScope(resources));
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
		}
		participant.run(this.part);
	}

}
