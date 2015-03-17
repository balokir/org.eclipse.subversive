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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNConflictDetectionProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Update operation implementation
 * 
 * @author Alexander Gurov
 */
public class UpdateOperation extends AbstractConflictDetectionOperation implements IResourceProvider {
	protected SVNRevision selectedRevision;
	protected int depth;
	protected long options;
	protected String updateDepthPath;
	
	public UpdateOperation(IResource []resources, boolean ignoreExternals) {
	    this(resources, null, ignoreExternals);
	}

	public UpdateOperation(IResourceProvider provider, boolean ignoreExternals) {
	    this(provider, null, ignoreExternals);
	}

	public UpdateOperation(IResourceProvider provider, SVNRevision selectedRevision, boolean ignoreExternals) {
		this(provider, selectedRevision, SVNDepth.INFINITY, ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE, null);
	}
	
	public UpdateOperation(IResource[] resources, SVNRevision selectedRevision, boolean ignoreExternals) {
		this(resources, selectedRevision, SVNDepth.INFINITY, ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE, null);
	}
	
	public UpdateOperation(IResourceProvider provider, SVNRevision selectedRevision, int depth, long options, String updateDepthPath) {
		super("Operation_Update", SVNMessages.class, provider); //$NON-NLS-1$		
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.UPDATE;
		this.updateDepthPath = updateDepthPath;
	}
	
	public UpdateOperation(IResource[] resources, SVNRevision selectedRevision, int depth, long options, String updateDepthPath) {
		super("Operation_Update", SVNMessages.class, resources); //$NON-NLS-1$
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.UPDATE;
		this.updateDepthPath = updateDepthPath;
	}
	
	public void setDepthOptions(int depth, boolean isStickyDepth, String updateDepthPath) {
		this.depth = depth;
		this.options &= ~ISVNConnector.Options.DEPTH_IS_STICKY;
		this.options |= isStickyDepth ? ISVNConnector.Options.DEPTH_IS_STICKY : ISVNConnector.Options.NONE;
		this.updateDepthPath = updateDepthPath;
	}
	
	public int getOperationWeight() {
		return 19;
	}
	
	public IResource []getResources() {
	    return this.getProcessed();
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		// container with resources that is really updated
		this.defineInitialResourceSet(resources);

		IRemoteStorage storage = SVNRemoteStorage.instance();
		Map wc2Resources = SVNUtility.splitWorkingCopies(resources);
		for (Iterator it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = storage.getRepositoryLocation((IProject)entry.getKey());
			IResource []wcResources = (IResource [])((List)entry.getValue()).toArray(new IResource[0]);
			if (this.depth == SVNDepth.INFINITY || this.depth == SVNDepth.UNKNOWN) {
			    wcResources = FileUtility.shrinkChildNodes(wcResources);
			}
			else {
			    FileUtility.reorder(wcResources, true);
			}
			final String []paths = FileUtility.asPathArray(wcResources);
			
			//append update depth path
			if ((this.options & ISVNConnector.Options.DEPTH_IS_STICKY) != 0 && this.updateDepthPath != null &&  paths.length == 1) {
				String newPath = paths[0] + "/" + this.updateDepthPath;
				newPath = FileUtility.normalizePath(newPath);
				paths[0] = newPath;
			}						
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn update" + ISVNConnector.Options.asCommandLine(UpdateOperation.this.options)); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -r " + UpdateOperation.this.selectedRevision + SVNUtility.getDepthArg(UpdateOperation.this.depth, UpdateOperation.this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.update(
					    paths, 
					    UpdateOperation.this.selectedRevision, 
					    UpdateOperation.this.depth,
					    UpdateOperation.this.options, 
						new ConflictDetectionProgressMonitor(UpdateOperation.this, monitor, null));
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
			UpdateOperation.this.setUnresolvedConflict(true);
		    for (IResource res : UpdateOperation.this.getProcessed()) {
		        IPath conflictPath = new Path(state.path);
		        IPath resourcePath = FileUtility.getResourcePath(res);
		        if (resourcePath.isPrefixOf(conflictPath)) {
			        if (resourcePath.equals(conflictPath)) {
			        	UpdateOperation.this.removeProcessed(res);
			        }
		        	IResource conflictResource = ResourcesPlugin.getWorkspace().getRoot().findMember(res.getFullPath().append(conflictPath.removeFirstSegments(resourcePath.segmentCount())));
		            if (conflictResource != null) {
		            	UpdateOperation.this.addUnprocessed(conflictResource);
		            }
		            break;
		        }
		    }			
		}
		
	}

}
