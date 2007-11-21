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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.ISVNClient.Depth;
import org.eclipse.team.svn.core.client.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
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
public class CompareResourcesOperation extends AbstractNonLockingOperation {
	protected IResource resource;
	protected SVNRevision revision;
	protected SVNRevision pegRevision;
	protected boolean useDialog = false;
	
	public CompareResourcesOperation(IResource resource, SVNRevision revision, SVNRevision pegRevision) {
		super("Operation.CompareLocal");
		this.resource = resource;
		this.revision = revision;
		this.pegRevision = pegRevision;
	}
	
	public CompareResourcesOperation(IResource resource, SVNRevision revision, SVNRevision pegRevision, boolean useDialog) {
		this(resource, revision, pegRevision);
		this.useDialog = useDialog;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		
		final IRepositoryResource remoteBase = storage.asRepositoryResource(this.resource);
		final ILocalResource local = storage.asLocalResource(this.resource);
		if (local == null) {
			return;
		}

		final SVNEntryStatus localChanges[][] = new SVNEntryStatus[1][];
		final SVNEntryStatus remoteChanges[][] = new SVNEntryStatus[1][];
		
		final IRepositoryLocation location = remoteBase.getRepositoryLocation();
		final ISVNClient proxy = location.acquireSVNProxy();
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				localChanges[0] = SVNUtility.status(proxy, 
						FileUtility.getWorkingCopyPath(CompareResourcesOperation.this.resource),
						Depth.INFINITY, false, false, false, true,
						new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
			}
		}, monitor, 3);
		if (localChanges[0] != null && !monitor.isCanceled()) {
			// Remove all folders that are mapped to external resources
			ArrayList changesList = new ArrayList();
			for (int i = 0; i < localChanges[0].length; i++) {
				if (localChanges[0][i].textStatus != org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.EXTERNAL) {
					changesList.add(localChanges[0][i]);
				}
			}
			localChanges[0] = (SVNEntryStatus[])changesList.toArray(new SVNEntryStatus[changesList.size()]);
			
			if (this.revision.getKind() == Kind.HEAD || this.revision.getKind() == Kind.NUMBER) {
				// all revisions should be set here because unversioned resources can be compared
				IRepositoryResource tmpRight = local.isCopied() ? this.getRepositoryResourceFor(this.resource, SVNUtility.decodeURL(localChanges[0][0].urlCopiedFrom), location) : storage.asRepositoryResource(this.resource);
				tmpRight.setSelectedRevision(this.revision);
				tmpRight.setPegRevision(this.pegRevision);
				LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource [] {tmpRight}, true);
				ProgressMonitorUtility.doTaskExternal(op, monitor);
				final IRepositoryResource remoteRight = op.getRepositoryResources() [0];
				remoteBase.setPegRevision(this.pegRevision);
				// status order may be inconsistent for next lines
				SVNUtility.reorder(localChanges[0], true);
				if (local.isCopied()) {
					remoteBase.setSelectedRevision(remoteRight.getPegRevision());
				}
				else if (local.getRevision() != SVNRevision.INVALID_REVISION_NUMBER) {
					remoteBase.setSelectedRevision(SVNRevision.fromNumber(local.getRevision()));
				}
				
				remoteRight.setSelectedRevision(this.revision);
				final String baseUrl = local.isCopied() ? remoteRight.getUrl() : remoteBase.getUrl();
					
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						SVNEntryRevisionReference ref1 = new SVNEntryRevisionReference(SVNUtility.encodeURL(baseUrl), remoteBase.getPegRevision(), remoteBase.getSelectedRevision());
						SVNEntryRevisionReference ref2 = SVNUtility.getEntryRevisionReference(remoteRight);
						if (SVNUtility.useSingleReferenceSignature(ref1, ref2)) {
							remoteChanges[0] = SVNUtility.diffStatus(proxy, ref1, ref1.revision, ref2.revision, Depth.INFINITY, false, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
						}
						else {
							remoteChanges[0] = SVNUtility.diffStatus(proxy, ref1, ref2, Depth.INFINITY, false, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
						}
					}
				}, monitor, 3);
			}
			else {
				remoteChanges[0] = new SVNEntryStatus[0];
			}
		}
		
		location.releaseSVNProxy(proxy);
		
		if (remoteChanges[0] != null && !monitor.isCanceled()) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					CompareConfiguration cc = new CompareConfiguration();
					cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
					final ThreeWayResourceCompareInput compare = new ThreeWayResourceCompareInput(cc, CompareResourcesOperation.this.resource, CompareResourcesOperation.this.revision, CompareResourcesOperation.this.pegRevision, localChanges[0], remoteChanges[0]);
					compare.initialize(monitor);
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (CompareResourcesOperation.this.useDialog) {
								if (CompareResourcesOperation.this.compareResultOK(compare)) {
									ComparePanel panel = new ComparePanel(compare, CompareResourcesOperation.this.resource);
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
	
	protected IRepositoryResource getRepositoryResourceFor(IResource resource, String url, IRepositoryLocation location) {
		return resource instanceof IFile ? (IRepositoryResource)location.asRepositoryFile(url, false) : location.asRepositoryContainer(url, false);
	}
	
	protected boolean compareResultOK(CompareEditorInput input) {
		final Shell shell = UIMonitorUtility.getShell();
		
		try {
			SVNTeamUIPlugin.instance().getWorkbench().getProgressService().run(true, true, input);
						
			String message= input.getMessage();
			if (message != null) {
				MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message);
				return false;
			}
			
			if (input.getCompareResult() == null) {
				MessageDialog.openInformation(shell, Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences"));
				return false;
			}
			
			return true;

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
