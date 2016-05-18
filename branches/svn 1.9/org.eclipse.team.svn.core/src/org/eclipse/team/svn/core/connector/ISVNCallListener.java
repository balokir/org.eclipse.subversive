/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.connector;

import java.util.Map;

/**
 * Change list information call-back interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNCallListener {
	public final String GET_CONFIG_DIRECTORY = "getConfigDirectory";
	public final String SET_CONFIG_DIRECTORY = "setConfigDirectory";
	public final String SET_USERNAME = "setUsername";
	public final String SET_PASSWORD = "setPassword";
	public final String IS_CREDENTIALS_CACHE_ENABLED = "isCredentialsCacheEnabled";
	public final String SET_CREDENTIALS_CACHE_ENABLED = "setCredentialsCacheEnabled";
	public final String SET_PROMPT = "setPrompt";
	public final String GET_PROMPT = "getPrompt";
	public final String SET_PROXY = "setProxy";
	public final String SET_CLIENT_SSL_CERTIFICATE = "setClientSSLCertificate";
	public final String IS_SSL_CERTIFICATE_CACHE_ENABLED = "isSSLCertificateCacheEnabled";
	public final String SET_SSL_CERTIFICATE_CACHE_ENABLED = "setSSLCertificateCacheEnabled";
	public final String SET_SSH_CREDENTIALS = "setSSHCredentials";
	public final String SET_SSH_CREDENTIALS_PASSWORD = "setSSHCredentialsPassword";
	public final String SET_COMMIT_MISSING_FILES = "setCommitMissingFiles";
	public final String IS_COMMIT_MISSING_FILES = "isCommitMissingFiles";
	public final String SET_NOTIFICATION_CALLBACK = "setNotificationCallback";
	public final String GET_NOTIFICATION_CALLBACK = "getNotificationCallback";
	public final String CHECKOUT = "checkout";
	public final String LOCK = "lock";
	public final String UNLOCK = "unlock";
	public final String ADD = "add";
	public final String COMMIT = "commit";
	public final String UPDATE = "update";
	public final String SWITCH = "switchTo";
	public final String REVERT = "revert";
	public final String STATUS = "status";
	public final String RELOCATE = "relocate";
	public final String CLEANUP = "cleanup";
	public final String MERGE = "merge";
	public final String MERGE_TWO = "mergeTwo";
	public final String MERGE_REINTEGRATE = "mergeReintegrate";
	public final String GET_MERGE_INFO = "getMergeInfo";
	public final String LIST_MERGE_INFO_LOG = "listMergeInfoLog";
	public final String SUGGEST_MERGE_SOURCES = "suggestMergeSources";
	public final String RESOLVE = "resolve";
	public final String SET_CONFLICT_RESOLVER = "setConflictResolver";
	public final String GET_CONFLICT_RESOLVER = "getConflictResolver";
	public final String ADD_TO_CHANGE_LIST = "addToChangeList";
	public final String REMOVE_FROM_CHANGE_LISTS = "removeFromChangeLists";
	public final String DUMP_CHANGE_LISTS = "dumpChangeLists";
	public final String IMPORT = "importTo";
	public final String EXPORT = "exportTo";
	public final String DIFF_FILE = "diff";
	public final String DIFF_TWO_FILE = "diffTwoFile";
	public final String DIFF_STREAM = "diffStream";
	public final String DIFF_TWO_STREAM = "diffTwoStream";
	public final String DIFF_STATUS = "diffStatus";
	public final String DIFF_STATUS_TWO = "diffStatusTwo";
	public final String GET_INFO = "getInfo";
	public final String STREAM_FILE_CONTENT = "streamFileContent";
	public final String MKDIR = "mkdir";
	public final String MOVE_REMOTE = "moveRemote";
	public final String MOVE_LOCAL = "moveLocal";
	public final String COPY_REMOTE = "copyRemote";
	public final String COPY_LOCAL = "copyLocal";
	public final String REMOVE_REMOTE = "removeRemote";
	public final String REMOVE_LOCAL = "removeLocal";
	public final String LIST_HISTORY_LOG = "listHistoryLog";
	public final String ANNOTATE = "annotate";
	public final String LIST = "list";
	public final String GET_PROPERTIES = "getProperties";
	public final String GET_PROPERTY = "getProperty";
	public final String SET_PROPERTY_LOCAL = "setPropertyLocal";
	public final String SET_PROPERTY_REMOTE = "setPropertyRemote";
	public final String LIST_REVISION_PROPERTIES = "listRevisionProperties";
	public final String GET_REVISION_PROPERTY = "getRevisionProperty";
	public final String SET_REVISION_PROPERTY = "setRevisionProperty";
	public final String UPGRADE = "upgrade";
	public final String PATCH = "patch";
	public final String GET_CONFIGURATION_EVENT_HANDLER = "getConfigurationEventHandler";
	public final String SET_CONFIGURATION_EVENT_HANDLER = "setConfigurationEventHandler";
	public final String VACUUM = "vacuum";


	public final String CREATE = "createRepository";
	public final String DELTIFY = "deltify";
	public final String HOT_COPY = "hotCopy";
	public final String DUMP = "dump";
	public final String LIST_DB_LOGS = "listDBLogs";
	public final String LOAD = "load";
	public final String LIST_TRANSACTIONS = "listTransactions";
	public final String RECOVER = "recover";
	public final String FREEZE = "freeze";
	public final String REMOVE_TRANSACTIONS = "removeTransaction";
	public final String SET_REPOSITORY_REVISION_PROPERTY = "setReppositoryRevisionProperty";
	public final String VERIFY = "verify";
	public final String LIST_LOCKS = "listLocks";
	public final String REMOVE_LOCKS = "removeLocks";
	public final String REPOSITORY_UPGRADE = "repositoryUpgrade";
	public final String PACK = "pack";
	
	/**
	 * Allows to modify parameters before call (for example, you can wrap progress monitor in order to intercept progress notifications)
	 * @param methodName the called method name
	 * @param parameters the call parameters
	 */
	public void asked(String methodName, Map<String, Object> parameters);
	/**
	 * The method is called if SVN call was completed successfully.
	 * @param methodName the called method name
	 * @param parameters the call parameters
	 * @param returnValue the return value if any, null otherwise
	 */
	public void succeeded(String methodName, Map<String, Object> parameters, Object returnValue);
	/**
	 * The method is called if there was a failure while calling SVN.
	 * @param methodName the called method name
	 * @param parameters the call parameters
	 * @param exception the failure reason
	 */
	public void failed(String methodName, Map<String, Object> parameters, SVNConnectorException exception);
}
