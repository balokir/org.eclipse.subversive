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

import java.io.OutputStream;

/**
 * SVN client wrapper interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNConnector {
	/**
	 * Repository or working copy traversal depths enumeration
	 */
	public final class Depth {
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
	
	public static final String []EMPTY_LOG_ENTRY_PROPS = new String[] {};
	public static final String []DEFAULT_LOG_ENTRY_PROPS = new String[] {SVNProperty.BuiltIn.REV_LOG, SVNProperty.BuiltIn.REV_DATE, SVNProperty.BuiltIn.REV_AUTHOR};
	
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

    
    public void setReportRevisionChange(boolean report); 
    public boolean isReportRevisionChange();
    
    public void setCommitMissingFiles(boolean commitMissingFiles);
    public boolean isCommitMissingFiles();
    
    public void setTouchUnresolved(boolean touchUnresolved);
    public boolean isTouchUnresolved();

    public void setNotificationCallback(ISVNNotificationCallback notify);
    public ISVNNotificationCallback getNotificationCallback();
    

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public void lock(String []path, String comment, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public void unlock(String []path, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void add(String path, int depth, boolean skipIgnores, boolean addParents, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public long []commit(String []path, String message, int depth, boolean noUnlock, boolean keepChangelist, String changelistName, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public long []update(String []path, SVNRevision revision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public long doSwitch(String path, SVNEntryRevisionReference toReference, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void revert(String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void status(String path, int depth, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void cleanup(String path, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public SVNRevisionRange []getAvailableMerges(SVNEntryReference reference, String mergeSource, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public String []suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void resolved(String path, int depth, int conflictResult, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void setConflictResolver(ISVNConflictResolutionCallback listener);
    
    public void addToChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public void removeFromChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNConnectorException;
    public String[] getChangelist(String changelist, String rootPath, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String mergePath, SVNEntryStatus[] mergeStatus, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange []revisions, String path, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	
	public void doImport(String path, String url, String message, int depth, boolean noIgnore, boolean ignoreUnknownNodeTypes, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public long doExport(SVNEntryRevisionReference fromReference, String destPath, boolean force, boolean ignoreExternals, int depth, String nativeEOL, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void diff(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void diff(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void diffStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void diffStatus(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	public void info(SVNEntryRevisionReference reference, int depth, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void streamFileContent(SVNEntryRevisionReference reference, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void mkdir(String []path, String message, boolean makeParents, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void move(String []srcPath, String dstPath, String message, boolean force, boolean moveAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void copy(String srcPath, String destPath, SVNRevision revision, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void copy(SVNEntryRevisionReference []srcPath, String destPath, String message, boolean copyAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void remove(String []path, String message, boolean force, boolean keepLocal, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void logEntries(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, boolean stopOnCopy, boolean discoverPath, boolean includeMergedRevisions, String[] revProps, long limit, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void annotate(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, boolean ignoreMimeType, boolean includeMergedRevisions, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	public void list(SVNEntryRevisionReference reference, int depth, int direntFields, boolean fetchLocks, ISVNEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException;

	
	public void properties(SVNEntryRevisionReference reference, int depth, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public SVNProperty propertyGet(SVNEntryRevisionReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void propertySet(String path, String name, byte []value, int depth, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void propertySet(String path, String name, String value, int depth, boolean force, ISVNProgressMonitor monitor) throws SVNConnectorException;
	public void propertyRemove(String path, String name, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException;
	
	
	public void dispose();
}
