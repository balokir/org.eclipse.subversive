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

/**
 * Enumeration of possible working copy entry locking states
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class LockStatus {
	/**
	 * The operation does not require any locks
	 */
	public static final int INAPPLICABLE = 0;

	/**
	 * The lock state is unknown
	 */
	public static final int UNKNOWN = 1;

	/**
	 * The lock state are same as before starting the operation
	 */
	public static final int UNCHANGED = 2;

	/**
	 * The working copy entry was locked
	 */
	public static final int LOCKED = 3;

	/**
	 * The working copy entry was unlocked
	 */
	public static final int UNLOCKED = 4;

}
