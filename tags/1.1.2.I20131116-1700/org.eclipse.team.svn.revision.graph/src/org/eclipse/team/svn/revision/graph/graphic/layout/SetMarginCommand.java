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
package org.eclipse.team.svn.revision.graph.graphic.layout;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * Sets the outer margin for the entire graph
 * 
 * @author Igor Burilo
 */
public class SetMarginCommand extends AbstractLayoutCommand {

	protected Insets margin;
	
	public SetMarginCommand(RevisionNode startNode, Insets margin) {
		super(startNode);
		this.margin = margin;
	}

	public void run() {
		new TopRightTraverseVisitor<RevisionNode>() {			
			protected void visit(RevisionNode node) {																	
				node.setX(node.getX() + margin.left);
				node.setY(node.getY() + margin.top);
			}
		}.traverse(this.startNode);
	}

}
