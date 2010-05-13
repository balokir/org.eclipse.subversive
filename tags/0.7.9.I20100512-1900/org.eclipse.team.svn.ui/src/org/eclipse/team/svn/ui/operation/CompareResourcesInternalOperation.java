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

package org.eclipse.team.svn.ui.operation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.compare.ComparePanel;
import org.eclipse.team.svn.ui.compare.ResourceCompareInput;
import org.eclipse.team.svn.ui.compare.ThreeWayResourceCompareInput;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * This operation calculate and show differences between WORKING and BASE revisions of a local resources
 * 
 * @author Alexander Gurov
 */
public class CompareResourcesInternalOperation extends AbstractActionOperation {
	protected ILocalResource local;
	protected IRepositoryResource ancestor;
	protected IRepositoryResource remote;
	protected boolean showInDialog;
	protected boolean forceReuse;
	protected String forceId;
		
	public CompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote) {
		this(local, remote, false, false);
	}
	
	public CompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse) {
		this(local, remote, forceReuse, false);
	}
	
	public CompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse, boolean showInDialog) {
		super("Operation_CompareLocal", SVNUIMessages.class); //$NON-NLS-1$
		this.local = local;
		this.ancestor = local.isCopied() ? SVNUtility.getCopiedFrom(local.getResource()) : SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
		this.ancestor.setSelectedRevision(SVNRevision.fromNumber(local.getBaseRevision()));
		this.remote = remote;
		this.showInDialog = showInDialog;
		this.forceReuse = forceReuse;
	}

	public void setForceId(String forceId) {
		this.forceId = forceId;
	}

	public String getForceId() {
		return this.forceId;
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		/*
		 * As there's no svn operation (svn diff) which can compare working copy url and repository url
		 * which doesn't match to working copy url (e.g. compare working copy with another branch etc.)
		 * we need extra handling: and so we calculate local changes (using svn status)
		 * and remote changes (using svn diff).
		 * In order to compare working copy and not matched to it repository url we detect repository
		 * url corresponding to working copy and compare it with repository url, in other words,
		 * we compare 2 repository urls.
		 */		
		final ArrayList<SVNDiffStatus> localChanges = new ArrayList<SVNDiffStatus>();
		final ArrayList<SVNDiffStatus> remoteChanges = new ArrayList<SVNDiffStatus>();
		
		final IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.local.getResource());
		final ISVNConnector proxy = location.acquireSVNProxy();
		
		final IRepositoryResource []diffPair = new IRepositoryResource[] {this.ancestor, this.remote};
		SVNRevision revision = this.remote.getSelectedRevision();
		boolean fetchRemote = revision.getKind() == Kind.HEAD || revision.getKind() == Kind.NUMBER;
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				proxy.status(FileUtility.getWorkingCopyPath(CompareResourcesInternalOperation.this.local.getResource()), Depth.INFINITY, ISVNConnector.Options.IGNORE_EXTERNALS, null, new ISVNEntryStatusCallback() {
					public void next(SVNChangeStatus status) {
						localChanges.add(new SVNDiffStatus(status.path, status.path, status.nodeKind, status.textStatus, status.propStatus));
					}
				}, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
			}
		}, monitor, 100, fetchRemote ? 5 : 60);
		
		if (!monitor.isCanceled() && fetchRemote) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(diffPair);
					ProgressMonitorUtility.doTaskExternal(op, monitor);
					if (op.getExecutionState() != IActionOperation.OK) {
						CompareResourcesInternalOperation.this.reportStatus(op.getStatus());
						return;
					}
					diffPair[0] = op.getRepositoryResources()[0];
					diffPair[1] = op.getRepositoryResources()[1];
				}
			}, monitor, 100, 55);
			if (this.getExecutionState() == IActionOperation.ERROR) {
				return;
			}
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					ProgressMonitorUtility.setTaskInfo(monitor, CompareResourcesInternalOperation.this, SVNMessages.Progress_Running);
					
					SVNEntryRevisionReference refPrev = SVNUtility.getEntryRevisionReference(diffPair[0]);
					SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(diffPair[1]);
					if (SVNUtility.useSingleReferenceSignature(refPrev, refNext)) {
						SVNUtility.diffStatus(proxy, remoteChanges, refPrev, refPrev.revision, refNext.revision, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
					}
					else {
						SVNUtility.diffStatus(proxy, remoteChanges, refPrev, refNext, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
					}
				}
			}, monitor, 100, 5);
		}
		
		location.releaseSVNProxy(proxy);
		
		if (!monitor.isCanceled()) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					CompareConfiguration cc = new CompareConfiguration();
					cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
					diffPair[0].setSelectedRevision(SVNRevision.BASE);
					final ThreeWayResourceCompareInput compare = new ThreeWayResourceCompareInput(cc, CompareResourcesInternalOperation.this.local, diffPair[0], diffPair[1], localChanges, remoteChanges);
					compare.setForceId(CompareResourcesInternalOperation.this.forceId);
					compare.initialize(monitor);
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (CompareResourcesInternalOperation.this.showInDialog) {
								if (CompareResourcesInternalOperation.this.compareResultOK(compare)) {
									ComparePanel panel = new ComparePanel(compare, CompareResourcesInternalOperation.this.local.getResource());
									DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
									dialog.open();
								}
							}
							else {
								ResourceCompareInput.openCompareEditor(compare, CompareResourcesInternalOperation.this.forceReuse);
							}
						}
					});
				}
			}, monitor, 100, 40);
		}
	}
	
	protected boolean compareResultOK(CompareEditorInput input) {
		final Shell shell = UIMonitorUtility.getShell();
		
		try {
			SVNTeamUIPlugin.instance().getWorkbench().getProgressService().run(true, true, input);
						
			String message = input.getMessage();
			if (message != null) {
				MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message); //$NON-NLS-1$ Compare's property
			}
			else if (input.getCompareResult() == null) {
				MessageDialog.openInformation(shell, Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences")); //$NON-NLS-1$ //$NON-NLS-2$ Compare's properties
			}
			else {
				return true;
			}
		} 
		catch (InterruptedException x) {
			// cancelled by user		
		} 
		catch (InvocationTargetException x) {
			MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), x.getTargetException().getMessage()); //$NON-NLS-1$ Compare's property
		}
		return false;
	}
}
