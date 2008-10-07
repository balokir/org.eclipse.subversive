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

	public SSHSettings() {
		this.port = SSHSettings.SSH_PORT_DEFAULT;
		this.useKeyFile = false;
		this.privateKeyPath = "";
		this.passPhrase = "";
		this.passPhraseSaved = false;
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

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPrivateKeyPath() {
		return this.privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}

	public boolean isUseKeyFile() {
		return this.useKeyFile;
	}

	public void setUseKeyFile(boolean useKeyFile) {
		this.useKeyFile = useKeyFile;
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
	
}
