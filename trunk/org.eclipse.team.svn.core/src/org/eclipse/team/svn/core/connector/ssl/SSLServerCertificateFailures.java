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

package org.eclipse.team.svn.core.connector.ssl;

/**
 * SSL server certificate parsing failures representation
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SSLServerCertificateFailures {
	public static final int NOT_YET_VALID = 0x00000001;
	public static final int EXPIRED = 0x00000002;
	public static final int CN_MISMATCH = 0x00000004;
	public static final int UNKNOWN_CA = 0x00000008;
	public static final int OTHER = 0x40000000;
	
	public static final int ALL_KNOWN = (NOT_YET_VALID | EXPIRED | CN_MISMATCH | UNKNOWN_CA | OTHER);

	public final int failures;
	
	/**
	 * Allows to check if any of the conditions specified by the <code>mask</code> are reached
	 * @param mask conditions mask to check
	 */
	public boolean anyOf(int mask) {
		return (this.failures & mask) != 0;
	}
	
	/**
	 * Allows to check if all of the conditions specified by the <code>mask</code> are reached
	 * @param mask conditions mask to check
	 */
	public boolean allOf(int mask) {
		return (this.failures & mask) == mask;
	}
	
	public SSLServerCertificateFailures(int failures) {
		this.failures = failures;
	}
	
}
