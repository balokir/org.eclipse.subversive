/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

/**
 * Repository location factory interface
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryLocationFactory {
	public IRepositoryLocation newRepositoryLocation();
	public void copyRepositoryLocation(IRepositoryLocation to, IRepositoryLocation from);
	public IRepositoryLocation newRepositoryLocation(String reference);
	/*
	 * see IRepositoryLocation class comments why we need 'saveRevisionLinksComments' parameter
	 */
	public String repositoryLocationAsReference(IRepositoryLocation location, boolean saveRevisionLinksComments);
}
