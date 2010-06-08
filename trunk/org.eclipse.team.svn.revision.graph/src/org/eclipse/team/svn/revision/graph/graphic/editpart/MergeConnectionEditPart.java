/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.editpart;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.team.svn.revision.graph.graphic.MergeConnectionRouter;

/**
 * Edit part for merge connection
 * 
 * @author Igor Burilo
 */
public class MergeConnectionEditPart extends RevisionConnectionEditPart {	
	
	@Override
	protected IFigure createFigure() {					
		PolylineConnection figure = new PolylineConnection();	
		//TODO make correct color
		figure.setForegroundColor(ColorConstants.red);
		//add arrow in the end
		figure.setTargetDecoration(new PolygonDecoration());		
		
		//set router
		MergeConnectionRouter router = ((RevisionGraphEditPart) this.getViewer().getContents()).getMergeConnectionRouter();
		figure.setConnectionRouter(router);		
		return figure;		
	}

}
