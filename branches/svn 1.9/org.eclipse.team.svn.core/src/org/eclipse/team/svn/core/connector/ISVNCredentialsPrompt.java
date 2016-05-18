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

package org.eclipse.team.svn.core.connector;

import org.eclipse.team.svn.core.connector.ssl.SSLServerCertificateFailures;
import org.eclipse.team.svn.core.connector.ssl.SSLServerCertificateInfo;

/**
 * Interface that provide ability to ask user about repository credentials
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNCredentialsPrompt {
	public static final ISVNCredentialsPrompt DEFAULT_PROMPT = new ISVNCredentialsPrompt() {
		public boolean promptSSL(Object context, String realm) {
			return false;
		}

		public boolean promptSSH(Object context, String realm) {
			return false;
		}

		public boolean promptProxy(Object context) {
			return false;
		}

		public boolean prompt(Object context, String realm) {
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

		public Answer askTrustSSLServer(Object context, SSLServerCertificateFailures failures, SSLServerCertificateInfo info, boolean allowPermanently) {
			return Answer.ACCEPT_TEMPORARY;
		}
	};

	public enum Answer {
		/**
		 * Reject the connection to the server.
		 */
		REJECT(0),
		/**
		 * Accept the connection to the server <i>once</i> per session.
		 */
		ACCEPT_TEMPORARY(1),
		/**
		 * Accept the connection to the server <i>permanently</i>.
		 */
		ACCEPT_PERMANENTLY(2);
		
		public final int id;
		
		private Answer(int id) {
			this.id = id;
		}
	}

	public static final String ROOT_LOCATION = "<Repository Location>"; //$NON-NLS-1$

	public boolean prompt(Object context, String realm);

	public boolean promptSSL(Object context, String realm);

	public boolean promptSSH(Object context, String realm);

	public boolean promptProxy(Object context);

	public Answer askTrustSSLServer(Object context, SSLServerCertificateFailures failures, SSLServerCertificateInfo info, boolean allowPermanently);

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
