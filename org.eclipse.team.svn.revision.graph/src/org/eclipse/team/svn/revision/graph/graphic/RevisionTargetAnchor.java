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

/**
 * Anchor for target revision node.
 * 
 * For general connection:
 * it's positioned in the middle of bottom side of the node
 * 
 * For merge connection:
 * it's positioned either in the left or right
 * 
 * @author Igor Burilo
 */
public class RevisionTargetAnchor extends AbstractConnectionAnchor {

	protected RevisionNode source;
	protected RevisionNode target;
	protected boolean isMergeConnection;
	
	public RevisionTargetAnchor(IFigure figure, RevisionNode source, RevisionNode target, boolean isMergeConnection) {
		super(figure);
		this.source = source;
		this.target = target;
		this.isMergeConnection = isMergeConnection;
	}
	
	public Point getLocation(Point reference) {
		Point point;
		Rectangle rect = getOwner().getBounds();
		if (!this.isMergeConnection) {
			point = rect.getBottom();
		} else {
			point = this.target.x > this.source.x ? rect.getLeft() : rect.getRight();
			point.y += GraphConstants.MERGE_TO_GENERAL_CONNECTION_VERTICAL_OFFSET;
		}	
		getOwner().translateToAbsolute(point);
		return point;
	}

}
