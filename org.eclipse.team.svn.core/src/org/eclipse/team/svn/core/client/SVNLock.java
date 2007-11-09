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
 * The lock information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLock {
	/**
	 * The lock owner.
	 */
	public final String owner;

	/**
	 * The locked entry path.
	 */
	public final String path;

	/**
	 * The lock token.
	 */
	public final String token;

	/**
	 * The lock comment. Could be <code>null</code>.
	 */
	public final String comment;

	/**
	 * The lock creation date.
	 */
	public final long creationDate;

	/**
	 * The lock expiration date.
	 */
	public final long expirationDate;

	/**
	 * The {@link SVNLock} instance could be initialized only once because all fields are final
	 * 
	 * @param owner
	 *            the lock owner
	 * @param path
	 *            the locked entry path
	 * @param token
	 *            the lock token
	 * @param comment
	 *            the lock comment
	 * @param creationDate
	 *            the lock creation date
	 * @param expirationDate
	 *            the lock expiration date
	 */
	public SVNLock(String owner, String path, String token, String comment, long creationDate, long expirationDate) {
		this.owner = owner;
		this.path = path;
		this.token = token;
		this.comment = comment;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
	}

}
