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

package org.eclipse.team.svn.ui.repository;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;

/**
 * Repository resource based editor input interface
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryEditorInput extends IStorageEditorInput, IPathEditorInput {
    public IRepositoryResource getRepositoryResource();
	public void fetchContents(IProgressMonitor monitor);
}
