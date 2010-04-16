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
import java.util.List;

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * 
 * @author Igor Burilo
 */
public class ColumnData {

	protected int id;
	
	private int top;
	private int currentBottom;
	private int currentTop;	
	private List<RevisionNode> currentNodes = new ArrayList<RevisionNode>();
	
	public ColumnData(int id) {
		this.id  = id;
	}
	
	public void addNode(RevisionNode node) {
		if (this.currentNodes.isEmpty()) {
			this.currentBottom = node.getY();
		}
		
		this.currentTop = node.getY() + node.getHeight();		
		this.currentNodes.add(node);
	}

	public void increase(int offset) {		
		this.top = this.currentTop + offset;
		
		if (offset != 0) {
			for (RevisionNode node : this.currentNodes) {
				node.setY(node.getY() + offset);
			}	
		}
	}
	
	public RevisionNode[] getCurrentNodes() {
		return this.currentNodes.toArray(new RevisionNode[0]);
	}
	
	public void resetCurrentValues() {
		this.currentBottom = 0;
		this.currentTop = 0;
		this.currentNodes.clear();
	}

	public int getCurrentBottom() {
		return currentBottom;
	}

	public int getCurrentTop() {
		return currentTop;
	}	
	
	public int getTop() {
		return this.top;
	}
}
