/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;


/**
 * Fictive repository tree node (errors, pending etc.)
 * 
 * @author Alexander Gurov
 */
public abstract class RepositoryFictiveNode implements IWorkbenchAdapter, IWorkbenchAdapter2, IAdaptable {
	public Object getParent(Object o) {
		return null;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class) || adapter.equals(IWorkbenchAdapter2.class)) {
			return this;
		}
		return null;
	}
	
    public RGB getBackground(Object element) {
    	// do not change default background color
    	return null;
    }
    
    public RGB getForeground(Object element) {
    	return null;
    }
    
    public FontData getFont(Object element) {
    	// do not change default font
    	return null;
    }
    
}
