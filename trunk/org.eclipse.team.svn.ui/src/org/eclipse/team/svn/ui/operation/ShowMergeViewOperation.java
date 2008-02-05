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
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.local.MergeSet;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.synchronize.merge.MergeParticipant;
import org.eclipse.team.svn.ui.synchronize.merge.MergeScope;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Merge operation implementation
 * 
 * @author Alexander Gurov
 */
public class ShowMergeViewOperation extends AbstractActionOperation {
    protected MergeSet mergeSet;
    protected IWorkbenchPart part;

    public ShowMergeViewOperation(IResource []locals, IRepositoryResource []fromStart, IRepositoryResource []fromEnd, boolean ignoreAncestry, IWorkbenchPart part) {
        super("Operation.ShowMergeView");
        this.mergeSet = new MergeSet(locals, fromStart, fromEnd, ignoreAncestry);
        this.part = part;
    }

    protected void runImpl(IProgressMonitor monitor) throws Exception {
    	//SubscriberParticipant.getMatchingParticipant silently changes resources order. So, make a copy...
    	IResource []copy = new IResource[this.mergeSet.to.length];
    	System.arraycopy(this.mergeSet.to, 0, copy, 0, this.mergeSet.to.length);
		MergeParticipant participant = (MergeParticipant)SubscriberParticipant.getMatchingParticipant(MergeParticipant.PARTICIPANT_ID, copy);
		if (participant == null) {
			participant = new MergeParticipant(new MergeScope(this.mergeSet));
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
		}
		else {
		    ((MergeScope)participant.getScope()).setMergeSet(this.mergeSet);
		}

		participant.run(this.part);
    }
    
}
