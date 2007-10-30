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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.compare.ConflictingFileEditorInput;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show conflict editor operation implementation
 * 
 * @author Alexander Gurov
 */
public class ShowConflictEditorOperation extends AbstractWorkingCopyOperation {

	public ShowConflictEditorOperation(IResource []resources) {
		super("Operation.ShowConflictEditor", resources);
	}

	public ShowConflictEditorOperation(IResourceProvider provider) {
		super("Operation.ShowConflictEditor", provider);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []conflictingResources = this.operableData();
		
		for (int i = 0; i < conflictingResources.length && !monitor.isCanceled(); i++) {
			final IResource current = conflictingResources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					ShowConflictEditorOperation.this.showEditorFor((IFile)current);
				}
			}, monitor, conflictingResources.length);
		}
	}

	protected void showEditorFor(IFile resource) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		
		IRepositoryLocation location = storage.getRepositoryLocation(resource);
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		
		try {
			Status []status = proxy.status(FileUtility.getWorkingCopyPath(resource), false, false, false, false, false, new SVNNullProgressMonitor());
			if (status.length == 1) {
				IContainer parent = resource.getParent();
				this.openEditor((IFile)resource, status[0].conflictWorking == null || status[0].conflictWorking.length() == 0 ? resource : (IFile)parent.findMember(status[0].conflictWorking), (IFile)parent.findMember(status[0].conflictNew), (IFile)parent.findMember(status[0].conflictOld));
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected void openEditor(IFile target, IFile left, IFile right, IFile ancestor) throws Exception {
		CompareConfiguration cc = new CompareConfiguration();
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
		final ConflictingFileEditorInput compare = new ConflictingFileEditorInput(cc, target, left, right, ancestor);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				CompareUI.openCompareEditor(compare);
			}
		});
	}
	
}
