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

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
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
import org.eclipse.team.svn.revision.graph.graphic.RevisionSourceAnchor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionTargetAnchor;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionTooltipFigure;

/**
 * Edit part for revision node
 *  
 * TODO add expand/collapse
 * 
 * @author Igor Burilo
 */
public class RevisionEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {				
	
//	protected final static String REVISION_LAYER = "revision"; //$NON-NLS-1$
//	protected final static String COLLAPSE_LAYER = "collapse"; //$NON-NLS-1$
//	protected final static String EXPAND_LAYER = "expand"; //$NON-NLS-1$
	
//	protected LayeredPane mainPane;
	protected RevisionFigure revisionFigure;
	
//	protected ExpandCollapseDecorationFigure collapseFigure;
//	protected Layer expandLayer;
//	protected ExpandCollapseDecorationFigure expandFigure;	
//	protected NodeMouseMotionListener nodeMouseMotionListener;
//	
//	/*
//	 * Show expand/collapse decoration
//	 * 
//	 * TODO There are cases in which expand/collapse decoration isn't removed
//	 * when we leave revision node: it happens when we leave
//	 * node by moving mouse cursor over plus/minus icon. 
//	 */
//	protected class NodeMouseMotionListener extends MouseMotionListener.Stub {
//		
//		public void mouseEntered(MouseEvent me) {
//			RevisionEditPart.this.addExpandFigure();
//		}
//
//		public void mouseExited(MouseEvent me) {
//			if (!mainPane.getBounds().contains(me.x, me.y)) { 
//				RevisionEditPart.this.removeExpandFigure();
//			}
//		}
//	}
//	
//	public void removeExpandFigure() {
//		this.expandLayer.setVisible(false);
//	}
//	
//	public void addExpandFigure() {
//		this.expandLayer.setVisible(true);
//	}
	
	
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
		
//		if (this.nodeMouseMotionListener != null) {
//			this.mainPane.removeMouseMotionListener(this.nodeMouseMotionListener);
//		}
		
		super.deactivate();
	} 	

	@Override
	protected IFigure createFigure() {				
//		this.mainPane = new LayeredPane();
//		this.mainPane.addMouseMotionListener(this.nodeMouseMotionListener = new NodeMouseMotionListener());
		
		RevisionNode revision = this.getCastedModel();
		
		//main layer
		String path = revision.getPath();				
		
		this.revisionFigure = new RevisionFigure(revision, path);													
//		Layer revisionLayer = new Layer();			
//		revisionLayer.add(this.revisionFigure);
		
//		//expand/collapse layers
//		Layer collapseLayer = new Layer();
//		this.collapseFigure = new ExpandCollapseDecorationFigure(revision, true);
//		collapseLayer.add(this.collapseFigure);				
//		
//		this.expandLayer = new Layer();											
//		this.expandFigure = new ExpandCollapseDecorationFigure(revision, false);
//		this.expandLayer.add(this.expandFigure);
//		this.expandLayer.setVisible(false);
//		
//		this.mainPane.add(revisionLayer, RevisionEditPart.REVISION_LAYER);
//		
//		this.mainPane.add(collapseLayer, RevisionEditPart.COLLAPSE_LAYER);
//		this.mainPane.add(this.expandLayer, RevisionEditPart.EXPAND_LAYER);
//					
//		this.mainPane.setToolTip(new RevisionTooltipFigure(revision, rootNode.getRepositoryCache()));		
//				
//		return this.mainPane;
		
		this.revisionFigure.setToolTip(new RevisionTooltipFigure(revision));
		
		return this.revisionFigure;
	}
	
	public void applyLayoutResults() {
		RevisionNode node = this.getCastedModel();
		Rectangle bounds = new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight());
//		this.getFigure().setBounds(bounds);
		this.revisionFigure.setBounds(bounds);
		
//		this.collapseFigure.setBounds(bounds);
//		this.expandFigure.setBounds(bounds);
		
//		Iterator<?> conIter = this.getSourceConnections().iterator();
//		while (conIter.hasNext()) {
//			RevisionConnectionEditPart conEditPart = (RevisionConnectionEditPart) conIter.next();
//			conEditPart.applyLayoutResults();
//		}
	}
	
	public RevisionFigure getRevisionFigure() {
		return this.revisionFigure;
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

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 * 
	 * As we don't allow dragging, override basic implementation
	 */
	@Override
	public DragTracker getDragTracker(Request request) {		
		return new SelectEditPartTracker(this); 
	}
	
	
	//--- anchors
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {	
		RevisionConnectionNode conNode = ((RevisionConnectionEditPart) connection).getCastedModel(); 
		return new RevisionSourceAnchor(this.getFigure(), conNode.getSource(), conNode.getTarget());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		//it should never happen
		throw new IllegalStateException();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new RevisionTargetAnchor(this.getFigure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */	
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		//it should never happen
		throw new IllegalStateException();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 * 
	 * Listen to model notifications
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (ChangesNotifier.REFRESH_NODE_CONNECTIONS_PROPERTY.equals(evt.getPropertyName())) {
			this.refreshSourceConnections();
			this.refreshTargetConnections();
		} else if (ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY.equals(evt.getPropertyName())) {
//			this.collapseFigure.update();
//			this.expandFigure.update();
//			
//			this.removeExpandFigure();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RevisionEditPart) {
			return this.getCastedModel().equals(((RevisionEditPart) obj).getCastedModel());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getCastedModel().hashCode();
	};
}
