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

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.GraphConstants;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/** 
 * Set x as column
 * Set y as initial coordinate, as a result nodes can cross with each other by height
 * 
 * @author Igor Burilo
 */
public class SetInitialLocationCommand extends AbstractLayoutCommand {
	
	public SetInitialLocationCommand(RevisionNode startNode) {
		super(startNode);
	}	
	
	public void run() {		
		this.startNode.setX(0);
		this.startNode.setY(0);
		
		Queue<RevisionNode> queue = new LinkedList<RevisionNode>();
		queue.add(this.startNode);
		
		RevisionNode node = null;
		while ((node = queue.poll()) != null) {
			this.processCopyTo(node, queue);
			
			//process next nodes
			RevisionNode previous = node;
			while (previous.getNext() != null) {
				RevisionNode next = previous.getNext();				
				next.setX(previous.getX());
				next.setY(previous.getY() + previous.getHeight() + GraphConstants.NEIGHBOUR_NODES_VERTICAL_OFFSET);
				previous = next;
				
				this.processCopyTo(next, queue);
			}						
		}		
	}

	protected void processCopyTo(RevisionNode node, Queue<RevisionNode> queue) {		
		RevisionNode[] copiedTos = node.getCopiedTo();
		if (copiedTos.length > 0) {
			int column = node.getX();
			int row = node.getY();			
			
			int copyToCount = 0;
			for (int i = 0; i < copiedTos.length; i ++) {
				/*
				 * Copy to nodes are shown in next column, except of 'Rename' action
				 * for which we show nodes in the same column  
				 */
				RevisionNode copiedTo = copiedTos[i];
				int nextNodeColumn = copiedTo.getAction() == RevisionNodeAction.RENAME ? column : (column + ++copyToCount);
				copiedTo.setX(nextNodeColumn);
				copiedTo.setY(row + node.getHeight() + GraphConstants.NEIGHBOUR_NODES_VERTICAL_OFFSET);
				
				queue.offer(copiedTo);
			}
		}		
	}

}
