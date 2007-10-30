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

package org.eclipse.team.svn.ui.extension.factory;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * This interface will provide PolClipse 
 * 
 * @author Alexander Gurov
 */
public interface ICommentView {

	/**
	 * Current implementation of History comment message view
	 * 
	 * @param parent
	 * @return
	 */
	public void createCommentView(Composite parent);
	
	public void createCommentView(Composite parent, int style);
	
	public void usedFor(IResource resource);
	
	public void usedFor(IRepositoryResource resource);
	
	public void setComment(String comment);
	
}
