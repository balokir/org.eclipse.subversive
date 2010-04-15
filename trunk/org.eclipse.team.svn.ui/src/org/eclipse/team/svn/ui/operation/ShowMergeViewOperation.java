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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.local.AbstractMergeSet;
import org.eclipse.team.svn.core.operation.local.MergeSet1URL;
import org.eclipse.team.svn.core.operation.local.MergeSet2URL;
import org.eclipse.team.svn.core.operation.local.MergeSetReintegrate;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.synchronize.merge.MergeParticipant;
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
    protected IWorkbenchPart part;
    protected IResource []locals;
    protected IRepositoryResourceProvider from;
    protected IRepositoryResourceProvider fromEnd;
    protected SVNRevisionRange []revisions;
    protected boolean ignoreAncestry;
    protected int depth;

    public ShowMergeViewOperation(IResource []locals, IRepositoryResource []from, SVNRevisionRange []revisions, boolean ignoreAncestry, int depth, IWorkbenchPart part) {
        this(locals, new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(from), revisions, ignoreAncestry, depth, part);
    }

    public ShowMergeViewOperation(IResource []locals, IRepositoryResource []fromStart, IRepositoryResource []fromEnd, boolean ignoreAncestry, int depth, IWorkbenchPart part) {
        this(locals, new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(fromStart), new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(fromEnd), ignoreAncestry, depth, part);
    }

    public ShowMergeViewOperation(IResource []locals, IRepositoryResource []from, IWorkbenchPart part) {
        this(locals, new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(from), part);
    }

    public ShowMergeViewOperation(IResource []locals, IRepositoryResourceProvider from, SVNRevisionRange []revisions, boolean ignoreAncestry, int depth, IWorkbenchPart part) {
        super("Operation_ShowMergeView", SVNUIMessages.class); //$NON-NLS-1$
        this.part = part;
        this.locals = locals;
        this.from = from;
        this.revisions = revisions;
        this.ignoreAncestry = ignoreAncestry;
        this.depth = depth;
    }
    
    public ShowMergeViewOperation(IResource []locals, IRepositoryResourceProvider fromStart, IRepositoryResourceProvider fromEnd, boolean ignoreAncestry, int depth, IWorkbenchPart part) {
        super("Operation_ShowMergeView", SVNUIMessages.class); //$NON-NLS-1$
        this.part = part;
        this.locals = locals;
        this.from = fromStart;
        this.fromEnd = fromEnd;
        this.ignoreAncestry = ignoreAncestry;
        this.depth = depth;
    }
    
    public ShowMergeViewOperation(IResource []locals, IRepositoryResourceProvider from, IWorkbenchPart part) {
        super("Operation_ShowMergeView", SVNUIMessages.class); //$NON-NLS-1$
        this.part = part;
        this.locals = locals;
        this.from = from;
    }
    
    public int getOperationWeight() {
		return 0;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
    	AbstractMergeSet mergeSet = null;
    	if (this.fromEnd != null) {
        	mergeSet = new MergeSet2URL(this.locals, this.from.getRepositoryResources(), this.fromEnd.getRepositoryResources(), this.ignoreAncestry, this.depth);
    	}
    	else if (this.revisions != null) {
        	mergeSet = new MergeSet1URL(this.locals, this.from.getRepositoryResources(), this.revisions, this.ignoreAncestry, this.depth);
    	}
    	else {
        	mergeSet = new MergeSetReintegrate(this.locals, this.from.getRepositoryResources());
    	}
        
    	//SubscriberParticipant.getMatchingParticipant silently changes resources order. So, make a copy...
    	IResource []copy = new IResource[mergeSet.to.length];
    	System.arraycopy(mergeSet.to, 0, copy, 0, mergeSet.to.length);
		MergeParticipant participant = (MergeParticipant)SubscriberParticipant.getMatchingParticipant(MergeParticipant.PARTICIPANT_ID, copy);
		if (participant == null) {
			participant = new MergeParticipant(new MergeScope(mergeSet));
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
		}
		else {
		    ((MergeScope)participant.getScope()).setMergeSet(mergeSet);
		}

		participant.run(this.part);
    }
    
}
