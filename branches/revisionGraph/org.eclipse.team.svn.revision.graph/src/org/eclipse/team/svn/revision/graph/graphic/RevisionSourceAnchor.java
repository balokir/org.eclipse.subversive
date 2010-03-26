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
 * Anchor for source revision node:
 * depending on relationship between source and target nodes
 * it's positioned in the middle of top or right side of the source node
 * 
 * @author Igor Burilo
 */
public class RevisionSourceAnchor extends AbstractConnectionAnchor {

	protected RevisionNode source;
	protected RevisionNode target;
	
	public RevisionSourceAnchor(IFigure figure, RevisionNode source, RevisionNode target) {
		super(figure);
		this.source = source;
		this.target = target;
	}

	public Point getLocation(Point reference) {
		boolean isTop = 
			this.target.getAction() == RevisionNodeAction.RENAME || 
			this.target.getPrevious() != null; 
		
		Rectangle rect = getOwner().getBounds();
		Point point = isTop ? rect.getTop() : rect.getRight();
		getOwner().translateToAbsolute(point);
		return point;
	}
	
}
