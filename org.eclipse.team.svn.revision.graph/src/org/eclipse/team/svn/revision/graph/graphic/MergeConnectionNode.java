/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic;

/**
 * Merge connection between revision nodes
 * 
 * @author Igor Burilo
 */
public class MergeConnectionNode extends RevisionConnectionNode {

	public MergeConnectionNode(RevisionNode source, RevisionNode target) {
		super(source, target);		
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		//check on class in order to avoid problems with derived classes
		if (obj != null && this.getClass().equals(obj.getClass())) {
			MergeConnectionNode node = (MergeConnectionNode) obj;
			return this.source.equals(node.source) && this.target.equals(node.target);
		}				
		return false;
	}
	
}
