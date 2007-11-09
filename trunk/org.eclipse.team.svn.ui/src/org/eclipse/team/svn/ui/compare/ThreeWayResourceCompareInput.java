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
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.SVNRevision.Kind;
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
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Implements three way comparision of resource trees
 * 
 * @author Alexander Gurov
 */
public class ThreeWayResourceCompareInput extends ResourceCompareInput {
	protected IResource localLeft;
	protected SVNEntryStatus []localChanges;
	protected SVNEntryStatus []remoteChanges;
	protected Map newUrl2OldUrl;
	
	public ThreeWayResourceCompareInput(CompareConfiguration configuration, IResource left, SVNRevision revision, SVNRevision pegRevision, SVNEntryStatus []localChanges, SVNEntryStatus []remoteChanges) {
		super(configuration);
		this.newUrl2OldUrl = new HashMap();
		
		this.localLeft = left;

		IRemoteStorage storage = SVNRemoteStorage.instance();
		this.rootLeft = storage.asRepositoryResource(this.localLeft);
		this.rootLeft.setSelectedRevision(SVNRevision.WORKING);
		this.rootAncestor = storage.asRepositoryResource(this.localLeft);
		this.rootAncestor.setSelectedRevision(SVNRevision.BASE);
		this.rootRight = storage.asRepositoryResource(this.localLeft);
		this.rootRight.setSelectedRevision(revision);
		this.rootRight.setPegRevision(pegRevision);
		
		this.localChanges = localChanges;
		this.remoteChanges = remoteChanges;
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
		super.initialize(monitor);
		
		Map localChanges = new HashMap();
		Map remoteChanges = new HashMap();
		HashSet allChangesSet = new HashSet();
		for (int i = 0; i < this.localChanges.length; i++) {
			String url = this.getUrl(this.localChanges[i].path);
			allChangesSet.add(url);
			localChanges.put(url, this.localChanges[i]);
		}
		for (int i = 0; i < this.remoteChanges.length; i++) {
			String url = SVNUtility.decodeURL(this.remoteChanges[i].path);
			allChangesSet.add(url);
			remoteChanges.put(url, this.remoteChanges[i]);
		}
		String []allChanges = (String [])allChangesSet.toArray(new String[allChangesSet.size()]);
		FileUtility.sort(allChanges);
		HashMap path2node = new HashMap();
		
		String message = SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.CheckingDelta");
		for (int i = 0; i < allChanges.length; i++) {
			monitor.subTask(MessageFormat.format(message, new String[] {allChanges[i]}));
			this.makeBranch(allChanges[i], (SVNEntryStatus)localChanges.get(allChanges[i]), (SVNEntryStatus)remoteChanges.get(allChanges[i]), path2node, monitor);
			ProgressMonitorUtility.progress(monitor, i, allChanges.length);
		}
		
		this.root = (CompareNode)path2node.get(new Path(this.rootRight.getUrl()));
		if (this.root == null) {
			LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {this.rootRight}, false);
			UIMonitorUtility.doTaskExternalDefault(op, monitor);
			IRepositoryResource converted = op.getRepositoryResources()[0];
			this.root = (CompareNode)path2node.get(new Path(converted.getUrl()));
		}
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
	
	protected void makeBranch(String url, SVNEntryStatus stLocal, SVNEntryStatus stRemote, Map path2node, IProgressMonitor monitor) throws Exception {
		// skip all ignored resources that does not have real remote variants
		if (stRemote == null) {
			IProject project = this.localLeft.getProject();
			String relative = stLocal.path.substring(FileUtility.getWorkingCopyPath(project).length());
			IResource resource = relative.length() == 0 ? project : project.findMember(relative);
			
			ILocalResource local;
			if (resource == null || 
				(local = SVNRemoteStorage.instance().asLocalResource(resource)) == null || 
				IStateFilter.SF_IGNORED.accept(resource, local.getStatus(), local.getChangeMask())) {
				return;
			}
		}
		CompareNode node = this.makeNode(url, stLocal, stRemote, path2node, monitor);
		if (node != null) {
			path2node.put(new Path(url), node);
		}
	}
	
	protected CompareNode makeNode(String oldUrl, SVNEntryStatus stLeft, SVNEntryStatus stRight, Map path2node, IProgressMonitor monitor) throws Exception {
		int rightNodeKind = stRight == null ? this.getNodeKind(stLeft) : this.getNodeKind(stRight);
		int leftNodeKind = stLeft == null ? this.getNodeKind(stRight) : this.getNodeKind(stLeft);
		int ancestorNodeKind = this.getAncestorKind(stLeft, leftNodeKind, rightNodeKind);
		
		IRepositoryLocation location = this.rootLeft.getRepositoryLocation();
		
		IRepositoryResource left = this.createResourceFor(location, leftNodeKind, oldUrl);
		left.setSelectedRevision(SVNRevision.WORKING);
		left.setPegRevision(null);
		
		IRepositoryResource ancestor = this.createResourceFor(location, ancestorNodeKind, oldUrl);
		ancestor.setSelectedRevision(SVNRevision.BASE);
		ancestor.setPegRevision(null);
		
		String rightUrl = stRight != null ? stRight.url : (stLeft != null && stLeft.isCopied ? stLeft.urlCopiedFrom : oldUrl);
		IRepositoryResource right = this.createResourceFor(location, rightNodeKind, rightUrl);
		right.setPegRevision(this.rootRight.getPegRevision());
		right.setSelectedRevision(this.rootRight.getSelectedRevision());
		
		LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {right}, true);
		ProgressMonitorUtility.doTaskExternalDefault(op, monitor);

		IRepositoryResource tRes = op.getRepositoryResources()[0];
		ILocalResource local = this.getLocalResourceFor(tRes);
		if (!tRes.exists()) {
			if (local == null || IStateFilter.SF_NOTEXISTS.accept(local.getResource(), local.getStatus(), local.getChangeMask())) {
				return null;
			}
			stRight = null;
		}
		right = tRes;
		
		int statusLeft = stLeft == null ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL : (stLeft.textStatus == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL ? stLeft.propStatus : stLeft.textStatus);
		if (statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.DELETED && new File(stLeft.path).exists()) {
			statusLeft = org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.REPLACED;
		}
		int statusRight = stRight == null ? (statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.IGNORED || statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE || statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.UNVERSIONED ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE :  org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL) : (stRight.textStatus == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL ? stRight.propStatus : stRight.textStatus);
		
		// skip resources that already up-to-date
		if (stRight != null && local != null) {
			ILocalResource tmp = SVNRemoteStorage.instance().asLocalResource(this.localLeft);
			if (this.rootRight.getSelectedRevision().getKind() == Kind.NUMBER && tmp != null && tmp.getRevision() >= ((SVNRevision.Number)this.rootRight.getSelectedRevision()).getNumber()) {
				if (!local.getResource().exists() && statusRight == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.DELETED || 
					statusRight != org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.DELETED && local.getRevision() == right.getRevision()) {
					return null;
				}
			}
			else if (local.getRevision() == right.getRevision()) {
				if (stLeft == null) {
					return null;
				}
				else {
					stRight = null;
					statusRight = statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.ADDED || statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.IGNORED || statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE || statusLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.UNVERSIONED ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE :  org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL;
				}
			}
		}
		
		this.newUrl2OldUrl.put(right.getUrl(), oldUrl);
		
		int diffKindLeft = ResourceCompareInput.getDiffKind(statusLeft, stLeft == null ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE : stLeft.propStatus, org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL);
		if (diffKindLeft != Differencer.NO_CHANGE) {
			diffKindLeft |= Differencer.LEFT;
		}
		int diffKindRight = ResourceCompareInput.getDiffKind(statusRight, stRight == null ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE : stRight.propStatus, org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL);
		if (diffKindRight != Differencer.NO_CHANGE) {
			diffKindRight |= Differencer.RIGHT;
		}
		IDiffContainer parent = this.getParentCompareNode(tRes, path2node);
		return new CompareNode(parent, diffKindLeft | diffKindRight, left, ancestor, right, statusLeft, statusRight);
	}
	
	protected int getAncestorKind(SVNEntryStatus stLeft, int leftNodeKind, int rightNodeKind) {
		if (stLeft != null) {
			switch (stLeft.textStatus) {
				case org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE:
				case org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.ADDED:
				case org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.UNVERSIONED:
				case org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.IGNORED:
				case org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.EXTERNAL: {
					return rightNodeKind;
				}
			}
			return leftNodeKind;
		}
		return rightNodeKind;
	}
	
	protected String getUrl(String localPath) {
		// Try to find resource using provided path.
		// And then get the resource URL.
		// It is necessary for external resources.
		IResource resource = this.getResourceForPath(localPath);
		if (resource != null) {
			IRemoteStorage storage = SVNRemoteStorage.instance();
			return storage.asRepositoryResource(resource).getUrl();
		}
		// In case the resource is not found make up the URL using
		// project URL and relative path
		IProject project = this.localLeft.getProject();
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
		return new CompareNode(parent, Differencer.NO_CHANGE, node, node, node, org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL, org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL);
	}
	
	protected boolean isThreeWay() {
		return true;
	}
	
	protected String getLeftLabel() throws Exception {
		ResourceElement element = this.getLeftResourceElement();
		return element.getLocalResource().getResource().getFullPath().toString().substring(1) + this.getRevisionPart(element);
	}
	
	protected ILocalResource getLocalResourceFor(IRepositoryResource base) {
		String url = base.getUrl();
		if (this.newUrl2OldUrl.containsKey(url)) {
			url = (String)this.newUrl2OldUrl.get(url);
		}
		return SVNRemoteStorage.instance().asLocalResource(this.localLeft.getProject(), url, base instanceof IRepositoryContainer ? IResource.FOLDER : IResource.FILE);
	}
	
	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config) {
			public void setLabelProvider(IBaseLabelProvider labelProvider) {
				super.setLabelProvider(new LabelProviderWrapper((ILabelProvider)labelProvider) {
					public Image getImage(Object element) {
						if (element instanceof CompareNode && (((CompareNode)element).getStatusKindLeft() == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.REPLACED || ((CompareNode)element).getStatusKindRight() == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.REPLACED)) {
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
	
	protected class CompareNode extends BaseCompareNode {
		protected int statusKindLeft;
		protected int statusKindRight;
		
		public CompareNode(IDiffContainer parent, int kind, IRepositoryResource left, IRepositoryResource ancestor, IRepositoryResource right, int statusKindLeft, int statusKindRight) {
			super(parent, kind);
			this.statusKindLeft = statusKindLeft;
			this.statusKindRight = statusKindRight;
			ResourceElement leftElt = new ResourceElement(left, statusKindLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE || statusKindLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.DELETED ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE : org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL);
			leftElt.setEditable(left instanceof IRepositoryFile);
			this.setLeft(leftElt);
			this.setAncestor(new ResourceElement(ancestor, statusKindLeft == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.UNVERSIONED || statusKindRight == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.ADDED ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE : org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL));
			this.setRight(new ResourceElement(right, statusKindRight == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.DELETED || statusKindRight == org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE ? org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NONE : org.eclipse.team.svn.core.client.SVNEntryStatus.Kind.NORMAL));
		}

		public int getStatusKindLeft() {
			return this.statusKindLeft;
		}
		
		public int getStatusKindRight() {
			return this.statusKindRight;
		}
		
	}
	
}
