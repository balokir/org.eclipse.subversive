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

	/** 
	 * constant identifying the "bdb"  repository type 
	 */
	public final static String REPOSITORY_FSTYPE_BDB = "bdb";
	/** 
	 * constant identifying the "fsfs"  repository type 
	 */
	public final static String REPOSITORY_FSTYPE_FSFS = "fsfs";
    
	public void create(String repositoryPath, String repositoryType, String configPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void deltify(String path, SVNRevisionRange range, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void hotCopy(String path, String targetPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void dump(String path, OutputStream dataOut, SVNRevisionRange range, ISVNRepositoryNotificationCallback callback, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void listDBLogs(String path, ISVNRepositoryMessageCallback receiver, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void load(String path, InputStream dataInput, SVNRevisionRange range, String relativePath, ISVNRepositoryNotificationCallback callback, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void listTransactions(String path, ISVNRepositoryMessageCallback receiver, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public long recover(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void freeze(ISVNRepositoryFreezeAction action, String []paths, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void removeTransaction(String path, String []transactions, ISVNProgressMonitor monitor) throws SVNConnectorException;
			
	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, long options, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void verify(String path, SVNRevisionRange range, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public SVNLock []listLocks(String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void removeLocks(String path, String []locks, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public void upgrade(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public abstract void pack(String path, ISVNRepositoryNotificationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void dispose();
}
