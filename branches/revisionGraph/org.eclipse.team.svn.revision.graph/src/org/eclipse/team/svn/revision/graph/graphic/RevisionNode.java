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

import org.eclipse.team.svn.revision.graph.operation.NodeConnections;
import org.eclipse.team.svn.revision.graph.operation.PathRevision;

/**
 * 
 * @author Igor Burilo
 */
public class RevisionNode extends ChangesNotifier {

	public final PathRevision pathRevision;
	
	protected RevisionNodeItem initialConnectionItem;
	protected RevisionNodeItem currentConnectionItem;	
	
	protected boolean isNextCollapsed;
	protected boolean isPreviousCollapsed;
	protected boolean isCopiedToCollapsed;
	protected boolean isCopiedFromCollapsed;
	
	//TODO use in different hierarchy
	protected int width;
	protected int height;
	
	protected int x;
	protected int y;
	
	public class RevisionNodeItem extends NodeConnections {					
				
		public RevisionNode getRevisionNode() {
			return RevisionNode.this;
		}
		
		@Override
		public RevisionNodeItem getNext() {
			return (RevisionNodeItem) this.next;
		}

		@Override
		public RevisionNodeItem[] getCopiedTo() {
			return this.copiedTo.toArray(new RevisionNodeItem[0]);
		}
		
		@Override
		public String toString() {			
			return RevisionNode.this.toString();
		}				
	}		
	
	public RevisionNode(PathRevision pathRevision) {
		this.pathRevision = pathRevision;
		this.initialConnectionItem = new RevisionNodeItem();
		this.currentConnectionItem = new RevisionNodeItem();
	}
	
	public RevisionNodeItem getInitialConnectionItem() {
		return this.initialConnectionItem;
	}
		
	public RevisionNodeItem getCurrentConnectionItem() {
		return this.currentConnectionItem;
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return this.pathRevision.toString() + 
		", location: " + this.x + ", " + this.y + 
		", size: " + this.width + ", " + this.height; 
	}
	
	@Override
	public boolean equals(Object obj) {	
		if (obj instanceof RevisionNode) {
			RevisionNode rNode = (RevisionNode) obj;
			return this.pathRevision.equals(rNode.pathRevision);
		}		
		return false;
	}
	
//	@Override
//	public int hashCode() {	
//		return this.pathRevision.hashCode();
//	}
	
	public RevisionNode getNext() {		
		return this.castItem(this.currentConnectionItem.getNext());
	}
	
	public RevisionNode getPrevious() {		
		return this.castItem(this.currentConnectionItem.getPrevious());
	}
	
	public RevisionNode[] getCopiedTo() {
		RevisionNode[] res;
		NodeConnections[] copiedTo = this.currentConnectionItem.getCopiedTo();
		if (copiedTo.length > 0) {
			res = new RevisionNode[copiedTo.length];
			for (int i = 0; i < copiedTo.length; i ++) {
				NodeConnections node =  copiedTo[i];
				res[i] = this.castItem(node);
			}
		} else {
			res = new RevisionNode[0];
		}
		return res;
	}
	
	public RevisionNode getCopiedFrom() {
		return this.castItem(this.currentConnectionItem.getCopiedFrom());
	}
	
	protected RevisionNode castItem(NodeConnections node) {		
		return node != null ? ((RevisionNodeItem) node).getRevisionNode() : null;
	}

	public void refreshConnections() {
		this.firePropertyChange(ChangesNotifier.REFRESH_CONNECTIONS_PROPERTY, null, this);		
	}
		
}
