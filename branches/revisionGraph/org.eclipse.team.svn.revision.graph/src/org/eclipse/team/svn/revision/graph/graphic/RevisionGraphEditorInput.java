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

	protected RevisionRootNode model;
	
	public RevisionGraphEditorInput(RevisionRootNode model) {
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
		return "Revision Graph Input Tooltip";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public RevisionRootNode getModel() {
		return this.model;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof RevisionGraphEditorInput;
	}

}
