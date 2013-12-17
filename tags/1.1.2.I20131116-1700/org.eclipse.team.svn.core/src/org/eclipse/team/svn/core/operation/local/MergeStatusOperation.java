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

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNMergeStatusCallback;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge status operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeStatusOperation extends AbstractWorkingCopyOperation implements IRemoteStatusOperation {
	protected AbstractMergeSet info;
	protected SVNMergeStatus []retVal;
	
	public MergeStatusOperation(AbstractMergeSet info, IResource []resources) {
		super("Operation_MergeStatus", SVNMessages.class, resources == null ? info.to : resources); //$NON-NLS-1$
		this.info = info;
	}
	
	public IResource []getScope() {
		return this.info.to;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
		final ArrayList<SVNMergeStatus> st = new ArrayList<SVNMergeStatus>();
		
		HashSet<IResource> resources = new HashSet<IResource>(Arrays.asList(this.operableData()));
		
		final ISVNMergeStatusCallback cb = new ISVNMergeStatusCallback() {
			public void next(SVNMergeStatus status) {
				st.add(status);
			}
		};
		
		for (int i = 0; i < this.info.to.length && !monitor.isCanceled(); i++) {
			if (resources.contains(this.info.to[i])) {
				ProgressMonitorUtility.setTaskInfo(monitor, this, this.info.to[i].getFullPath().toString());
				final int idx = i;
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						if (MergeStatusOperation.this.info instanceof MergeSet1URL) {
							MergeStatusOperation.this.doMerge1URL(idx, cb, monitor);
						}
						else if (MergeStatusOperation.this.info instanceof MergeSet2URL) {
							MergeStatusOperation.this.doMerge2URL(idx, cb, monitor);
						}
						else {
							MergeStatusOperation.this.doMergeReintegrate(idx, cb, monitor);
						}
					}
				}, monitor, this.info.to.length);
			}
		}
		this.info.addStatuses(this.retVal = st.toArray(new SVNMergeStatus[st.size()]));
    }

	public SVNEntryStatus[]getStatuses() {
		return this.retVal;
	}

    public void setPegRevision(IResourceChange change) {
        
    }
    
    protected void doMerge1URL(int idx, ISVNMergeStatusCallback cb, IProgressMonitor monitor) throws Exception {
    	MergeSet1URL info = (MergeSet1URL)this.info;
		SVNEntryRevisionReference mergeRef = SVNUtility.getEntryRevisionReference(info.from[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		long options = info.ignoreAncestry ? (ISVNConnector.Options.IGNORE_ANCESTRY/* | ISVNConnector.Options.FORCE*/) : ISVNConnector.Options.NONE/*ISVNConnector.Options.FORCE*/; 		
		options |= info.recordOnly ? ISVNConnector.Options.RECORD_ONLY : ISVNConnector.Options.NONE;
		ISVNConnector proxy = info.from[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.mergeStatus(mergeRef, info.revisions, wcPath, info.depth, options, cb, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			info.from[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }
    
    protected void doMerge2URL(int idx, ISVNMergeStatusCallback cb, IProgressMonitor monitor) throws Exception {
    	MergeSet2URL info = (MergeSet2URL)this.info;
		SVNEntryRevisionReference startRef = SVNUtility.getEntryRevisionReference(info.fromStart[idx]);
		SVNEntryRevisionReference endRef = SVNUtility.getEntryRevisionReference(info.fromEnd[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		long options = info.ignoreAncestry ? (ISVNConnector.Options.IGNORE_ANCESTRY | ISVNConnector.Options.FORCE) : ISVNConnector.Options.FORCE;
		options |= info.recordOnly ? ISVNConnector.Options.RECORD_ONLY : ISVNConnector.Options.NONE;
		ISVNConnector proxy = info.fromEnd[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.mergeStatus(startRef, endRef, wcPath, info.depth, options, cb, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			info.fromEnd[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }
    
    protected void doMergeReintegrate(int idx, ISVNMergeStatusCallback cb, IProgressMonitor monitor) throws Exception {
    	MergeSetReintegrate info = (MergeSetReintegrate)this.info;
		SVNEntryRevisionReference mergeRef = SVNUtility.getEntryRevisionReference(info.from[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		ISVNConnector proxy = info.from[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.mergeStatus(mergeRef, wcPath, ISVNConnector.Options.NONE, cb, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			info.from[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }
    
}
