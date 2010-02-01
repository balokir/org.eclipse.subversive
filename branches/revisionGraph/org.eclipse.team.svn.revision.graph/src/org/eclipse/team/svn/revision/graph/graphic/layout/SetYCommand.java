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
		if (node.getCopiedTo().length == 0) {
			/*
			 * If node doesn't have 'copy to nodes' then we can
			 * set its location at once without taking into account other nodes
			 */
			ColumnData columnStructure = this.getColumnStructure(node);			
			columnStructure.top += node.getHeight();
					
			nextNodeToProcess = node.getNext() != null ? node.getNext() : this.findNextNodeToProcess(node);									
		} else {
			RevisionNode topNode = this.goTopOnMostRightDirection(node);
			nextNodeToProcess = this.findNextNodeToProcess(topNode);		
		}				
		
		if (nextNodeToProcess != null) {
			this.processNode(nextNodeToProcess);
		}
	}
	
	protected RevisionNode goTopOnMostRightDirection(RevisionNode node) {
		//go top by most right direction
		RevisionNode topNode = node;
		while (true) {
			ColumnData columnData = this.getColumnStructure(topNode);
			columnData.addNode(topNode);
			
			if (topNode.getCopiedTo().length > 0) {
				RevisionNode[] copyToNodes = topNode.getCopiedTo();
				//process most right copy to node
				topNode = copyToNodes[copyToNodes.length - 1];
			} else if (topNode.getNext() != null) {
				topNode = topNode.getNext();
			} else {
				break;
			}
		} 											
		
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
		while (true) {			
			if (tmpNode.getCopiedFrom() != null) {
				RevisionNode copiedFrom = tmpNode.getCopiedFrom();				
				RevisionNode[] copyToNodes = copiedFrom.getCopiedTo();
				if (copyToNodes.length > 1) {
					for (int i = copyToNodes.length - 1; i >= 0; i --) {
						if (!copyToNodes[i].equals(tmpNode)) {
							return copyToNodes[i];
						} 
					}
				}
				if (copiedFrom.getNext() != null)  {
					return copiedFrom.getNext();
				} else {
					tmpNode = copiedFrom.getPrevious();
				}
			} else if (tmpNode.getPrevious() != null) {
				tmpNode = tmpNode.getPrevious();
			} else {
				break;
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
