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

import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * Set correct X coordinate: previously it contained column number
 * but this command translates it to real coordinate.
 * 
 * Set correct Y coordinate: rotate relative to top value
 * 
 * @author Igor Burilo
 */
public class SetFinalCoordinates extends AbstractLayoutCommand {

	protected final int widthOffset;
	protected SetYCommand setYcommand;
	
	public SetFinalCoordinates(RevisionNode startNode, int widthOffset, SetYCommand setYcommand) {
		super(startNode);
		this.widthOffset = widthOffset;
		this.setYcommand = setYcommand;
	}

	@Override
	public void run() {	
		final int maxY = this.setYcommand.getMaxY();						
		new TopRightTraverseVisitor<RevisionNode>() {			
			@Override
			protected void visit(RevisionNode node) {				
				node.setX(node.getX() * (node.getWidth() + SetFinalCoordinates.this.widthOffset));							
				node.setY(maxY - (node.getY() + node.getHeight()));				
			}
		}.traverse(this.startNode);
	}

}
