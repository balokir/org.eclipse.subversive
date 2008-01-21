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
import java.util.Collection;
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
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;

/**
 * Two way resource compare input
 * 
 * @author Alexander Gurov
 */
public class TwoWayResourceCompareInput extends ResourceCompareInput {
	protected SVNDiffStatus []statuses;
	
	public TwoWayResourceCompareInput(CompareConfiguration configuration, IRepositoryResource next, IRepositoryResource prev, Collection<SVNDiffStatus> statuses) {
		super(configuration);
		
		this.rootLeft = SVNUtility.copyOf(next);
		this.rootRight = SVNUtility.copyOf(prev);
		this.statuses = statuses.toArray(new SVNDiffStatus[statuses.size()]);
	}

	public void initialize(IProgressMonitor monitor) throws Exception {
		SVNUtility.reorder(this.statuses, true);
		
		HashMap path2node = new HashMap();
		String message = SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.CheckingDelta");
		for (int i = 0; i < this.statuses.length; i++) {
			monitor.subTask(MessageFormat.format(message, new Object[] {SVNUtility.decodeURL(this.statuses[i].pathPrev)}));
			
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
	
	protected CompareNode makeNode(SVNDiffStatus st, Map path2node, IProgressMonitor monitor) throws Exception {
		String urlNext = SVNUtility.decodeURL(st.pathNext);
		String urlPrev = SVNUtility.decodeURL(st.pathPrev);
		int nodeKind = this.getNodeKind(st);
		
		IRepositoryResource next = this.createResourceFor(this.rootLeft.getRepositoryLocation(), nodeKind, urlNext);
		next.setSelectedRevision(this.rootLeft.getSelectedRevision());
		next.setPegRevision(this.rootLeft.getPegRevision());
		
		IRepositoryResource prev = this.createResourceFor(this.rootRight.getRepositoryLocation(), nodeKind, urlPrev);
		prev.setSelectedRevision(this.rootRight.getSelectedRevision());
		prev.setPegRevision(this.rootRight.getPegRevision());
		
		IDiffContainer parent = this.getParentCompareNode(prev, path2node);
		
		// invert diffKind in order to make compare view the same as Eclipse "Compare Each Other"
		int diffKind = ResourceCompareInput.getDiffKind(st.textStatus, st.propStatus);
		diffKind = diffKind == Differencer.DELETION ? Differencer.ADDITION : (diffKind == Differencer.ADDITION ? Differencer.DELETION : diffKind);
		
		return new CompareNode(parent, diffKind, next, prev, st.textStatus == SVNEntryStatus.Kind.NORMAL ? st.propStatus : st.textStatus);
	}
	
	protected IDiffContainer makeStubNode(IDiffContainer parent, IRepositoryResource node) {
		IRepositoryResource next = node;
		
		String prevUrl = node.getUrl();
		if (prevUrl.length() > this.rootRight.getUrl().length()) {
			String urlPart = prevUrl.substring(this.rootRight.getUrl().length());
			String urlNext = this.rootLeft.getUrl() + urlPart;
			
			next = this.createResourceFor(this.rootLeft.getRepositoryLocation(), node instanceof IRepositoryFile ? SVNEntry.Kind.FILE : SVNEntry.Kind.DIR, urlNext);
			next.setSelectedRevision(this.rootLeft.getSelectedRevision());
			next.setPegRevision(this.rootLeft.getPegRevision());
		}
		
		return new CompareNode(parent, Differencer.NO_CHANGE, next, node, SVNEntryStatus.Kind.NORMAL);
	}
	
	protected boolean isThreeWay() {
		return false;
	}

	protected ResourceCompareViewer createDiffViewerImpl(Composite parent, CompareConfiguration config) {
		return new ResourceCompareViewer(parent, config) {
			public void setLabelProvider(IBaseLabelProvider labelProvider) {
				super.setLabelProvider(new LabelProviderWrapper((ILabelProvider)labelProvider) {
					public Image getImage(Object element) {
						if (element instanceof CompareNode && ((CompareNode)element).getChangeType() == SVNEntryStatus.Kind.REPLACED) {
							Image image = (Image)this.images.get(element);
							if (image == null) {
								OverlayedImageDescriptor imageDescriptor = new OverlayedImageDescriptor(this.baseProvider.getImage(element), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replaced_2way.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
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
		protected int changeType;
		
		public CompareNode(IDiffContainer parent, int kind, IRepositoryResource next, IRepositoryResource prev, int changeType) {
			super(parent, kind);
			
			this.changeType = changeType;
			
			this.setRight(new ResourceElement(prev, null, changeType != SVNEntryStatus.Kind.ADDED));
			this.setLeft(new ResourceElement(next, null, changeType != SVNEntryStatus.Kind.DELETED));
		}

		public int getChangeType() {
			return this.changeType;
		}
		
	}
	
}
