/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.runtime.IPath;

/**
 * SVN storage interface. Allows to manage repository locations.
 * 
 * @author Alexander Gurov
 */
public interface ISVNStorage extends IRepositoryLocationFactory {
	public static final int IGNORE_NAME = 0;
	public static final int IGNORE_EXTENSION = 1;
	public static final int IGNORE_PATTERN = 2;
	
	public void initialize(IPath stateInfoLocation) throws Exception;
	public void dispose();
	
	public IRepositoryLocation []getRepositoryLocations();
	public IRepositoryLocation getRepositoryLocation(String id);
	public void addRepositoryLocation(IRepositoryLocation location);
	public void removeRepositoryLocation(IRepositoryLocation location);
	public void reconfigureLocations();
	public void saveConfiguration() throws Exception;

	public byte []repositoryResourceAsBytes(IRepositoryResource resource);
	public IRepositoryResource repositoryResourceFromBytes(byte []bytes);
}
