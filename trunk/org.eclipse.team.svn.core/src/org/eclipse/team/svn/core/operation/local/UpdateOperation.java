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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
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
	protected boolean doRecursiveUpdate;
	protected boolean ignoreExternals;
	
	public UpdateOperation(IResource []resources, boolean doRecursiveUpdate, boolean ignoreExternals) {
	    this(resources, null, doRecursiveUpdate, ignoreExternals);
	}

	public UpdateOperation(IResourceProvider provider, boolean doRecursiveUpdate, boolean ignoreExternals) {
	    this(provider, null, doRecursiveUpdate, ignoreExternals);
	}

	public UpdateOperation(IResourceProvider provider, SVNRevision selectedRevision, boolean doRecursiveUpdate, boolean ignoreExternals) {
		super("Operation_Update", provider);		 //$NON-NLS-1$
		this.doRecursiveUpdate = doRecursiveUpdate;
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
		this.ignoreExternals = ignoreExternals;
	}
	
	public UpdateOperation(IResource[] resources, SVNRevision selectedRevision, boolean doRecursiveUpdate, boolean ignoreExternals) {
		super("Operation_Update", resources); //$NON-NLS-1$
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
		this.doRecursiveUpdate = doRecursiveUpdate;
		this.ignoreExternals = ignoreExternals;
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
			if (this.doRecursiveUpdate) {
			    wcResources = FileUtility.shrinkChildNodes(wcResources);
			}
			else {
			    FileUtility.reorder(wcResources, true);
			}
			final String []paths = FileUtility.asPathArray(wcResources);

			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn update"); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -r " + UpdateOperation.this.selectedRevision + SVNUtility.getIgnoreExternalsArg(UpdateOperation.this.ignoreExternals) + (UpdateOperation.this.doRecursiveUpdate ? "" : " -N") + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					long options = UpdateOperation.this.ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE;
					proxy.update(
					    paths, 
					    UpdateOperation.this.selectedRevision, 
						Depth.unknownOrFiles(UpdateOperation.this.doRecursiveUpdate),
						options, 
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
