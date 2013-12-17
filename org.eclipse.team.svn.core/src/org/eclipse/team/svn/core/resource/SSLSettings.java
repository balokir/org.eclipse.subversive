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

package org.eclipse.team.svn.core.resource;

import java.io.Serializable;

import org.eclipse.team.svn.core.resource.events.ISSLSettingsStateListener;
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
	private transient ISSLSettingsStateListener parentLocation;
	
	public SSLSettings() {
		this(null);
	}

	public SSLSettings(ISSLSettingsStateListener parentLocation) {
		this.certificatePath = ""; //$NON-NLS-1$
		this.passPhrase = ""; //$NON-NLS-1$
		this.passPhraseSaved = false;
		this.parentLocation = parentLocation;
	}

	public String getCertificatePath() {
		return this.certificatePath;
	}

	public void setCertificatePath(String certificatePath) {
		String oldValue = this.certificatePath;
		this.certificatePath = certificatePath;
		this.fireSSLChanged(ISSLSettingsStateListener.SSL_CERTIFICATE_PATH, oldValue, certificatePath);
	}

	public String getPassPhrase() {
		return this.passPhraseSaved ? SVNUtility.base64Decode(this.passPhrase) : SVNUtility.base64Decode(this.passPhraseTemporary);
	}

	public void setPassPhrase(String passPhrase) {
		String oldValue = this.passPhraseSaved ? this.passPhrase : this.passPhraseTemporary;
		oldValue = oldValue != null ? SVNUtility.base64Decode(oldValue) : oldValue;
		if (this.passPhraseSaved) {
			this.passPhrase = SVNUtility.base64Encode(passPhrase);
		}
		else {
			this.passPhraseTemporary = SVNUtility.base64Encode(passPhrase);
		}
		this.fireSSLChanged(ISSLSettingsStateListener.SSL_PASS_PHRASE, oldValue, passPhrase);
	}

	public boolean isPassPhraseSaved() {
		return this.passPhraseSaved;
	}

	public void setPassPhraseSaved(boolean passPhraseSaved) {
		if (this.passPhraseSaved == passPhraseSaved) {
			return;
		}
		boolean oldValue = this.passPhraseSaved;
		this.passPhraseSaved = passPhraseSaved;
		if (!passPhraseSaved) {
			this.passPhraseTemporary = this.passPhrase;
			this.passPhrase = null;
		}
		else {
			this.passPhrase = this.passPhraseTemporary;
		}
		this.fireSSLChanged(ISSLSettingsStateListener.SSL_PASS_PHRASE_SAVED, Boolean.valueOf(oldValue), Boolean.valueOf(passPhraseSaved));
	}

	public boolean isAuthenticationEnabled() {
		return this.authenticationEnabled;
	}

	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		boolean oldValue = this.authenticationEnabled;
		this.authenticationEnabled = authenticationEnabled;
		this.fireSSLChanged(ISSLSettingsStateListener.SSL_AUTHENTICATION_ENABLED, Boolean.valueOf(oldValue), Boolean.valueOf(authenticationEnabled));
	}
	
	protected void fireSSLChanged(String field, Object oldValue, Object newValue) {
		if (this.parentLocation != null) {
			this.parentLocation.sslChanged(null, field, oldValue, newValue);
		}
	}
	
}
