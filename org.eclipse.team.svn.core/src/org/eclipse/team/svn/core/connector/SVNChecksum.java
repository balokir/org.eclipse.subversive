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

package org.eclipse.team.svn.core.connector;

/**
 * The SVN checksum representation
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNChecksum {
	public enum Kind {
		/**
		 * Pre-SVN 1.7
		 */
		LEGACY(-1),
		/**
		 * MD5 checksum type
		 */
		MD5(0),
		/**
		 * SHA1 checksum type
		 */
		SHA1(1);
		
		public final int id;
		
		private Kind(int id) {
			this.id = id;
		}
	}

	/**
	 * Checksum kind
	 */
	public final Kind kind;

	/**
	 * Checksum data
	 */
	public final byte []digest;

	/**
	 * The {@link SVNChecksum} instance could be initialized only once because all fields are final
	 * 
	 * @param kind
	 *            the checksum kind
	 * @param digest
	 *            the checksum digest
	 */
	public SVNChecksum(Kind kind, byte []digest) {
		this.kind = kind;
		this.digest = digest != null ? new byte[digest.length] : null;
		if (digest != null) {
			System.arraycopy(digest, 0, this.digest, 0, digest.length);
		}
	}
}
