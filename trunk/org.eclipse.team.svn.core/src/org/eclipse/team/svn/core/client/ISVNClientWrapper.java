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

package org.eclipse.team.svn.core.client;

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
public interface ISVNClientWrapper {
	public String getConfigDirectory() throws ClientWrapperException;
	public void setConfigDirectory(String configDir) throws ClientWrapperException;
	
	
    public void setUsername(String username);
    public void setPassword(String password);
    
    public boolean isCredentialsCacheEnabled();
    public void setCredentialsCacheEnabled(boolean cacheCredentials);

    public void setPrompt(ICredentialsPrompt prompt);
    public ICredentialsPrompt getPrompt();
    
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

    public void setNotificationCallback(INotificationCallback notify);
    public INotificationCallback getNotificationCallback();
    

	public long checkout(String moduleName, String destPath, Revision revision, Revision pegRevision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public void lock(String []path, String comment, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public void unlock(String []path, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void add(String path, int depth, boolean skipIgnores, boolean addParents, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public long []commit(String []path, String message, int depth, boolean noUnlock, boolean keepChangelist, String changelistName, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public long []update(String []path, Revision revision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public long doSwitch(String path, String url, Revision revision, Revision pegRevision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void revert(String path, int depth, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void status(String path, int depth, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, IStatusCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void cleanup(String path, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void merge(String path1, Revision revision1, String path2, Revision revision2, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void merge(String path, Revision pegRevision, RevisionRange []revisions, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public MergeInfo getMergeInfo(String path, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public RevisionRange []getAvailableMerges(String path, Revision pegRevision, String mergeSource, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public String []suggestMergeSources(String path, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void resolved(String path, int depth, int conflictResult, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void setConflictResolver(IConflictResolutionCallback listener);
    
    public void addToChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public void removeFromChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws ClientWrapperException;
    public String[] getChangelist(String changelist, String rootPath, ISVNProgressMonitor monitor) throws ClientWrapperException;

	public void merge(String url, Revision peg, RevisionRange []revisions, String mergePath, Status[] mergeStatus, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void mergeStatus(String url, Revision peg, RevisionRange []revisions, String path, int depth, boolean ignoreAncestry, IStatusCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException;

	
	public void doImport(String path, String url, String message, int depth, boolean noIgnore, boolean ignoreUnknownNodeTypes, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public long doExport(String srcPath, String destPath, Revision revision, Revision pegRevision, boolean force, boolean ignoreExternals, int depth, String nativeEOL, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void diff(String target1, Revision revision1, Revision peg1, String target2, Revision revision2, Revision peg2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void diffStatus(String url1, Revision pegRevision1, Revision revision1, String url2, Revision pegRevision2, Revision revision2, int depth, boolean ignoreAncestry, IStatusCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException;

	public void info(String pathOrUrl, Revision revision, Revision pegRevision, int depth, IEntryInfoCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void streamFileContent(String path, Revision revision, Revision pegRevision, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void mkdir(String []path, String message, boolean makeParents, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void move(String []srcPath, String dstPath, String message, boolean force, boolean moveAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void copy(String srcPath, String destPath, Revision revision, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void copy(CopySource []srcPath, String destPath, String message, boolean copyAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void remove(String []path, String message, boolean force, boolean keepLocal, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void logEntries(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean stopOnCopy, boolean discoverPath, boolean includeMergedRevisions, String[] revProps, long limit, ILogEntriesCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void annotate(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean ignoreMimeType, boolean includeMergedRevisions, IAnnotationCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void list(String url, Revision revision, Revision pegRevision, int depth, int direntFields, boolean fetchLocks, IRepositoryEntryCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException;

	
	public void properties(String path, Revision revision, Revision peg, int depth, IPropertyDataCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public PropertyData propertyGet(String path, String name, Revision revision, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void propertySet(String path, String name, byte []value, int depth, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void propertySet(String path, String name, String value, int depth, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	public void propertyRemove(String path, String name, int depth, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	
	public void dispose();
}
