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

package org.eclipse.team.svn.ui.compare;

import java.io.File;
import java.text.MessageFormat;
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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;

/**
 * Implements three way comparison of resource trees
 * 
 * @author Alexander Gurov
 */
public class ThreeWayResourceCompareInput extends ResourceCompareInput {
	protected IResource local;
	protected Collection<SVNDiffStatus> localChanges;
	protected Collection<SVNDiffStatus> remoteChanges;
	
	protected Map newUrl2OldUrl;
	protected boolean compareWithCopySource;
	protected IRepositoryResource copiedFrom;
	
	public ThreeWayResourceCompareInput(CompareConfiguration configuration, IResource local, IRepositoryResource remote, Collection<SVNDiffStatus> localChanges, Collection<SVNDiffStatus> remoteChanges) {
		super(configuration);

		this.local = local;
		this.localChanges = localChanges;
		this.remoteChanges = remoteChanges;

		IRemoteStorage storage = SVNRemoteStorage.instance();
		this.rootLeft = storage.asRepositoryResource(this.local);
		this.rootLeft.setSelectedRevision(SVNRevision.WORKING);
		this.rootAncestor = storage.asRepositoryResource(this.local);
		this.rootAncestor.setSelectedRevision(SVNRevision.BASE);
		this.rootRight = remote == null ? this.rootAncestor : remote;
		
		this.newUrl2OldUrl = new HashMap();
		this.copiedFrom = SVNUtility.getCopiedFrom(this.local);
		this.compareWithCopySource = this.copiedFrom != null;
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
		Map<String, SVNDiffStatus> localChanges = new HashMap<String, SVNDiffStatus>();
		Map<String, SVNDiffStatus> remoteChanges = new HashMap<String, SVNDiffStatus>();
		HashSet<String> allChangesSet = new HashSet<String>();
		for (Iterator<SVNDiffStatus> it = this.localChanges.iterator(); it.hasNext(); ) {
			SVNDiffStatus status = it.next();
			String url = this.getUrl(status.pathPrev);
			allChangesSet.add(url);
			localChanges.put(url, status);
		}
		for (Iterator<SVNDiffStatus> it = this.remoteChanges.iterator(); it.hasNext(); ) {
			SVNDiffStatus status = it.next();
			String url = SVNUtility.decodeURL(status.pathPrev);
			allChangesSet.add(url);
			remoteChanges.put(url, status);
		}
		String []allChanges = allChangesSet.toArray(new String[allChangesSet.size()]);
		Arrays.sort(allChanges);
		HashMap path2node = new HashMap();
		
		String message = SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.CheckingDelta");
		for (int i = 0; i < allChanges.length; i++) {
			monitor.subTask(MessageFormat.format(message, new String[] {allChanges[i]}));
			this.makeBranch(allChanges[i], localChanges.get(allChanges[i]), remoteChanges.get(allChanges[i]), path2node, monitor);
			ProgressMonitorUtility.progress(monitor, i, allChanges.length);
		}
		
		this.findRootNode(path2node, this.rootRight, monitor);
		if (this.root == null) {
			this.findRootNode(path2node, this.rootLeft, monitor);
		}
		
		super.initialize(monitor);
	}
	
	protected void makeBranch(String url, SVNDiffStatus stLocal, SVNDiffStatus stRemote, Map path2node, IProgressMonitor monitor) throws Exception {
		// skip all ignored resources that does not have real remote variants
		if (stRemote == null) {
			IProject project = this.local.getProject();
			String relative = stLocal.pathPrev.substring(FileUtility.getWorkingCopyPath(project).length());
			IResource resource = relative.length() == 0 ? project : project.findMember(relative);
			
			ILocalResource local;
			if (resource == null || 
				(local = SVNRemoteStorage.instance().asLocalResource(resource)) == null || 
				IStateFilter.SF_IGNORED.accept(resource, local.getStatus(), local.getChangeMask())) {
				return;
			}
		}
		// 1) take local statuses
		// 2) create node
		// 3) if node is moved and is not a root node traverse all children
		// 4) for each found children create locally "added" node
		CompareNode node = this.makeNode(url, stLocal, stRemote, path2node, monitor);
		if (node != null) {
			path2node.put(new Path(url), node);
		}
	}
	
	protected CompareNode makeNode(String localUrl, SVNDiffStatus stLocal, SVNDiffStatus stRemote, Map path2node, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = this.rootLeft.getRepositoryLocation();
		
		int localNodeKind = stLocal == null ? this.getNodeKind(stRemote) : this.getNodeKind(stLocal);
		IRepositoryResource left = this.createResourceFor(location, localNodeKind, localUrl);
		left.setSelectedRevision(SVNRevision.WORKING);
		left.setPegRevision(null);
		ILocalResource local = this.getLocalResourceFor(left);
		
		IRepositoryResource ancestor = this.createResourceFor(location, localNodeKind, localUrl);
		ancestor.setSelectedRevision(SVNRevision.BASE);
		ancestor.setPegRevision(null);

		int rightNodeKind = stRemote == null ? this.getNodeKind(stLocal) : this.getNodeKind(stRemote);
		IRepositoryResource copiedFrom = SVNUtility.getCopiedFrom(local.getResource());
		String rightUrl = stRemote != null ? SVNUtility.decodeURL(stRemote.pathNext) : (this.compareWithCopySource && copiedFrom != null ? copiedFrom.getUrl() : (this.rootRight.getUrl() + localUrl.substring(this.rootLeft.getUrl().length())));
		IRepositoryResource right = this.createResourceFor(location, rightNodeKind, rightUrl);
		right.setPegRevision(this.rootRight.getPegRevision());
		right.setSelectedRevision(this.rootRight.getSelectedRevision());
		
		local = this.getLocalResourceFor(right);
		
		if (right.exists()) {
			LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {right}, true);
			ProgressMonitorUtility.doTaskExternalDefault(op, monitor);
			right = op.getRepositoryResources()[0];
		}
		else if (local == null || IStateFilter.SF_NOTEXISTS.accept(local.getResource(), local.getStatus(), local.getChangeMask())) {
			return null;
		}
		
		int statusLeft = !this.compareWithCopySource & local.isCopied() ? SVNEntryStatus.Kind.ADDED : (stLocal == null ? SVNEntryStatus.Kind.NORMAL : (stLocal.textStatus == SVNEntryStatus.Kind.NORMAL ? stLocal.propStatus : stLocal.textStatus));
		if (statusLeft == SVNEntryStatus.Kind.DELETED && localNodeKind == SVNEntry.Kind.FILE && new File(stLocal.pathPrev).exists()) {
			statusLeft = SVNEntryStatus.Kind.REPLACED;
		}
		int statusRight = stRemote == null ? (statusLeft == SVNEntryStatus.Kind.ADDED || statusLeft == SVNEntryStatus.Kind.IGNORED || statusLeft == SVNEntryStatus.Kind.NONE || statusLeft == SVNEntryStatus.Kind.UNVERSIONED ? SVNEntryStatus.Kind.NONE :  SVNEntryStatus.Kind.NORMAL) : (stRemote.textStatus == SVNEntryStatus.Kind.NORMAL ? stRemote.propStatus : stRemote.textStatus);
		
		// skip resources that already up-to-date
		if (stRemote != null && local != null) {
			ILocalResource tmp = SVNRemoteStorage.instance().asLocalResource(this.local);
			if (this.rootRight.getSelectedRevision().getKind() == Kind.NUMBER && tmp != null && tmp.getRevision() >= ((SVNRevision.Number)this.rootRight.getSelectedRevision()).getNumber()) {
				if (!local.getResource().exists() && statusRight == SVNEntryStatus.Kind.DELETED || statusRight != SVNEntryStatus.Kind.DELETED && local.getRevision() == right.getRevision()) {
					return null;
				}
			}
			else if (local.getRevision() == right.getRevision()) {
				if (stLocal == null) {
					return null;
				}
				else {
					stRemote = null;
					statusRight = statusLeft == SVNEntryStatus.Kind.ADDED || statusLeft == SVNEntryStatus.Kind.IGNORED || statusLeft == SVNEntryStatus.Kind.NONE || statusLeft == SVNEntryStatus.Kind.UNVERSIONED ? SVNEntryStatus.Kind.NONE : SVNEntryStatus.Kind.NORMAL;
				}
			}
		}
		
		this.newUrl2OldUrl.put(right.getUrl(), localUrl);
		
		int diffKindLeft = ResourceCompareInput.getDiffKind(statusLeft, stLocal == null ? SVNEntryStatus.Kind.NONE : stLocal.propStatus, SVNEntryStatus.Kind.NORMAL);
		if (diffKindLeft != Differencer.NO_CHANGE) {
			diffKindLeft |= Differencer.LEFT;
		}
		int diffKindRight = ResourceCompareInput.getDiffKind(statusRight, stRemote == null ? SVNEntryStatus.Kind.NONE : stRemote.propStatus, SVNEntryStatus.Kind.NORMAL);
		if (diffKindRight != Differencer.NO_CHANGE) {
			diffKindRight |= Differencer.RIGHT;
		}
		IDiffContainer parent = this.getParentCompareNode(right, path2node);
		return new CompareNode(parent, diffKindLeft | diffKindRight, left, ancestor, right, statusLeft, statusRight);
	}
	
	protected String getUrl(String localPath) {
		// Try to find resource using provided path.
		IResource resource = this.getResourceForPath(localPath);
		if (resource != null) {
			return SVNRemoteStorage.instance().asRepositoryResource(resource).getUrl();
		}
		// If the resource is not found make up the URL using project URL and relative path
		IProject project = this.local.getProject();
		String relativePath = localPath.substring(FileUtility.getWorkingCopyPath(project).length());
		IRemoteStorage storage = SVNRemoteStorage.instance();
		return storage.asRepositoryResource(project).getUrl() + relativePath;
	}
	
	protected IResource getResourceForPath(String pathString) {
		Path path = new Path(pathString);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = workspaceRoot.findFilesForLocation(path);
		if (files != null && files.length != 0 && files[0] != null) {
			return (IResource)files[0];
		}
		IContainer[] containers = workspaceRoot.findContainersForLocation(path);
		if (containers != null && containers.length != 0 && containers[0] != null) {
			return (IResource)containers[0];
		}
		return null;
	}

	protected IDiffContainer makeStubNode(IDiffContainer parent, IRepositoryResource node) {
		return new CompareNode(parent, Differencer.NO_CHANGE, node, node, node, SVNEntryStatus.Kind.NORMAL, SVNEntryStatus.Kind.NORMAL);
	}
	
	protected boolean isThreeWay() {
		return true;
	}
	
	protected String getLeftLabel() throws Exception {
		ResourceElement element = this.getLeftResourceElement();
		return element.getLocalResource().getResource().getFullPath().toString().substring(1);
	}
	
	protected String getRevisionPart(ResourceElement element) throws Exception {
		IRepositoryResource resource = element.getRepositoryResource();
		SVNRevision selected = resource.getSelectedRevision();
		int kind = selected.getKind();
		
		if (kind == Kind.WORKING) {
			return "Local";
		}
		else if (kind == Kind.BASE) {
			long revision = element.getLocalResource().getRevision();
			if (revision == SVNRevision.INVALID_REVISION_NUMBER) {
				return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.ResourceIsNotAvailable");
			}
			return "Base:" + String.valueOf(revision);
		}

		return super.getRevisionPart(element);
	}
	
	protected ILocalResource getLocalResourceFor(IRepositoryResource base) {
		String url = base.getUrl();
		if (this.compareWithCopySource && this.copiedFrom != null && new Path(this.copiedFrom.getUrl()).isPrefixOf(new Path(url))) {
			url = this.rootLeft.getUrl() + url.substring(this.copiedFrom.getUrl().length());
		}
		else if (this.newUrl2OldUrl.containsKey(url)) {
			url = (String)this.newUrl2OldUrl.get(url);
		}
		return SVNRemoteStorage.instance().asLocalResource(this.local.getProject(), url, base instanceof IRepositoryContainer ? IResource.FOLDER : IResource.FILE);
	}
	
	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config) {
			public void setLabelProvider(IBaseLabelProvider labelProvider) {
				super.setLabelProvider(new LabelProviderWrapper((ILabelProvider)labelProvider) {
					public Image getImage(Object element) {
						if (element instanceof CompareNode && (((CompareNode)element).getLocalChangeType() == SVNEntryStatus.Kind.REPLACED || ((CompareNode)element).getRemoteChangeType() == SVNEntryStatus.Kind.REPLACED)) {
							Image image = (Image)this.images.get(element);
							if (image == null) {
								OverlayedImageDescriptor imageDescriptor = null;
								int direction = ((CompareNode)element).getKind() & Differencer.DIRECTION_MASK;
								if (direction == Differencer.LEFT) {
									imageDescriptor = new OverlayedImageDescriptor(baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_out.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
								}
								else if (direction == Differencer.RIGHT) {
									imageDescriptor = new OverlayedImageDescriptor(baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_in.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
								}
								else {
									imageDescriptor = new OverlayedImageDescriptor(baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_conf.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
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
				pm.beginTask(SVNTeamUIPlugin.instance().getResource("ThreeWayResourceCompareInput.SaveChanges"), -1);
				this.saveChanges((CompareNode)this.root);
			}
			finally {
				pm.done();
			}
		}
	}
	
	protected void saveChanges(CompareNode node) throws CoreException {
		ResourceElement left = (ResourceElement)node.getLeft();
		if (left.isEditable() && left.isDirty()) {
			left.commit(new NullProgressMonitor());
		}
		IDiffElement []children = node.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				this.saveChanges((CompareNode)children[i]);
			}
		}
	}
	
	protected class CompareNode extends BaseCompareNode {
		protected int localChangeType;
		protected int remoteChangeType;
		
		public CompareNode(IDiffContainer parent, int kind, IRepositoryResource local, IRepositoryResource ancestor, IRepositoryResource remote, int localChangeType, int remoteChangeType) {
			super(parent, kind);
			
			this.localChangeType = localChangeType;
			this.remoteChangeType = remoteChangeType;
			
			ILocalResource wcInfo = ThreeWayResourceCompareInput.this.getLocalResourceFor(local);
			

			ResourceElement leftElt = new ResourceElement(local, wcInfo, localChangeType == SVNEntryStatus.Kind.NONE || localChangeType == SVNEntryStatus.Kind.DELETED ? SVNEntryStatus.Kind.NONE : SVNEntryStatus.Kind.NORMAL);
			leftElt.setEditable(local instanceof IRepositoryFile);
			this.setLeft(leftElt);
			this.setAncestor(new ResourceElement(ancestor, wcInfo, localChangeType == SVNEntryStatus.Kind.UNVERSIONED || remoteChangeType == SVNEntryStatus.Kind.ADDED ? SVNEntryStatus.Kind.NONE : SVNEntryStatus.Kind.NORMAL));
			this.setRight(new ResourceElement(remote, wcInfo, remoteChangeType == SVNEntryStatus.Kind.DELETED || remoteChangeType == SVNEntryStatus.Kind.NONE ? SVNEntryStatus.Kind.NONE : SVNEntryStatus.Kind.NORMAL));
		}

		public int getLocalChangeType() {
			return this.localChangeType;
		}
		
		public int getRemoteChangeType() {
			return this.remoteChangeType;
		}
		
	}
	
}
