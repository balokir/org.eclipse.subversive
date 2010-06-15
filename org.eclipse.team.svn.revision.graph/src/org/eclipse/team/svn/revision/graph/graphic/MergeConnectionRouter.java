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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionGraphEditPart;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;

/**
 * Router for merge connections
 * 
 * @author Igor Burilo
 */
public class MergeConnectionRouter extends AbstractRouter {
		
	protected final static int MERGE_LINE_TO_NODE_OFFSET = 1; 
	protected final static int NODES_HORIZONTAL_OFFSET = RevisionGraphEditPart.NODES_HORIZONTAL_OFFSET / 2;
	//step by which try to find intersected figure
	protected final static int HORIZONTAL_STEP = 5;
	
	public void route(Connection conn) {		
		if ((conn.getSourceAnchor() == null) || (conn.getTargetAnchor() == null)) {
			return;
		}												
																
		PointList bendPoints = this.calculateInitialPoints(conn);
		this.checkIntersectsWithNodes(conn, bendPoints);					
		this.applyResult(conn, bendPoints);						
	}
	
	/* 
	 * Connection may intersect with revision nodes
	 */
	protected PointList calculateInitialPoints(Connection conn) {
		//points in absolute coordinates
		Point start = this.getStartPoint(conn);
		Point end = this.getEndPoint(conn);
		
		Point point2 = new Point();
		int offset = NODES_HORIZONTAL_OFFSET;
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
		
		return bendPoints;
	}
	
	/*
	 * avoid intersects with revision nodes when drawing
	 * horizontal lines which span several columns
	 */
	protected void checkIntersectsWithNodes(Connection conn, PointList bendPoints) {		
		IFigure nodesParent = conn.getSourceAnchor().getOwner().getParent();														
		if (nodesParent == null) {
			//should not happen
			return;
		}		
		
		//find intersection by going horizontally
		boolean isChanged = false;
		Point startPoint = bendPoints.getFirstPoint();
		Point endPoint = bendPoints.getLastPoint();
		if (startPoint.x != endPoint.x) {			
			int pointIndex = 0;
			if (startPoint.x < endPoint.x) {
				//go right
				int i = startPoint.x + 1;
				while (i < endPoint.x) {										
					//translate coordinates in order to be able to find figure
					Point p = new Point(i, startPoint.y);
					nodesParent.translateToRelative(p);
										
					IFigure figure = nodesParent.findFigureAt(p);
					if (figure instanceof RevisionFigure) {						
						//translate coordinates back to absolute
						Rectangle figureBounds = figure.getBounds().getCopy();
						nodesParent.translateToAbsolute(figureBounds);
											
						//create 2 new points to work around intersected node
						Point point1 = new Point();
						point1.x = figureBounds.x - NODES_HORIZONTAL_OFFSET;
						point1.y = startPoint.y;
						 
						Point point2 = new Point();
						point2.x = point1.x;
						//go top or bottom					
						boolean isGoTop = startPoint.y < figureBounds.bottom() / 2;
						point2.y = isGoTop ? (figureBounds.y - MERGE_LINE_TO_NODE_OFFSET) : (figureBounds.bottom() + MERGE_LINE_TO_NODE_OFFSET); 
						
						bendPoints.insertPoint(point1, ++ pointIndex);
						bendPoints.insertPoint(point2, ++ pointIndex);
						
						startPoint = point2;
						i = figureBounds.right() + 1;			 
						
						isChanged = true;
					} else {
						i += HORIZONTAL_STEP;
					}
				}
			} else {
				//go left
				int i = startPoint.x - 1;
				while (i > endPoint.x) {
					//translate coordinates in order to be able to find figure					
					Point p = new Point(i, startPoint.y);
					nodesParent.translateToRelative(p);															
					
					IFigure figure = nodesParent.findFigureAt(p);
					if (figure instanceof RevisionFigure) {						
						//translate coordinates back to absolute
						Rectangle figureBounds = figure.getBounds().getCopy();
						nodesParent.translateToAbsolute(figureBounds);											
						
						//create 2 new points to work around intersected node
						Point point1 = new Point();
						point1.x = figureBounds.right() + NODES_HORIZONTAL_OFFSET;
						point1.y = startPoint.y;
						 
						Point point2 = new Point();
						point2.x = point1.x;
						//go top or bottom					
						boolean isGoTop = startPoint.y < figureBounds.bottom() / 2;
						point2.y = isGoTop ? (figureBounds.y - MERGE_LINE_TO_NODE_OFFSET) : (figureBounds.bottom() + MERGE_LINE_TO_NODE_OFFSET); 
						
						bendPoints.insertPoint(point1, ++ pointIndex);
						bendPoints.insertPoint(point2, ++ pointIndex);
						
						startPoint = point2;
						i = figureBounds.x - 1;
						
						isChanged = true;	
					} else {
						i -= HORIZONTAL_STEP;
					}
				}
			}
			
			if (isChanged) {
				/*
				 * update one of the last points
				 * 
				 * Before processing we had following points:
				 * 	start
				 * 	some
				 * 	pre-end
				 * 	end
				 * If we have intersects we insert new points which bend over
				 * intersected revision nodes, so we need to update 'some' point too 
				 */
				int index = bendPoints.size() - 3;					
				Point point = bendPoints.getPoint(index);
				bendPoints.removePoint(index);
				point.y = bendPoints.getPoint(index - 1).y;					
				bendPoints.insertPoint(point, index);															
			}		
		}	
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
