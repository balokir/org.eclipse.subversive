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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/** 
 * Add expand/collapse decoration to figure
 * 
 * It's layout aware
 * 
 * @author Igor Burilo
 */
public class ExpandCollapseDecoration {

	public final static Image MINUS_IMAGE; 
	public final static Image PLUS_IMAGE;
	
	protected final static Dimension IMAGE_SIZE;
	
	protected final static int EDGE_SPACING = 2;
	
	protected final RevisionNode revisionNode;
	protected IFigure parent;
	protected Figure decoratedFigure;
	
	protected boolean isShowExpanded;
	protected boolean isShowCollapsed;
	
	protected RevisionNode rename;
	protected List<RevisionNode> onlyCopyTo;
	
	protected ImageFigure topFigure;
	protected ImageFigure rightFigure;
	protected ImageFigure bottomFigure;		 
	
	protected Point topLocation;
	protected Point rightLocation;
	protected Point bottomLocation;
	
	/*
	 * Hide expanded figures when we exit from them.
	 * We can use the same listener for all figures.
	 */
	protected MouseMotionListener figureMouseListener = new MouseMotionListener.Stub() {		
		public void mouseExited(MouseEvent me) { 			
			//System.out.println("--decoration: exited");					
			showExpanded(false);			
		}		
	};
	
	protected enum Status { EXPANDED, COLLAPSED, NONE };
	
	static {
		MINUS_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/minus.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(MINUS_IMAGE);
		
		PLUS_IMAGE = SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/plus.gif").createImage(); //$NON-NLS-1$
		SVNRevisionGraphPlugin.disposeOnShutdown(PLUS_IMAGE);
		
		//assume that minus and plus images have the same size
		IMAGE_SIZE = new Rectangle(MINUS_IMAGE.getBounds()).getSize();
	}
	
	public ExpandCollapseDecoration(RevisionNode revisionNode, IFigure parent) {
		this.revisionNode = revisionNode;
		this.parent = parent;		
		this.isShowCollapsed = true;
		this.isShowExpanded = false;
	}			
	
	//Re-create all figures on each update
	protected void updateControls() {
		//remove old
		this.removeDecoration();
		
		this.addTopFigure();
		this.addRightFigure();
		this.addBottomFigure();
	}
	
	protected boolean isApplicableStatus(Status status) {
		return status == Status.COLLAPSED && this.isShowCollapsed ||
			   status == Status.EXPANDED && this.isShowExpanded;
	}
	
	
	//--- top figure
	
	protected void addTopFigure() {
		final Status status = this.getTopStatus();
		if (this.isApplicableStatus(status)) {			
			this.topFigure = new ImageFigure();
			this.parent.add(this.topFigure);
						
			this.topFigure.setImage(getIcon(status));												
			this.topFigure.setBounds(new Rectangle(this.topLocation, IMAGE_SIZE));
			
			this.topFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {					
					/*
					 * When we mouse press on plus/minus, then cursor is
					 * changed and switched to selection mode, which isn't what
					 * user expects.
					 * Source of the problem: as plus/minus isn't an EditPart
					 * we have such effect in contrast to revision nodes and connections
					 * which are edit parts. This also applies to other figures: bottom, right etc.
					 * As a workaround mark event as consumed.					
					 */
					me.consume();
															
					ExpandCollapseDecoration.this.processTop(status);
				}
			});
			
			this.topFigure.addMouseMotionListener(this.figureMouseListener);			
		}
	}
			
	protected void processTop(Status status) {		
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
	
	protected Status getTopStatus() {
		Status status = Status.NONE;				
		if (this.revisionNode.isNextCollapsed() || this.revisionNode.isRenameCollapsed()) {
			status = Status.COLLAPSED;
		} else if (this.revisionNode.getNext() != null || this.rename != null) {
			status = Status.EXPANDED;
		}			
		return status;
	}
	
	protected Point getTopLocation() {
		Rectangle bounds = this.decoratedFigure.getClientArea();
		Point midTop = bounds.getTop();		
		Point result = new Point(midTop.x - IMAGE_SIZE.width / 2, midTop.y + EDGE_SPACING);				
		return result;
	}
			
	
	//--- right figure
	
	protected void addRightFigure() {		
		final Status status = this.getRightStatus();
		if (this.isApplicableStatus(status)) {			
			this.rightFigure = new ImageFigure();
			this.parent.add(this.rightFigure);
						
			this.rightFigure.setImage(getIcon(status));												
			this.rightFigure.setBounds(new Rectangle(this.rightLocation, IMAGE_SIZE));
			
			this.rightFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {										
					me.consume();															
					ExpandCollapseDecoration.this.processRight(status);
				}
			});
			
			this.rightFigure.addMouseMotionListener(this.figureMouseListener);
		}
	}
	
	protected Status getRightStatus() {
		Status status = Status.NONE;		
		if (this.revisionNode.isCopiedToCollapsed()) {
			status = Status.COLLAPSED;	
		} else if (!this.onlyCopyTo.isEmpty()) {
			status = Status.EXPANDED;
		}
		return status;
	}
	
	protected void processRight(Status status) {			
		boolean isCollapsed = status == Status.COLLAPSED;
		this.revisionNode.setCopiedToCollapsed(!isCollapsed);					
	}
	
	protected Point getRightLocation() {
		Rectangle bounds = this.decoratedFigure.getClientArea();
		Point midRight = bounds.getRight();
		Point result = new Point(midRight.x - IMAGE_SIZE.width - EDGE_SPACING, midRight.y - IMAGE_SIZE.height / 2);
		return result;
	}
	
	
	//--- bottom figure
	
	protected void addBottomFigure() {
		final Status status = this.getBottomStatus();
		if (this.isApplicableStatus(status)) {			
			this.bottomFigure = new ImageFigure();
									
			this.parent.add(this.bottomFigure);					
						
			this.bottomFigure.setImage(getIcon(status));												
			this.bottomFigure.setBounds(new Rectangle(this.bottomLocation, IMAGE_SIZE));
			
			this.bottomFigure.addMouseListener(new MouseListener.Stub() {
				public void mousePressed(MouseEvent me) {										
					me.consume();															
					ExpandCollapseDecoration.this.processBottom(status);
				}
			});
			
			this.bottomFigure.addMouseMotionListener(this.figureMouseListener);
		}
	}	
	
	protected Status getBottomStatus() {
		Status status = Status.NONE;		
		if (this.revisionNode.isPreviousCollapsed() || this.revisionNode.isCopiedFromCollapsed()) {
			status = Status.COLLAPSED;
		} else if (this.revisionNode.getPrevious() != null || this.revisionNode.getCopiedFrom() != null) {
			status = Status.EXPANDED;		
		}				
		return status;
	}
	
	protected void processBottom(Status status) {									
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
	
	protected Point getBottomLocation() {
		Rectangle bounds = this.decoratedFigure.getClientArea();
		Point bottomTop = bounds.getBottom();		
		Point result = new Point(bottomTop.x - IMAGE_SIZE.width / 2, bottomTop.y - IMAGE_SIZE.height - EDGE_SPACING);				
		return result;
	}
	
	
	public boolean containsPoint(Point point) {		
		if (this.topFigure != null && isFigureContainsPoint(this.topFigure, point)) {
			return true;
		} else if (this.rightFigure != null && isFigureContainsPoint(this.rightFigure, point)) {
			return true;
		} else if (this.bottomFigure != null && isFigureContainsPoint(this.bottomFigure, point)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Re-implement this method from {@link IFigure#containsPoint(Point)} because
	 * in it the boundaries exclusive of the bottom and right edges. 
	 * But in our case we need to check them as inclusive. 
	 */
	public static boolean isFigureContainsPoint(IFigure figure, Point point) {
		int x = point.x;
		int y = point.y;		
		Rectangle bounds = figure.getBounds();
		return y >= bounds.y &&
			   y <= bounds.y + bounds.height && 
			   x >= bounds.x && 
			   x <= bounds.x + bounds.width;
	}
	
	protected static Image getIcon(Status status) {
		return status == Status.COLLAPSED ? ExpandCollapseDecoration.PLUS_IMAGE : 
			(status == Status.EXPANDED ? ExpandCollapseDecoration.MINUS_IMAGE : null);
	}
	
	public void setDecoratedFigure(Figure decoratedFigure) {
		this.decoratedFigure = decoratedFigure;				
		
		//re-calculate
		this.onlyCopyTo = null;
		this.rename = null;		
		this.onlyCopyTo = new ArrayList<RevisionNode>();
		for (RevisionNode node : this.revisionNode.getCopiedTo()) {
			if (node.getAction() == RevisionNodeAction.RENAME) {
				this.rename = node;
			} else {
				this.onlyCopyTo.add(node);
			}
		}
		this.topLocation = this.getTopLocation();
		this.rightLocation = this.getRightLocation();
		this.bottomLocation = this.getBottomLocation();
		
		this.updateControls();
	}
	
	public void showExpanded(boolean isShowExpanded) {							
		this.internalShowExpanded(isShowExpanded);
		this.updateControls();
	}
	
	public void internalShowExpanded(boolean isShowExpanded) {
		//System.out.println("show expanded: " + isShowExpanded);		
		this.isShowExpanded = isShowExpanded;
	}		
	
	public void removeDecoration() {	
		if (this.topFigure != null) {
			this.parent.remove(this.topFigure);
			this.topFigure = null;
		}
		
		if (this.rightFigure != null) {
			this.parent.remove(this.rightFigure);
			this.rightFigure = null;
		}
		
		if (this.bottomFigure != null) {
			this.parent.remove(this.bottomFigure);
			this.bottomFigure = null;
		}
	}
		
}
