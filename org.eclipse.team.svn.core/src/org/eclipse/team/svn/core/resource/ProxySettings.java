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
 * Proxy settings implementation
 * 
 * @author Alexander Gurov
 */
public class ProxySettings implements Serializable {
	public static final int DEFAULT_PORT = 3128;
	
	private static final long serialVersionUID = 7631295136124698228L;
	
	protected String username;
	protected String host;
	protected int port;
	// Base64 encoded
	protected String password;
	private transient String passwordTemporary;
	protected boolean enabled;
	protected boolean authenticationEnabled;
	protected boolean passwordSaved;
	
	public ProxySettings() {
		this.username = "";
		this.password = "";
		this.host = "";
		this.port =ProxySettings.DEFAULT_PORT;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return this.passwordSaved ? SVNUtility.base64Decode(this.password) : SVNUtility.base64Decode(this.passwordTemporary);
	}
	
	public void setPassword(String password) {
		if (this.passwordSaved) {
			this.password = SVNUtility.base64Encode(password);
		}
		else {
			this.passwordTemporary = SVNUtility.base64Encode(password);
		}
	}
	
	public String getHost() {
		return this.host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public boolean isAuthenticationEnabled() {
		return this.authenticationEnabled;
	}

	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}

	public boolean isPasswordSaved() {
		return this.passwordSaved;
	}

	public void setPasswordSaved(boolean passwordSaved) {
		if (this.passwordSaved == passwordSaved) {
			return;
		}
		this.passwordSaved = passwordSaved;
		if (!passwordSaved) {
			this.passwordTemporary = this.password;
			this.password = null;
		}
		else {
			this.password = this.passwordTemporary;
		}
	}
	
}
