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

import java.util.ArrayList;

import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/** 
 * Set Y value in order not to have crossing with bottom nodes
 * 
 * @author Igor Burilo
 */
public class SetYCommand extends AbstractLayoutCommand {
	
	protected ArrayList<ColumnData> columnsData = new ArrayList<ColumnData>();			
			 	
	public SetYCommand(RevisionNode startNode) {
		super(startNode);	
	}

	protected static class RevisionNodeData {
		public final RevisionNode node;
		/*
		 * If node has several copy to nodes, then we add all of them to column data at once
		 * and for further processing we don't need to add them again. So this flag indicates
		 * whether we can add node to column data or not.
		 */
		public final boolean canAddNodeToColumnData;
		
		public RevisionNodeData(RevisionNode node, boolean isAddCurrentNodeToColumnData) {
			this.node = node;			
			this.canAddNodeToColumnData = isAddCurrentNodeToColumnData;
		}
	}
	
	@Override
	public void run() {
		RevisionNodeData nodeData = new RevisionNodeData(this.startNode, true);
		
		while (nodeData.node != null) {
			RevisionNodeData nextNodeToProcessData;
			
			RevisionNode[] copiedTo = nodeData.node.getCopiedTo();
			boolean hasOnlyRename = copiedTo.length == 1 && copiedTo[0].getAction() == RevisionNodeAction.RENAME;
			
			if (copiedTo.length == 0 || hasOnlyRename) {
				/*
				 * If node doesn't have 'copy to nodes' or it has 'Renamed' copy to node 
				 * then we can set its location at once without taking into account other nodes
				 */	
				
				if (nodeData.canAddNodeToColumnData) {
					ColumnData columnData = this.getColumnStructure(nodeData.node);
					columnData.addNode(nodeData.node);
				}							
				
				if (hasOnlyRename) {
					nextNodeToProcessData = new RevisionNodeData(copiedTo[0], true);					
				} else {
					if (nodeData.node.getNext() != null) {
						nextNodeToProcessData = new RevisionNodeData(nodeData.node.getNext(), true);						
					} else {
						nextNodeToProcessData = this.findNextNodeToProcess(nodeData.node);
					}										
				}												
			} else {
				//go top by most right direction
				RevisionNode topNode = this.goTopOnMostRightDirection(nodeData.node, nodeData.canAddNodeToColumnData);				
				nextNodeToProcessData = this.findNextNodeToProcess(topNode); 				
			}
					
			this.updateColumnData();		
				
			nodeData = nextNodeToProcessData;
		}				
	}			
	
	protected void updateColumnData() {
		/*
		 * find max difference between currentBottom and top
		 * in order to calculate new top value
		 */
		int maxDiff = 0;
		int offsetForMax = 0;
		for (ColumnData columnData : this.columnsData) {
			if (columnData == null || columnData.getCurrentBottom() == 0 && columnData.getCurrentTop() == 0) {
				continue;
			}
			int diff = columnData.getTop() - columnData.getCurrentBottom();
			
			/*
			 * Between nodes in the same column we need to set offset
			 * if these nodes don't have direct connection   
			 */
			if (diff >= 0) {
				int offset;			
				RevisionNode bottomCurrentNode = columnData.getCurrentNodes()[0];			
				if (bottomCurrentNode.getPrevious() != null || 
					(bottomCurrentNode.getAction() == RevisionNodeAction.RENAME) ||
					(bottomCurrentNode.getCopiedFrom() != null && bottomCurrentNode.getCopiedFrom().getAction() == RevisionNodeAction.RENAME)) {
					offset = 0;
				} else {
					offset = this.getHeightOffset(columnData);
				}			
				
				if (diff >= maxDiff) {
					maxDiff = diff;
					offsetForMax = offset;
				}
			}
		}						
		
		//set new top value
		for (ColumnData columnData : this.columnsData) {
			if (columnData == null || columnData.getCurrentBottom() == 0 && columnData.getCurrentTop() == 0) {
				continue;
			}
			columnData.increase(maxDiff + offsetForMax);
			columnData.resetCurrentValues();
		}
	}
	
	protected RevisionNode goTopOnMostRightDirection(RevisionNode node, boolean isAddCurrentNodeToColumnData) {		
		RevisionNode topNode = node;
		boolean isFirst = true;
		while (true) {
						
			if (!isFirst ? true : isAddCurrentNodeToColumnData) {
				ColumnData columnData = this.getColumnStructure(topNode);
				columnData.addNode(topNode);				
			}
			isFirst = false;			
			
			RevisionNode[] copyToNodes = topNode.getCopiedTo();
			if (copyToNodes.length > 0) {
				/*
				 * If there are several copy to nodes then add all of them to column data, we do it for instance,
				 * because there can be a situation that 'top' coordinate for previous copy to node is higher
				 * than 'top' coordinate for most right node.
				 * But in further processing we don't need to re-add copy to nodes to column data again.
				 * 
				 * Don't add most right node as it will be added latter
				 */				
				for (int i = 0; i < copyToNodes.length - 1; i ++) {
					RevisionNode copyTo = copyToNodes[i];
					ColumnData copyToColumnData = this.getColumnStructure(copyTo);
					copyToColumnData.addNode(copyTo);
				}
				
				topNode = copyToNodes[copyToNodes.length - 1];
				
			} else if (topNode.getNext() != null) {
				topNode = topNode.getNext();
			} else {
				break;
			}
		} 
		return topNode;
	}
	
	protected int getHeightOffset(ColumnData columnData) {
		//make offset to copied from element's height of lowest node in revision chain
		int heightOffset = 0;
		RevisionNode[] columnNodes = columnData.getCurrentNodes();
		if (columnNodes.length > 0 && columnNodes[0].getCopiedFrom() != null) {
			heightOffset = columnNodes[0].getCopiedFrom().getHeight();					
		}
		return heightOffset;
	}
	
	protected RevisionNodeData findNextNodeToProcess(RevisionNode topNode) {		
		/*
		 * go bottom until we find another node to process:
		 * either there are other copy to nodes or top nodes 		
		 */
		RevisionNode tmpNode = topNode;		
		while (tmpNode != null) {			
			if (tmpNode.getCopiedFrom() != null) {
				RevisionNode copiedFrom = tmpNode.getCopiedFrom();				
				RevisionNode[] copyToNodes = copiedFrom.getCopiedTo();
				if (copyToNodes.length > 1) {
					//find node just before current node
					boolean isFoundCurrentNode = false;
					for (int i = copyToNodes.length - 1; i >= 0; i --) {
						if (isFoundCurrentNode) {
							//this copy to node was added previously to column data, so don't add it again
							return new RevisionNodeData(copyToNodes[i], false);														
						}
						if (copyToNodes[i].equals(tmpNode)) {
							isFoundCurrentNode = true;
						} 
					}
				}
				if (copiedFrom.getNext() != null)  {
					return new RevisionNodeData(copiedFrom.getNext(), true);
				}
				
				tmpNode = copiedFrom;
			} else {
				tmpNode = tmpNode.getPrevious();
			}
		}		
		return new RevisionNodeData(null, false);
	}
	
	protected ColumnData getColumnStructure(RevisionNode node) {
		int index = node.getX();
		ColumnData columnStructure;
		if (index >= this.columnsData.size() ||  this.columnsData.get(index) == null) {			
			if (index >= this.columnsData.size()) {
				int max = index - this.columnsData.size() + 1;
				for (int i = 0; i < max; i ++) {
					this.columnsData.add(null);
				}
			}			
			this.columnsData.add(index, columnStructure = new ColumnData(index));
		} else {
			columnStructure = this.columnsData.get(index);
		}
		return columnStructure;
	}
	
	public int getMaxY() {
		int maxY = -1;
		for (ColumnData data : this.columnsData) {
			if (data != null) {
				maxY = data.getTop() > maxY ? data.getTop() : maxY;
			}
		}
		return maxY;
	}

}
