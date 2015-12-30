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
 * Error handling facility allows external code to provide its own decision about the problem resolution 
 * and prevents core module from crash or non-user-friendly actions.
 * This will be very helpful in case of internal problems like "Unable connect to project" etc.
 * 
 * @author Alexander Gurov
 */
public interface IErrorHandlingFacility extends IResolutionHelper {
	/**
	 * The method adds external resolution helper
	 * @param helper resolution helper instance
	 */
	public void addResolutionHelper(IResolutionHelper helper);
	/**
	 * The method removes external resolution helper
	 * @param helper resolution helper instance
	 */
	public void removeResolutionHelper(IResolutionHelper helper);
}
