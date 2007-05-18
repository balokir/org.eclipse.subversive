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
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.StatusKind;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
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
	protected Revision revision;
	protected Revision pegRevision;
	protected boolean useDialog = false;
	
	public CompareResourcesOperation(IResource resource, Revision revision, Revision pegRevision) {
		super("Operation.CompareLocal");
		this.resource = resource;
		this.revision = revision;
		this.pegRevision = pegRevision;
	}
	
	public CompareResourcesOperation(IResource resource, Revision revision, Revision pegRevision, boolean useDialog) {
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

		final Status localChanges[][] = new Status[1][];
		final Status remoteChanges[][] = new Status[1][];
		
		final IRepositoryLocation location = remoteBase.getRepositoryLocation();
		final ISVNClientWrapper proxy = location.acquireSVNProxy();
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				localChanges[0] = proxy.status(
						FileUtility.getWorkingCopyPath(CompareResourcesOperation.this.resource),
						true, false, false, false, false, true,
						new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
			}
		}, monitor, 3);
		if (localChanges[0] != null && !monitor.isCanceled()) {
			// Remove all folders that are mapped to external resources
			ArrayList changesList = new ArrayList();
			for (int i = 0; i < localChanges[0].length; i++) {
				if (localChanges[0][i].textStatus != StatusKind.external) {
					changesList.add(localChanges[0][i]);
				}
			}
			localChanges[0] = (Status[])changesList.toArray(new Status[changesList.size()]);
			
			if (this.revision.getKind() == Revision.Kind.head || this.revision.getKind() == Revision.Kind.number) {
				// all revisions should be set here because unversioned resources can be compared
				final IRepositoryResource remoteRight = local.isCopied() ? this.getRepositoryResourceFor(this.resource, localChanges[0][0].urlCopiedFrom, location) : storage.asRepositoryResource(this.resource);
				remoteRight.setPegRevision(this.pegRevision);
				remoteBase.setPegRevision(this.pegRevision);
				// status order may be inconsistent for next lines
				SVNUtility.reorder(localChanges[0], true);
				if (local.isCopied()) {
					remoteRight.setPegRevision(Revision.getInstance(localChanges[0][0].revisionCopiedFrom));
					remoteBase.setSelectedRevision(remoteRight.getPegRevision());
				}
				else if (local.getRevision() != Revision.SVN_INVALID_REVNUM) {
					remoteBase.setSelectedRevision(Revision.getInstance(local.getRevision()));
				}
				
				remoteRight.setSelectedRevision(this.revision);
				final String baseUrl = local.isCopied() ? remoteRight.getUrl() : remoteBase.getUrl(); 
					
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						remoteChanges[0] = proxy.diffStatus(
								SVNUtility.encodeURL(baseUrl), remoteBase.getPegRevision(), remoteBase.getSelectedRevision(),
								SVNUtility.encodeURL(remoteRight.getUrl()), remoteRight.getPegRevision(), remoteRight.getSelectedRevision(), 
								true, false, new SVNProgressMonitor(CompareResourcesOperation.this, monitor, null, false));
					}
				}, monitor, 3);
			}
			else {
				remoteChanges[0] = new Status[0];
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
