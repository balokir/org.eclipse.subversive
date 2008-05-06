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
import org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
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
	
	public UpdateOperation(IResource []resources, boolean doRecursiveUpdate) {
	    this(resources, null, doRecursiveUpdate);
	}

	public UpdateOperation(IResourceProvider provider, boolean doRecursiveUpdate) {
	    this(provider, null, doRecursiveUpdate);
	}

	public UpdateOperation(IResourceProvider provider, SVNRevision selectedRevision, boolean doRecursiveUpdate) {
		super("Operation.Update", provider);		
		this.doRecursiveUpdate = doRecursiveUpdate;
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
	}
	
	public UpdateOperation(IResource[] resources, SVNRevision selectedRevision, boolean doRecursiveUpdate) {
		super("Operation.Update", resources);
		this.selectedRevision = selectedRevision == null ? SVNRevision.HEAD : selectedRevision;
		this.doRecursiveUpdate = doRecursiveUpdate;
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
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn update");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					UpdateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -r " + UpdateOperation.this.selectedRevision + (UpdateOperation.this.doRecursiveUpdate ? "" : " -N") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.update(
					    paths, 
					    UpdateOperation.this.selectedRevision, 
						Depth.infinityOrFiles(UpdateOperation.this.doRecursiveUpdate),
						ISVNConnector.Options.NONE, 
						new ConflictDetectionProgressMonitor(UpdateOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			
			location.releaseSVNProxy(proxy);
		}
	}

	protected class ConflictDetectionProgressMonitor extends SVNProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
		    if (state.contentState == NodeStatus.CONFLICTED || 
		        state.propState == NodeStatus.CONFLICTED) {
		        UpdateOperation.this.hasUnresolvedConflict = true;
			    for (Iterator it = UpdateOperation.this.processed.iterator(); it.hasNext(); ) {
			        IResource res = (IResource)it.next();
			        IPath conflictPath = new Path(state.path);
			        IPath resourcePath = FileUtility.getResourcePath(res);
			        if (resourcePath.isPrefixOf(conflictPath)) {
				        if (resourcePath.equals(conflictPath)) {
				            it.remove();
				        }
			        	IResource conflictResource = ResourcesPlugin.getWorkspace().getRoot().findMember(res.getFullPath().append(conflictPath.removeFirstSegments(resourcePath.segmentCount())));
			            if (conflictResource != null) {
			            	UpdateOperation.this.unprocessed.add(conflictResource);
			            }
			            break;
			        }
			    }
		    }
		}
		
	}

}
