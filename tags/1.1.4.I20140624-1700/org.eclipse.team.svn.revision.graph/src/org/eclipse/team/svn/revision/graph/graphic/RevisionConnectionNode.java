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


/**
 * Connection between revision nodes
 * 
 * @author Igor Burilo
 */
public class RevisionConnectionNode {

	public final RevisionNode source;
	public final RevisionNode target;
	
	public RevisionConnectionNode(RevisionNode source, RevisionNode target) {
		if (source == null) {
			throw new IllegalArgumentException("source"); //$NON-NLS-1$
		}
		if (target == null) {
			throw new IllegalArgumentException("target"); //$NON-NLS-1$
		}
		this.source = source;
		this.target = target;	
	}
	
	public RevisionNode getSource() {
		return this.source;
	}
	
	public RevisionNode getTarget() {
		return this.target;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}		
		//check on class in order to avoid problems with derived classes
		if (obj != null && this.getClass().equals(obj.getClass())) {
			RevisionConnectionNode node = (RevisionConnectionNode) obj;
			return this.source.equals(node.source) && this.target.equals(node.target);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + this.source.hashCode();
		result = result * 31 + this.target.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "Connection. Source: " + this.source + ", target: " + this.target; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
