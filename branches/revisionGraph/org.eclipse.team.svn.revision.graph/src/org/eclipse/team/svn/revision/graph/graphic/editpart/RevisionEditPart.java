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
package org.eclipse.team.svn.revision.graph.graphic.editpart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.team.svn.revision.graph.graphic.ChangesNotifier;
import org.eclipse.team.svn.revision.graph.graphic.RevisionConnectionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.figure.ExpandCollapseDecorationFigure;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionTooltipFigure;

/**
 * Edit part for revision node 
 *  
 * @author Igor Burilo
 */
public class RevisionEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {				
	
	protected final static String REVISION_LAYER = "revision";
	protected final static String EXPAND_COLLAPSE_LAYER = "expandCollapse";
	
	protected LayeredPane mainPane;
	protected RevisionFigure revisionFigure;
	protected Layer expandLayer;
	protected ExpandCollapseDecorationFigure expandCollapseDecorationFigure;	
	
	protected NodeMouseMotionListener nodeMouseMotionListener;
	
	/*
	 * Show expand/collapse decoration
	 */
	protected class NodeMouseMotionListener extends MouseMotionListener.Stub {

		public void mouseEntered(MouseEvent me) {
			expandCollapseDecorationFigure.setBounds(revisionFigure.getBounds());									
			expandLayer.setVisible(true);
		}

		public void mouseExited(MouseEvent me) {			
			expandLayer.setVisible(false);			
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {		
		super.activate();						
		
		getCastedModel().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		getCastedModel().removePropertyChangeListener(this);
		
		if (this.nodeMouseMotionListener != null) {
			this.mainPane.removeMouseMotionListener(this.nodeMouseMotionListener);
		}
		
		super.deactivate();
	} 	

	@Override
	protected IFigure createFigure() {				
		this.mainPane = new LayeredPane();
		this.mainPane.addMouseMotionListener(this.nodeMouseMotionListener = new NodeMouseMotionListener());
		
		RevisionNode revision = this.getCastedModel();
		
		//main layer
		RevisionRootNode rootNode = this.getRevisionRootNode();
		String path = rootNode.getRevisionPath(revision.pathRevision.getPathIndex());				
		
		this.revisionFigure = new RevisionFigure(revision, path);													
		Layer revisionLayer = new Layer();			
		revisionLayer.add(this.revisionFigure);
		
		//expand/collapse layer
		this.expandCollapseDecorationFigure = new ExpandCollapseDecorationFigure(revision);			
		this.expandLayer = new Layer();					
		this.expandLayer.add(this.expandCollapseDecorationFigure);						
		this.expandLayer.setVisible(false);
					
		this.mainPane.add(revisionLayer, RevisionEditPart.REVISION_LAYER);
		this.mainPane.add(this.expandLayer, RevisionEditPart.EXPAND_COLLAPSE_LAYER);
					
		this.mainPane.setToolTip(new RevisionTooltipFigure(revision, rootNode.getDataContainer()));		
		
		return this.mainPane;
	}	
	
	public RevisionFigure getRevisionFigure() {
		return this.revisionFigure;
	}
	
	public Layer getExpandLayer() {
		return this.expandLayer;
	}
	
	@Override
	protected void refreshVisuals() {	
		super.refreshVisuals();					
		
		this.revisionFigure.init();
	}
	
	public RevisionRootNode getRevisionRootNode() {
		RevisionRootNode root = ((RevisionGraphEditPart) getParent()).getCastedModel();
		return root;
	}
	
	public RevisionNode getCastedModel() {
		return (RevisionNode) getModel();
	}
		
	@Override
	protected List<RevisionConnectionNode> getModelSourceConnections() {
		RevisionRootNode root = this.getRevisionRootNode();
		return root.getConnections(this.getCastedModel(), true);
	}
	
	@Override
	protected List<RevisionConnectionNode> getModelTargetConnections() {
		RevisionRootNode root = this.getRevisionRootNode();
		return root.getConnections(this.getCastedModel(), false);
	}	
	
	@Override
	protected void createEditPolicies() { 
		this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new SelectionEditPolicy() {			
			protected void showSelection() {
				revisionFigure.setSelected(true);				
			}			
			protected void hideSelection() {
				revisionFigure.setSelected(false);	
			}
		});	
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RevisionEditPart) {
			return this.getCastedModel().equals(((RevisionEditPart) obj).getCastedModel());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 * 
	 * As we don't allow dragging, override basic implementation
	 */
	@Override
	public DragTracker getDragTracker(Request request) {		
		return new SelectEditPartTracker(this); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {	
		return new ChopboxAnchor(this.getFigure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(this.getFigure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 * 
	 * Listen to model notifications
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (ChangesNotifier.REFRESH_CONNECTIONS_PROPERTY.equals(evt.getPropertyName())) {
			this.refreshSourceConnections();
			this.refreshTargetConnections();
		} else if (ChangesNotifier.EXPAND_COLLAPSE_PROPERTY.equals(evt.getPropertyName())) {			
			this.expandCollapseDecorationFigure.update();
		}
	}
}
