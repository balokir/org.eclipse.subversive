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
package org.eclipse.team.svn.revision.graph.graphic;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;

/**
 * Anchor for source revision node.
 * 
 * For general connection: 
 * depending on relationship between source and target nodes
 * it's positioned in the middle of top or right side of the source node
 * 
 * For merge connection:
 * it's positioned either in the left or right
 * 
 * @author Igor Burilo
 */
public class RevisionSourceAnchor extends AbstractConnectionAnchor {
	
	//make a little vertical offset in order not to intersect with general connections
	public final static int VERTICAL_OFFSET = 3;
	
	protected RevisionNode source;
	protected RevisionNode target;
	protected boolean isMergeConnection;
	
	public RevisionSourceAnchor(IFigure figure, RevisionNode source, RevisionNode target, boolean isMergeConnection) {
		super(figure);
		this.source = source;
		this.target = target;
		this.isMergeConnection = isMergeConnection;
	}

	public Point getLocation(Point reference) {
		Point point;
		Rectangle rect = getOwner().getBounds();
		if (!this.isMergeConnection) {
			boolean isTop = 
				this.target.getAction() == RevisionNodeAction.RENAME || 
				this.target.getPrevious() != null; 
			
			point = isTop ? rect.getTop() : rect.getRight();									
		} else {
			point = this.source.x <= this.target.x ? rect.getRight() : rect.getLeft();			
			point.y += RevisionSourceAnchor.VERTICAL_OFFSET;
		}
		getOwner().translateToAbsolute(point);
		return point;
	}
	
}
