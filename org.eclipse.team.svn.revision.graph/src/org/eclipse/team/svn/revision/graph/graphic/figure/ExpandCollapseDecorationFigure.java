/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/** 
 * Add expand/collapse decoration to main figure
 * 
 * It's layout aware
 * 
 * @author Igor Burilo
 */
public class ExpandCollapseDecorationFigure extends Figure {

	public final static Image MINUS_IMAGE; 
	public final static Image PLUS_IMAGE;
	
	protected final RevisionNode revisionNode;
	protected final boolean isShowOnlyCollapse;
	
	protected RevisionNode rename;
	protected List<RevisionNode> onlyCopyTo;
	
	protected ImageFigure topFigure;
	protected ImageFigure rightFigure;
	protected ImageFigure bottomFigure;
	
	protected enum Status { EXPANDED, COLLAPSED, NONE };
	
	static {
		MINUS_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/minus.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(MINUS_IMAGE);
		
		PLUS_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/plus.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(PLUS_IMAGE);
	}
	
	public ExpandCollapseDecorationFigure(RevisionNode revisionNode, boolean isShowOnlyCollapse) {
		this.revisionNode = revisionNode;
		this.isShowOnlyCollapse = isShowOnlyCollapse;
		
		this.onlyCopyTo = new ArrayList<RevisionNode>();
		for (RevisionNode node : this.revisionNode.getCopiedTo()) {
			if (node.getAction() == RevisionNodeAction.RENAME) {
				this.rename = node;
			} else {
				this.onlyCopyTo.add(node);
			}
		}
		
		this.createControls();
		this.initControls();
				
		//make transparent
		this.setOpaque(false);
	}	
	
	protected void createControls() {
		GridLayout layout = new GridLayout();		
		layout.numColumns = 3;		
		layout.marginHeight = layout.marginWidth = 2;
		layout.horizontalSpacing = layout.verticalSpacing = 3;
			
		this.setLayoutManager(layout);
						
		//top
		Status status = this.getTopStatus();
		if (status != null) {
			this.topFigure = new ImageFigure();
			this.add(this.topFigure);
			GridData data = new GridData();
			data.horizontalAlignment = SWT.CENTER;
			data.horizontalSpan = 3;
			layout.setConstraint(this.topFigure, data);				
			
			this.topFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {
					ExpandCollapseDecorationFigure.this.processTop();
				}
			});	
		}
		
		//content
		Figure contentFigure = new Figure();
		this.add(contentFigure);	
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);		
		layout.setConstraint(contentFigure, data);				
		
		//right
		status = this.getRightStatus();
		if (status != null) {
			this.rightFigure = new ImageFigure();
			this.add(this.rightFigure);
			data = new GridData();
			data.verticalAlignment = SWT.CENTER;
			layout.setConstraint(this.rightFigure, data);				

			this.rightFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {
					ExpandCollapseDecorationFigure.this.processRight();
				}
			});	
		}
		
		//bottom
		status = this.getBottomStatus();
		if (status != null) {
			this.bottomFigure = new ImageFigure();
			this.add(this.bottomFigure);
			data = new GridData();
			data.horizontalAlignment = SWT.CENTER;
			data.horizontalSpan = 3;
			layout.setConstraint(this.bottomFigure, data);
			
			this.bottomFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {
					ExpandCollapseDecorationFigure.this.processBottom();
				}
			});	
		}
	}

	public void update() {
		this.removeAll();
		
		this.createControls();
		this.initControls();
	}
	
	protected void initControls() {				
		//top: next and rename		
		Status status = this.getTopStatus();		
		this.topFigure.setImage(this.getIcon(status));
		
		//right: copy to without rename
		status = this.getRightStatus();
		this.rightFigure.setImage(this.getIcon(status));
		
		//bottom: previous and copied from
		status = this.getBottomStatus();
		this.bottomFigure.setImage(this.getIcon(status));									
	}
	
	protected Image getIcon(Status status) {
		return status == Status.COLLAPSED ? ExpandCollapseDecorationFigure.PLUS_IMAGE : 
			(status == Status.EXPANDED ? ExpandCollapseDecorationFigure.MINUS_IMAGE : null);
	}
	
	protected Status postProcessStatus(Status status) {
		return status = this.isShowOnlyCollapse ? 
				(status == Status.COLLAPSED ? Status.COLLAPSED : Status.NONE) :
				(status == Status.EXPANDED ? Status.EXPANDED : Status.NONE);
	}
	
	protected Status getTopStatus() {
		Status status = Status.NONE;				
		if (this.revisionNode.isNextCollapsed() || this.revisionNode.isRenameCollapsed()) {
			status = Status.COLLAPSED;
		} else if (this.revisionNode.getNext() != null || this.rename != null) {
			status = Status.EXPANDED;
		}			
		return this.postProcessStatus(status);
	}
	
	protected void processTop() {
		Status status = this.getTopStatus();
		if (status != Status.NONE) {
			boolean isCollapsed = status == Status.COLLAPSED;
			if (isCollapsed) {
				if (this.revisionNode.isNextCollapsed()) {
					this.revisionNode.setNextCollapsed(false);
				} else {
					this.revisionNode.setRenameCollapsed(false);
				}
			} else {
				if (this.rename != null) {
					this.revisionNode.setRenameCollapsed(true);
				} else {
					this.revisionNode.setNextCollapsed(true);	
				}		
			}	
		}
	}
	
	protected Status getRightStatus() {
		Status status = Status.NONE;		
		if (this.revisionNode.isCopiedToCollapsed()) {
			status = Status.COLLAPSED;	
		} else if (!this.onlyCopyTo.isEmpty()) {
			status = Status.EXPANDED;
		}
		return this.postProcessStatus(status);
	}
	
	protected void processRight() {
		Status status = this.getRightStatus();
		if (status != Status.NONE) {			
			boolean isCollapsed = status == Status.COLLAPSED;
			this.revisionNode.setCopiedToCollapsed(!isCollapsed);			
		}		
	}
	
	protected Status getBottomStatus() {
		Status status = Status.NONE;		
		if (this.revisionNode.isPreviousCollapsed() || this.revisionNode.isCopiedFromCollapsed()) {
			status = Status.COLLAPSED;
		} else if (this.revisionNode.getPrevious() != null || this.revisionNode.getCopiedFrom() != null) {
			status = Status.EXPANDED;		
		}				
		return this.postProcessStatus(status);
	}
	
	protected void processBottom() {
		Status status = this.getBottomStatus();
		if (status != Status.NONE) {									
			boolean isCollapsed = status == Status.COLLAPSED;
			if (isCollapsed) {
				if (this.revisionNode.isPreviousCollapsed()) {
					this.revisionNode.setPreviousCollapsed(false);
				} else {
					this.revisionNode.setCopiedFromCollapsed(false);
				}
			} else {
				if (this.revisionNode.getPrevious() != null) {
					this.revisionNode.setPreviousCollapsed(true);
				} else {
					this.revisionNode.setCopiedFromCollapsed(true);
				}	
			}										
		}		
	}
	
}
