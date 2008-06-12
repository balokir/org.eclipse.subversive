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
	/**
	 * Repository or working copy traversal depths enumeration
	 */
	public class Depth {
		/**
		 * Depth undetermined or ignored.
		 */
		public static final int UNKNOWN = -2;

		/**
		 * Exclude (i.e, don't descend into) directory D.
		 */
		public static final int EXCLUDE = -1;

		/**
		 * Just the named file or folder without entries.
		 */
		public static final int EMPTY = 0;

		/**
		 * The folder and child files.
		 */
		public static final int FILES = 1;

		/**
		 * The folder and all direct child entries.
		 */
		public static final int IMMEDIATES = 2;

		/**
		 * The folder and all descendants at any depth.
		 */
		public static final int INFINITY = 3;

		public static final int infinityOrEmpty(boolean recurse) {
			return (recurse ? Depth.INFINITY : Depth.EMPTY);
		}

		public static final int infinityOrFiles(boolean recurse) {
			return (recurse ? Depth.INFINITY : Depth.FILES);
		}

		public static final int infinityOrImmediates(boolean recurse) {
			return (recurse ? Depth.INFINITY : Depth.IMMEDIATES);
		}

	}
	
	public static final String[] EMPTY_LOG_ENTRY_PROPS = new String[] {};

	public static final String[] DEFAULT_LOG_ENTRY_PROPS = new String[] { SVNProperty.BuiltIn.REV_LOG, SVNProperty.BuiltIn.REV_DATE, SVNProperty.BuiltIn.REV_AUTHOR };

	/**
	 * All available SVN commands options
	 */
	public class Options {
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
		 * Ignore resource ancestry.
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
	}

	/**
	 * Command-related option masks
	 */
	public class CommandMasks {
		public static final long CHECKOUT = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS;

		public static final long LOCK = Options.FORCE;

		public static final long UNLOCK = Options.FORCE;

		public static final long ADD = Options.FORCE | Options.INCLUDE_IGNORED | Options.INCLUDE_PARENTS;

		public static final long COMMIT = Options.KEEP_LOCKS | Options.KEEP_CHANGE_LIST;

		public static final long UPDATE = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS | Options.DEPTH_IS_STICKY;

		public static final long SWITCH = Options.IGNORE_EXTERNALS | Options.ALLOW_UNVERSIONED_OBSTRUCTIONS | Options.DEPTH_IS_STICKY;

		public static final long STATUS = Options.SERVER_SIDE | Options.INCLUDE_UNCHANGED | Options.INCLUDE_IGNORED | Options.IGNORE_EXTERNALS;

		public static final long MERGE = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SIMULATE | Options.RECORD_ONLY;

		public static final long MERGE_REINTEGRATE = Options.SIMULATE;

		public static final long MERGE_STATUS = Options.FORCE | Options.IGNORE_ANCESTRY;

		public static final long IMPORT = Options.INCLUDE_IGNORED | Options.IGNORE_UNKNOWN_NODE_TYPES;

		public static final long EXPORT = Options.FORCE | Options.IGNORE_EXTERNALS;

		public static final long DIFF = Options.FORCE | Options.IGNORE_ANCESTRY | Options.SKIP_DELETED;

		public static final long DIFF_STATUS = Options.IGNORE_ANCESTRY;

		public static final long MKDIR = Options.INCLUDE_PARENTS;

		public static final long MOVE_LOCAL = Options.FORCE;

		public static final long MOVE_SERVER = Options.FORCE | Options.INTERPRET_AS_CHILD | Options.INCLUDE_PARENTS;

		public static final long COPY_SERVER = Options.INTERPRET_AS_CHILD | Options.INCLUDE_PARENTS;

		public static final long REMOVE = Options.FORCE | Options.KEEP_LOCAL;

		public static final long LOG = Options.STOP_ON_COPY | Options.DISCOVER_PATHS | Options.INCLUDE_MERGED_REVISIONS;

		public static final long ANNOTATE = Options.IGNORE_MIME_TYPE | Options.INCLUDE_MERGED_REVISIONS;

		public static final long LIST = Options.FETCH_LOCKS;

		public static final long PROPERTY_SET = Options.FORCE;

		public static final long REVISION_PROPERTY_SET = Options.FORCE;
	}

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

	public void setTouchUnresolved(boolean touchUnresolved);

	public boolean isTouchUnresolved();

	public void setNotificationCallback(ISVNNotificationCallback notify);

	public ISVNNotificationCallback getNotificationCallback();

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void lock(String[] path, String comment, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void unlock(String[] path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void add(String path, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long[] commit(String[] path, String message, String[] changelistNames, int depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long[] update(String[] path, SVNRevision revision, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long doSwitch(String path, SVNEntryRevisionReference toReference, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void revert(String path, int depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void status(String path, int depth, long options, String[] changelistNames, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void cleanup(String path, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String localPath, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;
	
	public void mergeReintegrate(SVNEntryReference reference, String localPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void getMergeInfoLog(int logKind, SVNEntryReference reference, SVNEntryReference mergeSourceReference, String[] revProps, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public String[] suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void resolve(String path, int conflictResult, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setConflictResolver(ISVNConflictResolutionCallback listener);

	public void addToChangeList(String[] paths, String changelist, int depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void removeFromChangeLists(String[] paths, int depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void dumpChangeLists(String[] changeLists, String rootPath, int depth, ISVNChangeListCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void merge(SVNEntryReference reference, SVNRevisionRange[] revisions, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange[] revisions, String path, int depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor)
		throws SVNConnectorException;
	
	public void mergeStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String path, int depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor)
		throws SVNConnectorException;

	public void doImport(String path, String url, String message, int depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public long doExport(SVNEntryRevisionReference fromReference, String destPath, String nativeEOL, int depth, long options, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void diff(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, String relativeToDir, String outFileName, int depth, long options,
			String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diff(SVNEntryReference reference, SVNRevision revPrev, SVNRevision revNext, String relativeToDir, String outFileName, int depth, long options,
			String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diffStatus(SVNEntryRevisionReference refPrev, SVNEntryRevisionReference refNext, int depth, long options, String[] changelistNames, 
			ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void diffStatus(SVNEntryReference reference, SVNRevision revPrev, SVNRevision revNext, int depth, long options, String[] changelistNames, 
			ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void info(SVNEntryRevisionReference reference, int depth, String[] changelistNames, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void streamFileContent(SVNEntryRevisionReference reference, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void mkdir(String[] path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void move(String[] srcPaths, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void move(SVNEntryReference[] srcPaths, String dstPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void copy(String[] srcPaths, String destPath, SVNRevision revision, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void copy(SVNEntryRevisionReference[] srcPaths, String destPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void remove(String[] path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void logEntries(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, String[] revProps, long limit, long options, ISVNLogEntryCallback cb,
			ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void annotate(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, long options, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void list(SVNEntryRevisionReference reference, int depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor)
			throws SVNConnectorException;

	public void getProperties(SVNEntryRevisionReference reference, int depth, String[] changelistNames, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty getProperty(SVNEntryRevisionReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setProperty(String path, String name, String value, int depth, long options, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void removeProperty(String path, String name, int depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty []getRevisionProperties(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public SVNProperty getRevisionProperty(SVNEntryReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void setRevisionProperty(SVNEntryReference reference, String name, String value, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void removeRevisionProperty(SVNEntryReference reference, String name, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void dispose();
}
