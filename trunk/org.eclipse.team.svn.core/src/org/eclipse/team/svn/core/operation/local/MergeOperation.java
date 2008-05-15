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
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeOperation extends AbstractConflictDetectionOperation implements IResourceProvider {
	protected MergeSet info;
	protected boolean force;

	public MergeOperation(IResource []resources, MergeSet info, boolean force) {
		super("Operation.Merge", resources);
		this.info = info;
		this.force = force;
	}

	public MergeOperation(IResourceProvider provider, MergeSet info, boolean force) {
		super("Operation.Merge", provider);
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
		
		SVNEntryRevisionReference startRef = SVNUtility.getEntryRevisionReference(this.info.fromStart[0]);
		SVNEntryRevisionReference endRef = SVNUtility.getEntryRevisionReference(this.info.fromEnd[0]);
		IRepositoryLocation location = this.info.fromEnd[0].getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		long options = this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
		options = this.info.ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE;
		try {
			if (SVNUtility.useSingleReferenceSignature(startRef, endRef)) {
				proxy.merge(endRef, new SVNRevisionRange [] {new SVNRevisionRange(startRef.revision, endRef.revision)}, null, statuses, options, new ConflictDetectionProgressMonitor(this, monitor, null));
			}
			else {
				proxy.merge(startRef, endRef, null, statuses, options, new ConflictDetectionProgressMonitor(this, monitor, null));
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
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

	protected class ConflictDetectionProgressMonitor extends SVNProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
		    if (state.contentState == NodeStatus.CONFLICTED || state.propState == NodeStatus.CONFLICTED) {
		        MergeOperation.this.hasUnresolvedConflict = true;
			    for (Iterator<IResource> it = MergeOperation.this.processed.iterator(); it.hasNext(); ) {
			        IResource res = it.next();
			        if (FileUtility.getResourcePath(res).equals(new Path(state.path))) {
			            it.remove();
			            MergeOperation.this.unprocessed.add(res);
			            break;
			        }
			    }
		    }
		}
		
	}

}
