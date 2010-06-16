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

import org.eclipse.team.svn.revision.graph.graphic.GraphConstants;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionGraphEditPart;

/**
 * Layout manager for revision graph
 * 
 * @author Igor Burilo
 */
public class GraphLayoutManager extends AbstractGraphLayoutManager {

	protected int widthOffset;	
	
	public GraphLayoutManager(RevisionGraphEditPart graphPart, int widthOffset) {
		super(graphPart);
		this.widthOffset = widthOffset;		
	}

	@Override
	protected AbstractLayoutCommand[] getLayoutCommands(RevisionNode startNode) {
		SetYCommand setYcommand = new SetYCommand(startNode);
		AbstractLayoutCommand[] commands = new AbstractLayoutCommand[]{
			new SetInitialLocationCommand(startNode),
			setYcommand,
			new SetFinalCoordinatesCommand(startNode, this.widthOffset, setYcommand),
			new SetMarginCommand(startNode, GraphConstants.GRAPH_MARGIN)
		};	
		return commands;
	}	
}
