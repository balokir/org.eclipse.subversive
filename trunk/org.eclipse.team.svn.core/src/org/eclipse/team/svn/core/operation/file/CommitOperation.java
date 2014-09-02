/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNCommitStatus;
import org.eclipse.team.svn.core.connector.SVNConnectorUnresolvedConflictException;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IPostCommitErrorsProvider;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Commit operation implementation
 * 
 * @author Alexander Gurov
 */
public class CommitOperation extends AbstractFileConflictDetectionOperation implements IRevisionProvider, IPostCommitErrorsProvider {
	protected int depth;
	protected long options;
	protected String message;
	protected ArrayList<RevisionPair> revisionsPairs;
	protected ArrayList<SVNCommitStatus> postCommitErrors;
	
	protected String []paths;

	public CommitOperation(File []files, String message, boolean recursive, boolean keepLocks) {
		this(files, message, SVNDepth.infinityOrEmpty(recursive), keepLocks ? ISVNConnector.Options.KEEP_LOCKS : ISVNConnector.Options.NONE);
	}

	public CommitOperation(IFileProvider provider, String message, boolean recursive, boolean keepLocks) {
		this(provider, message, SVNDepth.infinityOrEmpty(recursive), keepLocks ? ISVNConnector.Options.KEEP_LOCKS : ISVNConnector.Options.NONE);
	}

	public CommitOperation(File []files, String message, int depth, long options) {
		super("Operation_CommitFile", SVNMessages.class, files); //$NON-NLS-1$
		this.message = message;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.COMMIT;
	}

	public CommitOperation(IFileProvider provider, String message, int depth, long options) {
		super("Operation_CommitFile", SVNMessages.class, provider); //$NON-NLS-1$
		this.message = message;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.COMMIT;
	}

	public RevisionPair[] getRevisions() {
		return this.revisionsPairs == null ? null : this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
	public SVNCommitStatus [] getPostCommitErrors() {
		return this.postCommitErrors == null ? null : this.postCommitErrors.toArray(new SVNCommitStatus[this.postCommitErrors.size()]);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList<RevisionPair>();
		this.postCommitErrors = new ArrayList<SVNCommitStatus>();
		File []files = this.operableData();

		this.defineInitialResourceSet(files);

		if (this.depth == SVNDepth.INFINITY) {
		    files = FileUtility.shrinkChildNodes(files, false);
		}
		else {
			FileUtility.reorder(files, true);
		}
		
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.ATOMIC_X_COMMIT) != 0) {
			Map proxy2Resources = SVNUtility.splitRepositoryLocations(files);
			for (Iterator it = proxy2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				IRepositoryLocation location = (IRepositoryLocation)entry.getKey();
				this.performCommit(location, (List)entry.getValue(), monitor, proxy2Resources.size());
			}
		}
		else {
			Map project2Resources = SVNUtility.splitWorkingCopies(files);
			for (Iterator it = project2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				
				IRepositoryLocation location = SVNFileStorage.instance().asRepositoryResource((File)entry.getKey(), false).getRepositoryLocation();
				this.performCommit(location, (List)entry.getValue(), monitor, project2Resources.size());
			}
		}
	}
	
	protected void performCommit(final IRepositoryLocation location, List files, final IProgressMonitor monitor, int total) {
		this.paths = FileUtility.asPathArray((File [])files.toArray(new File[files.size()]));
		
		this.complexWriteToConsole(new Runnable() {
			public void run() {
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn commit" + ISVNConnector.Options.asCommandLine(CommitOperation.this.options)); //$NON-NLS-1$
				for (int i = 0; i < CommitOperation.this.paths.length && !monitor.isCanceled(); i++) {
					CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + CommitOperation.this.paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (CommitOperation.this.depth == SVNDepth.INFINITY ? "" : " -N") + ((CommitOperation.this.options & ISVNConnector.Options.KEEP_LOCKS) != 0 ? " --no-unlock" : "") + " -m \"" + CommitOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
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
					CommitOperation.this.depth, CommitOperation.this.options, 
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

	public void reportStatus(int severity, String message, Throwable t) {
    	if (t instanceof SVNConnectorUnresolvedConflictException) {
          	this.hasUnresolvedConflict = true;
          	this.conflictMessage = t.getMessage();
        	for (int i = 0; i < this.paths.length; i++) {
                for (Iterator it = this.processed.iterator(); it.hasNext(); ) {
                    File res = (File)it.next();
    		        if (new Path(res.getAbsolutePath()).equals(new Path(this.paths[i]))) {
    		            it.remove();
    		            this.unprocessed.add(res);
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
