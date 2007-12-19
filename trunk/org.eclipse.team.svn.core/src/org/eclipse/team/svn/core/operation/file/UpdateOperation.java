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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNNotification.NotifyStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Update resources operation
 * 
 * @author Alexander Gurov
 */
public class UpdateOperation extends AbstractFileConflictDetectionOperation implements IFileProvider {
	protected boolean updateUnresolved;
	protected SVNRevision selectedRevision;
	
	public UpdateOperation(File []files, SVNRevision selectedRevision, boolean updateUnresolved) {
		super("Operation.UpdateFile", files);
		this.updateUnresolved = updateUnresolved;
		this.selectedRevision = selectedRevision;
	}

	public UpdateOperation(IFileProvider provider, SVNRevision selectedRevision, boolean updateUnresolved) {
		super("Operation.UpdateFile", provider);
		this.updateUnresolved = updateUnresolved;
		this.selectedRevision = selectedRevision;
	}

	public File []getFiles() {
	    return this.getProcessed();
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		
		// container with resources that is really updated
		this.defineInitialResourceSet(files);
		
		Map wc2Resources = SVNUtility.splitWorkingCopies(files);
		for (Iterator it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			
			IRepositoryResource wcRoot = SVNFileStorage.instance().asRepositoryResource((File)entry.getKey(), false);
			final IRepositoryLocation location = wcRoot.getRepositoryLocation();
			
			final String []paths = FileUtility.asPathArray((File [])((List)entry.getValue()).toArray(new File[0]));

			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn update");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -r " + UpdateOperation.this.selectedRevision + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			proxy.setTouchUnresolved(this.updateUnresolved);
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.update(paths, UpdateOperation.this.selectedRevision, Depth.infinityOrFiles(true), ISVNConnector.Options.NONE, new ConflictDetectionProgressMonitor(UpdateOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			proxy.setTouchUnresolved(false);
			
			location.releaseSVNProxy(proxy);
		}
	}

	protected class ConflictDetectionProgressMonitor extends SVNProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
		    if (state.contentState == NotifyStatus.CONFLICTED || 
		        state.propState == NotifyStatus.CONFLICTED) {
		        UpdateOperation.this.hasUnresolvedConflict = true;
		        UpdateOperation.this.unprocessed.add(new File(state.path));
		        IPath conflictPath = new Path(state.path);
			    for (Iterator it = UpdateOperation.this.processed.iterator(); it.hasNext(); ) {
			    	File res = (File)it.next();
			        if (new Path(res.getAbsolutePath()).equals(conflictPath)) {
			            it.remove();
			            break;
			        }
			    }
		    }
		}
	}

}
