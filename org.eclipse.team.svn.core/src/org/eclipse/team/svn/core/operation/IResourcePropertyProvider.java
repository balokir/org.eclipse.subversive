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

package org.eclipse.team.svn.core.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * This interface allows us to redefine property editor behaviour depending on property source
 * 
 * @author Alexander Gurov
 */
public interface IResourcePropertyProvider extends IActionOperation {
	public IResource getLocal();
	public IRepositoryResource getRemote();
	public SVNProperty []getProperties();
	public boolean isEditAllowed();
	public void refresh();
}
