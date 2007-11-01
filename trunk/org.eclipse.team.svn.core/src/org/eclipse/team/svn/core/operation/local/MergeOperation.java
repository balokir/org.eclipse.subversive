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
import org.eclipse.team.svn.core.client.RevisionRange;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.IActionOperation;
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
		
		ArrayList retVal = new ArrayList();
		for (int i = 0; i < localTo.length && !monitor.isCanceled(); i++) {
			Status st = this.getStatusFor(localTo[i]);
			if (st != null) {
				retVal.add(st);
			}
		}
		Status []statuses = (Status [])retVal.toArray(new Status[retVal.size()]);
		
		IRepositoryResource from = this.info.from[0];
		IRepositoryLocation location = from.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		
		try {
			proxy.merge(null, from.getPegRevision(), new RevisionRange [] {new RevisionRange(this.info.start, from.getSelectedRevision())}, null, statuses, this.force, new ConflictDetectionProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected Status getStatusFor(IResource resource) {
		Status []statuses = this.info.getStatuses();
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
		    if (state.contentState == NotifyStatus.CONFLICTED ||
		        state.propState == NotifyStatus.CONFLICTED) {
		        MergeOperation.this.hasUnresolvedConflict = true;
			    for (Iterator it = MergeOperation.this.processed.iterator(); it.hasNext(); ) {
			        IResource res = (IResource)it.next();
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
