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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/** 
 * TODO correctly implement
 * 
 * @author Igor Burilo
 */
public class RevisionGraphEditorInput implements IEditorInput {

	protected Object model;
	
	public RevisionGraphEditorInput(Object model) {
		this.model = model;
	}
	
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "Revision Graph Input";
	}

	public IPersistableElement getPersistable() {	
		return null;
	}

	public String getToolTipText() {
		return "Revision Graph";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	/**
	 * Current model can be either <class>String</class> or <class>RevisionRootNode</class>
	 * 
	 * String indicates that we can't create model for some reason, e.g.
	 * there's no connection to repository and cache is empty
	 * 
	 * @return model
	 */
	public Object getModel() {
		return this.model;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof RevisionGraphEditorInput;
	}

}
