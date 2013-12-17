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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNConflictDetectionProgressMonitor;
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
	protected SVNRevision selectedRevision;
	protected boolean ignoreExternals;
	protected int depth = ISVNConnector.Depth.INFINITY;
	protected boolean isStickyDepth;
	protected String updateDepthPath;
	
	public UpdateOperation(File []files, SVNRevision selectedRevision, boolean ignoreExternals) {
		super("Operation_UpdateFile", SVNMessages.class, files); //$NON-NLS-1$
		this.selectedRevision = selectedRevision;
		this.ignoreExternals = ignoreExternals;
	}

	public UpdateOperation(IFileProvider provider, SVNRevision selectedRevision, boolean ignoreExternals) {
		super("Operation_UpdateFile", SVNMessages.class, provider); //$NON-NLS-1$
		this.selectedRevision = selectedRevision;
		this.ignoreExternals = ignoreExternals;
	}

	public void setDepthOptions(int depth, boolean isStickyDepth, String updateDepthPath) {
		this.depth = depth;
		this.isStickyDepth = isStickyDepth;
		this.updateDepthPath = updateDepthPath;
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

			//append update depth path
			if (this.isStickyDepth && this.updateDepthPath != null &&  paths.length == 1) {
				String newPath = paths[0] + "/" + this.updateDepthPath;
				newPath = FileUtility.normalizePath(newPath);
				paths[0] = newPath;
			}	
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn update"); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -r " + UpdateOperation.this.selectedRevision + SVNUtility.getDepthArg(UpdateOperation.this.depth, UpdateOperation.this.isStickyDepth) + SVNUtility.getIgnoreExternalsArg(UpdateOperation.this.ignoreExternals) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					long options = UpdateOperation.this.ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE;
					options |= UpdateOperation.this.isStickyDepth ? ISVNConnector.Options.DEPTH_IS_STICKY : ISVNConnector.Options.NONE;
					proxy.update(paths, UpdateOperation.this.selectedRevision, UpdateOperation.this.depth, options, new ConflictDetectionProgressMonitor(UpdateOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			
			location.releaseSVNProxy(proxy);
		}
	}

	protected class ConflictDetectionProgressMonitor extends SVNConflictDetectionProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}

		protected void processConflict(ItemState state) {
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
