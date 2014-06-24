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
package org.eclipse.team.svn.revision.graph.graphic.layout;

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

/**
 * Base class for layout commands
 *  
 * @author Igor Burilo
 */
public abstract class AbstractLayoutCommand {
	
	protected final RevisionNode startNode;
	
	public AbstractLayoutCommand(RevisionNode startNode) {
		this.startNode = startNode;
	}				
	
	public abstract void run();
}
