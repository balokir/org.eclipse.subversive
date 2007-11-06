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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.Revision.Kind;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Compare editor input for the versioned trees
 * 
 * @author Alexander Gurov
 */
public abstract class ResourceCompareInput extends CompareEditorInput {
	protected ResourceCompareViewer viewer;
	protected DiffNode root;
	
	protected IRepositoryResource rootLeft;
	protected IRepositoryResource rootAncestor;
	protected IRepositoryResource rootRight;
	
	public ResourceCompareInput(CompareConfiguration configuration) {
		super(configuration);
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
		this.refreshTitles();
	}
	
	public final Viewer createDiffViewer(Composite parent) {
		return this.viewer = this.createDiffViewerImpl(parent, this.getCompareConfiguration());
	}
	
	protected abstract boolean isThreeWay();
	
	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config);
	}
	
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (this.root != null) {
			ResourceElement left = (ResourceElement)this.root.getLeft();
			ResourceElement ancestor = (ResourceElement)this.root.getAncestor();
			ResourceElement right = (ResourceElement)this.root.getRight();
			if ((left.getType() == ITypedElement.FOLDER_TYPE || 
				ancestor != null && ancestor.getType() == ITypedElement.FOLDER_TYPE || 
				right.getType() == ITypedElement.FOLDER_TYPE) &&
				(this.root.getKind() & Differencer.CHANGE_TYPE_MASK) != 0) {
				this.root = (DiffNode)this.root.getParent();
			}
		}
		monitor.done();
		return this.root;
	}

	protected void refreshTitles() throws Exception {
		CompareConfiguration cc = this.getCompareConfiguration();
		
		cc.setLeftLabel(this.getLeftLabel());
		cc.setLeftImage(this.getLeftImage());
		
		cc.setRightLabel(this.getRightLabel());
		cc.setRightImage(this.getRightImage());
		
		String leftLabel = this.getShortLeftLabel();
		String rightLabel = this.getShortRightLabel();

		if (this.isThreeWay()) {
			cc.setAncestorLabel(this.getAncestorLabel());
			cc.setAncestorImage(this.getAncestorImage());
			
			String format = CompareUI.getResourceBundle().getString("ResourceCompare.threeWay.title");
			String ancestorLabel = this.getShortAncestorLabel();
			this.setTitle(MessageFormat.format(format, new String[] {ancestorLabel, leftLabel, rightLabel}));	
		} 
		else {
			String format = CompareUI.getResourceBundle().getString("ResourceCompare.twoWay.title");
			this.setTitle(MessageFormat.format(format, new String[] {leftLabel, rightLabel}));
		}
	}
	
	protected String getShortAncestorLabel() throws Exception {
		return this.getShortLabel(this.getAncestorResourceElement());
	}

	protected String getAncestorLabel() throws Exception {
		return this.getLabel(this.getAncestorResourceElement());
	}

	protected Image getAncestorImage() throws Exception {
		return CompareUI.getImage(RepositoryFolder.wrapChild(null, this.getAncestorResourceElement().getRepositoryResource()));
	}

	protected String getShortLeftLabel() throws Exception {
		return this.getShortLabel(this.getLeftResourceElement());
	}

	protected String getLeftLabel() throws Exception {
		return this.getLabel(this.getLeftResourceElement());
	}
	
	protected Image getLeftImage() throws Exception {
		return CompareUI.getImage(RepositoryFolder.wrapChild(null, this.getLeftResourceElement().getRepositoryResource()));
	}
	
	protected String getShortRightLabel() throws Exception {
		return this.getShortLabel(this.getRightResourceElement());
	}
	
	protected String getRightLabel() throws Exception {
		return this.getLabel(this.getRightResourceElement());
	}
	
	protected Image getRightImage() throws Exception {
		return CompareUI.getImage(RepositoryFolder.wrapChild(null, this.getRightResourceElement().getRepositoryResource()));
	}
	
	protected String getShortLabel(ResourceElement element) throws Exception {
		return element.getName() + this.getRevisionPart(element);
	}
	
	protected String getLabel(ResourceElement element) throws Exception {
		return element.getRepositoryResource().getUrl() + this.getRevisionPart(element);
	}
	
	protected String getRevisionPart(ResourceElement element) throws Exception {
		IRepositoryResource resource = element.getRepositoryResource();
		Revision selected = resource.getSelectedRevision();
		if (selected == Revision.INVALID_REVISION) {
			return "";
		}
		int kind = selected.getKind();
		ILocalResource local = element.getLocalResource();
		String msg = SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.RevisionPart");
		if (kind == Kind.WORKING || kind == Kind.BASE || (local != null && local.isCopied())) {
			if (local == null || local.getRevision() == Revision.INVALID_REVISION_NUMBER) {
				return "";
			}
			return " " + MessageFormat.format(msg, new String[] {String.valueOf(local.getRevision())});
		}
		return " " + MessageFormat.format(msg, new String[] {String.valueOf(resource.getRevision())});
	}
	
	protected ResourceElement getLeftResourceElement() {
		DiffNode node = this.getSelectedNode();
		if (node != null) {
			return (ResourceElement)node.getLeft();
		}
		return new ResourceElement(this.rootLeft, org.eclipse.team.svn.core.client.Status.Kind.NORMAL);
	}
	
	protected ResourceElement getRightResourceElement() {
		DiffNode node = this.getSelectedNode();
		if (node != null) {
			return (ResourceElement)node.getRight();
		}
		return new ResourceElement(this.rootRight, org.eclipse.team.svn.core.client.Status.Kind.NORMAL);
	}
	
	protected ResourceElement getAncestorResourceElement() {
		DiffNode node = this.getSelectedNode();
		if (node != null) {
			return (ResourceElement)node.getAncestor();
		}
		return new ResourceElement(this.rootAncestor, org.eclipse.team.svn.core.client.Status.Kind.NORMAL);
	}
	
	protected ILocalResource getLocalResourceFor(IRepositoryResource base) {
		return null;
	}
	
	protected DiffNode getSelectedNode() {
		if (this.viewer != null) {
			IStructuredSelection selection = (IStructuredSelection)this.viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof DiffNode) {
				return (DiffNode)selection.getFirstElement();
			}
		}
		return null;
	}
	
	protected IDiffContainer getParentCompareNode(IRepositoryResource current, Map path2node) throws Exception {
		IRepositoryResource parent = current.getParent();
		if (parent == null) {
			return null;
		}
		
		Path parentUrl = new Path(parent.getUrl());
		IDiffContainer node = (IDiffContainer)path2node.get(parentUrl);
		if (node == null) {
			path2node.put(parentUrl, node = this.makeStubNode(this.getParentCompareNode(parent, path2node), parent));
		}
		return node;
	}
	
	protected abstract IDiffContainer makeStubNode(IDiffContainer parent, IRepositoryResource node);
	
	protected static int getDiffKind(int textStatus, int propStatus, int kindOverride) {
		if (kindOverride == org.eclipse.team.svn.core.client.Status.Kind.REPLACED) {
			return Differencer.CHANGE;
		}
		if (textStatus == org.eclipse.team.svn.core.client.Status.Kind.ADDED ||
			textStatus == org.eclipse.team.svn.core.client.Status.Kind.UNVERSIONED) {
			return Differencer.ADDITION;
		}
		if (textStatus == org.eclipse.team.svn.core.client.Status.Kind.DELETED) {
			return Differencer.DELETION;
		}
		if (textStatus == org.eclipse.team.svn.core.client.Status.Kind.REPLACED) {
			return Differencer.CHANGE;
		}
		if (textStatus == org.eclipse.team.svn.core.client.Status.Kind.MODIFIED ||
			propStatus == org.eclipse.team.svn.core.client.Status.Kind.MODIFIED) {
			return Differencer.CHANGE;
		}
		return Differencer.NO_CHANGE;
	}
	
	protected int getNodeKind(Status st) {
		int kind = SVNUtility.getNodeKind(st.path, st.nodeKind, true);
		return kind == NodeKind.NONE ? SVNUtility.getNodeKind(st.path, st.reposKind, false) : kind;
	}
	
	protected IRepositoryResource createResourceFor(IRepositoryLocation location, int kind, String url) {
		IRepositoryResource retVal = null;
		if (kind == NodeKind.FILE) {
			retVal = location.asRepositoryFile(url, false);
		}
		else if (kind == NodeKind.DIR) {
			retVal = location.asRepositoryContainer(url, false);
		}
		if (retVal == null) {
			throw new RuntimeException(SVNTeamUIPlugin.instance().getResource("Error.CompareUnknownNodeKind"));
		}
		return retVal;
	}
	
	public class ResourceElement implements ITypedElement, IStreamContentAccessor, IContentChangeNotifier, IEditableContent {
		protected Vector listenerList;
		protected boolean dirty;
		
		protected IRepositoryResource resource;
		protected AbstractGetFileContentOperation op;
		protected ILocalResource localAlias;
		protected int kind;
		protected boolean editable;
		
		public ResourceElement(IRepositoryResource resource, int kind) {
			this(resource, ResourceCompareInput.this.getLocalResourceFor(resource), kind);
		}
		
		public ResourceElement(IRepositoryResource resource, ILocalResource alias, int kind) {
			this.resource = resource;
			this.localAlias = alias;
			this.kind = kind;
			this.editable = false;
			this.listenerList = new Vector();
			if (kind == org.eclipse.team.svn.core.client.Status.Kind.NONE) {
				resource.setSelectedRevision(Revision.INVALID_REVISION);
			}
		}
		
		public void addContentChangeListener(IContentChangeListener listener) {
			this.listenerList.add(listener);
		}
		
		public void removeContentChangeListener(IContentChangeListener listener) {
			this.listenerList.remove(listener);
		}
		
		public boolean isDirty() {
			return this.dirty;
		}
		
		public boolean isEditable() {
			return this.editable && this.localAlias != null && this.localAlias.getResource() instanceof IFile;
		}

		public void setEditable(boolean editable) {
			this.editable = editable;
		}

		public ITypedElement replace(ITypedElement dest, ITypedElement src) {
			return dest;
		}
		
		public void commit(IProgressMonitor pm) throws CoreException {
			if (this.isDirty()) {
				IFile file = (IFile)this.localAlias.getResource();
				file.refreshLocal(IResource.DEPTH_ZERO, pm);
				this.dirty = false;
			}
		}

		public void setContent(byte[] newContent) {
			if (this.isEditable()) {
				// ensure content is accessible
				this.fetchContent(new NullProgressMonitor());
				if (this.op != null) {
					this.op.setContent(newContent);
					this.fireContentChanged();
				}
			}
		}
		
		public IRepositoryResource getRepositoryResource() {
			return this.resource;
		}
		
		public ILocalResource getLocalResource() {
			return this.localAlias;
		}
		
		public String getName() {
			return this.resource.getName();
		}
	
		public Image getImage() {
			return CompareUI.getImage(RepositoryFolder.wrapChild(null, this.resource));
		}
	
		public String getType() {
			if (this.resource instanceof IRepositoryContainer) {
				return ITypedElement.FOLDER_TYPE;
			}
			String fileName = this.resource.getName();
			int dotIdx = fileName.lastIndexOf('.');
			return dotIdx == -1 ? ITypedElement.UNKNOWN_TYPE : fileName.substring(dotIdx + 1);
		}
		
		public void fetchContent(IProgressMonitor monitor) {
			if (this.kind != org.eclipse.team.svn.core.client.Status.Kind.NONE && this.resource instanceof IRepositoryFile && this.op == null) {
				int revisionKind = this.resource.getSelectedRevision().getKind();
				AbstractGetFileContentOperation op = 
					revisionKind == Kind.WORKING || revisionKind == Kind.BASE ? 
					(AbstractGetFileContentOperation)new GetLocalFileContentOperation(this.localAlias.getResource(), revisionKind) : 
					new GetFileContentOperation(this.resource);
				UIMonitorUtility.doTaskExternalDefault(op, monitor);
				if (op.getExecutionState() == IActionOperation.OK) {
					this.op = op;
				}
			}
		}
	
		public InputStream getContents() throws CoreException {
			if (this.kind != org.eclipse.team.svn.core.client.Status.Kind.NONE) {
				this.fetchContent(new NullProgressMonitor());
				return this.op == null ? null : this.op.getContent();
			}
			return new ByteArrayInputStream(new byte[0]);
		}
	
		protected void fireContentChanged() {
			this.dirty = true;
			Object []listeners = this.listenerList.toArray();
			for (int i= 0; i < listeners.length; i++) {
				((IContentChangeListener)listeners[i]).contentChanged(this);
			}
		}

	}

	protected class ResourceCompareViewer extends DiffTreeViewer {
		public ResourceCompareViewer(Composite parent, CompareConfiguration configuration) {
			super(parent, configuration);
		}
		
		protected void handleOpen(final SelectionEvent event) {
			final BaseCompareNode node = (BaseCompareNode)((TreeItem)event.item).getData();
			IActionOperation fetchContent = new AbstractNonLockingOperation("Operation.FetchContent") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					node.fetchContent(monitor);
					final Throwable []t = new Throwable[1];
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							try {
								ResourceCompareInput.this.refreshTitles();
								ResourceCompareViewer.super.handleOpen(event);
							} 
							catch (Exception e) {
								t[0] = e;
							}
						}
					});
					if (t[0] != null ){
						this.reportError(t[0]);
					}
				}
			};
			UIMonitorUtility.doTaskNowDefault(fetchContent, true);
		}
		
		protected class LabelProviderWrapper implements ILabelProvider {
			protected Map images;
			protected ILabelProvider baseProvider;
			
			public LabelProviderWrapper(ILabelProvider baseProvider) {
				this.images = new HashMap();
				this.baseProvider = baseProvider;
			}
			
			public void addListener(ILabelProviderListener listener) {
				this.baseProvider.addListener(listener);
			}
			
			public void removeListener(ILabelProviderListener listener) {
				this.baseProvider.removeListener(listener);
			}
			
			public boolean isLabelProperty(Object element, String property) {
				return this.baseProvider.isLabelProperty(element, property);
			}
			
			public String getText(Object element) {
				return this.baseProvider.getText(element);
			}
			
			public Image getImage(Object element) {
				return this.baseProvider.getImage(element);
			}
			
			public void dispose() {
				for (Iterator it = this.images.values().iterator(); it.hasNext(); ) {
					((Image)it.next()).dispose();
				}
				this.baseProvider.dispose();
			}
			
		}
	}
	
	protected class BaseCompareNode extends DiffNode {
		protected IDiffElement []ordered;
		
		public BaseCompareNode(IDiffContainer parent, int kind) {
			super(parent, kind);
		}
		
		public void fetchContent(IProgressMonitor monitor) {
			ResourceElement left = (ResourceElement)this.getLeft();
			ResourceElement ancestor = (ResourceElement)this.getAncestor();
			ResourceElement right = (ResourceElement)this.getRight();
			if (left != null && !monitor.isCanceled()) {
				monitor.subTask(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.FetchLeft"), new String[] {left.getRepositoryResource().getUrl()}));
				left.fetchContent(monitor);
				ProgressMonitorUtility.progress(monitor, 0, 3);
			}
			if (ancestor != null && !monitor.isCanceled()) {
				monitor.subTask(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.FetchAncestor"), new String[] {ancestor.getRepositoryResource().getUrl()}));
				ancestor.fetchContent(monitor);
				ProgressMonitorUtility.progress(monitor, 1, 3);
			}
			if (right != null && !monitor.isCanceled()) {
				monitor.subTask(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.FetchRight"), new String[] {right.getRepositoryResource().getUrl()}));
				right.fetchContent(monitor);
				ProgressMonitorUtility.progress(monitor, 2, 3);
			}
		}
		
	}
	
}
