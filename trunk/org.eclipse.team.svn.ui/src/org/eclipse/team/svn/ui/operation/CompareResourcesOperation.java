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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
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
import org.eclipse.team.svn.ui.compare.ComparePanel;
import org.eclipse.team.svn.ui.compare.ThreeWayResourceCompareInput;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * This operation calculate and show differences between WORKING and BASE revisions of a local resources
 * 
 * @author Alexander Gurov
 */
public class CompareResourcesOperation extends AbstractActionOperation {
	protected ILocalResource local;
	protected IRepositoryResource ancestor;
	protected IRepositoryResource remote;
	protected boolean showInDialog;
	
	public CompareResourcesOperation(ILocalResource local, IRepositoryResource remote) {
		this(local, remote, false);
	}
	
	public CompareResourcesOperation(ILocalResource local, IRepositoryResource remote, boolean showInDialog) {
		super("Operation.CompareLocal");
		this.local = local;
		this.ancestor = local.isCopied() ? SVNUtility.getCopiedFrom(local.getResource()) : SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
		this.ancestor.setSelectedRevision(SVNRevision.fromNumber(local.getRevision()));
		this.remote = remote;
		this.showInDialog = showInDialog;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		final ArrayList<SVNDiffStatus> localChanges = new ArrayList<SVNDiffStatus>();
		final ArrayList<SVNDiffStatus> remoteChanges = new ArrayList<SVNDiffStatus>();
		
		final IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.local.getResource());
		final ISVNConnector proxy = location.acquireSVNProxy();
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				proxy.status(FileUtility.getWorkingCopyPath(CompareResourcesOperation.this.local.getResource()), Depth.INFINITY, ISVNConnector.Options.IGNORE_EXTERNALS, null, new ISVNEntryStatusCallback() {
					public void next(SVNChangeStatus status) {
						localChanges.add(new SVNDiffStatus(status.path, status.path, status.nodeKind, status.textStatus, status.propStatus));
					}
				}, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
			}
		}, monitor, 3);
		
		final IRepositoryResource []diffPair = new IRepositoryResource[] {this.ancestor, this.remote};
		SVNRevision revision = this.remote.getSelectedRevision();
		if (!monitor.isCanceled() && (revision.getKind() == Kind.HEAD || revision.getKind() == Kind.NUMBER)) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(diffPair, true);
					ProgressMonitorUtility.doTaskExternal(op, monitor);
					diffPair[0] = op.getRepositoryResources()[0];
					diffPair[1] = op.getRepositoryResources()[1];
					
					SVNEntryRevisionReference refPrev = SVNUtility.getEntryRevisionReference(diffPair[0]);
					SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(diffPair[1]);
					if (SVNUtility.useSingleReferenceSignature(refPrev, refNext)) {
						SVNUtility.diffStatus(proxy, remoteChanges, refPrev, refPrev.revision, refNext.revision, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
					}
					else {
						SVNUtility.diffStatus(proxy, remoteChanges, refPrev, refNext, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
					}
				}
			}, monitor, 3);
		}
		
		location.releaseSVNProxy(proxy);
		
		if (!monitor.isCanceled()) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					CompareConfiguration cc = new CompareConfiguration();
					cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
					diffPair[0].setSelectedRevision(SVNRevision.BASE);
					final ThreeWayResourceCompareInput compare = new ThreeWayResourceCompareInput(cc, CompareResourcesOperation.this.local, diffPair[0], diffPair[1], localChanges, remoteChanges);
					compare.initialize(monitor);
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (CompareResourcesOperation.this.showInDialog) {
								if (CompareResourcesOperation.this.compareResultOK(compare)) {
									ComparePanel panel = new ComparePanel(compare, CompareResourcesOperation.this.local.getResource());
									DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
									dialog.open();
								}
							}
							else {
								CompareUI.openCompareEditor(compare);
							}
						}
					});
				}
			}, monitor, 3);
		}
	}
	
	protected boolean compareResultOK(CompareEditorInput input) {
		final Shell shell = UIMonitorUtility.getShell();
		
		try {
			SVNTeamUIPlugin.instance().getWorkbench().getProgressService().run(true, true, input);
						
			String message = input.getMessage();
			if (message != null) {
				MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message);
			}
			else if (input.getCompareResult() == null) {
				MessageDialog.openInformation(shell, Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences"));
			}
			else {
				return true;
			}
		} 
		catch (InterruptedException x) {
			// cancelled by user		
		} 
		catch (InvocationTargetException x) {
			MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), x.getTargetException().getMessage());
		}
		return false;
	}

}
