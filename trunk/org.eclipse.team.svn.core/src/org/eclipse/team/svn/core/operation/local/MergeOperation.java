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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNConflictDetectionProgressMonitor;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeOperation extends AbstractConflictDetectionOperation implements IResourceProvider {
	protected AbstractMergeSet info;
	protected boolean force;

	public MergeOperation(IResource []resources, AbstractMergeSet info, boolean force) {
		super("Operation_Merge", resources); //$NON-NLS-1$
		this.info = info;
		this.force = force;
	}

	public MergeOperation(IResourceProvider provider, AbstractMergeSet info, boolean force) {
		super("Operation_Merge", provider); //$NON-NLS-1$
		this.info = info;
		this.force = force;
	}

	public IResource []getResources() {
	    return this.getProcessed();
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []localTo = this.operableData();
		this.defineInitialResourceSet(localTo);
		localTo = FileUtility.shrinkChildNodes(localTo);
		
		ArrayList<SVNMergeStatus> retVal = new ArrayList<SVNMergeStatus>();
		for (int i = 0; i < localTo.length && !monitor.isCanceled(); i++) {
			SVNMergeStatus st = this.getStatusFor(localTo[i]);
			if (st != null) {
				retVal.add(st);
			}
		}
		SVNMergeStatus []statuses = retVal.toArray(new SVNMergeStatus[retVal.size()]);

		if (this.info instanceof MergeSet1URL) {
			this.doMerge1URL(0, statuses, monitor);
		}
		else if (this.info instanceof MergeSet2URL) {
			this.doMerge2URL(0, statuses, monitor);
		}
		else {
			this.doMergeReintegrate(0, statuses, monitor);
		}
	}
	
	protected SVNMergeStatus getStatusFor(IResource resource) {
		SVNMergeStatus []statuses = this.info.getStatuses();
		IPath target = FileUtility.getResourcePath(resource);
		for (int i = 0; i < statuses.length; i++) {
			if (target.equals(new Path(statuses[i].path))) {
				return statuses[i];
			}
		}
		return null;
	}

    protected void doMerge1URL(int idx, SVNMergeStatus []statuses, IProgressMonitor monitor) throws Exception {
    	MergeSet1URL info = (MergeSet1URL)this.info;
		SVNEntryRevisionReference mergeRef = SVNUtility.getEntryRevisionReference(info.from[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		long options = this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
		options |= info.ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE;
		ISVNConnector proxy = info.from[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.merge(mergeRef, info.revisions, wcPath, statuses, options, new ConflictDetectionProgressMonitor(this, monitor, null));
		}
		finally {
			info.from[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }

    protected void doMerge2URL(int idx, SVNMergeStatus []statuses, IProgressMonitor monitor) throws Exception {
    	MergeSet2URL info = (MergeSet2URL)this.info;
		SVNEntryRevisionReference startRef = SVNUtility.getEntryRevisionReference(info.fromStart[idx]);
		SVNEntryRevisionReference endRef = SVNUtility.getEntryRevisionReference(info.fromEnd[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		long options = this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
		options |= info.ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE;
		ISVNConnector proxy = info.fromEnd[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.merge(startRef, endRef, wcPath, statuses, options, new ConflictDetectionProgressMonitor(this, monitor, null));
		}
		finally {
			info.fromEnd[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }
    
    protected void doMergeReintegrate(int idx, SVNMergeStatus []statuses, IProgressMonitor monitor) throws Exception {
    	MergeSetReintegrate info = (MergeSetReintegrate)this.info;
		SVNEntryRevisionReference mergeRef = SVNUtility.getEntryRevisionReference(info.from[idx]);
		String wcPath = FileUtility.getWorkingCopyPath(info.to[idx]);
		long options = this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
		ISVNConnector proxy = info.from[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			proxy.merge(mergeRef, wcPath, statuses, options, new ConflictDetectionProgressMonitor(this, monitor, null));
		}
		finally {
			info.from[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }
    
	protected class ConflictDetectionProgressMonitor extends SVNConflictDetectionProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}		
		protected void processConflict(ItemState state) {
			MergeOperation.this.setUnresolvedConflict(true);
		    for (IResource res : MergeOperation.this.getProcessed()) {
		        if (FileUtility.getResourcePath(res).equals(new Path(state.path))) {
		        	MergeOperation.this.removeProcessed(res);			           
		            MergeOperation.this.addUnprocessed(res);
		            break;
		        }
		    }
		}		
	}

}
