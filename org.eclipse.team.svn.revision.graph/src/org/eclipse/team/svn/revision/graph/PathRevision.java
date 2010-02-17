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
package org.eclipse.team.svn.revision.graph;

import java.util.Iterator;

import org.eclipse.team.svn.revision.graph.cache.ChangedPathStructure;

/** 
 * TODO implement IPropertySource for Properties View ?
 * 
 * @author Igor Burilo
 */
public class PathRevision extends NodeConnections {

	public enum ReviosionNodeType {
		TRUNK,
		BRANCH,
		TAG,
		OTHER
	}
	
	public enum RevisionNodeAction {
		ADD,
		DELETE,
		MODIFY,
		COPY,		
		RENAME,		
		NONE		
	}
		
	protected final int pathIndex;	
	protected final long revision;
	
	protected long date;
	protected String author;
	protected String message;	
	protected final ChangedPathStructure[] changedPaths;

	//TODO move to UI model ?
	public final ReviosionNodeType type;
	
	public final RevisionNodeAction action;			
	
	public PathRevision(long revision, int pathIndex, long date, String author, String message, ChangedPathStructure[] changedPaths, RevisionNodeAction action, ReviosionNodeType type) {
		this.revision = revision;
		this.pathIndex = pathIndex;
		this.date = date;
		this.author = author;
		this.message = message;
		this.changedPaths =	changedPaths;
		this.action = action;					
		this.type = type;
	}
	
	public int getPathIndex() {
		return pathIndex;
	}
	
//	public String getPath() {
//		return CacheReader.pathStorage.getPath(this.pathIndex);
//	}
	
	public long getRevision() {
		return revision;
	}
	
	public long getDate() {
		return date;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getMessage() {
		return message;
	}

	public ChangedPathStructure[] getChangedPaths() {
		return changedPaths;
	}		
	
	public void insertNodeInRevisionsChain(PathRevision node) { 
		PathRevision prevNodeToProcess = null;
		Iterator<NodeConnections> iter = this.iterateRevisionsChain();
		while (iter.hasNext()) {
			PathRevision nodeToProcess = (PathRevision) iter.next();
			if (nodeToProcess.getRevision() < node.getRevision()) {
				prevNodeToProcess = nodeToProcess;
			} else {
				break;
			}
		}																		

		if (prevNodeToProcess == null) {
			node.setNext(this.getStartNodeInChain());
		} else if (prevNodeToProcess.getNext() == null) {
			prevNodeToProcess.setNext(node);
		} else {
			NodeConnections tmpNode = prevNodeToProcess.getNext();
			prevNodeToProcess.setNext(node);
			node.setNext(tmpNode);
		}	
	}
	
	public PathRevision findNodeInChain(long revision) {		
		Iterator<NodeConnections> iter = this.iterateRevisionsChain();
		while (iter.hasNext()) {
			PathRevision nodeToProcess = (PathRevision) iter.next();
			if (nodeToProcess.getRevision() == revision) {
				return nodeToProcess;
			}
		} 
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("%s@%d, action:%s", this.pathIndex, this.revision, this.action);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PathRevision) {
			PathRevision node = (PathRevision) obj;
			return
				this.getRevision() == node.getRevision() && 
				this.getPathIndex() == node.getPathIndex() &&
				this.action == node.action;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;		
		int result = 17;
		result += prime * this.getRevision();
		result += prime * this.getPathIndex();
		result += prime * this.action.hashCode();		
		return result;
	}
	
	public PathRevision getStartNodeInChain() {
		return (PathRevision) super.getStartNodeInChain();
	}
	
	public PathRevision getEndNodeInChain() {
		return (PathRevision) super.getEndNodeInChain();
	}	
	
	public PathRevision getNext() {
		return (PathRevision) super.getNext();
	}
	
	public PathRevision getPrevious() {
		return (PathRevision) super.getPrevious();
	}
	
	public PathRevision[] getCopiedTo() {
		return this.copiedTo.toArray(new PathRevision[0]);
	}
	
	public PathRevision getCopiedFrom() {
		return (PathRevision) super.getCopiedFrom();
	}

	public void setMessage(String message) {
		this.message = message;	
	}
	
	public void setDate(long date) {
		this.date = date;		
	}

	public void setAuthor(String author) {
		this.author = author;		
	}		
}
