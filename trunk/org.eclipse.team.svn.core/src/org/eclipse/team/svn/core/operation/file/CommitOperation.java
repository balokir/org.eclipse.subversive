/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorUnresolvedConflictException;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IConsoleStream;
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
public class CommitOperation extends AbstractFileConflictDetectionOperation implements IRevisionProvider {
	protected boolean recursive;
	protected boolean keepLocks;
	protected String message;
	protected ArrayList revisionsPairs;
	
	protected String []paths;

	public CommitOperation(File []files, String message, boolean recursive, boolean keepLocks) {
		super("Operation.CommitFile", files);
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}

	public CommitOperation(IFileProvider provider, String message, boolean recursive, boolean keepLocks) {
		super("Operation.CommitFile", provider);
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}

	public RevisionPair[] getRevisions() {
		return this.revisionsPairs == null ? null : (RevisionPair [])this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList();
		File []files = this.operableData();

		this.defineInitialResourceSet(files);

		if (this.recursive) {
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
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn commit");
				for (int i = 0; i < CommitOperation.this.paths.length && !monitor.isCanceled(); i++) {
					CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + CommitOperation.this.paths[i] + "\"");
				}
				CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (CommitOperation.this.recursive ? "" : " -N") + (CommitOperation.this.keepLocks ? " --no-unlock" : "") + " -m \"" + CommitOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			}
		});
		
		final ISVNConnector proxy = location.acquireSVNProxy();
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				long revisionNumbers[] = proxy.commit(
				    CommitOperation.this.paths, 
					CommitOperation.this.message, 
					Depth.infinityOrEmpty(CommitOperation.this.recursive),
					CommitOperation.this.keepLocks ? ISVNConnector.Options.KEEP_LOCKS : ISVNConnector.Options.NONE, null, 
					new SVNProgressMonitor(CommitOperation.this, monitor, null));
				if (revisionNumbers.length > 0) {
					CommitOperation.this.revisionsPairs.add(new RevisionPair(revisionNumbers[0], CommitOperation.this.paths, location));	
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
					CommitOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(revisionNumbers[0])}));
				}
			}
		}, monitor, total);
		location.releaseSVNProxy(proxy);
	}

    protected void reportError(Throwable t) {
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
    		super.reportError(t);
    	}
    }	
	
}
