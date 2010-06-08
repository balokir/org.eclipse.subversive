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
package org.eclipse.team.svn.revision.graph.graphic;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionGraphEditPart;

/**
 * Router for merge connections
 * 
 * @author Igor Burilo
 */
public class MergeConnectionRouter extends AbstractRouter {
		
	/*
	 * we need to know all model in order to avoid intersections                                             
	 * between revision nodes and merge connections
	 */
	protected RevisionRootNode rootNode;
		
	public void setRevisionRootNode(RevisionRootNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public void route(Connection conn) {
		if ((conn.getSourceAnchor() == null) || (conn.getTargetAnchor() == null)) {
			return;
		}
		
//		IFigure startFigure = conn.getSourceAnchor().getOwner();
//		IFigure endFigure = conn.getTargetAnchor().getOwner();						
//		Rectangle startBound = startFigure.getBounds();
//		Rectangle endBound = endFigure.getBounds();								
		
		//points in absolute coordinates
		Point start = this.getStartPoint(conn);
		Point end = this.getEndPoint(conn);
		
		Point point2 = new Point();
		int offset = RevisionGraphEditPart.NODES_HORIZONTAL_OFFSET / 2;
		if (end.x > start.x) {
			offset = -offset;
		}
		point2.x = end.x + offset;
		point2.y = start.y;
		
		Point point3 = new Point();
		point3.x = point2.x;
		point3.y = end.y;		
				
		PointList bendPoints = new PointList();
		bendPoints.addPoint(start);						
		bendPoints.addPoint(point2);
		bendPoints.addPoint(point3);
		bendPoints.addPoint(end);								
		
		/*
		 * TODO avoid intersects with revision nodes when drawing
		 * horizontal lines which span several columns
		 */
		
		this.applyResult(conn, bendPoints);
	}
	
	/*
	 * Set points to connection where points are in absolute coordinates 
	 */
	protected void applyResult(Connection conn, PointList initialPoints) {
		PointList points = conn.getPoints();
		points.removeAllPoints();

		for (int i = 0; i < initialPoints.size(); i++) {
			Point point = initialPoints.getPoint(i);
			conn.translateToRelative(point);
			points.addPoint(point);
		}
		
		//set points to connection
		conn.setPoints(points);
	}

}
