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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNConnector;
import org.eclipse.team.svn.core.client.SVNConnectorUnresolvedConflictException;
import org.eclipse.team.svn.core.client.ISVNConnector.Depth;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IConsoleStream;
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
public class CommitOperation extends AbstractConflictDetectionOperation implements IRevisionProvider {
	protected boolean recursive;
	protected boolean keepLocks;
	protected String message;
	protected ArrayList revisionsPairs;
	
	protected String []paths;

	public CommitOperation(IResource []resources, String message, boolean recursive, boolean keepLocks) {
		super("Operation.Commit", resources);
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}
	
	public CommitOperation(IResource []resources, String message, boolean recursive) {
		this(resources, message, recursive, false);
	}

	public CommitOperation(IResourceProvider provider, String message, boolean recursive, boolean keepLocks) {
		super("Operation.Commit", provider);
		this.message = message;
		this.recursive = recursive;
		this.keepLocks = keepLocks;
	}
	
	public CommitOperation(IResourceProvider provider, String message, boolean recursive) {
		this(provider, message, recursive, false);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList();
		IResource []resources = this.operableData();
		
		this.defineInitialResourceSet(resources);

		if (this.recursive) {
		    resources = FileUtility.shrinkChildNodes(resources);
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
					CommitOperation.this.keepLocks,
					false, null, 
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
	
	public RevisionPair []getRevisions() {
		return this.revisionsPairs == null ? null : (RevisionPair [])this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
    protected void reportError(Throwable t) {
    	if (t instanceof SVNConnectorUnresolvedConflictException) {
          	this.hasUnresolvedConflict = true;
          	this.conflictMessage = t.getMessage();
        	for (int i = 0; i < this.paths.length; i++) {
                for (Iterator it = this.processed.iterator(); it.hasNext(); ) {
                    IResource res = (IResource)it.next();
    		        if (FileUtility.getResourcePath(res).equals(new Path(this.paths[i]))) {
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
