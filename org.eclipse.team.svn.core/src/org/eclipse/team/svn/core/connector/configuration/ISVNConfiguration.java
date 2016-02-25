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

package org.eclipse.team.svn.core.connector.configuration;

/**
 * SVN configuration access object interface. Manipulates in-memory information.
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNConfiguration {
	/*
	 * The following constants are section and option names from the "config" configuration file.
	 */
	public static final String SECTION_AUTH          = "auth";
	public static final String PASSWORD_STORES           = "password-stores";
	public static final String KWALLET_WALLET            = "kwallet-wallet";
	public static final String KWALLET_SVN_APPLICATION_NAME_WITH_PID = "kwallet-svn-application-name-with-pid";
	public static final String SSL_CLIENT_CERT_FILE_PROMPT = "ssl-client-cert-file-prompt";
	
	public static final String SECTION_HELPERS       = "helpers";
	public static final String EDITOR_CMD                = "editor-cmd";
	public static final String DIFF_CMD                  = "diff-cmd";
	public static final String DIFF_EXTENSIONS           = "diff-extensions";
	public static final String DIFF3_CMD                 = "diff3-cmd";
	public static final String DIFF3_HAS_PROGRAM_ARG     = "diff3-has-program-arg";
	public static final String MERGE_TOOL_CMD            = "merge-tool-cmd";
	
	public static final String SECTION_MISCELLANY    = "miscellany";
	public static final String GLOBAL_IGNORES            = "global-ignores";
	public static final String LOG_ENCODING              = "log-encoding";
	public static final String USE_COMMIT_TIMES          = "use-commit-times";
	public static final String ENABLE_AUTO_PROPS         = "enable-auto-props";
	public static final String ENABLE_MAGIC_FILE         = "enable-magic-file";
	public static final String NO_UNLOCK                 = "no-unlock";
	public static final String MIMETYPES_FILE            = "mime-types-file";
	public static final String PRESERVED_CF_EXTS         = "preserved-conflict-file-exts";
	public static final String INTERACTIVE_CONFLICTS     = "interactive-conflicts";
	public static final String MEMORY_CACHE_SIZE         = "memory-cache-size";
	public static final String DIFF_IGNORE_CONTENT_TYPE  = "diff-ignore-content-type";
	
	public static final String SECTION_TUNNELS       = "tunnels";
	
	public static final String SECTION_AUTO_PROPS    = "auto-props";
	
	public static final String SECTION_WORKING_COPY  = "working-copy";
	public static final String SQLITE_EXCLUSIVE          = "exclusive-locking";
	public static final String SQLITE_EXCLUSIVE_CLIENTS  = "exclusive-locking-clients";
	public static final String SQLITE_BUSY_TIMEOUT       = "busy-timeout";

	/*
	 * The following constants are section and option names from the "servers" configuration file.
	 */
	public static final String SECTION_GROUPS        = "groups";
	public static final String SECTION_GLOBAL        = "global";
	
	public static final String HTTP_PROXY_HOST           = "http-proxy-host";
	public static final String HTTP_PROXY_PORT           = "http-proxy-port";
	public static final String HTTP_PROXY_USERNAME       = "http-proxy-username";
	public static final String HTTP_PROXY_PASSWORD       = "http-proxy-password";
	public static final String HTTP_PROXY_EXCEPTIONS     = "http-proxy-exceptions";
	public static final String HTTP_TIMEOUT              = "http-timeout";
	public static final String HTTP_COMPRESSION          = "http-compression";
	public static final String NEON_DEBUG_MASK           = "neon-debug-mask";
	public static final String HTTP_AUTH_TYPES           = "http-auth-types";
	public static final String SSL_AUTHORITY_FILES       = "ssl-authority-files";
	public static final String SSL_TRUST_DEFAULT_CA      = "ssl-trust-default-ca";
	public static final String SSL_CLIENT_CERT_FILE      = "ssl-client-cert-file";
	public static final String SSL_CLIENT_CERT_PASSWORD  = "ssl-client-cert-password";
	public static final String SSL_PKCS11_PROVIDER       = "ssl-pkcs11-provider";
	public static final String HTTP_LIBRARY              = "http-library";
	public static final String STORE_PASSWORDS           = "store-passwords";
	public static final String STORE_PLAINTEXT_PASSWORDS = "store-plaintext-passwords";
	public static final String STORE_AUTH_CREDS          = "store-auth-creds";
	public static final String STORE_SSL_CLIENT_CERT_PP  = "store-ssl-client-cert-pp";
	public static final String STORE_SSL_CLIENT_CERT_PP_PLAINTEXT = "store-ssl-client-cert-pp-plaintext";
	public static final String USERNAME                  = "username";
	public static final String HTTP_BULK_UPDATES         = "http-bulk-updates";
	public static final String HTTP_MAX_CONNECTIONS      = "http-max-connections";
	public static final String HTTP_CHUNKED_REQUESTS     = "http-chunked-requests";
	public static final String SERF_LOG_COMPONENTS       = "serf-log-components";
	public static final String SERF_LOG_LEVEL            = "serf-log-level";
	
	/**
	 * "true" value in configuration.
	 */
	public static final String TRUE = "TRUE";
	
	/**
	 * "false" value in configuration.
	 */
	public static final String FALSE = "FALSE";

	/**
	 * "ask" value in configuration.
	 */
	public static final String ASK = "ASK";

	/**
	 * Returns a reference to the "config" settings category.
	 * @return ISVNConfigurationCategory
	 */
	public ISVNConfigurationCategory getConfigCategory();
	
	/**
	 * Returns a reference to the "servers" settings category.
	 * @return ISVNConfigurationCategory
	 */
	public ISVNConfigurationCategory getServersCategory();
}
