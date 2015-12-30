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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.graphic.MergeConnectionNode;
import org.eclipse.team.svn.revision.graph.graphic.MergeConnectionRouter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Edit part for merge connection
 * 
 * @author Igor Burilo
 */
public class MergeConnectionEditPart extends RevisionConnectionEditPart {	
	
	public final static Color MERGE_LINES_COLOR = new Color(UIMonitorUtility.getDisplay(), 98, 148, 229);
	
	static {
		SVNRevisionGraphPlugin.disposeOnShutdown(MERGE_LINES_COLOR);
	}
	
	@Override
	public MergeConnectionNode getCastedModel() {
		return (MergeConnectionNode) getModel();
	}
	
	@Override
	protected IFigure createFigure() {					
		PolylineConnection figure = new PolylineConnection();
		figure.setForegroundColor(MERGE_LINES_COLOR);
		//add arrow in the end
		figure.setTargetDecoration(new PolygonDecoration());		
		
		//set router
		MergeConnectionRouter router = ((RevisionGraphEditPart) this.getViewer().getContents()).getMergeConnectionRouter();
		figure.setConnectionRouter(router);		
		return figure;		
	}

}
