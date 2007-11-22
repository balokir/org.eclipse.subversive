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

package org.eclipse.team.svn.core.connector;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Interface that provide ability to ask user about repository credentials
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNCredentialsPrompt {
	public static final ISVNCredentialsPrompt DEFAULT_PROMPT = new ISVNCredentialsPrompt() {
		public boolean promptSSL(IRepositoryLocation location, String realm) {
			return false;
		}

		public boolean promptSSH(IRepositoryLocation location, String realm) {
			return false;
		}

		public boolean promptProxy(IRepositoryLocation location) {
			return false;
		}

		public boolean prompt(IRepositoryLocation location, String realm) {
			return false;
		}

		public boolean isSaveProxyPassword() {
			return false;
		}

		public boolean isSaveCredentialsEnabled() {
			return false;
		}

		public boolean isSSLSavePassphrase() {
			return false;
		}

		public boolean isSSLAuthenticationEnabled() {
			return false;
		}

		public boolean isSSHPublicKeySelected() {
			return false;
		}

		public boolean isSSHPrivateKeyPassphraseSaved() {
			return false;
		}

		public boolean isProxyEnabled() {
			return false;
		}

		public boolean isProxyAuthenticationEnabled() {
			return false;
		}

		public String getUsername() {
			return null;
		}

		public String getSSLClientCertPath() {
			return null;
		}

		public String getSSLClientCertPassword() {
			return null;
		}

		public String getSSHPrivateKeyPath() {
			return null;
		}

		public String getSSHPrivateKeyPassphrase() {
			return null;
		}

		public int getSSHPort() {
			return -1;
		}

		public String getRealmToSave() {
			return ISVNCredentialsPrompt.ROOT_LOCATION;
		}

		public String getProxyUserName() {
			return null;
		}

		public int getProxyPort() {
			return -1;
		}

		public String getProxyPassword() {
			return null;
		}

		public String getProxyHost() {
			return null;
		}

		public String getPassword() {
			return null;
		}

		public int askTrustSSLServer(IRepositoryLocation location, String info, boolean allowPermanently) {
			return ISVNCredentialsPrompt.ACCEPT_TEMPORARY;
		}
	};

	/**
	 * Reject the connection to the server.
	 */
	public static final int REJECT = 0;

	/**
	 * Accept the connection to the server <i>once</i> per session.
	 */
	public static final int ACCEPT_TEMPORARY = 1;

	/**
	 * Accept the connection to the server <i>permanently</i>.
	 */
	public static final int ACCEPT_PERMANENTLY = 2;

	public static final String ROOT_LOCATION = "<Repository Location>";

	public boolean prompt(IRepositoryLocation location, String realm);

	public boolean promptSSL(IRepositoryLocation location, String realm);

	public boolean promptSSH(IRepositoryLocation location, String realm);

	public boolean promptProxy(IRepositoryLocation location);

	public int askTrustSSLServer(IRepositoryLocation location, String info, boolean allowPermanently);

	public String getSSHPrivateKeyPath();

	public String getSSHPrivateKeyPassphrase();

	public boolean isSSHPrivateKeyPassphraseSaved();

	public int getSSHPort();

	public String getSSLClientCertPath();

	public String getSSLClientCertPassword();

	public String getUsername();

	public String getPassword();

	public boolean isSaveCredentialsEnabled();

	public boolean isSSHPublicKeySelected();

	public boolean isSSLAuthenticationEnabled();

	public boolean isSSLSavePassphrase();

	public boolean isProxyEnabled();

	public boolean isProxyAuthenticationEnabled();

	public String getProxyHost();

	public int getProxyPort();

	public String getProxyUserName();

	public String getProxyPassword();

	public boolean isSaveProxyPassword();

	public String getRealmToSave();

}
