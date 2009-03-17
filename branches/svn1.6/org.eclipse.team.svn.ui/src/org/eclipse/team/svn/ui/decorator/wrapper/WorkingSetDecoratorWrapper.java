/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator.wrapper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.team.svn.ui.decorator.WorkingSetDecorator;
import org.eclipse.ui.IWorkingSet;

/**
 * Lazy-loader for the working set decorator
 * 
 * @author Alexander Gurov
 */
public class WorkingSetDecoratorWrapper extends AbstractDecoratorWrapper {
	private WorkingSetDecorator workingSetDecorator;
	
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IWorkingSet) {
			IWorkingSet set = (IWorkingSet)element;
			IAdaptable []adaptables = set.getElements();
			boolean allowed = false;
			for (int i = 0; i < adaptables.length; i++) {
				IProject project = (IProject)adaptables[i].getAdapter(IProject.class);
				if (AbstractDecoratorWrapper.isSVNShared(project)) {
					allowed = true;
					break;
				}
			}
			if (allowed) {
				this.getWorkingSetDecorator().decorate(element, decoration);
			}
		}
	}

	protected synchronized WorkingSetDecorator getWorkingSetDecorator() {
		if (this.workingSetDecorator == null) {
			this.workingSetDecorator = new WorkingSetDecorator();
		}				
		return this.workingSetDecorator;
	}

}
