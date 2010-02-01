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
package org.eclipse.team.svn.revision.graph.operation;


/**
 * Allow to traverse nodes and visit process
 *  
 * @author Igor Burilo
 */
public interface INodeVisitor {

	//implement traverse algorithm
	void traverse(NodeConnections node);
	
	//callback to process node
	void visit(NodeConnections node);	
}
