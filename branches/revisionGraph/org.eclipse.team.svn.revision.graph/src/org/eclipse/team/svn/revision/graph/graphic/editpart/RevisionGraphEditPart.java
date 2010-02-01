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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.layout.GraphLayoutManager;

/**
 * Root edit part
 * 
 * @author Igor Burilo
 */
public class RevisionGraphEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		//f.setBorder(new MarginBorder(3));
		//f.setOpaque(true);
		//f.setLayoutManager(new FreeformLayout());
						
		f.setLayoutManager(new GraphLayoutManager(this, 40));
		
		return f;
	}
	
	@Override
	protected List<RevisionNode> getModelChildren() {
		return this.getCastedModel().getChildren();
	}
	
	public RevisionRootNode getCastedModel() {
		return (RevisionRootNode) this.getModel();
	}

	@Override
	protected void createEditPolicies() {
		
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
		
		super.deactivate();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (RevisionRootNode.LAYOUT_PROPERTY.equals(evt.getPropertyName())) {
			refreshChildren();			
		}
	}
	
}
