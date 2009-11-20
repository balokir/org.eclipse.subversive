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

package org.eclipse.team.svn.ui.decorator;

import org.eclipse.core.resources.IResource;

/**
 * Check if resource decoration is allowed
 * 
 * @author Alexander Gurov
 */
public interface IDecorationFilter {
	/**
	 * Returns <code>false</code> if resource should not be decorated
	 * @param resource resource which is about to be decorated
	 * @return <code>false</code> if resource should not be decorated, <code>true</code> otherwise
	 */
	public boolean isAcceptable(IResource resource);
}
