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

import org.eclipse.team.svn.core.resource.events.ISSHSettingsStateListener;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SSH settings implementation
 * 
 * @author Alexander Gurov
 */
public class SSHSettings implements Serializable {
	private static final long serialVersionUID = 300172856661110588L;
	
	public static final int SSH_PORT_DEFAULT = 22;
	
	protected int port;
	protected boolean useKeyFile;
	protected String privateKeyPath;
	// Base64 encoded
	protected String passPhrase;
	protected boolean passPhraseSaved;
	
	private transient String passPhraseTemporary;
	private transient ISSHSettingsStateListener parentLocation;

	public SSHSettings() {
		this(null);
	}

	public SSHSettings(ISSHSettingsStateListener parentLocation) {
		this.port = SSHSettings.SSH_PORT_DEFAULT;
		this.useKeyFile = false;
		this.privateKeyPath = ""; //$NON-NLS-1$
		this.passPhrase = ""; //$NON-NLS-1$
		this.passPhraseSaved = false;
		this.parentLocation = parentLocation;
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
		this.fireSSHChanged(ISSHSettingsStateListener.SSH_PASS_PHRASE, oldValue, passPhrase);
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		int oldValue = this.port;
		this.port = port;
		this.fireSSHChanged(ISSHSettingsStateListener.SSH_PORT, Integer.valueOf(oldValue), Integer.valueOf(port));
	}

	public String getPrivateKeyPath() {
		return this.privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		String oldValue = this.privateKeyPath;
		this.privateKeyPath = privateKeyPath;
		this.fireSSHChanged(ISSHSettingsStateListener.SSH_PRIVATE_KEY_PATH, oldValue, privateKeyPath);
	}

	public boolean isUseKeyFile() {
		return this.useKeyFile;
	}

	public void setUseKeyFile(boolean useKeyFile) {
		boolean oldValue = this.useKeyFile;
		this.useKeyFile = useKeyFile;
		this.fireSSHChanged(ISSHSettingsStateListener.SSH_USE_KEY_FILE, Boolean.valueOf(oldValue), Boolean.valueOf(useKeyFile));
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
		this.fireSSHChanged(ISSHSettingsStateListener.SSH_PASS_PHRASE_SAVED, Boolean.valueOf(oldValue), Boolean.valueOf(passPhraseSaved));
	}
	
	protected void fireSSHChanged(String field, Object oldValue, Object newValue) {
		if (this.parentLocation != null) {
			this.parentLocation.sshChanged(null, field, oldValue, newValue);
		}
	}
	
}
