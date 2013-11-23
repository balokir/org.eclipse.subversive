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
import java.util.Map;

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
		public static final long DISALLOW_MIXED_REVISIONS = 0x10000000;

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
	}
	
	public static class DiffOptions {
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

		public static final long STATUS = Options.SERVER_SIDE | Options.INCLUDE_UNCHANGED | Options.INCLUDE_IGNORED | Options.IGNORE_EXTERNALS;

		public static final long MERGE = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SIMULATE | Options.RECORD_ONLY | Options.IGNORE_MERGE_HISTORY;

		public static final long MERGE_REINTEGRATE = Options.FORCE /*OVR&UPD*/ | Options.SIMULATE;

		public static final long MERGE_STATUS = Options.FORCE | Options.IGNORE_ANCESTRY;

		public static final long MERGE_STATUS_REINTEGRATE = Options.NONE;

		public static final long IMPORT = Options.INCLUDE_IGNORED | Options.IGNORE_UNKNOWN_NODE_TYPES | Options.IGNORE_AUTOPROPS;

		public static final long EXPORT = Options.FORCE | Options.IGNORE_EXTERNALS;

		public static final long DIFF = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SKIP_DELETED | Options.IGNORE_PROPERTY_CHANGES | Options.IGNORE_CONTENT_CHANGES;

		public static final long DIFF_STATUS = Options.IGNORE_ANCESTRY;

		public static final long MKDIR = Options.INCLUDE_PARENTS;

		public static final long MOVE_LOCAL = Options.FORCE | Options.DISALLOW_MIXED_REVISIONS | Options.METADATA_ONLY;

		public static final long MOVE_SERVER = Options.FORCE | Options.INTERPRET_AS_CHILD | Options.INCLUDE_PARENTS;

		public static final long COPY_LOCAL = Options.IGNORE_EXTERNALS;

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
	}

	public void addCallListener(ISVNCallListener listener);
	
	public void removeCallListener(ISVNCallListener listener);
	
	public String getConfigDirectory() throws SVNConnectorException;

	public void setConfigDirectory(String configDir) throws SVNConnectorException;

	public void setUsername(String username);

	public void setPassword(String password);

	public boolean isCredentialsCacheEnabled();

	public void setCredentialsCacheEnabled(boolean cacheCredentials);

	public void setPrompt(ISVNCredentialsPrompt prompt);

	public ISVNCredentialsPrompt getPrompt();

	public void setProxy(String host, int port, String userName, String password);

	public void setClientSSLCertificate(String certPath, String passphrase);

	public boolean isSSLCertificateCacheEnabled();

	public void setSSLCertificateCacheEnabled(boolean enabled);

	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port);

	public void setSSHCredentials(String userName, String password, int port);

	public void setCommitMissingFiles(boolean commitMissingFiles);

	public boolean isCommitMissingFiles();

	public void setNotificationCallback(ISVNNotificationCallback notify);

	public ISVNNotificationCallback getNotificationCallback();

	public void setConflictResolver(ISVNConflictResolutionCallback listener);

	public ISVNConflictResolutionCallback getConflictResolver();

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void lock(String []path, String comment, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void unlock(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void add(String path, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void commit(String []path, String message, String []changeLists, int depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long []update(String []path, SVNRevision revision, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long switchTo(String path, SVNEntryRevisionReference toReference, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void revert(String path, int depth, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void status(String path, int depth, long options, String []changeLists, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void cleanup(String path, ISVNProgressMonitor monitor) throws SVNConnectorException;

	//Behaves like the 1.8 where ignoreAncestry maps to both ignoreMergeinfo and diffIgnoreAncestry
	public void mergeTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	//Behaves like the 1.8 where ignoreAncestry maps to both ignoreMergeinfo and diffIgnoreAncestry
	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String localPath, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	/**
	 * Will be deprecated in future SVN releases
	 */
	public void mergeReintegrate(SVNEntryReference reference, String localPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void listMergeInfoLog(int logKind, SVNEntryReference reference, SVNEntryReference mergeSourceReference, SVNRevisionRange mergeSourceRange, String []revProps, int depth, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public String []suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void resolve(String path, int conflictResult, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void addToChangeList(String []paths, String targetChangeList, int depth, String []filterByChangeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void removeFromChangeLists(String []paths, int depth, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void dumpChangeLists(String []changeLists, String rootPath, int depth, ISVNChangeListCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	//--
	public void merge(SVNEntryReference reference, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void mergeStatus(SVNEntryReference reference, String mergePath, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange[] revisions, String path, int depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
		throws SVNConnectorException;
	
	public void mergeStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String path, int depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
		throws SVNConnectorException;
	//--

	public void importTo(String path, String url, String message, int depth, long options, Map revProps, ISVNImportFilterCallback filter, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long exportTo(SVNEntryRevisionReference fromReference, String destPath, String nativeEOL, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void diffTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, String relativeToDir, String fileName, int depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diff(SVNEntryReference reference, SVNRevisionRange range, String relativeToDir, String fileName, int depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diffTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, String relativeToDir, OutputStream stream, int depth, long options,
			String []changeLists, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diff(SVNEntryReference reference, SVNRevisionRange range, String relativeToDir, OutputStream stream, int depth, long options, String []changeLists,
			long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diffStatusTwo(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, int depth, long options, String []changeLists, 
			ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diffStatus(SVNEntryReference reference, SVNRevisionRange range, int depth, long options, String []changeLists, ISVNDiffStatusCallback cb, 
			ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void getInfo(SVNEntryRevisionReference reference, int depth, String []changeLists, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void streamFileContent(SVNEntryRevisionReference reference, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void mkdir(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void moveLocal(String []srcPaths, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void moveRemote(String []srcPaths, String dstPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void copyLocal(SVNEntryRevisionReference []srcPaths, String destPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void copyRemote(SVNEntryRevisionReference []srcPaths, String destPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void removeLocal(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void removeRemote(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void listHistoryLog(SVNEntryReference reference, SVNRevisionRange []revisionRanges, String []revProps, long limit, long options, ISVNLogEntryCallback cb,
			ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void annotate(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, long options, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void listEntries(SVNEntryRevisionReference reference, int depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void listProperties(SVNEntryRevisionReference reference, int depth, String []changeLists, long options, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty getProperty(SVNEntryRevisionReference reference, String name, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setPropertyLocal(String []path, SVNProperty property, int depth, long options, String []changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setPropertyRemote(SVNEntryReference reference, SVNProperty property, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty []listRevisionProperties(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty getRevisionProperty(SVNEntryReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, String originalValue, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void upgrade(String path, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void patch(String patchPath, String targetPath, int stripCount, long options, ISVNPatchCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void dispose();
}
