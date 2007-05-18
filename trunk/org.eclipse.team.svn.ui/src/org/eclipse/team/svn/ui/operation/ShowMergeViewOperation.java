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
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.synchronize.merge.MergeParticipant;
import org.eclipse.team.svn.ui.synchronize.merge.MergeScope;

/**
 * Merge operation implementation
 * 
 * @author Alexander Gurov
 */
public class ShowMergeViewOperation extends AbstractNonLockingOperation {
    protected IResource []locals;
    protected IRepositoryResource []remoteResources;
    protected IWorkbenchPart part;
    protected Revision startRevision;

    public ShowMergeViewOperation(IResource []locals, IRepositoryResource []remoteResources, IWorkbenchPart part, Revision startRevision) {
        super("Operation.ShowMergeView");
        this.locals = locals;
        this.remoteResources = remoteResources;
        this.part = part;
        this.startRevision = startRevision;
    }

    protected void runImpl(IProgressMonitor monitor) throws Exception {
		MergeParticipant participant = (MergeParticipant)SubscriberParticipant.getMatchingParticipant(MergeParticipant.PARTICIPANT_ID, this.locals);
		if (participant == null) {
			participant = new MergeParticipant(new MergeScope(this.locals, this.remoteResources, this.startRevision));
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
		}
		else {
		    ((MergeScope)participant.getScope()).setInfo(this.locals, this.remoteResources, this.startRevision);
		}

		participant.run(this.part);
    }
    
}
