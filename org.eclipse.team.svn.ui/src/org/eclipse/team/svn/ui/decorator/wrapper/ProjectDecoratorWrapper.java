/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Panagiotis Korros - [patch] initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator.wrapper;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.team.svn.ui.decorator.ProjectDecorator;

/**
 * Lazy-loader for the project decorator
 * 
 * @author Panagiotis Korros
 */
public class ProjectDecoratorWrapper extends AbstractDecoratorWrapper {
	private ProjectDecorator projectDecorator; 

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject && AbstractDecoratorWrapper.isSVNShared((IProject)element)) {
			this.getProjectDecorator().decorate(element, decoration);
		}
	}

	protected synchronized ProjectDecorator getProjectDecorator() {
		if (this.projectDecorator == null) {
			this.projectDecorator = new ProjectDecorator(this);
		}				
		return this.projectDecorator;
	}

}
