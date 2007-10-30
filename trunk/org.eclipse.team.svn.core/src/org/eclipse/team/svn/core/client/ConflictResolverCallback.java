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

import java.util.EventListener;

/**
 * Replacement for org.tigris.subversion.javahl.ConflictResolverCallback
 * 
 * @author Alexander Gurov
 */
public interface ConflictResolverCallback extends EventListener {
	/**
	 * The callback method invoked for each conflict during a
	 * merge/update/switch operation. NOTE: The files that are potentially
	 * passed in the ConflictDescriptor are in repository-normal format (LF line
	 * endings and contracted keywords).
	 * 
	 * @param descrip
	 *            A description of the conflict.
	 * @return The result of any conflict resolution, from the {@link #Result}
	 *         enum.
	 * @throws SubversionException
	 *             If an error occurs.
	 * @see ConflictResolverCallback.Result
	 */
	public ConflictResult resolve(ConflictDescriptor descrip) throws ClientWrapperException;

}
