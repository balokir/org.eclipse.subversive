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

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.NotifyStatus;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Merge operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeOperation extends AbstractConflictDetectionOperation implements IResourceProvider {
	protected MergeStatusOperation statusOp;
	protected boolean force;

	//FIXME works incorrectly for multi-project merge
	public MergeOperation(IResource []localTo, MergeStatusOperation statusOp, boolean force) {
		super("Operation.Merge", localTo);
		this.statusOp = statusOp;
		this.force = force;
	}

	public MergeOperation(IResourceProvider provider, MergeStatusOperation statusOp, boolean force) {
		super("Operation.Merge", provider);
		this.statusOp = statusOp;
		this.force = force;
	}

	public IResource []getResources() {
	    return this.getProcessed();
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final IResource []localTo = this.operableData();
		
		this.defineInitialResourceSet(localTo);
		
		// merge parent nodes first
		FileUtility.reorder(localTo, true);
		
		final IRepositoryResource from = this.statusOp.getFrom()[0];
		IRepositoryLocation location = from.getRepositoryLocation();
		final String wcTo = FileUtility.getWorkingCopyPath(MergeOperation.this.statusOp.getLocalTo()[0]);
		final ISVNClientWrapper proxy = location.acquireSVNProxy();
		
		for (int i = 0; i < localTo.length && !monitor.isCanceled(); i++) {
			final int idx = i;
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.merge(
						from.getUrl(), from.getPegRevision(),  												// branch URL, peg, start, stop
				    	MergeOperation.this.statusOp.getFromStartRevision() == null ? MergeOperation.this.statusOp.getCopiedFromRevision()[0] : MergeOperation.this.statusOp.getFromStartRevision(), from.getSelectedRevision(), 
				    	wcTo, MergeOperation.this.getStatusesFor(localTo[idx]),	MergeOperation.this.force,	// head
						new ConflictDetectionProgressMonitor(MergeOperation.this, monitor, null));
				}
			}, monitor, localTo.length);
		}
		
		location.releaseSVNProxy(proxy);
	}
	
	protected Status []getStatusesFor(IResource resource) {
		ArrayList retVal = new ArrayList();
		Status []statuses = this.statusOp.getStatuses();
		IPath target = resource.getLocation();
		for (int i = 0; i < statuses.length; i++) {
			if (target.equals(new Path(statuses[i].path))) {
				retVal.add(statuses[i]);
				break; // now we use non-recursive merge
			}
		}
		return (Status [])retVal.toArray(new Status[retVal.size()]);
	}

	protected class ConflictDetectionProgressMonitor extends SVNProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
		    if (state.contentState == NotifyStatus.conflicted_unresolved || 
		    	state.contentState == NotifyStatus.conflicted ||
		        state.propState == NotifyStatus.conflicted_unresolved ||
		        state.propState == NotifyStatus.conflicted) {
		        MergeOperation.this.hasUnresolvedConflict = true;
			    for (Iterator it = MergeOperation.this.processed.iterator(); it.hasNext(); ) {
			        IResource res = (IResource)it.next();
			        if (res.getLocation().equals(new Path(state.path))) {
			            it.remove();
			            MergeOperation.this.unprocessed.add(res);
			            break;
			        }
			    }
		    }
		}
		
	}

}
