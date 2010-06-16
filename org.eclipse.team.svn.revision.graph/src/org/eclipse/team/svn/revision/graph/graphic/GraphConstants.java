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

import org.eclipse.draw2d.geometry.Insets;

/**
 * Settings to customize graph presentation
 * 
 * @author Igor Burilo
 */
public class GraphConstants {

	//sets the outer margin for the entire graph
	public final static Insets GRAPH_MARGIN = new Insets(3);
	
	//make a little vertical offset in order not to intersect merge connections with general ones
	public final static int MERGE_TO_GENERAL_CONNECTION_VERTICAL_OFFSET = 3;
	
	//vertical distance between merge line and revision node if merge line works around the node
	public final static int MERGE_LINE_TO_NODE_OFFSET = 1;
	
	//horizontal distance between nodes 
	public final static int NODES_HORIZONTAL_OFFSET = 40;
	
	//vertical distance between neighbor nodes
	public final static int NEIGHBOUR_NODES_VERTICAL_OFFSET = 3;
	
	public final static int NODE_WIDTH = 200;
	
	public final static int NODE_SHADOW_OFFSET = 2;
}
