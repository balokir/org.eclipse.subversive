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
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
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
import org.eclipse.team.svn.revision.graph.graphic.figure.ExpandCollapseDecoration;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionTooltipFigure;

/**
 * Edit part for revision node
 * 
 * @author Igor Burilo
 */
public class RevisionEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {				
			
	protected RevisionFigure revisionFigure;		
	protected ExpandCollapseDecoration collapseDecoration;
	
	protected NodeMouseMotionListener nodeMouseMotionListener;
	
	protected class NodeMouseMotionListener extends MouseMotionListener.Stub {
		public void mouseEntered(MouseEvent me) {
			collapseDecoration.showExpanded(true);
		}
		public void mouseExited(MouseEvent me) {
			if (!collapseDecoration.containsPoint(me.getLocation())) {
				//System.out.println("--main: mouseExited");
				collapseDecoration.showExpanded(false);
			}			
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
			this.revisionFigure.removeMouseMotionListener(this.nodeMouseMotionListener);
		}
		
		if (this.collapseDecoration != null) {
			this.collapseDecoration.removeDecoration();
		}
		
		super.deactivate();
	} 	

	@Override
	protected IFigure createFigure() {
		RevisionNode revision = this.getCastedModel();				
		String path = revision.getPath();						
		this.revisionFigure = new RevisionFigure(revision, path);
		
		this.revisionFigure.setToolTip(new RevisionTooltipFigure(revision));
		this.revisionFigure.addMouseMotionListener(this.nodeMouseMotionListener = new NodeMouseMotionListener());			
		
		IFigure decorationLayer = this.getLayer(GraphScalableRootEditPart.DECORATION_LAYER);
		this.collapseDecoration = new ExpandCollapseDecoration(revision, decorationLayer);
		
		return this.revisionFigure;
	}
		
	public void applyLayoutResults() {
		RevisionNode node = this.getCastedModel();
		Rectangle bounds = new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight());				
		this.revisionFigure.setBounds(bounds);		
		this.collapseDecoration.setDecoratedFigure(this.revisionFigure);			
		
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
			//remove old decoration			
			this.collapseDecoration.internalShowExpanded(false);
			this.collapseDecoration.removeDecoration();			
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
