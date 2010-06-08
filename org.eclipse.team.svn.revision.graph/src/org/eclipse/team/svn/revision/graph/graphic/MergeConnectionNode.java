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
		if (obj instanceof MergeConnectionNode) {
			return super.equals(obj);
		}				
		return false;
	}
	
}
