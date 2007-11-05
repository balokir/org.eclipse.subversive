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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.IStatusCallback;
import org.eclipse.team.svn.core.client.RevisionRange;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge status operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeStatusOperation extends AbstractWorkingCopyOperation implements IRemoteStatusOperation {
	protected MergeSet info;
	
	public MergeStatusOperation(MergeSet info) {
		super("Operation.MergeStatus", info.to);
		this.info = info;
	}
	
	public IResource []getScope() {
		return this.info.to;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.info.setStatuses(new Status[0]);
		
		final ArrayList st = new ArrayList();
		
		for (int i = 0; i < this.info.to.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource from = this.info.from[i];
			final ISVNClientWrapper proxy = from.getRepositoryLocation().acquireSVNProxy();
			final String wcPath = FileUtility.getWorkingCopyPath(this.info.to[i]);
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.mergeStatus(SVNUtility.getEntryReference(from), new RevisionRange [] {new RevisionRange(MergeStatusOperation.this.info.start, from.getSelectedRevision())}, 
					    	wcPath, 
							Depth.INFINITY, false, new IStatusCallback() {
								public void nextStatus(Status status) {
									st.add(status);
								}
							}, 
							new SVNProgressMonitor(MergeStatusOperation.this, monitor, null));
				}
			}, monitor, this.info.to.length);
			
			from.getRepositoryLocation().releaseSVNProxy(proxy);
		}
		this.info.setStatuses((Status [])st.toArray(new Status[st.size()]));
    }

	public Status []getStatuses() {
		return this.info.getStatuses();
	}

    public void setPegRevision(IResourceChange change) {
        
    }
    
}
