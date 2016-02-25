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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.team.svn.core.connector.SVNConflictResolution.Choice;
import org.eclipse.team.svn.core.connector.SVNMergeInfo.LogKind;
import org.eclipse.team.svn.core.connector.configuration.ISVNConfigurationEventHandler;

/**
 * SVN connector wrapper interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNConnector {
	public static final String []EMPTY_LOG_ENTRY_PROPS = new String []{};

	public static final String []DEFAULT_LOG_ENTRY_PROPS = new String []{ SVNProperty.BuiltIn.REV_LOG, SVNProperty.BuiltIn.REV_DATE, SVNProperty.BuiltIn.REV_AUTHOR };
	
	public static final Map<String, List<SVNExternalReference>> NO_EXTERNALS_TO_PIN = new HashMap<String, List<SVNExternalReference>>();

	/**
	 * All available SVN commands options
	 */
	public static class Options {
		/**
		 * No options specified for the SVN command.
		 */
		public static final long NONE = 0;

		/**
		 * Ignore svn:externals property.
		 */
		public static final long IGNORE_EXTERNALS = 0x01;

		/**
		 * Allow unversioned resources in the path where operation is performed.
		 */
		public static final long ALLOW_UNVERSIONED_OBSTRUCTIONS = 0x02;

		/**
		 * Force operation execution.
		 */
		public static final long FORCE = 0x04;

		/**
		 * Include related parents into operation context.
		 */
		public static final long INCLUDE_PARENTS = 0x08;

		/**
		 * Do not unlock resources after commit.
		 */
		public static final long KEEP_LOCKS = 0x10;

		/**
		 * Keep change list when commit is performed.
		 */
		public static final long KEEP_CHANGE_LIST = 0x20;

		/**
		 * Report server side changes.
		 */
		public static final long SERVER_SIDE = 0x40;

		/**
		 * Get statuses for versioned but not modified nodes also.
		 */
		public static final long INCLUDE_UNCHANGED = 0x80;

		/**
		 * Do not handle svn:ignore property and global ignores.
		 */
		public static final long INCLUDE_IGNORED = 0x100;

		/**
		 * Ignore resource ancestry/always treat source files as related.
		 */
		public static final long IGNORE_ANCESTRY = 0x200;

		/**
		 * Do not perform real operation.
		 */
		public static final long SIMULATE = 0x400;

		/**
		 * Do not perform merge itself, but write merge records.
		 */
		public static final long RECORD_ONLY = 0x800;

		/**
		 * Ignore resources which node type is unknown.
		 */
		public static final long IGNORE_UNKNOWN_NODE_TYPES = 0x1000;

		/**
		 * Do not include deleted resources into patch.
		 */
		public static final long SKIP_DELETED = 0x2000;

		/**
		 * Indicate the depth value is ambient.
		 */
		public static final long DEPTH_IS_STICKY = 0x4000;

		/**
		 * Create moved or copied folder as child of the destination folder.
		 */
		public static final long INTERPRET_AS_CHILD = 0x8000;

		/**
		 * Keep local copies when resources are removed from the source control.
		 */
		public static final long KEEP_LOCAL = 0x10000;

		/**
		 * Do not fetch history after copy record found.
		 */
		public static final long STOP_ON_COPY = 0x20000;

		/**
		 * Extract changed paths from the history.
		 */
		public static final long DISCOVER_PATHS = 0x40000;

		/**
		 * Include information about merged revisions.
		 */
		public static final long INCLUDE_MERGED_REVISIONS = 0x80000;

		/**
		 * Ignore resource mime-type.
		 */
		public static final long IGNORE_MIME_TYPE = 0x100000;

		/**
		 * Fetch locks information also.
		 */
		public static final long FETCH_LOCKS = 0x200000;

		/**
		 * @since 1.7 Report copied resources as additions.
		 */
		public static final long COPIES_AS_ADDITIONS = 0x400000;

		/**
		 * @since 1.7 Reverse patch.
		 */
		public static final long REVERSE = 0x800000;

		/**
		 * @since 1.7 Ignore whitespace difference while applying patch.
		 */
		public static final long IGNORE_WHITESPACE = 0x1000000;

		/**
		 * @since 1.7 Remove temporary files after patch is applied.
		 */
		public static final long REMOVE_TEMPORARY_FILES = 0x2000000;

		/**
		 * @since 1.8 Ignores any auto-props configuration.
		 */
		public static final long IGNORE_AUTOPROPS = 0x4000000;

		/**
		 * @since 1.8 Moves just the metadata and not the working files/dirs.
		 */
		public static final long METADATA_ONLY = 0x8000000;

		/**
		 * @since 1.8 Use copy and delete without move tracking when a srcPath is mixed-revision, if false return an error when a srcPath is mixed-revision.
		 */
		public static final long ALLOW_MIXED_REVISIONS = 0x10000000;

		/**
		 * TODO check IGNORE_ANCESTRY! - compatibility with earlier versions
		 * @since 1.8 Ignore merge history, treat sources as unrelated.
		 */
		public static final long IGNORE_MERGE_HISTORY = 0x20000000;

		/**
		 * @since 1.8 Don't show property changes.
		 */
		public static final long IGNORE_PROPERTY_CHANGES = 0x40000000;

		/**
		 * @since 1.8 Show property changes only.
		 */
		public static final long IGNORE_CONTENT_CHANGES = 0x80000000;

		/**
		 * @since 1.8 Inherit properties.
		 */
		public static final long INHERIT_PROPERTIES = 0x100000000L;

		/**
		 * @since 1.9 Report server local changes.
		 */
		public static final long LOCAL_SIDE = 0x200000000L;

		/**
		 * @since 1.9 Do not expand keywords.
		 */
		public static final long IGNORE_KEYWORDS = 0x400000000L;

		/**
		 * @since 1.9 Retrieve information about nodes that are excluded from the working copy.
		 */
		public static final long FETCH_EXCLUDED = 0x800000000L;

		/**
		 * @since 1.9 Retrieve information about node that are not versioned, but are still tree conflicted.
		 */
		public static final long FETCH_ACTUAL_ONLY = 0x1000000000L;

		/**
		 * @since 1.9 Recurs into externals directories.
		 */
		public static final long INCLUDE_EXTERNALS = 0x2000000000L;

		/**
		 * @since 1.9 Process change lists in an operation
		 */
		public static final long INCLUDE_CHANGELISTS = 0x4000000000L;

		/**
		 * @since 1.9 Break locks # no doc available
		 */
		public static final long BREAK_LOCKS = 0x8000000000L;

		/**
		 * @since 1.9 Process timestaps
		 */
		public static final long INCLUDE_TIMESTAMPS = 0x10000000000L;

		/**
		 * @since 1.9 Process DAV cache
		 */
		public static final long INCLUDE_DAVCACHE = 0x20000000000L;

		/**
		 * @since 1.9 Process unused pristines
		 */
		public static final long INCLUDE_UNUSED_PRISTINES = 0x40000000000L;

		/**
		 * @since 1.9 Include properties
		 */
		public static final long INCLUDE_PROPERTIES = 0x80000000000L;

		/**
		 * @since 1.9 Include unversioned
		 */
		public static final long INCLUDE_UNVERSIONED = 0x100000000000L;
		
		public static String asCommandLine(long options) {
			StringBuffer retVal = new StringBuffer();
			for (int i = 0; i < Options.optionNames.length; i++) {
				if ((options & 1l) != 0) {
					String option = Options.optionNames[i];
					if (retVal.indexOf(option) == -1) {
						retVal.append(option);
					}
				}
				options >>= 1;
			}
			return retVal.toString();
		}
		
		private static final String[] optionNames = { 
			" --ignore-externals", " --force", " --force", " --parents", " --no-unlock", " --keep-changelists", 
			" -u", " -v", " --no-ignore", " --ignore-ancestry", " --dry-run", " --record-only", " -- force", 
			" --no-diff-deleted", ""/*DEPTH_IS_STICKY*/, ""/*INTERPRET_AS_CHILD*/, " --keep-local", " --stop-on-copy", " -v", " -g", 
			" --force", " -v", " --show-copies-as-adds", " --reverse-diff", " --ignore-whitespace", ""/*REMOVE_TEMPORARY_FILES*/, 
			" --no-auto-props", "", " --allow-mixed-revisions", " --ignore-ancestry", " --ignore-properties", 
			" --properties-only", " --show-inherited-props"};
	}
	
	public static class DiffOptions {
		/**
		 * No options specified for the SVN command.
		 */
		public static final long NONE = 0;

		/**
		 * @since 1.8 Ignore difference in whitespace completely.
		 */
		public static final long IGNORE_WHITESPACE = 0x00000001;
		/**
		 * @since 1.8 Ignore difference in space numbers.
		 */
		public static final long IGNORE_SPACE_CHANGE = 0x00000002;
		/**
		 * @since 1.8 Ignore difference in EOL style.
		 */
		public static final long IGNORE_EOL_STYLE = 0x00000004;
		/**
		 * @since 1.8 Show C function name.
		 */
		public static final long SHOW_FUNCTION = 0x00000008;
		/**
		 * @since 1.8 Use extended GIT's format for patch files.
		 */
		public static final long GIT_FORMAT = 0x00000010;
		
		public static String asCommandLine(long options) {
			StringBuffer retVal = new StringBuffer();
			for (int i = 0; i < DiffOptions.optionNames.length; i++) {
				if ((options & 1l) != 0) {
					retVal.append(DiffOptions.optionNames[i]);
				}
				options >>= 1;
			}
			if (retVal.length() > 0) {
				retVal.insert(0, " -x");
			}
			return retVal.toString();
		}
		
		private static final String[] optionNames = { 
			" --ignore-all-space", " --ignore-space-change", " --ignore-eol-style", " --show-c-function", " --git"};
	}

	/**
	 * Command-related option masks
	 */
	public static class CommandMasks {
		public static final long CHECKOUT = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS;

		public static final long LOCK = Options.FORCE;

		public static final long UNLOCK = Options.FORCE;

		public static final long ADD = Options.FORCE | Options.INCLUDE_IGNORED | Options.INCLUDE_PARENTS | Options.IGNORE_AUTOPROPS;

		public static final long COMMIT = Options.KEEP_LOCKS | Options.KEEP_CHANGE_LIST;

		public static final long UPDATE = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS | Options.DEPTH_IS_STICKY;

		public static final long SWITCH = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS | Options.DEPTH_IS_STICKY;

		public static final long STATUS = Options.SERVER_SIDE | Options.LOCAL_SIDE | Options.INCLUDE_UNCHANGED | Options.INCLUDE_IGNORED | Options.IGNORE_EXTERNALS | Options.DEPTH_IS_STICKY;

		public static final long MERGE = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SIMULATE | Options.RECORD_ONLY | Options.IGNORE_MERGE_HISTORY | Options.ALLOW_MIXED_REVISIONS;

		public static final long MERGE_REINTEGRATE = Options.FORCE /*OVR&UPD*/ | Options.SIMULATE;

		public static final long MERGE_STATUS = Options.FORCE | Options.IGNORE_ANCESTRY;

		public static final long MERGE_STATUS_REINTEGRATE = Options.NONE;

		public static final long IMPORT = Options.INCLUDE_IGNORED | Options.IGNORE_UNKNOWN_NODE_TYPES | Options.IGNORE_AUTOPROPS;

		public static final long EXPORT = Options.FORCE | Options.IGNORE_EXTERNALS | Options.IGNORE_KEYWORDS;

		public static final long DIFF = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SKIP_DELETED | Options.IGNORE_PROPERTY_CHANGES | Options.IGNORE_CONTENT_CHANGES;

		public static final long DIFF_STATUS = Options.IGNORE_ANCESTRY;

		public static final long MKDIR = Options.INCLUDE_PARENTS;

		public static final long MOVE_LOCAL = Options.FORCE | Options.ALLOW_MIXED_REVISIONS | Options.METADATA_ONLY;

		public static final long MOVE_SERVER = Options.FORCE | Options.INTERPRET_AS_CHILD | Options.INCLUDE_PARENTS;

		public static final long COPY_LOCAL = Options.IGNORE_EXTERNALS | Options.METADATA_ONLY;

		public static final long COPY_SERVER = Options.INTERPRET_AS_CHILD | Options.INCLUDE_PARENTS;

		public static final long REMOVE_LOCAL = Options.FORCE | Options.KEEP_LOCAL;

		public static final long REMOVE_SERVER = Options.FORCE;

		public static final long LIST_HISTORY_LOG = Options.STOP_ON_COPY | Options.DISCOVER_PATHS | Options.INCLUDE_MERGED_REVISIONS;

		public static final long ANNOTATE = Options.IGNORE_MIME_TYPE | Options.INCLUDE_MERGED_REVISIONS;

		public static final long LIST_ENTRIES = Options.FETCH_LOCKS;

		public static final long LIST_PROPERTIES = Options.INHERIT_PROPERTIES;

		public static final long SET_PROPERTY_LOCAL = Options.FORCE;

		public static final long SET_PROPERTY_REMOTE = Options.FORCE;

		public static final long SET_REVISION_PROPERTY = Options.FORCE;
		
		public static final long PATCH = Options.IGNORE_WHITESPACE | Options.REVERSE | Options.SIMULATE | Options.REMOVE_TEMPORARY_FILES;
		
		public static final long GET_INFO = Options.FETCH_EXCLUDED | Options.FETCH_ACTUAL_ONLY | Options.INCLUDE_EXTERNALS;
		
		public static final long REVERT = Options.METADATA_ONLY | Options.INCLUDE_CHANGELISTS;
		
		public static final long CLEANUP = Options.BREAK_LOCKS | Options.INCLUDE_TIMESTAMPS | Options.INCLUDE_DAVCACHE | Options.INCLUDE_UNUSED_PRISTINES | Options.INCLUDE_EXTERNALS;
		
		public static final long STREAM_FILE_CONTENT = Options.IGNORE_KEYWORDS | Options.INCLUDE_PROPERTIES;
		
		public static final long VACUUM = Options.INCLUDE_UNVERSIONED | Options.INCLUDE_IGNORED | Options.INCLUDE_TIMESTAMPS | Options.INCLUDE_TIMESTAMPS | Options.INCLUDE_UNUSED_PRISTINES | Options.INCLUDE_EXTERNALS;
	}

	/**
	 * Adds an SVN calls listener.
	 * @param listener
	 */
	public void addCallListener(ISVNCallListener listener);
	
	/**
	 * Removes and SVN calls listener.
	 * @param listener
	 */
	public void removeCallListener(ISVNCallListener listener);

	/**
	 * Returns the configuration directory path.
	 * @return
	 * @throws SVNConnectorException
	 */
	public String getConfigDirectory() throws SVNConnectorException;

	/**
	 * Sets the configuration directory path.
	 * @param configDir
	 * @throws SVNConnectorException
	 */
	public void setConfigDirectory(String configDir) throws SVNConnectorException;

    /**
     * Set an event handler that will be called every time the
     * configuration is loaded by this client object.
     * @since 1.9
     */
	public void setConfigurationEventHandler(ISVNConfigurationEventHandler configHandler) throws SVNConnectorException;

    /**
     * Returns a reference to the installed configuration event
     * handler. The returned value may be <code>null</code>.
     * @since 1.9
     */
	public ISVNConfigurationEventHandler getConfigurationEventHandler() throws SVNConnectorException;

	/**
	 * Sets a username to access a repository.
	 * @param username
	 */
	public void setUsername(String username);

	/**
	 * Sets a password to access a repository.
	 * @param password
	 */
	public void setPassword(String password);

	/**
	 * Tells if credentials cache enabled or not.
	 * @return
	 */
	public boolean isCredentialsCacheEnabled();

	/**
	 * Enables or disables a credentials cache. Works with SVN Kit only.
	 * @param cacheCredentials
	 */
	public void setCredentialsCacheEnabled(boolean cacheCredentials);

	/**
	 * Sets a credentials prompt which will be asked if the provided authentication data weren't accepted by the server.
	 * @param prompt
	 */
	public void setPrompt(ISVNCredentialsPrompt prompt);

	/**
	 * Returns installed credentials prompt
	 * @return
	 */
	public ISVNCredentialsPrompt getPrompt();

	/**
	 * Defines a proxy authentication data. Works with SVN Kit only.
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 */
	public void setProxy(String host, int port, String userName, String password);

	/**
	 * Defines a client SSL certificate authentication data.
	 * @param certPath
	 * @param passphrase
	 */
	public void setClientSSLCertificate(String certPath, String passphrase);

	/**
	 * Tells if caching SSL certificates is enabled or not.
	 * @return
	 */
	public boolean isSSLCertificateCacheEnabled();

	/**
	 * Enables or disables SSL certificates caching.
	 * @param enabled
	 */
	public void setSSLCertificateCacheEnabled(boolean enabled);

	/**
	 * Defines SSH authentication credentials using private key. Works with SVN Kit only.
	 * @param userName
	 * @param privateKeyPath
	 * @param passphrase
	 * @param port
	 */
	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port);

	/**
	 * Defines SSH authentication credentials using username/password. Works with SVN Kit only.
	 * @param userName
	 * @param privateKeyPath
	 * @param passphrase
	 * @param port
	 */
	public void setSSHCredentials(String userName, String password, int port);

	/**
	 * Allows or disallows committing missing files.
	 * @param commitMissingFiles
	 */
	public void setCommitMissingFiles(boolean commitMissingFiles);

	/**
	 * Tells if committing missing files is enabled or not.
	 * @return
	 */
	public boolean isCommitMissingFiles();

	/**
	 * Installs a notification callback.
	 * @param notify
	 */
	public void setNotificationCallback(ISVNNotificationCallback notify);

	/**
	 * Returns the installed notification callback.
	 * @return
	 */
	public ISVNNotificationCallback getNotificationCallback();

	/**
	 * Installs a conflict resolver callback.
	 * @param listener
	 */
	public void setConflictResolver(ISVNConflictResolutionCallback listener);

	/**
	 * Returns the installed conflicts resolver callback.
	 * @return
	 */
	public ISVNConflictResolutionCallback getConflictResolver();

	/**
	 * Performs checkout from SVN into the local working copy.
	 * @param fromReference reference pointing at the exact resource revision
	 * @param destPath target folder
	 * @param depth checkout depth
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @return working copy revision
	 * @throws SVNConnectorException
	 */
	public long checkout(SVNEntryRevisionReference fromReference, String destPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Locks the specified resource.
	 * @param path the resource to be locked
	 * @param comment lock reason or null if none
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void lock(String []path, String comment, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Unlocks the specified resource.
	 * @param path the resource to be unlocked
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void unlock(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Adds resources to the source control.
	 * @param path the resource to be added 
	 * @param depth processing depth
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void add(String path, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Commits all the selected resources.
	 * @param path the resource set to be committed
	 * @param message the commit message or null if none
	 * @param changeLists 
	 * @param depth processing depth
	 * @param options @see CommandMasks
	 * @param revProps revision properties to be set
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void commit(String []path, String message, String []changeLists, SVNDepth depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Updates all the selected resources.
	 * @param path the resource set to be updated
	 * @param revision the revision to update to
	 * @param depth processing depth
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @return resulting working copy revisions
	 * @throws SVNConnectorException
	 */
	public long []update(String []path, SVNRevision revision, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Switches the selected path to another repository URL
	 * @param path the path to be switched
	 * @param toReference switch destination 
	 * @param depth processing depth
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * @return resulting working copy revision
	 * @throws SVNConnectorException
	 */
	public long switchTo(String path, SVNEntryRevisionReference toReference, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reverts the state of the selected path to the SVNRevision.Kind.BASE working copy version.
	 * @param path the paths to be reverted
	 * @param depth processing depth
	 * @param changeLists
	 * @param options @see CommandMasks
	 * @param monitor operation progress monitor
	 * 
	 * Behaves like 1.8 version when options aren't specified
	 * 
	 * @throws SVNConnectorException
	 */
	public void revert(String []paths, SVNDepth depth, String []changeLists, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Tells the list of changes for the provided path including or excluding incoming changes.
	 * 
     * Behaves like the 1.9 version with
     *     <code>LOCAL_SIDE</code> set and
     *     <code>DEPTH_IS_STICKY</code> not set
     *     
	 * @param path the path to check the state of
	 * @param depth processing depth
	 * @param options @see CommandMasks
	 * @param changeLists
	 * @param callback the node status callback
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void status(String path, SVNDepth depth, long options, String []changeLists, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Relocates the attached working copy repository from one URL to another.
	 * @param from URL to relocate from
	 * @param to URL to relocate to
	 * @param path the working copy path to be relocated 
	 * @param depth processing depth
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void relocate(String from, String to, String path, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Provides a way to make working copy consistent after a crash. 
	 * @param path the path to be checked
	 * @param monitor operation progress monitor
	 * @param options @see CommandMasks
	 * 
	 * Behaves like the 1.9 version without <code>INCLUDE_EXTERNALS</code>, and the other flags set
     * 
	 * @throws SVNConnectorException
	 */
	public void cleanup(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Merges difference between 2 sources into the working copy
	 *  
     * Behaves like the 1.9 version with 
     * 		<code>ALLOW_MIXED_REVISIONS</code> set
     * 
	 * @param reference1 first source reference
	 * @param reference2 second source reference
	 * @param localPath local path to merge into
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void mergeTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	/**
	 * Merges the difference between the repository version of the working copy and the specified source into the local working copy.
	 * 
     * Behaves like the 1.9 version with 
     * 		<code>ALLOW_MIXED_REVISIONS</code> set
     * 
	 * @param reference source reference
	 * @param revisions a set of revisions to calculate difference from
	 * @param localPath a local working copy path
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	/**
	 * Perform a reintegration merge of path into localPath.
     * localPath must be a single-revision, infinite depth,
     * pristine, unswitched working copy -- in other words, it must
     * reflect a single revision tree, the "target".  The mergeinfo on
     * path must reflect that all of the target has been merged into it.
     * Then this behaves like a merge from the target's URL to the
     * localPath.
     * 
	 * Deprecated.
	 * 
	 * @param reference source path
	 * @param localPath target local path
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 * @deprecated Will be removed in a future release
	 */
	public void mergeReintegrate(SVNEntryReference reference, String localPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Return merge information for the selected node revision.
	 * @param reference node revision reference
	 * @param monitor operation progress monitor
	 * @return merge information
	 * @throws SVNConnectorException
	 */
	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Retrieve either merged or eligible-to-be-merged revisions.
	 * @param logKind kind of revisions to receive
	 * @param reference target of merge
	 * @param mergeSourceReference the source of the merge
	 * @param mergeSourceRange the source revision range
	 * @param revProps the revprops to retrieve
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param cb callback to receive the messages
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listMergeInfoLog(LogKind logKind, SVNEntryReference reference, SVNEntryReference mergeSourceReference, SVNRevisionRange mergeSourceRange, String []revProps, SVNDepth depth, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Returns an ordered list of suggested merge source URLs.
	 * @param reference the merge target for which to suggest sources.
	 * @param monitor operation progress monitor
	 * @return
	 * @throws SVNConnectorException
	 */
	public String []suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Resolves the conflicted state on a WC path (or tree).
	 * @param path the path to resolve
	 * @param conflictResult which version to choose in the case of conflict
	 * @param depth processing depth
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void resolve(String path, Choice conflictResult, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Add paths to a changelist.
	 * @param paths paths to be added
	 * @param targetChangeList change list name
	 * @param depth processing depth
	 * @param filterByChangeLists changelists to filter by
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void addToChangeList(String []paths, String targetChangeList, SVNDepth depth, String []filterByChangeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Remove paths from a changelist.
	 * @param paths paths to remove
	 * @param depth processing depth
	 * @param changeLists changelists to remove from
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void removeFromChangeLists(String []paths, SVNDepth depth, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Recursively get the paths which belong to a changelist.
	 * @param changeLists the changelists to look under
	 * @param rootPath the wc path under which to check
	 * @param depth processing depth
	 * @param cb callback to receive changelists data
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void dumpChangeLists(String []changeLists, String rootPath, SVNDepth depth, ISVNChangeListCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	//-- the following subset of merge functions could be removed later. So, for now mind them as "for internal usage only".
	/**
	 * Same as mergeReintegrate(), but performs merge based on the simulated changes.
	 */
	public void merge(SVNEntryReference reference, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Same as mergeReintegrate() with a one source reference, but returns simulated change states.
	 */
	public void mergeStatus(SVNEntryReference reference, String mergePath, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Same as merge() with a one source reference, but performs merge based on the simulated changes.
	 */
	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	/**
	 * Same as merge() with a one source reference, but returns simulated change states.
	 */
	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange[] revisions, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	/**
	 * Same as merge() with a two source references, but performs merge based on the simulated changes.
	 */
	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
		throws SVNConnectorException;

	/**
	 * Same as merge() with a two source references, but returns simulated change states.
	 */
	public void mergeStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
		throws SVNConnectorException;
	//-- end of "internal usage only" section

	/**
	 * Import a file or directory into a repository directory
	 * @param path the path to import resource from
	 * @param url the URL to import resource to
	 * @param message the commit message
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param revProps the revision properties to set
	 * @param filter the filter callback to filter out unnecessary resources
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void importTo(String path, String url, String message, SVNDepth depth, long options, Map revProps, ISVNImportFilterCallback filter, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Exports the repository or working copy content into a "clean" directory without any SVN meta information
	 * 
     * Behaves like the 1.9 version with
     * 		<code>IGNORE_KEYWORDS</code> not set
     * 
	 * @param fromReference the source path to export
	 * @param destPath the destination to export to
	 * @param nativeEOL which EOL characters to use during export
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @return the exported revision number
	 * @throws SVNConnectorException
	 */
	public long exportTo(SVNEntryRevisionReference fromReference, String destPath, String nativeEOL, SVNDepth depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	/**
	 * Reports the difference between two sources in form of a patch file
	 * @param refPrev the first source reference
	 * @param refNext the second source reference
	 * @param relativeToDir report paths relative to this directory
	 * @param fileName the file name to write patch to
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param outputOptions see {@link DiffOptions}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diffTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, String relativeToDir, String fileName, SVNDepth depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reports the difference between two revisions of the specified source in form of a patch file
	 * @param reference the source reference
	 * @param range the revision range
	 * @param relativeToDir report paths relative to this directory
	 * @param fileName the file name to write a patch file to 
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param outputOptions see {@link DiffOptions}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diff(SVNEntryReference reference, SVNRevisionRange range, String relativeToDir, String fileName, SVNDepth depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reports the difference between two sources in form of a patch file
	 * @param refPrev the first source reference
	 * @param refNext the second source reference
	 * @param relativeToDir report paths relative to this directory
	 * @param stream the stream to write patch to
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param outputOptions see {@link DiffOptions}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diffTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, String relativeToDir, OutputStream stream, SVNDepth depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reports the difference between two revisions of the specified source in form of a patch file
	 * @param reference the source reference
	 * @param range the revision range
	 * @param relativeToDir report paths relative to this directory
	 * @param stream the stream to write patch to 
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param outputOptions see {@link DiffOptions}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diff(SVNEntryReference reference, SVNRevisionRange range, String relativeToDir, OutputStream stream, SVNDepth depth, long options, String []changeLists,
			long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reports the difference between two sources through a callback
	 * @param refPrev the first source reference
	 * @param refNext the second source reference
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param cb the diff status callback
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diffStatusTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, SVNDepth depth, long options, String []changeLists, 
			ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Reports the difference between two revisions of the specified source through a callback
	 * @param reference the source reference
	 * @param range the revision range
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param cb the diff status callback
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void diffStatus(SVNEntryReference reference, SVNRevisionRange range, SVNDepth depth, long options, String []changeLists, ISVNDiffStatusCallback cb, 
			ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Returns complete information regarding the specified node
	 * 
     * Behaves like the 1.9 version, with 
     * 		<code>FETCH_EXCLUDED</code> not set, 
     * 		<code>FETCH_ACTUAL_ONLY</code> set and 
     * 		<code>INCLUDE_EXTERNALS</code> not set.
     * 
	 * @param reference node reference
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists if non-null, filter paths using changelists
	 * @param cb callback to receive the retrieved information
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void getInfo(SVNEntryRevisionReference reference, SVNDepth depth, long options, String []changeLists, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Writes the specified file content into the specified output stream
	 * @param reference file reference
	 * @param options @see CommandMasks
	 * @param stream the target output stream
	 * @param monitor operation progress monitor
	 * @return
	 * 
	 * Behaves like 1.8 version without IGNORE_KEYWORDS and INCLUDE_PROPERTIES
	 * 
	 * @throws SVNConnectorException
	 */
	public SVNProperty []streamFileContent(SVNEntryRevisionReference reference, long options, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Creates the specified folder directly on the repository 
	 * @param path the paths to create
	 * @param message the commit message
	 * @param options see {@link CommandMasks}
	 * @param revProps the revision properties to set or null
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void mkdir(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Moves resources inside the working copies.
	 * @param srcPaths paths to move
	 * @param dstPath destination path
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void moveLocal(String []srcPaths, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Moves resources directly in the repository.
	 * @param srcPaths paths to move
	 * @param dstPath destination path
	 * @param message the commit message
	 * @param options see {@link CommandMasks}
	 * @param revProps the revision properties to set or null
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void moveRemote(String []srcPaths, String dstPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Copies resources inside the working copies.
	 * @param srcPaths paths to move
	 * @param dstPath destination path
	 * @param options see {@link CommandMasks}
     * @param externalsToPin The set of externals to pin.
     *            Keys are either local absolute paths (when the source of the
     *            copy is the working copy) or URLs within the repository
     *            (when the source is the repository) where an
     *            <code>svn:externals</code> property is defined.
     *            Values are lists of parsed {@link SVNExternalReference}
     *            objects from each external definitions.
     *            If <code>externalsToPin</code> is set to a value different than <code>ISVNConnector.NO_EXTERNALS_TO_PIN</code>, 
     *            then the specified externals set will be pinned. If the passed list is empty (and it is not the 
     *            <code>ISVNConnector.NO_EXTERNALS_TO_PIN</code> constant), then all the existing externals will be pinned.
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void copyLocal(SVNEntryRevisionReference []srcPaths, String destPath, long options, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Copies resources directly in the repository.
	 * @param srcPaths paths to move
	 * @param dstPath destination path
	 * @param message the commit message
	 * @param options see {@link CommandMasks}
	 * @param revProps the revision properties to set or null
     * @param externalsToPin The set of externals to pin.
     *            Keys are either local absolute paths (when the source of the
     *            copy is the working copy) or URLs within the repository
     *            (when the source is the repository) where an
     *            <code>svn:externals</code> property is defined.
     *            Values are lists of parsed {@link SVNExternalReference}
     *            objects from each external definitions.
     *            If <code>externalsToPin</code> is set to a value different than <code>ISVNConnector.NO_EXTERNALS_TO_PIN</code>, 
     *            then the specified externals set will be pinned. If the passed list is empty (and it is not the 
     *            <code>ISVNConnector.NO_EXTERNALS_TO_PIN</code> constant), then all the existing externals will be pinned.
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void copyRemote(SVNEntryRevisionReference []srcPaths, String destPath, String message, long options, Map revProps, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Removes resources from the working copy.
	 * @param path paths to remove
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void removeLocal(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Removes resources from the repository.
	 * @param path paths to remove
	 * @param message the commit message
	 * @param options see {@link CommandMasks}
	 * @param revProps the revision properties to set or null
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void removeRemote(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Returns the change history log.
	 * @param reference the target reference
	 * @param revisionRanges the set of revision ranges 
	 * @param revProps the list of revision properties to report
	 * @param limit the maximum number of records to report
	 * @param options see {@link CommandMasks}
	 * @param cb callback to receive the messages
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listHistoryLog(SVNEntryReference reference, SVNRevisionRange []revisionRanges, String []revProps, long limit, long options, ISVNLogEntryCallback cb,
			ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Annotates changes in a non-binary file on the per-line basis.
	 * @param reference the resource reference
	 * @param revisionRange start and end revisions to pick up the changes
	 * @param options see {@link CommandMasks}
	 * @param diffOptions see {@link DiffOptions}
	 * @param callback the callback to receive annotated lines
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 * 
	 * Works as 1.8 version with the diff options set to their default value.
	 */
	public void annotate(SVNEntryReference reference, SVNRevisionRange revisionRange, long options, long diffOptions, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	/**
	 * Returns the repository nodes list.
	 * @param reference the path to enumerate nodes from
	 * @param depth processing depth
	 * @param direntFields the fields to retrieve. see {@link SVNEntry.Fields}
	 * @param options see {@link CommandMasks}
	 * @param cb callback to receive the entries
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listEntries(SVNEntryRevisionReference reference, SVNDepth depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	/**
	 * Reports all the properties for the target resource and its children. 
	 * @param reference the target resource
	 * @param depth processing depth
	 * @param changeLists the changelists to filter paths by or null
	 * @param options see {@link CommandMasks}
	 * @param callback the callback to report properties through
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listProperties(SVNEntryRevisionReference reference, SVNDepth depth, String []changeLists, long options, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Returns the single specified property or null if not found.
	 * @param reference the target resource
	 * @param name the property name
	 * @param changeLists changelists to filter path by or null
	 * @param monitor operation progress monitor
	 * @return
	 * @throws SVNConnectorException
	 */
	public SVNProperty getProperty(SVNEntryRevisionReference reference, String name, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Sets the property to a set of resources in the working copy.
	 * @param path the paths to set the property
	 * @param property the property to set
	 * @param depth processing depth
	 * @param options see {@link CommandMasks}
	 * @param changeLists changelists to filter paths by or null
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void setPropertyLocal(String []path, SVNProperty property, SVNDepth depth, long options, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Sets the property to a repository resource.
	 * @param reference the target resource reference
	 * @param property the property to set
	 * @param message the commit message 
	 * @param options see {@link CommandMasks}
	 * @param revProps th revision properties to set or null
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void setPropertyRemote(SVNEntryReference reference, SVNProperty property, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Lists revision properties for the target resource. 
	 * @param reference the target resource reference
	 * @param monitor operation progress monitor
	 * @return the list of revision properties
	 * @throws SVNConnectorException
	 */
	public SVNProperty []listRevisionProperties(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Returns a single specified revision property.
	 * @param reference the target resource reference
	 * @param name the revision property name
	 * @param monitor operation progress monitor
	 * @return
	 * @throws SVNConnectorException
	 */
	public SVNProperty getRevisionProperty(SVNEntryReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Changes the specified revision property.
	 * @param reference the target resource reference
	 * @param property the property to change
	 * @param originalValue the original value of the property.
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, String originalValue, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Upgrades the working copy specified by the path.
	 * @param path
	 * @param monitor
	 * @throws SVNConnectorException
	 */
	public void upgrade(String path, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Applies the patch to the specified path.
	 * @param patchPath path to patch file
	 * @param targetPath path to the resource to apply patch to
	 * @param stripCount how many leading path components should be removed
	 * @param options see {@link CommandMasks}
	 * @param callback allows to filter the patch
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void patch(String patchPath, String targetPath, int stripCount, long options, ISVNPatchCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
    /**
     * Recursively vacuum a working copy, removing unnecessary data, including unversioned, externals, unused pristines, 
     * ignored and fixing time stamps.

     * This method will report an error when remove unversioned items or remove ignored items are set, and the working copy
     * is already locked. This prevents accidental corruption of the working copy if this method is invoked while another client is
     * performing some other operation on the working copy.
     * 
     * @param path The path of the working copy directory.
     * @param options {@see CommandMasks}
     * 
     * @since 1.9
     */
	public void vacuum(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	// we do not provide the remote session support interface since it is currently marked as unstable
    
	/**
	 * Disposes of all the native resources allocated by the connector instance. 
	 */
	public void dispose();
}
