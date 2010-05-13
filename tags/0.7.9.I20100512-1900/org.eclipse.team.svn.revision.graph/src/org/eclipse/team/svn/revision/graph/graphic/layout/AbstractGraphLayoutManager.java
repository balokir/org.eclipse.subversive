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
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionGraphEditPart;

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
		TimeMeasure layoutMeasure = new TimeMeasure("Layout"); //$NON-NLS-1$
						
		//set width and height
		Iterator<?> iter = this.graphPart.getChildren().iterator();
		while (iter.hasNext()) {
			RevisionEditPart editPart = (RevisionEditPart) iter.next();
			Dimension size = editPart.getRevisionFigure().getPreferredSize(-1, -1);
			RevisionNode node = editPart.getCastedModel();
			node.setSize(size.width, size.height);
		}		
				
		RevisionNode startNode = this.graphPart.getCastedModel().getCurrentStartNode();				
		if (startNode != null) {
			//make actual layout
			AbstractLayoutCommand[] layoutCommands = this.getLayoutCommands(startNode); 		
			for (AbstractLayoutCommand command : layoutCommands) {
				command.run();
			}	
		}		
		
		//apply changes
		this.graphPart.applyLayoutResults();
		
		layoutMeasure.end();
	}			
	
	protected abstract AbstractLayoutCommand[] getLayoutCommands(RevisionNode startNode);

}
