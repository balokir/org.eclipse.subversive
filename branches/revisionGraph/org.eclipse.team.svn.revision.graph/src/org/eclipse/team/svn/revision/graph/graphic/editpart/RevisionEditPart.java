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
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.ChangesNotifier;
import org.eclipse.team.svn.revision.graph.graphic.RevisionConnectionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;

/**
 * Edit part for revision node
 * 
 * TODO correctly work with system resources, i.e. fonts, colors etc.
 *  
 * @author Igor Burilo
 */
public class RevisionEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {	
	
	protected Color bgColor;
	
	//TODO correctly handle system resources
	protected final Color trunkColor = new Color(null, 188, 255, 188);
	protected final Color branchColor = new Color(null, 229, 255, 229);
	protected final Color tagColor = new Color(null, 239, 252, 162);	
	protected final Color createOrCopyColor = new Color(null, 229, 255, 229);	
	protected final Color renameColor = new Color(null, 229, 229, 255);
	protected final Color deleteColor = new Color(null, 255, 229, 229);	
	protected final Color modifyColor = new Color(null, 229, 229, 229);
	protected final Color otherColor = new Color(null, 255, 255, 255);
	
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
		RevisionNode node = this.getCastedModel();		
		ReviosionNodeType type = node.pathRevision.type;
		RevisionNodeAction action = node.pathRevision.action;
		
		IFigure figure;
		Color color;
		if (ReviosionNodeType.TRUNK.equals(type)) {
			color = this.trunkColor;
			figure = new RevisionFigure();
		} else if (ReviosionNodeType.BRANCH.equals(type)) {
			color = this.branchColor;
			figure = new RevisionFigure();
		} else if (ReviosionNodeType.TAG.equals(type)) {
			color = this.tagColor;
			figure = new RevisionFigure();
		} else if (RevisionNodeAction.ADD.equals(action) || RevisionNodeAction.COPY.equals(action)) {
			color = this.createOrCopyColor;
			figure = new RevisionFigure();
		} else if (RevisionNodeAction.RENAME.equals(action)) {
			color = this.renameColor;
			figure = new RevisionFigure();
		} else if (RevisionNodeAction.DELETE.equals(action)) {
			color = this.deleteColor;			
			figure = new RevisionFigure();
		} else if (RevisionNodeAction.MODIFY.equals(action)) {
			color = this.modifyColor;
			figure = new RevisionFigure();
		} else {
			color = this.otherColor;
			figure = new RevisionFigure();
		}					
				
		figure.setBackgroundColor(this.bgColor = color);				
		
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
	
	protected RevisionRootNode getRevisionRootNode() {
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
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RevisionEditPart) {
			return this.getCastedModel().equals(((RevisionEditPart) obj).getCastedModel());
		}
		return false;
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
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (ChangesNotifier.REFRESH_CONNECTIONS_PROPERTY.equals(evt.getPropertyName())) {
			this.refreshSourceConnections();
			this.refreshTargetConnections();
		}		
	}
}
