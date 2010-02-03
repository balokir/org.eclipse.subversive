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

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.operation.PathRevision.RevisionNodeAction;

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

	@Override
	public void run() {		
		this.processNode(this.startNode);
	}			
	
	protected void processNode(RevisionNode node) {			
		RevisionNode nextNodeToProcess;
		RevisionNode[] copiedTo = node.getCopiedTo();
		boolean hasOnlyRename = copiedTo.length == 1 && copiedTo[0].pathRevision.action == RevisionNodeAction.RENAME;
		
		if (copiedTo.length == 0 || hasOnlyRename) {
			/*
			 * If node doesn't have 'copy to nodes' or it has 'Renamed' copy to node 
			 * then we can set its location at once without taking into account other nodes
			 */	
			ColumnData columnData = this.getColumnStructure(node);
			columnData.addNode(node);					
			
			if (hasOnlyRename) {
				nextNodeToProcess = copiedTo[0];
			} else {
				nextNodeToProcess = node.getNext() != null ? node.getNext() : this.findNextNodeToProcess(node);	
			}	
		} else {
			//go top by most right direction
			RevisionNode topNode = this.goTopOnMostRightDirection(node);
			nextNodeToProcess = this.findNextNodeToProcess(topNode);
		}
				
		this.updateColumnData();		
				
		if (nextNodeToProcess != null) {
			this.processNode(nextNodeToProcess);
		}
	}
	
	protected void updateColumnData() {
		/*
		 * find max difference between currentBottom and top
		 * in order to calculate new top value
		 */
		int maxDiff = -1;
		boolean isFirstRow = true;		
		int heightOffset = 0;
		for (ColumnData columnData : this.columnsData) {
			if (columnData == null || columnData.getCurrentBottom() == 0 && columnData.getCurrentTop() == 0) {
				continue;
			}
			int diff = columnData.top - columnData.getCurrentBottom();
			if (diff > 0) {
				maxDiff = diff > maxDiff ? diff : maxDiff;
				heightOffset = this.getHeightOffset(columnData);	
			} else if (diff == 0 && !isFirstRow && maxDiff == -1) {
				maxDiff = 0;
				heightOffset = this.getHeightOffset(columnData);
			}
			
			if (isFirstRow) {
				isFirstRow = false;
			}
		}						
		
		//set new top value		
		int increase = maxDiff > -1 ? (maxDiff + heightOffset) : 0;			
		for (ColumnData columnData : this.columnsData) {
			if (columnData == null || columnData.getCurrentBottom() == 0 && columnData.getCurrentTop() == 0) {
				continue;
			}
			columnData.increase(increase);
			columnData.resetCurrentValues();
		}
	}
	
	protected RevisionNode goTopOnMostRightDirection(RevisionNode node) {
		RevisionNode topNode = node;
		while (true) {
			ColumnData columnData = this.getColumnStructure(topNode);
			columnData.addNode(topNode);
			
			if (topNode.getCopiedTo().length > 0) {
				RevisionNode[] copyToNodes = topNode.getCopiedTo();
				//process most right copy to node
				topNode = copyToNodes[copyToNodes.length - 1];
														
				/*
				 * if there are several copy to nodes, then we need to take into account
				 * 'top' for each copy node while calculating Y for most right node,
				 * because there can be a situation that 'top' coordinate for previous copy to node is higher
				 * than 'top' coordinate for most right node. So in this case we set 'top' for all 
				 * copy to nodes as max 'top' value from them.  
				 */
				if (copyToNodes.length > 1) {
					//find max top
					int maxTop = - 1;
					for (RevisionNode copyTo : copyToNodes) {
						ColumnData copyToColumnData = this.getColumnStructure(copyTo);
						maxTop = copyToColumnData.top > maxTop ? copyToColumnData.top : maxTop; 
					}
					//increase tops for 'copy to' ColumnData's to max top
					if (maxTop > -1) {
						for (RevisionNode copyTo : copyToNodes) {
							ColumnData copyToColumnData = this.getColumnStructure(copyTo);
							copyToColumnData.top = maxTop;
						}
					}
				}
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

	protected RevisionNode findNextNodeToProcess(RevisionNode topNode) {		
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
							return copyToNodes[i];
						}
						if (copyToNodes[i].equals(tmpNode)) {
							isFoundCurrentNode = true;
						} 
					}
				}
				if (copiedFrom.getNext() != null)  {
					return copiedFrom.getNext();
				}
				
				tmpNode = copiedFrom;
			} else {
				tmpNode = tmpNode.getPrevious();
			}
		}		
		return null;
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
			this.columnsData.add(index, columnStructure = new ColumnData());
		} else {
			columnStructure = this.columnsData.get(index);
		}
		return columnStructure;
	}
	
	public int getMaxY() {
		int maxY = -1;
		for (ColumnData data : this.columnsData) {
			if (data != null) {
				maxY = data.top > maxY ? data.top : maxY;
			}
		}
		return maxY;
	}

}
