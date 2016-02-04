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

import java.util.Date;
import java.util.List;

/**
 * Parsed SSL server certificate information
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SSLServerCertificateInfo {
	/**
	 * The subject of the certificate.
	 */
    public final String subject;
	/**
	 * The certificate issuer.
	 */
    public final String issuer;
	/**
	 * The from which the certificate is valid.
	 */
    public final Date validFrom;
	/**
	 * The date after which the certificate is no longer valid.
	 */
    public final Date validTo;
	/**
	 * The certificate fingerprint.
	 */
    public final byte[] fingerprint;
	/**
	 * A list of host names that the certificate represents.
	 */
    public final List<String> hostnames;
	/**
	 * the Base64-encoded raw certificate data.
	 */
    public final String asciiCert;

	public SSLServerCertificateInfo(String subject, String issuer, long validFrom, long validTo, 
			byte[] fingerprint, List<String> hostnames, String asciiCert)
	{
		this.subject = subject;
		this.issuer = issuer;
		this.validFrom = new Date(validFrom);
		this.validTo = new Date(validTo);
		this.fingerprint = fingerprint;
		this.hostnames = hostnames;
		this.asciiCert = asciiCert;
	}
    
}
