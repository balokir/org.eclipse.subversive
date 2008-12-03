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

package org.eclipse.team.svn.ui.compare;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNLocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Implements three way comparison of resource trees
 * 
 * @author Alexander Gurov
 */
public class ThreeWayResourceCompareInput extends ResourceCompareInput {
	protected ILocalResource local;
	protected Collection<SVNDiffStatus> localChanges;
	protected Collection<SVNDiffStatus> remoteChanges;
	
	public ThreeWayResourceCompareInput(CompareConfiguration configuration, ILocalResource local, IRepositoryResource ancestor, IRepositoryResource remote, Collection<SVNDiffStatus> localChanges, Collection<SVNDiffStatus> remoteChanges) {
		super(configuration);

		this.local = local;
		this.localChanges = localChanges;
		this.remoteChanges = remoteChanges;

		this.rootLeft = SVNRemoteStorage.instance().asRepositoryResource(this.local.getResource());
		this.rootLeft.setSelectedRevision(SVNRevision.WORKING);
		this.rootAncestor = ancestor;
		this.rootRight = remote;
	}
	
	protected void fillMenu(IMenuManager manager, TreeSelection selection) {
		final CompareNode selectedNode = (CompareNode)selection.getFirstElement();
		Action tAction = null;
		boolean propertyComparisonAllowed = true;
		if ((selectedNode.getKind() & Differencer.DIRECTION_MASK) == Differencer.CONFLICTING) {
			propertyComparisonAllowed = (selectedNode.getKind() & Differencer.CHANGE_TYPE_MASK) != Differencer.DELETION;
		}
		else {
			propertyComparisonAllowed = (selectedNode.getKind() & Differencer.CHANGE_TYPE_MASK) == Differencer.CHANGE;
		}
		manager.add(tAction = new Action(SVNUIMessages.SynchronizeActionGroup_CompareProperties){
			public void run() {
				ResourceElement element = (ResourceElement)selectedNode.getLeft();
				SVNLocalResource local = (SVNLocalResource)element.getLocalResource();
				IResource left = local.getResource();
				element = (ResourceElement)selectedNode.getAncestor();
				SVNEntryRevisionReference baseReference = null;
				long baseRevNum = local.getBaseRevision();
				SVNRepositoryResource repoResource = (SVNRepositoryResource)element.getRepositoryResource();
				if (repoResource.getSelectedRevision().getKind() == SVNRevision.BASE.getKind()) {
					baseReference = new SVNEntryRevisionReference(
							FileUtility.getWorkingCopyPath(local.getResource()), null, SVNRevision.BASE);
				}
				else {
					baseReference = new SVNEntryRevisionReference(
							repoResource.getUrl(), repoResource.getPegRevision(), repoResource.getSelectedRevision());
				}
				element = (ResourceElement)selectedNode.getRight();
				SVNEntryRevisionReference rightReference = null;
				repoResource = (SVNRepositoryResource)element.getRepositoryResource();
				if (repoResource.getSelectedRevision().getKind() == SVNRevision.BASE.getKind()) {
					rightReference = new SVNEntryRevisionReference(
							FileUtility.getWorkingCopyPath(local.getResource()), null, SVNRevision.BASE);
				}
				else {
					rightReference = new SVNEntryRevisionReference(
							repoResource.getUrl(), repoResource.getPegRevision(), repoResource.getSelectedRevision());
				}
				ThreeWayPropertyCompareInput input = new ThreeWayPropertyCompareInput(
						new CompareConfiguration(),
						left,
						rightReference,
						baseReference,
						repoResource.getRepositoryLocation(),
						baseRevNum);
				try {
					input.run(new NullProgressMonitor());
					if (input.getCompareResult() == null) {
						MessageDialog dialog = new MessageDialog(
								UIMonitorUtility.getShell(),
								SVNUIMessages.ComparePropsNoDiff_Title,
								null,
								SVNUIMessages.ComparePropsNoDiff_Message,
								MessageDialog.INFORMATION,
								new String [] {IDialogConstants.OK_LABEL},
								0);
						dialog.open();
					}
					else {
						PropertyComparePanel panel = new PropertyComparePanel(input, true);
						DefaultDialog dlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
						dlg.open();
					}
				}
				catch (Exception ex) {
					UILoggedOperation.reportError("Compare Properties Operation", ex);
				}
			}
		});
		tAction.setEnabled(propertyComparisonAllowed && selection.size() == 1);
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
		Map<String, SVNDiffStatus> localChanges = new HashMap<String, SVNDiffStatus>();
		Map<String, SVNDiffStatus> remoteChanges = new HashMap<String, SVNDiffStatus>();
		HashSet<String> allChangesSet = new HashSet<String>();
		for (Iterator<SVNDiffStatus> it = this.localChanges.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			SVNDiffStatus status = it.next();
			allChangesSet.add(status.pathPrev);
			localChanges.put(status.pathPrev, status);
		}
		for (Iterator<SVNDiffStatus> it = this.remoteChanges.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			SVNDiffStatus status = it.next();
			String localPath = this.getLocalPath(SVNUtility.decodeURL(status.pathPrev), this.rootAncestor);
			allChangesSet.add(localPath);
			remoteChanges.put(localPath, status);
		}
		String []allChanges = allChangesSet.toArray(new String[allChangesSet.size()]);
		Arrays.sort(allChanges);
		HashMap path2node = new HashMap();
		
		String message = SVNUIMessages.ResourceCompareInput_CheckingDelta;
		for (int i = 0; i < allChanges.length && !monitor.isCanceled(); i++) {
			monitor.subTask(BaseMessages.format(message, new Object[] {allChanges[i]}));
			this.makeBranch(allChanges[i], localChanges.get(allChanges[i]), remoteChanges.get(allChanges[i]), path2node, monitor);
			ProgressMonitorUtility.progress(monitor, i, allChanges.length);
		}
		
		if (!monitor.isCanceled()) {
			this.findRootNode(path2node, this.rootLeft, monitor);
		}
		
		super.initialize(monitor);
	}
	
	protected void makeBranch(String localPath, SVNDiffStatus stLocal, SVNDiffStatus stRemote, final Map path2node, final IProgressMonitor monitor) throws Exception {
		// 1) take local statuses
		int nodeKind = stLocal == null ? this.getNodeKind(stRemote) : this.getNodeKind(stLocal);
		ILocalResource local = this.getLocalResource(localPath, nodeKind == SVNEntry.Kind.FILE);
		// 2) skip all ignored resources that does not have real remote variants
		if ((stRemote != null || !IStateFilter.SF_IGNORED.accept(local)) && !path2node.containsKey(new Path(SVNRemoteStorage.instance().asRepositoryResource(local.getResource()).getUrl()))) {
			if (local.isCopied() && IStateFilter.SF_ADDED.accept(local) && !local.getResource().equals(this.local.getResource())) {
				// 3) if node is moved and is not a root node traverse all children
				FileUtility.checkForResourcesPresenceRecursive(new IResource[] {local.getResource()}, new IStateFilter.AbstractStateFilter() {
					protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
						// do not traverse through ignored resources
						return !IStateFilter.SF_IGNORED.accept(resource, state, mask) && !IStateFilter.SF_DELETED.accept(resource, state, mask);
					}
					protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
						// 4) for each found children create locally "added" node
						if (!IStateFilter.SF_DELETED.accept(resource, state, mask)) {
							local = this.takeLocal(local, resource);
							String path = FileUtility.getWorkingCopyPath(resource);
							SVNDiffStatus stLocal = new SVNDiffStatus(path, path, resource.getType() == IResource.FILE ? SVNEntry.Kind.FILE : SVNEntry.Kind.DIR, SVNEntryStatus.Kind.ADDED, SVNEntryStatus.Kind.NONE);
							try {
								CompareNode node = ThreeWayResourceCompareInput.this.makeNode(local, stLocal, null, path2node, monitor);
								if (node != null) {
									IRepositoryResource remote = ((ResourceElement)node.getLeft()).getRepositoryResource();
									path2node.put(new Path(remote.getUrl()), node);
								}
							}
							catch (RuntimeException ex) {
								throw ex;
							}
							catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						}
						else {
							path2node.put(new Path(SVNRemoteStorage.instance().asRepositoryResource(resource).getUrl()), null);
						}
						return false;
					}
				});
			}
			else {
				// 3) create node
				CompareNode node = this.makeNode(local, stLocal, stRemote, path2node, monitor);
				if (node != null) {
					IRepositoryResource resource = ((ResourceElement)node.getLeft()).getRepositoryResource();
					path2node.put(new Path(resource.getUrl()), node);
				}
			}
		}
	}
	
	protected CompareNode makeNode(ILocalResource local, SVNDiffStatus stLocal, SVNDiffStatus stRemote, Map path2node, IProgressMonitor monitor) throws Exception {
		int localNodeKind = local instanceof ILocalFile ? SVNEntry.Kind.FILE : SVNEntry.Kind.DIR;
		int remoteNodeKind = stRemote == null ? localNodeKind : this.getNodeKind(stRemote);
		
		boolean useOriginator = this.local.isCopied() && (stLocal != null && stLocal.textStatus != SVNEntryStatus.Kind.ADDED || local.getResource().equals(this.local.getResource()));
		IRepositoryResource []entries = this.getRepositoryEntries(local, remoteNodeKind, stLocal, stRemote);
		
		IRepositoryResource left = entries[0];
		IRepositoryResource ancestor = entries[1];
		IRepositoryResource right = entries[2];
		
		if (!right.exists() && IStateFilter.SF_NOTEXISTS.accept(local)) {
			return null;
		}
		
		int statusLeft = stLocal == null ? SVNEntryStatus.Kind.NORMAL : (stLocal.textStatus == SVNEntryStatus.Kind.NORMAL ? stLocal.propStatus : stLocal.textStatus);
		if (statusLeft == SVNEntryStatus.Kind.DELETED && localNodeKind == SVNEntry.Kind.FILE && new File(FileUtility.getWorkingCopyPath(local.getResource())).exists()) {
			statusLeft = SVNEntryStatus.Kind.REPLACED;
		}
		int fictiveStatusRight = useOriginator || statusLeft != SVNEntryStatus.Kind.ADDED && statusLeft != SVNEntryStatus.Kind.IGNORED && statusLeft != SVNEntryStatus.Kind.NONE && statusLeft != SVNEntryStatus.Kind.UNVERSIONED ? SVNEntryStatus.Kind.NORMAL :  SVNEntryStatus.Kind.NONE;
		int statusRight = stRemote != null ? (stRemote.textStatus == SVNEntryStatus.Kind.NORMAL ? stRemote.propStatus : stRemote.textStatus) : fictiveStatusRight;
		
		// skip resources that already up-to-date: only in case if URL's are same
		if (stRemote != null && this.rootRight.getUrl().equals(this.rootAncestor.getUrl())) {
			if (this.rootRight.getSelectedRevision().getKind() == Kind.NUMBER && this.local.getRevision() >= ((SVNRevision.Number)this.rootRight.getSelectedRevision()).getNumber()) {
				if (!local.getResource().exists() && statusRight == SVNEntryStatus.Kind.DELETED || statusRight != SVNEntryStatus.Kind.DELETED && local.getRevision() == right.getRevision()) {
					return null;
				}
			}
			else if (local.getRevision() == right.getRevision()) {
				if (stLocal == null) {
					return null;
				}
				stRemote = null;
				statusRight = fictiveStatusRight;
			}
		}
		
		int diffKindLeft = ResourceCompareInput.getDiffKind(statusLeft, stLocal == null ? SVNEntryStatus.Kind.NONE : stLocal.propStatus);
		if (diffKindLeft != Differencer.NO_CHANGE) {
			diffKindLeft |= Differencer.LEFT;
		}
		int diffKindRight = ResourceCompareInput.getDiffKind(statusRight, stRemote == null ? SVNEntryStatus.Kind.NONE : stRemote.propStatus);
		if (diffKindRight != Differencer.NO_CHANGE) {
			diffKindRight |= Differencer.RIGHT;
		}
		
		if ((diffKindRight & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION || (diffKindLeft & Differencer.CHANGE_TYPE_MASK) == Differencer.DELETION) {
			left.setSelectedRevision(SVNRevision.INVALID_REVISION);
			ancestor.setSelectedRevision(SVNRevision.INVALID_REVISION);
		}
		if ((diffKindRight & Differencer.CHANGE_TYPE_MASK) == Differencer.DELETION || (diffKindLeft & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) {
			right.setSelectedRevision(SVNRevision.INVALID_REVISION);
		}
		
		IDiffContainer parent = this.getParentCompareNode(left, path2node);
		return new CompareNode(parent, diffKindLeft | diffKindRight, local, left, ancestor, right, statusLeft, statusRight);
	}
	
	protected ILocalResource getLocalResource(String path, boolean isFile) {
		if (path == null) {
			return SVNRemoteStorage.instance().asLocalResource(null);
		}
		IProject project = this.local.getResource().getProject();
		String relative = path.substring(FileUtility.getWorkingCopyPath(project).length());
		IResource resource = relative.length() == 0 ? project : project.findMember(relative);
		
		if (resource == null) {
			resource = isFile ? project.getFile(relative) : project.getFolder(relative);
		}
		
		return SVNRemoteStorage.instance().asLocalResourceAccessible(resource);
	}
	
	protected String getLocalPath(String url, IRepositoryResource base) {
		String baseUrl = base.getUrl();
		if (url.length() < baseUrl.length()) {
			return null;
		}
		String delta = url.substring(baseUrl.length());
		String projectPath = FileUtility.getWorkingCopyPath(this.local.getResource());
		return projectPath + delta;
	}
	
	protected IDiffContainer makeStubNode(IDiffContainer parent, IRepositoryResource node) {
		ILocalResource local = this.getLocalResource(this.getLocalPath(node.getUrl(), this.rootLeft), false);
		IRepositoryResource ancestor = node;
		IRepositoryResource remote = node;
		if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
			IRepositoryResource []entries = this.getRepositoryEntries(local, SVNEntry.Kind.DIR, null, null);
			ancestor = entries[1];
			remote = entries[2];
		}
		return new CompareNode(parent, Differencer.NO_CHANGE, local, node, ancestor, remote, SVNEntryStatus.Kind.NORMAL, SVNEntryStatus.Kind.NORMAL);
	}
	
	protected IRepositoryResource []getRepositoryEntries(ILocalResource local, int remoteNodeKind, SVNDiffStatus stLocal, SVNDiffStatus stRemote) {
		IRepositoryLocation location = this.rootLeft.getRepositoryLocation();
		
		IRepositoryResource left = SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
		left.setSelectedRevision(SVNRevision.WORKING);
		left.setPegRevision(null);
		
		boolean useOriginator = this.local.isCopied() && (stLocal != null && stLocal.textStatus != SVNEntryStatus.Kind.ADDED || local.getResource().equals(this.local.getResource()));
		IRepositoryResource ancestor = useOriginator ? SVNUtility.getCopiedFrom(local.getResource()) : SVNUtility.copyOf(left);
		IRepositoryResource right = useOriginator ? SVNUtility.getCopiedFrom(local.getResource()) : SVNUtility.copyOf(left);
		if (stRemote != null) {
			ancestor = this.createResourceFor(location, remoteNodeKind, SVNUtility.decodeURL(stRemote.pathPrev));
			right = this.createResourceFor(location, remoteNodeKind, SVNUtility.decodeURL(stRemote.pathNext));
		}
		else if (!this.rootLeft.getUrl().equals(this.rootRight.getUrl()) && left.getUrl().length() >= this.rootLeft.getUrl().length()) {
			String delta = left.getUrl().substring(this.rootLeft.getUrl().length());
			ancestor = this.createResourceFor(location, remoteNodeKind, this.rootAncestor.getUrl() + delta);
			right = this.createResourceFor(location, remoteNodeKind, this.rootRight.getUrl() + delta);
		}
		ancestor.setSelectedRevision(SVNRevision.BASE);
		ancestor.setPegRevision(null);
		right.setPegRevision(this.rootRight.getPegRevision());
		right.setSelectedRevision(this.rootRight.getSelectedRevision());
		
		return new IRepositoryResource[] {left, ancestor, right};
	}
	
	protected boolean isThreeWay() {
		return true;
	}
	
	protected String getLeftLabel() throws Exception {
		ResourceElement element = this.getLeftResourceElement();
		return element.getLocalResource().getResource().getFullPath().toString().substring(1) + " [" + this.getRevisionPart(element) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected String getRevisionPart(ResourceElement element) throws Exception {
		IRepositoryResource resource = element.getRepositoryResource();
		SVNRevision selected = resource.getSelectedRevision();
		int kind = selected.getKind();
		
		if (kind == Kind.WORKING) {
			return SVNUIMessages.ResourceCompareInput_LocalSign;
		}
		else if (kind == Kind.BASE) {
			long revision = element.getLocalResource().getRevision();
			if (revision == SVNRevision.INVALID_REVISION_NUMBER) {
				return SVNUIMessages.ResourceCompareInput_ResourceIsNotAvailable;
			}
			return SVNUIMessages.format(SVNUIMessages.ResourceCompareInput_BaseSign, new String[] {String.valueOf(revision)});
		}

		return super.getRevisionPart(element);
	}
	
	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config) {
			public void setLabelProvider(IBaseLabelProvider labelProvider) {
				super.setLabelProvider(new LabelProviderWrapper((ILabelProvider)labelProvider) {
					public Image getImage(Object element) {
						if (element instanceof CompareNode && (((CompareNode)element).getLocalChangeType() == SVNEntryStatus.Kind.REPLACED || ((CompareNode)element).getRemoteChangeType() == SVNEntryStatus.Kind.REPLACED)) {
							Image image = this.images.get(element);
							if (image == null) {
								OverlayedImageDescriptor imageDescriptor = null;
								int direction = ((CompareNode)element).getKind() & Differencer.DIRECTION_MASK;
								if (direction == Differencer.LEFT) {
									imageDescriptor = new OverlayedImageDescriptor(this.baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_out.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V); //$NON-NLS-1$
								}
								else if (direction == Differencer.RIGHT) {
									imageDescriptor = new OverlayedImageDescriptor(this.baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_in.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V); //$NON-NLS-1$
								}
								else {
									imageDescriptor = new OverlayedImageDescriptor(this.baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_conf.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V); //$NON-NLS-1$
								}
								this.images.put(element,image = imageDescriptor.createImage());
							}
							return image;
						}
						return super.getImage(element);
					}
				});
			}
		};
	}
	
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter)) {
			// disallow auto-flush of editor content
			return null;
		}
		return super.getAdapter(adapter);
	}
	
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		
		if (this.root != null) {
			try {
				pm.beginTask(SVNUIMessages.ThreeWayResourceCompareInput_SaveChanges, -1);
				this.saveChanges((CompareNode)this.root, pm);
			}
			finally {
				pm.done();
			}
		}
	}
	
	protected void saveChanges(CompareNode node, IProgressMonitor pm) throws CoreException {
		ResourceElement left = (ResourceElement)node.getLeft();
		if (left.isEditable() && left.isDirty()) {
			left.commit(pm);
		}
		IDiffElement []children = node.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length && !pm.isCanceled(); i++) {
				this.saveChanges((CompareNode)children[i], pm);
			}
		}
	}
	
	protected class CompareNode extends BaseCompareNode {
		protected int localChangeType;
		protected int remoteChangeType;
		
		public CompareNode(IDiffContainer parent, int kind, ILocalResource workingVersion, IRepositoryResource local, IRepositoryResource ancestor, IRepositoryResource remote, int localChangeType, int remoteChangeType) {
			super(parent, kind);
			
			this.localChangeType = localChangeType;
			this.remoteChangeType = remoteChangeType;
			
			boolean useOriginator = ThreeWayResourceCompareInput.this.local.isCopied() && (localChangeType != SVNEntryStatus.Kind.ADDED || workingVersion.getResource().equals(ThreeWayResourceCompareInput.this.local.getResource()));
			ResourceElement leftElt = new ResourceElement(local, workingVersion, localChangeType != SVNEntryStatus.Kind.NONE && localChangeType != SVNEntryStatus.Kind.DELETED);
			leftElt.setEditable(local instanceof IRepositoryFile);
			this.setLeft(leftElt);
			this.setAncestor(new ResourceElement(ancestor, workingVersion, useOriginator || localChangeType != SVNEntryStatus.Kind.UNVERSIONED && localChangeType != SVNEntryStatus.Kind.ADDED && remoteChangeType != SVNEntryStatus.Kind.ADDED));
			this.setRight(new ResourceElement(remote, workingVersion, remoteChangeType != SVNEntryStatus.Kind.DELETED && remoteChangeType != SVNEntryStatus.Kind.NONE));
		}

		public int getLocalChangeType() {
			return this.localChangeType;
		}
		
		public int getRemoteChangeType() {
			return this.remoteChangeType;
		}
		
	}
	
}
