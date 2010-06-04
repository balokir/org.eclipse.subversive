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
import org.eclipse.team.svn.revision.graph.PathRevision.MergeData;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/**
 * @author Igor Burilo
 */
public class RevisionNode extends NodeConnections<RevisionNode> {

	private final PathRevision pathRevision;
	private final RevisionRootNode rootNode;
	
	protected final ChangesNotifier changesNotifier;		
	
	protected boolean isFiltered;
	
	protected boolean isNextCollapsed;
	protected boolean isPreviousCollapsed;
	protected boolean isCopiedFromCollapsed;
	//Separate copy to and rename flags
	protected boolean isCopiedToCollapsed;
	protected boolean isRenameCollapsed;
	
	
	//TODO use in different hierarchy
	protected int width;
	protected int height;
	
	protected int x;
	protected int y;	
	
	public RevisionNode(PathRevision pathRevision, RevisionRootNode rootNode) {
		this.pathRevision = pathRevision;
		this.rootNode = rootNode;
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
	
	/*
	 * Getter methods which names start with 'internal' don't take
	 * into account filtering and collapsing
	 */
	 
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
		if (this.isCopiedToCollapsed && this.isRenameCollapsed) {
			return Collections.emptyList();
		}
		
		LinkedList<RevisionNode> result = new LinkedList<RevisionNode>();		
		Collection<RevisionNode> copiedTo = this.internalGetCopiedToAsCollection();
		Iterator<RevisionNode> iter = copiedTo.iterator();
		while (iter.hasNext()) {
			RevisionNode node = iter.next();						
			if (!node.isFiltered) {
				boolean isRename = node.getAction() == RevisionNodeAction.RENAME;
				if (!isRename && !this.isCopiedToCollapsed) {
					result.add(node);
				} else if (isRename && !this.isRenameCollapsed) {
					//if there's renamed node, we place it at first position
					result.addFirst(node);
				}
			}
		}
		return result;
	}
		
	public Collection<RevisionNode> internalGetCopiedToAsCollection() {
		return super.getCopiedToAsCollection();
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
		return super.getCopiedFrom();
	}

	
	//--- notifications
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.changesNotifier.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.changesNotifier.removePropertyChangeListener(listener);
	}
	
	public void refreshConnections() {
		this.changesNotifier.firePropertyChange(ChangesNotifier.REFRESH_NODE_CONNECTIONS_PROPERTY, null, this);		
	}
	

	//--- Accessors
	
	public String getPath() {
		return this.rootNode.getRepositoryCache().getPathStorage().getPath(this.pathRevision.getPathIndex());
	}
	
	public ReviosionNodeType getType() {
		return this.pathRevision.type;
	}
	
	public RevisionNodeAction getAction() {
		return this.pathRevision.action;
	}
	
	public long getRevision() {
		return this.pathRevision.getRevision();
	}
	
	public String getMessage() {
		int messageIndex = this.pathRevision.getMessageIndex();
		String message = messageIndex != RepositoryCache.UNKNOWN_INDEX ? this.rootNode.getRepositoryCache().getMessageStorage().getMessage(messageIndex) : null;
		return message;
	}
	
	public String getAuthor() {
		int authorIndex = this.pathRevision.getAuthorIndex();
		String author = authorIndex != RepositoryCache.UNKNOWN_INDEX ? this.rootNode.getRepositoryCache().getAuthorStorage().getValue(authorIndex) : null;
		return author;
	}
	
	public long getDate() {
		return this.pathRevision.getDate();
	}
	
	public boolean hasMergeTo() {
		return this.pathRevision.hasMergeTo();
	}
	
	public NodeMergeData[] getMergeTo() {
		MergeData[] rawData = this.pathRevision.getMergeTo();
		NodeMergeData[] data = new NodeMergeData[rawData.length];
		for (int i = 0; i < rawData.length; i ++) {
			String path = this.rootNode.getRepositoryCache().getPathStorage().getPath(rawData[i].path);
			data[i] = new NodeMergeData(path, rawData[i].getRevisions());
		}
		return data;
	}
	
	public boolean hasMergedFrom() {
		return this.pathRevision.hasMergedFrom();
	}
	
	public NodeMergeData[] getMergedFrom() {
		MergeData[] rawData = this.pathRevision.getMergedFrom();
		NodeMergeData[] data = new NodeMergeData[rawData.length];
		for (int i = 0; i < rawData.length; i ++) {
			String path = this.rootNode.getRepositoryCache().getPathStorage().getPath(rawData[i].path);
			data[i] = new NodeMergeData(path, rawData[i].getRevisions());
		}
		return data;
	}
	
	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
	
	public boolean isFiltered() {
		return this.isFiltered;
	}
	
	
	
	//--- expand/collapse
	
	/*
	 * Methods which names start with 'internal' don't fire notifications 
	 */
	
	public boolean isNextCollapsed() {
		return isNextCollapsed;
	}
	
	public void setNextCollapsed(boolean isNextCollapsed) {
		//delegate to parent
		if (isNextCollapsed) {
			this.rootNode.collapseNext(this);
		} else {
			this.rootNode.expandNext(this);
		}
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY, null, null);
	}
	
	public void internalSetNextCollapsed(boolean isNextCollapsed) {
		this.isNextCollapsed = isNextCollapsed;
	}
	
	public boolean isPreviousCollapsed() {
		return isPreviousCollapsed;
	}
	
	public void setPreviousCollapsed(boolean isPreviousCollapsed) {
		//delegate to parent
		if (isPreviousCollapsed) {
			this.rootNode.collapsePrevious(this);
		} else {
			this.rootNode.expandPrevious(this);
		}
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY, null, null);
	}
	
	public void internalSetPreviousCollapsed(boolean isPreviousCollapsed) {
		this.isPreviousCollapsed = isPreviousCollapsed;
	}
	 
	public boolean isCopiedToCollapsed() {
		return isCopiedToCollapsed;
	}
	
	public void setCopiedToCollapsed(boolean isCopiedToCollapsed) {
		//delegate to parent
		if (isCopiedToCollapsed) {
			this.rootNode.collapseCopiedTo(this);
		} else {
			this.rootNode.expandCopiedTo(this);
		}
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY, null, null);
	}
	
	public void internalSetCopiedToCollapsed(boolean isCopiedToCollapsed) {
		this.isCopiedToCollapsed = isCopiedToCollapsed;
	}
	
	public boolean isRenameCollapsed() {
		return isRenameCollapsed;
	}
	
	public void setRenameCollapsed(boolean isRenameCollapsed) {
		//delegate to parent
		if (isRenameCollapsed) {
			this.rootNode.collapseRename(this);
		} else {
			this.rootNode.expandRename(this);
		}
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY, null, null);
	}
	
	public void internalSetRenameCollapsed(boolean isRenameCollapsed) {
		this.isRenameCollapsed = isRenameCollapsed;
	}
	
	public boolean isCopiedFromCollapsed() {
		return isCopiedFromCollapsed;
	}
	
	public void setCopiedFromCollapsed(boolean isCopiedFromCollapsed) {
		//delegate to parent
		if (isCopiedFromCollapsed) {
			this.rootNode.collapseCopiedFrom(this);
		} else {
			this.rootNode.expandCopiedFrom(this);
		}
		
		this.changesNotifier.firePropertyChange(ChangesNotifier.EXPAND_COLLAPSE_ON_NODE_PROPERTY, null, null);
	}
	
	public void internalSetCopiedFromCollapsed(boolean isCopiedFromCollapsed) {
		this.isCopiedFromCollapsed = isCopiedFromCollapsed;	
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
			", location: " + this.x + ", " + this.y +  //$NON-NLS-1$ //$NON-NLS-2$
			", size: " + this.width + ", " + this.height;  //$NON-NLS-1$ //$NON-NLS-2$
	}
		
}
