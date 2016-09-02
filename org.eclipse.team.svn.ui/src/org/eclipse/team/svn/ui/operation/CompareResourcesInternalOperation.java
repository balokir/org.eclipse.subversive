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
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNDiffStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
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
		
	public CompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse, boolean showInDialog) {
		super("Operation_CompareLocal", SVNUIMessages.class); //$NON-NLS-1$
		this.local = local;
		this.ancestor = local.isCopied() ? SVNUtility.getCopiedFrom(local) : SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
		this.ancestor.setSelectedRevision(local.getBaseRevision() != SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.fromNumber(local.getBaseRevision()) : SVNRevision.INVALID_REVISION);
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
		final ArrayList<SVNDiffStatus> localChanges = new ArrayList<SVNDiffStatus>();
		final ArrayList<SVNDiffStatus> remoteChanges = new ArrayList<SVNDiffStatus>();
		
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.local.getResource());
		ISVNConnector proxy = location.acquireSVNProxy();

		try {
			if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_7_x ||
				this.remote.getSelectedRevision() == SVNRevision.BASE) {
				this.fetchStatuses17(proxy, localChanges, remoteChanges, monitor);
			}
			else {
				this.fetchStatuses18(proxy, localChanges, remoteChanges, monitor);
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				CompareConfiguration cc = new CompareConfiguration();
				cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
				final ThreeWayResourceCompareInput compare = new ThreeWayResourceCompareInput(cc, CompareResourcesInternalOperation.this.local, CompareResourcesInternalOperation.this.ancestor, CompareResourcesInternalOperation.this.remote, localChanges, remoteChanges);
				compare.setForceId(CompareResourcesInternalOperation.this.forceId);
				compare.initialize(monitor);
				if (!monitor.isCanceled())
				{
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
			}
		}, monitor, 100, 50);
	}

	protected void fetchStatuses17(final ISVNConnector proxy, final ArrayList<SVNDiffStatus> localChanges, final ArrayList<SVNDiffStatus> remoteChanges, final IProgressMonitor monitor) throws Exception {
		final HashMap<IResource, Long> resourcesWithChanges = new HashMap<IResource, Long>();
		final IContainer compareRoot = 
			this.local instanceof ILocalFolder ? (IContainer)this.local.getResource() : this.local.getResource().getParent();
		IRepositoryResource resource = SVNRemoteStorage.instance().asRepositoryResource(CompareResourcesInternalOperation.this.local.getResource());
		final long cmpTargetRevision = resource.exists() ? resource.getRevision() : SVNRevision.INVALID_REVISION_NUMBER;
		final LinkedHashSet<Long> revisions = new LinkedHashSet<Long>();
		
		if (cmpTargetRevision != SVNRevision.INVALID_REVISION_NUMBER) {
			revisions.add(cmpTargetRevision);
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					final String rootPath = FileUtility.getWorkingCopyPath(CompareResourcesInternalOperation.this.local.getResource());
					proxy.status(rootPath, SVNDepth.INFINITY, ISVNConnector.Options.IGNORE_EXTERNALS | ISVNConnector.Options.SERVER_SIDE, null, new ISVNEntryStatusCallback() {
						public void next(SVNChangeStatus status) {
							IPath tPath = new Path(status.path.substring(rootPath.length()));
							IResource resource = compareRoot.findMember(tPath);
							if (resource == null) {
								resource = status.nodeKind == SVNEntry.Kind.FILE ? compareRoot.getFile(tPath) : compareRoot.getFolder(tPath);
							}
							String textStatus = SVNRemoteStorage.getTextStatusString(status.propStatus, status.textStatus, false);
							if (IStateFilter.SF_ANY_CHANGE.accept(resource, textStatus, 0) || status.propStatus == SVNEntryStatus.Kind.MODIFIED) {
								localChanges.add(new SVNDiffStatus(status.path, status.path, status.nodeKind, status.textStatus, status.propStatus));
							}
							textStatus = SVNRemoteStorage.getTextStatusString(status.repositoryPropStatus, status.repositoryTextStatus, true);
							if ((IStateFilter.SF_ANY_CHANGE.accept(resource, textStatus, 0) || status.repositoryTextStatus == SVNEntryStatus.Kind.MODIFIED) && 
								status.revision != cmpTargetRevision) {
								resourcesWithChanges.put(resource, status.revision);
 								revisions.add(status.revision);
							}
						}
					}, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
				}
			}, monitor, 100, 10);
		}
		else
		{	// compare new unversioned files
			String path = FileUtility.getWorkingCopyPath(this.local.getResource());
			localChanges.add(new SVNDiffStatus(path, path, this.local.getResource().getType() == IResource.FILE ? SVNEntry.Kind.FILE : SVNEntry.Kind.DIR, SVNEntryStatus.Kind.UNVERSIONED, SVNEntryStatus.Kind.NORMAL));
		}
		
		if (this.remote.getSelectedRevision() != SVNRevision.BASE) {
			for (final long revision : revisions) {
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						IRepositoryResource resource = SVNRemoteStorage.instance().asRepositoryResource(CompareResourcesInternalOperation.this.local.getResource());
						resource.setSelectedRevision(SVNRevision.fromNumber(revision));
						final SVNEntryRevisionReference refPrev = SVNUtility.getEntryRevisionReference(resource);
						final SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(CompareResourcesInternalOperation.this.remote);
						final String prevRootURL = resource.getUrl();
						proxy.diffStatusTwo(refPrev, refNext, SVNDepth.INFINITY, ISVNConnector.Options.NONE, null, new ISVNDiffStatusCallback() {
							public void next(SVNDiffStatus status) {
								IPath tPath = new Path(status.pathPrev.substring(prevRootURL.length()));
								IResource resource = compareRoot.findMember(tPath);
								if (resource == null) {
									resource = status.nodeKind == SVNEntry.Kind.FILE ? compareRoot.getFile(tPath) : compareRoot.getFolder(tPath);
								}
								Long rev = resourcesWithChanges.get(resource);
								if (rev == null || rev == revision) {
									String pathPrev = CompareResourcesInternalOperation.this.ancestor.getUrl() + status.pathNext.substring(refNext.path.length());
									remoteChanges.add(new SVNDiffStatus(pathPrev, status.pathNext, status.nodeKind, status.textStatus, status.propStatus));
								}
							}
						}, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
					}
				}, monitor, 100, 40 / revisions.size());
			}
		}
	}
	
	protected void fetchStatuses18(final ISVNConnector proxy, final ArrayList<SVNDiffStatus> localChanges, final ArrayList<SVNDiffStatus> remoteChanges, final IProgressMonitor monitor) {
		final IContainer compareRoot = 
			CompareResourcesInternalOperation.this.local instanceof ILocalFolder ? 
			(IContainer)CompareResourcesInternalOperation.this.local.getResource() : 
			CompareResourcesInternalOperation.this.local.getResource().getParent();
			
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				String rootPath = 
						FileUtility.getWorkingCopyPath(CompareResourcesInternalOperation.this.local.getResource());
				final String searchPath = 
					CompareResourcesInternalOperation.this.local.getResource().getType() == IResource.FILE ? 
					FileUtility.getWorkingCopyPath(CompareResourcesInternalOperation.this.local.getResource().getParent()) :
					rootPath;
				proxy.status(rootPath, SVNDepth.INFINITY, ISVNConnector.Options.IGNORE_EXTERNALS | ISVNConnector.Options.SERVER_SIDE, null, new ISVNEntryStatusCallback() {
					public void next(SVNChangeStatus status) {
						IPath tPath = new Path(status.path.substring(searchPath.length()));
						IResource resource = compareRoot.findMember(tPath);
						if (resource == null) {
							resource = status.nodeKind == SVNEntry.Kind.FILE ? compareRoot.getFile(tPath) : compareRoot.getFolder(tPath);
						}
						String textStatus = SVNRemoteStorage.getTextStatusString(status.propStatus, status.textStatus, false);
						if (IStateFilter.SF_ANY_CHANGE.accept(resource, textStatus, 0) || status.propStatus == SVNEntryStatus.Kind.MODIFIED) {
							localChanges.add(new SVNDiffStatus(status.path, status.path, status.nodeKind, status.textStatus, status.propStatus));
						}
					}
				}, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
			}
		}, monitor, 100, 10);
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				final IPath rootPath = FileUtility.getResourcePath(compareRoot);

				SVNEntryRevisionReference refPrev = new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(CompareResourcesInternalOperation.this.local.getResource()), null, SVNRevision.WORKING);
				final SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(CompareResourcesInternalOperation.this.remote);
				// does not work with BASE working copy revision (not implemented yet exception)
				proxy.diffStatusTwo(refPrev, refNext, SVNDepth.INFINITY, ISVNConnector.Options.NONE, null, new ISVNDiffStatusCallback() {
					public void next(SVNDiffStatus status) {
						IPath tPath = new Path(status.pathPrev);
						tPath = tPath.removeFirstSegments(rootPath.segmentCount());
						IResource resource = compareRoot.findMember(tPath);
						if (resource == null) {
							resource = status.nodeKind == SVNEntry.Kind.FILE ? compareRoot.getFile(tPath) : compareRoot.getFolder(tPath);
						}
						ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
						if (!IStateFilter.SF_ANY_CHANGE.accept(local) || IStateFilter.SF_NOTEXISTS.accept(local)) {
							// it seems the status is calculated relatively to the working copy, so deletion and addition changes should actually be reversed
							SVNDiffStatus.Kind change = 
								status.textStatus == SVNDiffStatus.Kind.ADDED ? 
								SVNDiffStatus.Kind.DELETED : 
								(status.textStatus == SVNDiffStatus.Kind.DELETED ? SVNDiffStatus.Kind.ADDED : status.textStatus);
							// TODO could there be a case when relative paths are reported? If so - looks like a bug to me...
							String pathPrev = status.pathNext.startsWith(refNext.path) ? status.pathNext.substring(refNext.path.length()) : status.pathNext;
							pathPrev = CompareResourcesInternalOperation.this.ancestor.getUrl() + pathPrev;
							remoteChanges.add(new SVNDiffStatus(pathPrev, status.pathNext, status.nodeKind, change, status.propStatus));
						}
					}
				}, new SVNProgressMonitor(CompareResourcesInternalOperation.this, monitor, null, false));
			}
		}, monitor, 100, 40);
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
