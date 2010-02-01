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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.svn.revision.graph.graphic.AbstractRevisionNodeFilter.AndRevisionNodeFilter;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode.RevisionNodeItem;

/**
 * 
 * @author Igor Burilo
 */
public class NodesFilterManager {

	protected AndRevisionNodeFilter filters = new AndRevisionNodeFilter();
	
	public void addFilter(AbstractRevisionNodeFilter filter) {
		this.filters.addFilter(filter);	
	}
	
	public void removeFilter(AbstractRevisionNodeFilter filter) {
		this.filters.removeFilter(filter);
	}
	
	/*
	 * Return first node accepted by filter
	 */
	public RevisionNodeItem applyFilters(RevisionNodeItem inputNode) {		
		if (this.filters.filters.isEmpty()) {
			return inputNode;
		}
		
		RevisionNodeItem outputStartNode = null;
		
		//find first node accepted by filters
		RevisionNodeItem startNode = this.findFirstNodeAcceptedByFilter(inputNode);
		if (startNode != null) {
			if (startNode.getPrevious() != null) {
				startNode.removePrevious();				
			}
			if (startNode.getCopiedFrom() != null) {
				startNode.removeCopiedFrom();
			}
			
			outputStartNode = startNode;
			this.doApplyFilters(startNode);
		} else {
			outputStartNode = null;
		}		
		return outputStartNode;
	}
	
	protected void doApplyFilters(RevisionNodeItem startNode) {				
		//go top 
		RevisionNodeItem previouslyAcceptedNode = startNode;
		RevisionNodeItem currentNode = startNode;
		while ((currentNode = currentNode.getNext()) != null) {			
			if (this.filters.accept(currentNode.getRevisionNode())) {
				if (previouslyAcceptedNode.getNext() == null || previouslyAcceptedNode.getNext() != null && !previouslyAcceptedNode.getNext().equals(currentNode)) {
					previouslyAcceptedNode.setNext(currentNode);	
				}				
				previouslyAcceptedNode = currentNode;
			} else {
				if (previouslyAcceptedNode.getNext() != null) {
					previouslyAcceptedNode.removeNext();	
				}				
			}
		}
		
		//process copy to
		List<RevisionNodeItem> nextNodesToProcess = new ArrayList<RevisionNodeItem>(); 
		currentNode = startNode;
		do {
			if (currentNode.getCopiedTo().length > 0) {
				for (RevisionNodeItem copyTo : currentNode.getCopiedTo()) {
					if (!this.filters.accept(copyTo.getRevisionNode())) {
						currentNode.removeCopiedTo(copyTo);
					} else {
						nextNodesToProcess.add(copyTo);
					}
				}
			}						
		} while ((currentNode = currentNode.getNext()) != null);
		
		if (!nextNodesToProcess.isEmpty()) {
			for (RevisionNodeItem node : nextNodesToProcess) {
				this.doApplyFilters(node);
			}
		}
	}
		
	protected RevisionNodeItem findFirstNodeAcceptedByFilter(RevisionNodeItem startNode) {
		//traverse nodes until we find first node accepted by filter
		
		List<RevisionNodeItem> nextNodesToProcess = new ArrayList<RevisionNodeItem>(); 
		
		RevisionNodeItem currentNode = startNode;
		do {
			if (this.filters.accept(currentNode.getRevisionNode())) {
				return currentNode;
			} else {
				if (currentNode.getCopiedTo().length > 0) {
					for (RevisionNodeItem copyTo : currentNode.getCopiedTo()) {
						nextNodesToProcess.add(copyTo);
					}
				}
			}
		} while ((currentNode = currentNode.getNext()) != null);				
		
		if (!nextNodesToProcess.isEmpty()) {
			for (RevisionNodeItem node : nextNodesToProcess) {
				RevisionNodeItem res = this.findFirstNodeAcceptedByFilter(node);
				if (res != null) {
					return res;
				}
			}
		}		
		return null;
	}
}
