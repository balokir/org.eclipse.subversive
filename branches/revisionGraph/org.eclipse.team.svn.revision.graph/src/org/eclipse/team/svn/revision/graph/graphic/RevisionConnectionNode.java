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
 * 
 * @author Igor Burilo
 */
public class RevisionConnectionNode {

	public final RevisionNode source;
	public final RevisionNode target;
	
	public RevisionConnectionNode(RevisionNode source, RevisionNode target) {
		this.source = source;
		this.target = target;	
	}
	

	@Override
	public String toString() {
		return "Connection. Source: " + this.source + ", target: " + this.target;
	}

}
