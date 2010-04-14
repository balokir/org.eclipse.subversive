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
 * Anchor for target revision node:
 * it's positioned in the middle of bottom side of the node
 * 
 * @author Igor Burilo
 */
public class RevisionTargetAnchor extends AbstractConnectionAnchor {

	public RevisionTargetAnchor(IFigure figure) {
		super(figure);
	}
	
	public Point getLocation(Point reference) {
		Rectangle rect = getOwner().getBounds();
		Point point = rect.getBottom();
		getOwner().translateToAbsolute(point);
		return point;
	}

}
