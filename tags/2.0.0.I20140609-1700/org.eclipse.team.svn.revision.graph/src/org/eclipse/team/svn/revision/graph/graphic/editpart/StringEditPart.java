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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Edit part for string
 * 
 * @author Igor Burilo
 */
public class StringEditPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {		
		return new Label(this.getCastedModel());
	}	
	
	@Override
	protected void createEditPolicies() {		
		
	}
	
	public String getCastedModel() {
		return (String) getModel();
	}

}
