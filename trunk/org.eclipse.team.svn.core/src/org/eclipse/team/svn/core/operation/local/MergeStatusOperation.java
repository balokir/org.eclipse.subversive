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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ChangePath;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge status operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeStatusOperation extends AbstractNonLockingOperation implements IRemoteStatusOperation {
	protected Status []statuses;
	protected IResource []localTo;
	protected IRepositoryResource []from;
	protected IRepositoryResource []copiedFrom;
	protected Revision []copiedFromRevision;
	protected Revision fromStartRevision;
	
	public MergeStatusOperation(IResource []localTo, IRepositoryResource []from, Revision fromStartRevision) {
		super("Operation.MergeStatus");
		this.localTo = localTo;
		this.from = from;
		this.fromStartRevision = fromStartRevision;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.statuses = new Status[0];
		this.copiedFrom = new IRepositoryResource[this.from.length];
		this.copiedFromRevision = new Revision[this.from.length];
		
		for (int i = 0; i < this.localTo.length && !monitor.isCanceled(); i++) {
			final int idx = i;
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					MergeStatusOperation.this.doMergeStatus(idx, monitor);
				}
			}, monitor, this.localTo.length);
		}
    }

    public IResource []getLocalTo() {
    	return this.localTo;
    }
    
    public IRepositoryResource []getFrom() {
    	return this.from;
    }
    
    public IRepositoryResource []getCopiedFrom() {
    	return this.copiedFrom;
    }
    
    public Revision []getCopiedFromRevision() {
    	return this.copiedFromRevision;
    }
    
    public Revision getFromStartRevision() {
    	return this.fromStartRevision;
    }
    
	public Status []getStatuses() {
		return this.statuses;
	}

    public void setPegRevision(IResourceChange change) {
        
    }
    
    protected void doMergeStatus(int idx, IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		String wcPath = FileUtility.getWorkingCopyPath(this.localTo[idx]);
		ISVNClientWrapper proxy = this.from[idx].getRepositoryLocation().acquireSVNProxy();
		try {
			LogMessage []messages = GetLogMessagesOperation.getMessagesImpl(proxy, this.from[idx], Revision.getInstance(0), this.from[idx].getSelectedRevision(), 1, true, this, monitor);
			// possible in case of security restrictions
			ChangePath []changes = messages == null || messages.length == 0 ? new ChangePath[0] : messages[0].changedPaths;
			IRepositoryResource remote = storage.asRepositoryResource(this.localTo[idx]);
			ILocalResource tmp = storage.asLocalResource(this.localTo[idx]);
			if (tmp == null) {
				return;
			}
			remote.setSelectedRevision(Revision.getInstance(tmp.getRevision()));
			if (changes.length != 1 || changes[0].copySrcPath == null || !remote.getUrl().endsWith(changes[0].copySrcPath)) {
				LogMessage []localToMessages = GetLogMessagesOperation.getMessagesImpl(proxy, remote, Revision.getInstance(0), remote.getSelectedRevision(), 1, true, this, monitor);
				// possible in case of security restrictions
				ChangePath []localToChanges = localToMessages == null || localToMessages.length == 0 ? new ChangePath[0] : localToMessages[0].changedPaths;
				if (localToChanges.length == 1 && localToChanges[0].copySrcPath != null && this.from[idx].getUrl().endsWith(localToChanges[0].copySrcPath)) {
					// merging trunk into branch
					this.copiedFrom[idx] = SVNUtility.copyOf(this.from[idx]);
					this.copiedFromRevision[idx] = Revision.getInstance(localToChanges[0].copySrcRevision);
				}
				else if (localToChanges.length == 1 && localToChanges[0].copySrcPath != null && changes.length == 1 && localToChanges[0].copySrcPath.equals(changes[0].copySrcPath)) {
					// merging branch into branch
					ChangePath change = localToChanges[0].copySrcRevision < changes[0].copySrcRevision ? localToChanges[0] : changes[0];
					this.copiedFromRevision[idx] = Revision.getInstance(change.copySrcRevision);
					Info2 []infos = proxy.info2(SVNUtility.encodeURL(this.from[idx].getUrl()), Revision.HEAD, null, false, new SVNProgressMonitor(this, monitor, null));
					this.copiedFrom[idx] = remote.getRepositoryLocation().asRepositoryContainer(SVNUtility.decodeURL(infos[0].reposRootUrl) + change.copySrcPath, false);
				}
				// else merging two unrelated URL's or security restriction
			}
			else {
				// merging branch into trunk
				this.copiedFromRevision[idx] = Revision.getInstance(changes[0].copySrcRevision);
				this.copiedFrom[idx] = SVNUtility.copyOf(remote);
			}
			// merge status does not work with non start revision specified. So, doing quick fix.
			this.statuses = 
			    proxy.mergeStatus(
			        this.from[idx].getUrl(), this.from[idx].getPegRevision(),  	// branch URL, peg, start, stop
			        this.fromStartRevision == null ? this.copiedFromRevision[idx] : this.fromStartRevision, this.from[idx].getSelectedRevision(), 
					wcPath, 							// head
					null, true, false, 									// last merge revision etc.
					new SVNProgressMonitor(this, monitor, null));
			//FIXME debug output
//		    for (int i = 0; i < this.statuses.length; i++) {
//			    System.out.println(
//			    	"Resource: [" + statuses[i].getPath() + 
//			    	"] Head: [" + this.statuses[i].getTextStatusDescription() + ", " + this.statuses[i].getPropStatusDescription() + 
//			    	"] Branch: [" + Status.Kind.getDescription(this.statuses[i].getRepositoryTextStatus()) + ", " + Status.Kind.getDescription(this.statuses[i].getRepositoryPropStatus()) + "]");
//		    }
		}
		finally {
		    this.from[idx].getRepositoryLocation().releaseSVNProxy(proxy);
		}
    }

}
