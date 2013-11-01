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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNCommitStatus;
import org.eclipse.team.svn.core.connector.SVNConnectorUnresolvedConflictException;
import org.eclipse.team.svn.core.connector.SVNErrorCodes;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IPostCommitErrorsProvider;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Commit operation implementation
 * 
 * @author Alexander Gurov
 */
public class CommitOperation extends AbstractConflictDetectionOperation implements IRevisionProvider, IPostCommitErrorsProvider {
	protected boolean recursive;
	protected boolean keepLocks;
	protected String message;
	protected ArrayList<RevisionPair> revisionsPairs;
	protected ArrayList<SVNCommitStatus> postCommitErrors;
	
	protected String []paths;

	public CommitOperation(IResource []resources, String message, boolean recursive, boolean keepLocks) {
		super("Operation_Commit", SVNMessages.class, resources); //$NON-NLS-1$
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}
	
	public CommitOperation(IResource []resources, String message, boolean recursive) {
		this(resources, message, recursive, false);
	}

	public CommitOperation(IResourceProvider provider, String message, boolean recursive, boolean keepLocks) {
		super("Operation_Commit", SVNMessages.class, provider); //$NON-NLS-1$
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}
	
	public CommitOperation(IResourceProvider provider, String message, boolean recursive) {
		this(provider, message, recursive, false);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList<RevisionPair>();
		this.postCommitErrors = new ArrayList<SVNCommitStatus>();
		IResource []resources = this.operableData();
		
		this.defineInitialResourceSet(resources);

		if (this.recursive) {
		    resources = FileUtility.shrinkChildNodesWithSwitched(resources);
		}
		else {
			FileUtility.reorder(resources, true);
		}
		
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.ATOMIC_X_COMMIT) != 0) {
			Map proxy2Resources = SVNUtility.splitRepositoryLocations(resources);
			for (Iterator it = proxy2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				IRepositoryLocation location = (IRepositoryLocation)entry.getKey();
				this.performCommit(location, (List)entry.getValue(), monitor, proxy2Resources.size());
			}
		}
		else {
			Map project2Resources = SVNUtility.splitWorkingCopies(resources);
			IRemoteStorage storage = SVNRemoteStorage.instance();
			for (Iterator it = project2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				
				IRepositoryLocation location = storage.getRepositoryLocation((IResource)entry.getKey());
				this.performCommit(location, (List)entry.getValue(), monitor, project2Resources.size());
			}
		}
	}
	
	protected void performCommit(final IRepositoryLocation location, List resources, final IProgressMonitor monitor, int total) {
		this.paths = FileUtility.asPathArray((IResource [])resources.toArray(new IResource[0]));
		
		this.complexWriteToConsole(new Runnable() {
			public void run() {
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn commit"); //$NON-NLS-1$
				for (int i = 0; i < CommitOperation.this.paths.length && !monitor.isCanceled(); i++) {
					CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + CommitOperation.this.paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (CommitOperation.this.recursive ? "" : " -N") + (CommitOperation.this.keepLocks ? " --no-unlock" : "") + " -m \"" + CommitOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			}
		});
		
		final ISVNConnector proxy = location.acquireSVNProxy();
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				SVNProgressMonitor svnMonitor = new SVNProgressMonitor(CommitOperation.this, monitor, null);
				proxy.commit(
				    CommitOperation.this.paths, 
					CommitOperation.this.message, 
					null,
					Depth.infinityOrEmpty(CommitOperation.this.recursive), CommitOperation.this.keepLocks ? ISVNConnector.Options.KEEP_LOCKS : ISVNConnector.Options.NONE, 
					null, svnMonitor);
				SVNCommitStatus status = svnMonitor.getCommitStatuses().isEmpty() ? null : svnMonitor.getCommitStatuses().iterator().next();
				if (status != null && status.revision != SVNRevision.INVALID_REVISION_NUMBER) {
					CommitOperation.this.revisionsPairs.add(new RevisionPair(status.revision, CommitOperation.this.paths, location));						
					String message = SVNMessages.format(SVNMessages.Console_CommittedRevision, new String[] {String.valueOf(status.revision)});
					CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				}
				if (svnMonitor.getPostCommitErrors() != null) {
					CommitOperation.this.postCommitErrors.addAll(svnMonitor.getPostCommitErrors());
				}
			}
		}, monitor, total);
		location.releaseSVNProxy(proxy);
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionsPairs == null ? null : this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
	public SVNCommitStatus [] getPostCommitErrors() {
		return this.postCommitErrors == null || this.postCommitErrors.size() == 0 ? null : this.postCommitErrors.toArray(new SVNCommitStatus[this.postCommitErrors.size()]);
	}
	
	public void reportStatus(int severity, String message, Throwable t) {
    	if (t instanceof SVNConnectorUnresolvedConflictException) {
          	this.setUnresolvedConflict(true);
          	          
          	StringBuffer messageBuf = new StringBuffer();
          	if (t.getMessage() != null && t.getMessage().length() > 0) {
          		messageBuf.append(t.getMessage());
          	}          	
          	SVNConnectorUnresolvedConflictException ex = (SVNConnectorUnresolvedConflictException) t; 
          	if (ex.getErrorId() == SVNErrorCodes.fsConflict) {
          		messageBuf.append(messageBuf.toString().endsWith("\n") ? "\n" : "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          		messageBuf.append(SVNMessages.CommitOperation_3);
          	}           	
          	this.setConflictMessage(messageBuf.toString());
        	for (int i = 0; i < this.paths.length; i++) {
                for (IResource res : this.getProcessed()) {                    
    		        if (FileUtility.getResourcePath(res).equals(new Path(this.paths[i]))) {
    		        	this.removeProcessed(res);    		        
    		            this.addUnprocessed(res);
    		            break;
    		        }
                }
            }
    	}
    	else {
    		super.reportStatus(severity, message, t);
    	}
    }	
	
}
