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
package org.eclipse.team.svn.revision.graph.graphic.editpart;

import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.team.svn.revision.graph.graphic.RevisionConnectionNode;

/**
 * Edit part for connection
 * 
 * @author Igor Burilo
 */
public class RevisionConnectionEditPart extends AbstractConnectionEditPart {
	
	public RevisionConnectionNode getCastedModel() {
		return (RevisionConnectionNode) getModel();
	}

	@Override
	protected void createEditPolicies() {
		
	}
}
