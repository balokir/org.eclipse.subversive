/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import java.io.Serializable;

import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SSL settings implementation
 * 
 * @author Alexander Gurov
 */
public class SSLSettings implements Serializable {
	private static final long serialVersionUID = -5649960025841815445L;

	protected boolean authenticationEnabled;
	protected String certificatePath;
	protected boolean passPhraseSaved;
	// Base64 encoded
	protected String passPhrase;
	private transient String passPhraseTemporary;
	
	public SSLSettings() {
		this.certificatePath = "";
		this.passPhrase = "";
		this.passPhraseSaved = false;
	}

	public String getCertificatePath() {
		return this.certificatePath;
	}

	public void setCertificatePath(String certificatePath) {
		this.certificatePath = certificatePath;
	}

	public String getPassPhrase() {
		return this.passPhraseSaved ? SVNUtility.base64Decode(this.passPhrase) : SVNUtility.base64Decode(this.passPhraseTemporary);
	}

	public void setPassPhrase(String passPhrase) {
		if (this.passPhraseSaved) {
			this.passPhrase = SVNUtility.base64Encode(passPhrase);
		}
		else {
			this.passPhraseTemporary = SVNUtility.base64Encode(passPhrase);
		}
	}

	public boolean isPassPhraseSaved() {
		return this.passPhraseSaved;
	}

	public void setPassPhraseSaved(boolean passPhraseSaved) {
		if (this.passPhraseSaved == passPhraseSaved) {
			return;
		}
		this.passPhraseSaved = passPhraseSaved;
		if (!passPhraseSaved) {
			this.passPhraseTemporary = this.passPhrase;
			this.passPhrase = null;
		}
		else {
			this.passPhrase = this.passPhraseTemporary;
		}
	}

	public boolean isAuthenticationEnabled() {
		return this.authenticationEnabled;
	}

	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}
	
}
