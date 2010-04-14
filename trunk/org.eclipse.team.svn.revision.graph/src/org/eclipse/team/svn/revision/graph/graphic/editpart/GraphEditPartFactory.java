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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.team.svn.revision.graph.graphic.RevisionConnectionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;

/**
 * A factory for creating new EditParts 
 *  
 * @author Igor Burilo
 */
public class GraphEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {		
		EditPart editPart = null;
		
		if (model instanceof String) {
			editPart = new StringEditPart();
		} else if (model instanceof RevisionRootNode) {
			editPart = new RevisionGraphEditPart();
		} else if (model instanceof RevisionNode) {
			editPart = new RevisionEditPart();
		} else if (model instanceof RevisionConnectionNode) {
			editPart = new RevisionConnectionEditPart();			
		}
		
		if (editPart != null) {
			editPart.setModel(model);
			return editPart;
		} else { 
			//should never happen
			throw new RuntimeException("Can't create part for model element: " //$NON-NLS-1$
				+ ((model != null) ? model.getClass().getName() : "null")); //$NON-NLS-1$
		}
	}
}
