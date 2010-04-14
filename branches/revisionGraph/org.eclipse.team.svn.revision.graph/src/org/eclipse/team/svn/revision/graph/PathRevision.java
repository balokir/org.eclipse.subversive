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

import org.eclipse.team.svn.revision.graph.cache.CacheChangedPath;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.CacheRevision;
import org.eclipse.team.svn.revision.graph.operation.PathRevisionConnectionsValidator;

/** 
 * TODO implement IPropertySource for Properties View ?
 * 
 * @author Igor Burilo
 */
public class PathRevision extends NodeConnections<PathRevision> {

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
	
	protected final CacheRevision cacheRevision;	
		
	public final ReviosionNodeType type;
	
	public final RevisionNodeAction action;			
	
	protected PathRevisionConnectionsValidator validator;
	
	public PathRevision(CacheRevision revisionData, int pathIndex, RevisionNodeAction action, ReviosionNodeType type) {
		this.cacheRevision = revisionData;
		this.pathIndex = pathIndex;
		this.action = action;
		this.type = type;
	}
	
	public int getPathIndex() {
		return pathIndex;
	}
	
	public long getRevision() {
		return this.cacheRevision.getRevision();
	}
	
	public long getDate() {
		return this.cacheRevision.getDate();
	}

	public int getAuthorIndex() {
		return this.cacheRevision.getAuthorIndex();
	}
	
	public int getMessageIndex() {
		return this.cacheRevision.getMessageIndex();
	}

	public CacheChangedPath[] getChangedPaths() {
		return this.cacheRevision.getChangedPaths();
	}		
	
	public void insertNodeInRevisionsChain(PathRevision node) { 
		PathRevision prevNodeToProcess = null;
		Iterator<PathRevision> iter = this.iterateRevisionsChain();
		while (iter.hasNext()) {
			PathRevision nodeToProcess = iter.next();
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
			PathRevision tmpNode = prevNodeToProcess.getNext();
			prevNodeToProcess.setNext(node);
			node.setNext(tmpNode);
		}	
	}
	
	public PathRevision findNodeInChain(long revision) {		
		Iterator<PathRevision> iter = this.iterateRevisionsChain();
		while (iter.hasNext()) {
			PathRevision nodeToProcess = iter.next();
			if (nodeToProcess.getRevision() == revision) {
				return nodeToProcess;
			}
		} 
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("%d@%d, action:%s", this.pathIndex, this.getRevision(), this.action); //$NON-NLS-1$
	}
		
	public String toString(RepositoryCache repositoryCache) {
		return String.format("%s@%d, action:%s", repositoryCache.getPathStorage().getPath(this.pathIndex), this.getRevision(), this.action); //$NON-NLS-1$
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
	
	public PathRevision[] getCopiedTo() {
		return super.getCopiedTo(new PathRevision[0]);
	}

	public CacheRevision getRevisionData() {
		return this.cacheRevision;		
	}	
	
	public void setValidator(PathRevisionConnectionsValidator validator) {
		this.validator = validator;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.revision.graph.NodeConnections#validate()
	 */
	@Override
	protected void validate() {
		if (this.validator != null) {
			this.validator.validate(this);	
		}		
	}
	
}
