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

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.team.svn.revision.graph.NodeConnections;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;

/**
 * 
 * @author Igor Burilo
 */
public class RevisionNode extends NodeConnections<RevisionNode> {

	public final PathRevision pathRevision;
	
	protected final ChangesNotifier changesNotifier;		
	
	protected boolean isFiltered;
	
	protected boolean isNextCollapsed;
	protected boolean isPreviousCollapsed;
	protected boolean isCopiedToCollapsed;
	protected boolean isCopiedFromCollapsed;	
	
	//TODO use in different hierarchy
	protected int width;
	protected int height;
	
	protected int x;
	protected int y;	
	
	public RevisionNode(PathRevision pathRevision) {
		this.pathRevision = pathRevision;
		this.changesNotifier = new ChangesNotifier();				
	}	
	
	//--- layout methods 
	
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
	
	
	//--- connections manipulation
	
	@Override
	public RevisionNode getNext() {		
		if (this.isNextCollapsed) {
			return null;
		}		
		RevisionNode node = this;
		while ((node = node.internalGetNext()) != null) {						
			if (!node.isFiltered) {
				return node;
			}
			if (node.isNextCollapsed) {
				return null;
			}
		}
		return null;
	}
	
	public RevisionNode internalGetNext() {
		return super.getNext();
	}
	
	@Override
	public RevisionNode getPrevious() {		
		if (this.isPreviousCollapsed) {
			return null;
		}		
		RevisionNode node = this;
		while ((node = node.internalGetPrevious()) != null) {						
			if (!node.isFiltered) {
				return node;
			}			
			if (node.isPreviousCollapsed) {
				return null;
			}
		}
		return null;
	}
	
	public RevisionNode internalGetPrevious() {
		return super.getPrevious();
	}
		
	@Override
	public RevisionNode[] getCopiedTo(RevisionNode[] a) {
		return this.getCopiedTo();
	}
	
	public RevisionNode[] getCopiedTo() {
		return this.getCopiedToAsCollection().toArray(new RevisionNode[0]);		
	}
	
	@Override
	public Collection<RevisionNode> getCopiedToAsCollection() {
		if (this.isCopiedToCollapsed) {
			return Collections.emptyList();
		}
		Collection<RevisionNode> copiedTo = this.internalGetCopiedToAsCollection();
		Iterator<RevisionNode> iter = copiedTo.iterator();
		while (iter.hasNext()) {
			if (iter.next().isFiltered) {
				iter.remove();
			}
		}
		return copiedTo;
	}
		
	public Collection<RevisionNode> internalGetCopiedToAsCollection() {
		LinkedList<RevisionNode> res = new LinkedList<RevisionNode>();
		Collection<RevisionNode> copiedTo = super.getCopiedToAsCollection();
		if (!copiedTo.isEmpty()) {
			for (RevisionNode copiedToNode : copiedTo) { 
				//if there's renamed node, we place it at first position
				if (copiedToNode.pathRevision.action == RevisionNodeAction.RENAME) {
					res.addFirst(copiedToNode);
				} else {
					res.add(copiedToNode);
				}
			}
		}
		return res;
	}
	
	@Override
	public RevisionNode getCopiedFrom() {
		if (this.isCopiedFromCollapsed) {
			return null;
		}
		RevisionNode copiedFrom = this.internalGetCopiedFrom();
		return copiedFrom != null ? (copiedFrom.isFiltered ? null : copiedFrom) : null;
	}

	public RevisionNode internalGetCopiedFrom() {
		return this.copiedFrom;
	}
	
	//--- notifications
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.changesNotifier.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.changesNotifier.removePropertyChangeListener(listener);
	}
	
	public void refreshConnections() {
		this.changesNotifier.firePropertyChange(ChangesNotifier.REFRESH_CONNECTIONS_PROPERTY, null, this);		
	}
	
	
	//--- Accessors
	
	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
	
	public boolean isFiltered() {
		return this.isFiltered;
	}
	
	public boolean isNextCollapsed() {
		return isNextCollapsed;
	}
	
	public void setNextCollapsed(boolean isNextCollapsed) {
		this.isNextCollapsed = isNextCollapsed;
		
		//TODO check notifications on big graph
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_PROPERTY, null, null);
	}
	
	public boolean isPreviousCollapsed() {
		return isPreviousCollapsed;
	}
	
	public void setPreviousCollapsed(boolean isPreviousCollapsed) {
		this.isPreviousCollapsed = isPreviousCollapsed;
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_PROPERTY, null, null);
	}
	
	public boolean isCopiedToCollapsed() {
		return isCopiedToCollapsed;
	}
	
	public void setCopiedToCollapsed(boolean isCopiedToCollapsed) {
		this.isCopiedToCollapsed = isCopiedToCollapsed;
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_PROPERTY, null, null);
	}
	
	public boolean isCopiedFromCollapsed() {
		return isCopiedFromCollapsed;
	}
	
	public void setCopiedFromCollapsed(boolean isCopiedFromCollapsed) {
		this.isCopiedFromCollapsed = isCopiedFromCollapsed;
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_PROPERTY, null, null);
	}
	

	//--- Object methods

	@Override
	public boolean equals(Object obj) {	
		if (obj instanceof RevisionNode) {
			RevisionNode rNode = (RevisionNode) obj;
			return this.pathRevision.equals(rNode.pathRevision);
		}		
		return false;
	}
	
	@Override
	public int hashCode() {	
		return this.pathRevision.hashCode();
	}
	
	@Override
	public String toString() {
		return this.pathRevision.toString() + 
			", location: " + this.x + ", " + this.y + 
			", size: " + this.width + ", " + this.height; 
	}
		
}
