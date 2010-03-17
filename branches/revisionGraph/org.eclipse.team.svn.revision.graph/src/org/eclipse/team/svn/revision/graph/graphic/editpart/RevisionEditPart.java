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
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;

/**
 * Edit part for revision node 
 *  
 * @author Igor Burilo
 */
public class RevisionEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {	
	
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
		
		super.deactivate();
	}
	
	@Override
	protected IFigure createFigure() {
		RevisionFigure figure = new RevisionFigure(this.getCastedModel());									
		//TODO figure.setToolTip(new RevisionTooltipFigure(this.getCastedModel()));		
		return figure;
	}	
	
	@Override
	protected void refreshVisuals() {	
		super.refreshVisuals();
					
		//update the figure using data from the model
		RevisionNode revision = this.getCastedModel();
		IFigure figure = getFigure();
		if (figure instanceof RevisionFigure) {
			RevisionFigure rFigure = (RevisionFigure) figure;
			String path = this.getRevisionRootNode().getRevisionPath(revision.pathRevision.getPathIndex());
			rFigure.init(revision.pathRevision.getRevision(), path);
		}					    
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
				((RevisionFigure) this.getHostFigure()).setSelected(true);				
			}			
			protected void hideSelection() {
				((RevisionFigure) this.getHostFigure()).setSelected(false);	
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
			IFigure figure = getFigure();
			if (figure instanceof RevisionFigure) {
				((RevisionFigure) figure).updateExpandCollapseStatus();
			}
		}
	}
}
