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

import org.eclipse.core.runtime.IAdaptable;

/**
 * This interface represents base functionality that can be applied to the versioned resource
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryBase extends IAdaptable {
	public String getName();
	public String getUrl();
	
}
