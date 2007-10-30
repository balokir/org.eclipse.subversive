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

package org.eclipse.team.svn.core.client;

import java.util.Map;

/**
 * Replacement for org.tigris.subversion.javahl.ProplistCallback
 * 
 * @author Alexander Gurov
 */
public interface ProplistCallback {
	/**
	 * the method will be called for every line in a file.
	 * 
	 * @param path
	 *            the path.
	 * @param props
	 *            the properties on the path.
	 */
	public void singlePath(String path, Map properties);
}
