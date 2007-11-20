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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNEntryStatus.Kind;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Two way resource compare input
 * 
 * @author Alexander Gurov
 */
public class TwoWayResourceCompareInput extends ResourceCompareInput {
	protected SVNEntryStatus []statuses;
	
	public TwoWayResourceCompareInput(CompareConfiguration configuration, IRepositoryResource leftResource, IRepositoryResource rightResource, SVNEntryStatus []statuses) {
		super(configuration);
		
		this.rootLeft = SVNUtility.copyOf(leftResource);
		this.rootRight = SVNUtility.copyOf(rightResource);
		this.statuses = statuses;
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
			
		SVNUtility.reorder(this.statuses, true);
		
		HashMap path2node = new HashMap();
		String message = SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.CheckingDelta");
		for (int i = 0; i < this.statuses.length; i++) {
			monitor.subTask(MessageFormat.format(message, new String[] {SVNUtility.decodeURL(this.statuses[i].path)}));
			
			CompareNode node = this.makeNode(this.statuses[i], path2node, monitor);
			path2node.put(new Path(((ResourceElement)node.getRight()).getRepositoryResource().getUrl()), node);
			
			ProgressMonitorUtility.progress(monitor, i, this.statuses.length);
		}
		
		this.findRootNode(path2node, this.rootRight, monitor);
		if (this.root == null) {
			this.findRootNode(path2node, this.rootLeft, monitor);
		}
		
		super.initialize(monitor);
	}
	
	protected void findRootNode(Map path2node, IRepositoryResource resource, IProgressMonitor monitor) {
		this.root = (CompareNode)path2node.get(new Path(resource.getUrl()));
		if (this.root == null && !path2node.isEmpty()) {
			LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {resource}, false);
			UIMonitorUtility.doTaskExternalDefault(op, monitor);
			IRepositoryResource converted = op.getRepositoryResources()[0];
			this.root = (CompareNode)path2node.get(new Path(converted.getUrl()));
		}
	}
	
	protected CompareNode makeNode(SVNEntryStatus st, Map path2node, IProgressMonitor monitor) throws Exception {
		String urlLeft = SVNUtility.decodeURL(st.path);
		String urlRight = SVNUtility.decodeURL(st.url);
		int nodeKind = this.getNodeKind(st);
		
		IRepositoryResource left = this.createResourceFor(this.rootLeft.getRepositoryLocation(), nodeKind, urlLeft);
		left.setSelectedRevision(this.rootLeft.getSelectedRevision());
		left.setPegRevision(this.rootLeft.getPegRevision());
		
		IRepositoryResource right = this.createResourceFor(this.rootRight.getRepositoryLocation(), nodeKind, urlRight);
		right.setSelectedRevision(this.rootRight.getSelectedRevision());
		right.setPegRevision(this.rootRight.getPegRevision());
		
		IDiffContainer parent = this.getParentCompareNode(right, path2node);
		
		LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {left, right}, true);
		ProgressMonitorUtility.doTaskExternal(op, monitor);
		right = op.getRepositoryResources()[0];
		left = op.getRepositoryResources()[1];

		// invert diffKind and swap content in order to make compare view the same as Eclipse "Compare Each Other"
		int diffKind = ResourceCompareInput.getDiffKind(st.textStatus, st.propStatus, Kind.NORMAL);
		diffKind = diffKind == Differencer.DELETION ? Differencer.ADDITION : (diffKind == Differencer.ADDITION ? Differencer.DELETION : diffKind);
		
		return new CompareNode(parent, diffKind, right, left, st.textStatus == Kind.NORMAL ? st.propStatus : st.textStatus);
	}
	
	protected IDiffContainer makeStubNode(IDiffContainer parent, IRepositoryResource node) {
		return new CompareNode(parent, Differencer.NO_CHANGE, node, node, Kind.NORMAL);
	}
	
	protected boolean isThreeWay() {
		return false;
	}

	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config) {
			public void setLabelProvider(IBaseLabelProvider labelProvider) {
				super.setLabelProvider(new LabelProviderWrapper((ILabelProvider)labelProvider) {
					public Image getImage(Object element) {
						if (element instanceof CompareNode && ((CompareNode)element).getStatusKind() == Kind.REPLACED) {
							Image image = (Image)this.images.get(element);
							if (image == null) {
								OverlayedImageDescriptor imageDescriptor = new OverlayedImageDescriptor(baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_2way.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
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
		protected int statusKind;
		
		public CompareNode(IDiffContainer parent, int kind, IRepositoryResource left, IRepositoryResource right, int statusKindLeftToRight) {
			super(parent, kind);
			this.statusKind = statusKindLeftToRight;
			this.setRight(new ResourceElement(right, statusKindLeftToRight == Kind.ADDED ? Kind.NONE : Kind.NORMAL));
			this.setLeft(new ResourceElement(left, statusKindLeftToRight == Kind.DELETED ? Kind.NONE : Kind.NORMAL));
		}

		public int getStatusKind() {
			return this.statusKind;
		}
		
	}
	
}
