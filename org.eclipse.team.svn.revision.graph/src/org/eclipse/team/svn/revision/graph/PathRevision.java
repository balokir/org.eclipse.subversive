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
import org.eclipse.team.svn.revision.graph.cache.RevisionStructure;

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
	
	protected final RevisionStructure revisionData;	
	
	//TODO move to UI model ?
	public final ReviosionNodeType type;
	
	public final RevisionNodeAction action;			
	
	public PathRevision(RevisionStructure revisionData, int pathIndex, RevisionNodeAction action, ReviosionNodeType type) {
		this.revisionData = revisionData;
		this.pathIndex = pathIndex;
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
		return this.revisionData.getRevision();
	}
	
	public long getDate() {
		return this.revisionData.getDate();
	}

	public String getAuthor() {
		return this.revisionData.getAuthor();
	}
	
	public String getMessage() {
		return this.revisionData.getMessage();
	}

	public ChangedPathStructure[] getChangedPaths() {
		return this.revisionData.getChangedPaths();
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
		return String.format("%s@%d, action:%s", this.pathIndex, this.getRevision(), this.action);
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

	public RevisionStructure getRevisionData() {
		return this.revisionData;
		
	}	
}
