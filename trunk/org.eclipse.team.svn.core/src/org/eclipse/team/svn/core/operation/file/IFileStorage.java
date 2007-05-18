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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.ISVNStorage;

/**
 * java.io.File-based version of IRemoteStorage interface
 * 
 * @author Alexander Gurov
 */
public interface IFileStorage extends ISVNStorage {
	public IRepositoryResource asRepositoryResource(File file, boolean allowsNull);
}
