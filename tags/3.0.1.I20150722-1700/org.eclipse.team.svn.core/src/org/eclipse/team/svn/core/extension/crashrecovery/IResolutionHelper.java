/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension.crashrecovery;

/**
 * Provides ability to solve the problem which is unrecoverable in the core module context
 * 
 * @author Alexander Gurov
 */
public interface IResolutionHelper {
	/**
	 * The method provides resolution for errors about it is known 
	 * @param description structure which describes the error happened
	 */
	public boolean acquireResolution(ErrorDescription description);
}
