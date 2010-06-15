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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.team.svn.revision.graph.graphic.ChangesNotifier;
import org.eclipse.team.svn.revision.graph.graphic.MergeConnectionRouter;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.layout.GraphLayoutManager;

/**
 * Root edit part
 * 
 * @author Igor Burilo
 */
public class RevisionGraphEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	
	//TODO move all graph constants in one place
	public final static int NODES_HORIZONTAL_OFFSET = 40;
	
	//connection routers shared by connections
	protected final MergeConnectionRouter mergeConnectionRouter = new MergeConnectionRouter();
	protected final ConnectionRouter connectionRouter = new ManhattanConnectionRouter();
	
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		//f.setBorder(new MarginBorder(3));
		//f.setOpaque(true);
		//f.setLayoutManager(new FreeformLayout());
					
		f.setLayoutManager(new GraphLayoutManager(this, NODES_HORIZONTAL_OFFSET));
		
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
				
		RevisionRootNode model = getCastedModel();
		model.addPropertyChangeListener(this);
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
		if (RevisionRootNode.FILTER_NODES_PROPERTY.equals(evt.getPropertyName()) ||
			ChangesNotifier.EXPAND_COLLAPSE_NODES_PROPERTY.equals(evt.getPropertyName())) {
			refreshChildren();			
		}
	}
	
	public void applyLayoutResults() {
		Iterator<?> iter = this.getChildren().iterator();
		while (iter.hasNext()) {
			RevisionEditPart editPart = (RevisionEditPart) iter.next();
			editPart.applyLayoutResults();									
		}	
	}
	
	public ConnectionRouter getConnectionRouter() {
		return this.connectionRouter;
	}
	
	public MergeConnectionRouter getMergeConnectionRouter() {
		return this.mergeConnectionRouter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshChildren()
	 * 
	 * Override implementation in base class because there can be cases where
	 * not all previous children are deleted. Example:
	 * 
	 * Previous children:
	 * 1 2 3
	 * 
	 * New children:
	 * 2 4
	 * 
	 * In base class implementation '1' element will not be deleted
	 * and as a result we'll get incorrect layout
	 */
	@Override
	protected void refreshChildren() {
		int i;
		EditPart editPart;
		Object model;

		Map modelToEditPart = new HashMap();
		List children = getChildren();
		
		List oldChildren = new ArrayList(children);

		for (i = 0; i < children.size(); i++) {
			editPart = (EditPart)children.get(i);
			modelToEditPart.put(editPart.getModel(), editPart);
		}

		List modelObjects = getModelChildren();
		
		HashSet<Object> newModelObjectsHashSet = new HashSet<Object>();
		
		for (i = 0; i < modelObjects.size(); i++) {
			model = modelObjects.get(i);						

			newModelObjectsHashSet.add(model);									
			
			//Do a quick check to see if editPart[i] == model[i]
			if (i < children.size()
				&& ((EditPart) children.get(i)).getModel() == model)
					continue;

			//Look to see if the EditPart is already around but in the wrong location
			editPart = (EditPart)modelToEditPart.get(model);

			if (editPart != null)
				reorderChild (editPart, i);
			else {
				//An editpart for this model doesn't exist yet.  Create and insert one.
				editPart = createChild(model);
				addChild(editPart, i);
			}
		}
			
//		List trash = new ArrayList();
//		for (; i < children.size(); i++)
//			trash.add(children.get(i));
//		for (i = 0; i < trash.size(); i++) {
//			EditPart ep = (EditPart)trash.get(i);
//			//System.out.println("remove child: " + ep);
//			removeChild(ep);
//		}
		
		//remove not existed elements
		for (int j = 0; j < oldChildren.size(); j++) {
			EditPart oldEditPart = (EditPart) oldChildren.get(j);						
			if (!newModelObjectsHashSet.contains(oldEditPart.getModel())) {
				removeChild(oldEditPart);
			}
		}		
	}
	
}
