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

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.operation.PathRevision.RevisionNodeAction;

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

	@Override
	public void run() {
		this.processNode(this.startNode, 0, 0);
	}
	
	protected void processNode(RevisionNode node, int column, int row) {
		node.setX(column);
		node.setY(row);				
		
		RevisionNode next = node.getNext();
		if (next != null) {
			this.processNode(next, column, row + node.getHeight());
		}
		
		RevisionNode[] copiedTos = node.getCopiedTo();
		if (copiedTos.length > 0) {
			int copyToCount = 0;
			for (int i = 0; i < copiedTos.length; i ++) {
				/*
				 * Copy to nodes are shown in next column, except of 'Rename' action
				 * for which we show nodes in the same column  
				 */
				int nextNodeColumn = copiedTos[i].pathRevision.action == RevisionNodeAction.RENAME ? column : (column + ++copyToCount);
				this.processNode(copiedTos[i], nextNodeColumn, row + node.getHeight());
			}
		}
	}

}
