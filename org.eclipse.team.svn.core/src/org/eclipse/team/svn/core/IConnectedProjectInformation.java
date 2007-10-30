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

package org.eclipse.team.svn.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.svn.core.operation.HiddenException;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * This interface provides an information about project connection to the repository
 * 
 * @author Alexander Gurov
 */
public interface IConnectedProjectInformation {
	
	public IRepositoryLocation getRepositoryLocation() throws HiddenException;
	
	public IRepositoryResource getRepositoryResource() throws HiddenException;
	
	public void switchResource(IRepositoryResource resource) throws CoreException;
	
	public void relocateResource() throws CoreException;
	
}
