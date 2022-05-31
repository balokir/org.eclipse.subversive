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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * SVN repository manager interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNManager {
	/**
	 * All available SVN commands options
	 */
	public static class Options {
		public static final long NONE = 0x00;
		public static final long CLEAN_LOGS = 0x01;
		public static final long INCREMENTAL = 0x02;
		public static final long USE_DELTAS = 0x04;
		public static final long UNUSED_ONLY = 0x08;
		public static final long IGNORE_UUID = 0x10;
		public static final long FORCE_UUID = 0x20;
		public static final long USE_PRECOMMIT_HOOK = 0x40;
		public static final long USE_POSTCOMMIT_HOOK = 0x80;
		public static final long USE_PREREVPROPCHANGE_HOOK = 0x100;
		public static final long USE_POSTREVPROPCHANGE_HOOK = 0x200;
		/**
		 * Disable to fsync at the commit (BDB).
		 */
		public static final long DISABLE_FSYNC_COMMIT = 0x400;
		/**
		 * Keep the log files (BDB).
		 */
		public static final long KEEP_LOG = 0x800;
	}
	
	/**
	 * Command-related option masks
	 */
	public static class CommandMasks {
		public static final long CREATE = Options.DISABLE_FSYNC_COMMIT | Options.KEEP_LOG;
		public static final long DELTIFY = Options.CLEAN_LOGS | Options.INCREMENTAL;
		public static final long DUMP = Options.INCREMENTAL | Options.USE_DELTAS;
		public static final long LIST_DB_LOGS = Options.UNUSED_ONLY;
		public static final long LOAD = Options.IGNORE_UUID | Options.FORCE_UUID | Options.USE_PRECOMMIT_HOOK | Options.USE_POSTCOMMIT_HOOK;
		public static final long SET_REV_PROP = Options.USE_PREREVPROPCHANGE_HOOK | Options.USE_POSTREVPROPCHANGE_HOOK;
	}

	public enum RepositoryKind {
		/** 
		 * constant identifying the "bdb"  repository type 
		 */
		BDB("bdb"),
		/** 
		 * constant identifying the "fsfs"  repository type 
		 */
		FSFS("fsfs");
		
		public final String id;
		
		private RepositoryKind(String id) {
			this.id = id;
		}
	}
    
	/**
	 * Creates a SVN repository.
	 * @param repositoryPath the path were repository will be created
	 * @param repositoryType the repository type
	 * @param configPath optional path for user configuration files.
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void create(String repositoryPath, RepositoryKind repositoryType, String configPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Deltify the revisions in the repository.
	 * @param path the repository path
	 * @param range revisions to deltify
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void deltify(String path, SVNRevisionRange range, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Makes a hot copy of the repository.
	 * @param path the path to the source repository
	 * @param targetPath the path to the target repository
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void hotCopy(String path, String targetPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

    /**
     * make a hot copy of the repository
     * @param path              the path to the source repository
     * @param targetPath        the path to the target repository
     * @param cleanLogs         clean the unused log files in the source
     *                          repository
         * @param callback          the callback to receive notifications
     * @throws ClientException  throw in case of problem
         * @since 1.9
     */
//    public abstract void hotcopy(File path, File targetPath,
//                        boolean cleanLogs, boolean incremental,
//                        ReposNotifyCallback callback)
//                        throws ClientException;
	
	/**
	 * Dumps the data in a repository.
	 * @param path the repository to dump
	 * @param dataOut the stream to dump the data
	 * @param range the revisions to dump
	 * @param callback the callback to recieve notifications
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void dump(String path, OutputStream dataOut, SVNRevisionRange range, ISVNRepositoryNotificationCallback callback, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * List all logfiles (BDB) in use or not). 
	 * @param path the path to the repository
	 * @param receiver callback to receive the logfile names
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listDBLogs(String path, ISVNRepositoryMessageCallback receiver, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	/**
	 * Load the data of a dump into a repository.
	 * @param path the target repository path
	 * @param dataInput the data input stream
	 * @param range the revision range to load
	 * @param relativePath the directory in the repository, to put the data into. Optional.
	 * @param callback callback to receive all the notifications
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void load(String path, InputStream dataInput, SVNRevisionRange range, String relativePath, ISVNRepositoryNotificationCallback callback, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

    /**
     * load the data of a dump into a repository
     * @param path              the path to the repository
     * @param dataInput         the data input source
         * @param start             the first revision to load
         * @param end               the last revision to load
     * @param ignoreUUID        ignore any UUID found in the input stream
     * @param forceUUID         set the repository UUID to any found in the
     *                          stream
     * @param usePreCommitHook  use the pre-commit hook when processing commits
     * @param usePostCommitHook use the post-commit hook when processing commits
         * @param validateProps     validate "svn:" revision and node properties
         * @param ignoreDates       ignore revision datestamps in the dump stream
     * @param relativePath      the directory in the repository, where the data
     *                          in put optional.
     * @param callback          the target for processing messages
     * @throws ClientException  throw in case of problem
         * @since 1.9
         * for 1.8 
         * <code>validateProps</code> and <code>ignoreDates</code> to
         * <code>false</code>.
         * 
     */
//    public abstract void load(File path, InputStream dataInput,
//                                  Revision start, Revision end,
//                                  boolean ignoreUUID, boolean forceUUID,
//                                  boolean usePreCommitHook,
//                                  boolean usePostCommitHook,
//                                  boolean validateProps,
//                                  boolean ignoreDates,
//                                  String relativePath,
//                                  ReposNotifyCallback callback)
//        throws ClientException;
	
	/**
	 * List all open transactions in a repository.
	 * @param path the path to the repository
	 * @param receiver receives one transaction name per call
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void listTransactions(String path, ISVNRepositoryMessageCallback receiver, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Recover the filesystem backend of a repository.
	 * @param path the path to the repository
	 * @param callback callback to receive all the notifications
	 * @param monitor operation progress monitor
	 * @return youngest revision
	 * @throws SVNConnectorException
	 */
	public long recover(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Take an exclusive lock on each of the listed repositories
	 * to prevent commits; then, while holding all the locks, call
	 * the action.invoke().
	 *
	 * The repositories may or may not be readable by Subversion
	 * while frozen, depending on implementation details of the
	 * repository's filesystem backend.
	 * 
	 * Repositories are locked in the listed order.
	 * 
	 * @param action describes the action to perform
	 * @param paths the set of repository paths
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void freeze(ISVNRepositoryFreezeAction action, String []paths, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Remove open transaction in a repository.
	 * @param path the path to the repository
	 * @param transactions the transactions to be removed
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void removeTransaction(String path, String []transactions, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Sets the value of revision property. By default, does not run pre-/post-revprop-change hook scripts.
	 * @param reference The path to the repository.
	 * @param property the property data
	 * @param options see {@link CommandMasks}
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Verify the repository at <code>path</code> between revisions <code>start</code> and <code>end</code>.
	 * @param path the repository to verify
	 * @param range the revision range
	 * @param callback callback to receive all the notifications
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void verify(String path, SVNRevisionRange range, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
    /**
     * Verify the repository at <code>path</code> between revisions
     * <code>start</code> and <code>end</code>.
     *<p>
     * If <code>verifyCallback</code> is <code>null</code>, verification
     * will stop at the first encountered error. Otherwise, the verification
     * process may continue, depending on the value returned from the
     * invocation of <code>verifyCallback</code>.
     *
     * @param path              the path to the repository
     * @param start             the first revision
     * @param end               the last revision
     * @param checkNormalization report directory entry and mergeinfo name collisions
     *                           caused by denormalized Unicode representations
     * @param metadataOnly      check only metadata, not file contents
     * @param notifyCallback    the callback to receive notifications
     * @param verifyCallback    the callback to receive verification status
     * @throws ClientException If an error occurred.
     * @since 1.9
     * 
     * for 1.8
     *<b>Note:</b> Behaves like the 1.9 version with
     *             <code>checkNormailzation</code> and
     *             <code>metadataOnly</code> set to <code>false</code>
     *             and <code>verifyCallback</code> set to
     *             <code>null</code>.
     * 
     */
//    public abstract void verify(File path, Revision start, Revision end,
//                boolean checkNormalization,
//                boolean metadataOnly,
//                ReposNotifyCallback notifyCallback,
//                ReposVerifyCallback verifyCallback)
//            throws ClientException;
	
	/**
	 * List all locks in the repository.
	 * @param path the repository path
	 * @param depth processing depth
	 * @param monitor operation progress monitor
	 * @return 
	 * @throws SVNConnectorException
	 */
	public SVNLock []listLocks(String path, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Remove multiple locks from the repository.
	 * @param path the path to the repository
	 * @param locks the name of the locked items
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
	public void removeLocks(String path, String []locks, ISVNProgressMonitor monitor) throws SVNConnectorException;
	/**
	 * Upgrade the repository format.
	 * @param path the path to the repository
	 * @param callback callback to receive all the notifications
	 * @param monitor operation progress monitor
	 * @throws SVNConnectorException
	 */
    public void upgrade(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
    /**
     * Pack the repository.
     * @param path the path to the repository 
	 * @param callback callback to receive all the notifications
	 * @param monitor operation progress monitor
     * @throws SVNConnectorException
     */
    public abstract void pack(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	/**
	 * Disposes of all the native resources allocated by the connector instance. 
	 */
	public void dispose();
}
