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

package org.eclipse.team.svn.ui.action.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;

/**
 * Team services menu "replace with latest revision" action implementation
 * 
 * @author Alexander Gurov
 */
public class ReplaceWithLatestRevisionAction extends AbstractNonRecursiveTeamAction {
	public ReplaceWithLatestRevisionAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
		IActionOperation op = ReplaceWithLatestRevisionAction.getReplaceOperation(resources, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

	public static IActionOperation getReplaceOperation(IResource []resources, Shell shell) {
		ReplaceWarningDialog dialog = new ReplaceWarningDialog(shell);
		if (dialog.open() == 0) {
			CompositeOperation op = new CompositeOperation("Operation.ReplaceWithLatest");
			
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
			op.add(saveOp);
			
			SaveUnversionedOperation saveUnversioned = new SaveUnversionedOperation(resources);
			op.add(saveUnversioned);
			
			IActionOperation revertOp = new RevertOperation(resources, true);
			op.add(revertOp);
			IActionOperation removeOp = new RemoveNonVersionedResourcesOperation(resources, true);
			op.add(removeOp, new IActionOperation[] {revertOp});
			op.add(new UpdateOperation(resources, true), new IActionOperation[] {revertOp, removeOp});
			
			op.add(new RestoreUnversionedOperation(resources, saveUnversioned));
			
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new RefreshResourcesOperation(resources));

			return op;
		}
		return null;
	}
	
	protected static class SaveUnversionedOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor {
		public List<ResourceChange> changes;
		
		public SaveUnversionedOperation(IResource[] resources) {
			super("Operation.SaveUnversioned", resources);
			this.changes = new ArrayList<ResourceChange>();
		}
		
		public void doOperation(IActionOperation op, IProgressMonitor monitor) {
		    this.reportStatus(op.run(monitor).getStatus());
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			IResource []resources = this.operableData();
			for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
				final IResource current = resources[i];
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(current);
						ResourceChange change = ResourceChange.wrapLocalResource(null, local, true);
						change.traverse(new IResourceChangeVisitor() {
							public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
								ILocalResource local = change.getLocal();
								if (local instanceof ILocalFile && IStateFilter.SF_UNVERSIONED.accept(local) && !local.getResource().isDerived()) {
							    	File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
								    // optimize operation performance using "move on FS" if possible
									if (real.exists() && !real.renameTo(change.getTemporary())) {
										FileUtility.copyFile(change.getTemporary(), real, monitor);
										real.delete();
									}
								}
							}
							public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
							}
						}, IResource.DEPTH_INFINITE, SaveUnversionedOperation.this, monitor);
						SaveUnversionedOperation.this.changes.add(change);
					}
				}, monitor, resources.length);
			}
		}
		
	}
	
	protected static class RestoreUnversionedOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor {
		public SaveUnversionedOperation changes;
		
		public RestoreUnversionedOperation(IResource[] resources, SaveUnversionedOperation changes) {
			super("Operation.RestoreUnversioned", resources);
			this.changes = changes;
		}
		
		public void doOperation(IActionOperation op, IProgressMonitor monitor) {
		    this.reportStatus(op.run(monitor).getStatus());
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ResourceChange []changes = this.changes.changes.toArray(new ResourceChange[0]);
			for (int i = 0; i < changes.length && !monitor.isCanceled(); i++) {
				final ResourceChange change = changes[i];
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						change.traverse(new IResourceChangeVisitor() {
							public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
							}
							public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
								ILocalResource local = change.getLocal();
								if (local instanceof ILocalFile && IStateFilter.SF_UNVERSIONED.accept(local) && !local.getResource().isDerived()) {
							    	File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
								    // optimize operation performance using "move on FS" if possible
									if (!real.exists()) {
										real.getParentFile().mkdirs();
										if (!change.getTemporary().renameTo(real)) {
											FileUtility.copyFile(real, change.getTemporary(), monitor);
											change.getTemporary().delete();
										}
									}
								}
							}
						}, IResource.DEPTH_INFINITE, RestoreUnversionedOperation.this, monitor);
					}
				}, monitor, changes.length);
			}
		}
		
	}
	
}
