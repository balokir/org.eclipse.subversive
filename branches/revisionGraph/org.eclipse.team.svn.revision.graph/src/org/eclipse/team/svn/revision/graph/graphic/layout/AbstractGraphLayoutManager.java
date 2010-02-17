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
package org.eclipse.team.svn.revision.graph.graphic.layout;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionGraphEditPart;
import org.eclipse.team.svn.revision.graph.investigate.TimeMeasure;

/**
 * Base class for graph layout managers
 * 
 * @author Igor Burilo
 */
public abstract class AbstractGraphLayoutManager extends AbstractLayout {

	protected RevisionGraphEditPart graphPart;
		
	public AbstractGraphLayoutManager(RevisionGraphEditPart graphPart) {
		this.graphPart = graphPart;
	}
	
	@Override
	protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
		container.validate();
		List<?> children = container.getChildren();
		Rectangle result = new Rectangle().setLocation(container.getClientArea().getLocation());
		for (int i = 0; i < children.size(); i++) {
			result.union(((IFigure) children.get(i)).getBounds());
		}		
		result.resize(container.getInsets().getWidth(), container.getInsets().getHeight());
		return result.getSize();	
	}
		
	public void layout(IFigure container) {	
		TimeMeasure layoutMeasure = new TimeMeasure("Layout");
		
		RevisionNode startNode = null;
		
		//set width and height
		Iterator<?> iter = this.graphPart.getChildren().iterator();
		while (iter.hasNext()) {
			RevisionEditPart editPart = (RevisionEditPart) iter.next();					
			Dimension size = editPart.getFigure().getPreferredSize(-1, -1);
			RevisionNode node = editPart.getCastedModel();
			node.setSize(size.width, size.height);
			if (startNode == null && node.getCopiedFrom() == null && node.getPrevious() == null) {
				startNode = node;
			}
		}		
		
		//make actual layout
		AbstractLayoutCommand[] layoutCommands = this.getLayoutCommands(startNode); 		
		for (AbstractLayoutCommand command : layoutCommands) {
			command.run();
		}		
		
		//apply changes
		iter = this.graphPart.getChildren().iterator();
		while (iter.hasNext()) {
			RevisionEditPart editPart = (RevisionEditPart) iter.next();
			RevisionNode node = editPart.getCastedModel();
			Rectangle bounds = new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight());
			editPart.getFigure().setBounds(bounds);
		}	
		
		layoutMeasure.end();
	}			
	
	protected abstract AbstractLayoutCommand[] getLayoutCommands(RevisionNode startNode);

}
