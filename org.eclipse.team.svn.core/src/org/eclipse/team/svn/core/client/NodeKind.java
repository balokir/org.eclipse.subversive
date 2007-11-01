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
 * Entry node kind enumeration
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class NodeKind {
	/**
	 * The entry is absent.
	 */
	public static final int NONE = 0;

	/**
	 * The entry is a file
	 */
	public static final int FILE = 1;

	/**
	 * The entry is a directory
	 */
	public static final int DIR = 2;

	/**
	 * The entry kind is unknown
	 */
	public static final int UNKNOWN = 3;

}
