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

import org.eclipse.team.svn.core.ICredentialsPrompt;


/**
 * SVN client wrapper interface
 * 
 * @author Alexander Gurov
 */
public interface ISVNClientWrapper {
	public String getConfigDirectory() throws ClientWrapperException;
	
    public void username(String username);
    
    public void password(String password);
    
    public void setProxy(String host, int port, String userName, String password);
    
    public void setClientSSLCertificate(String certPath, String passphrase);
    
    public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port);

    public void setSSHCredentials(String userName, String password, int port);

    public void notification2(Notify2 notify);
    
    public Notify2 getNotification2();

    public boolean isCredentialsCacheEnabled();

    public void setCredentialsCacheEnabled(boolean cacheCredentials);

    public boolean isSSLCertificateCacheEnabled();

    public void setSSLCertificateCacheEnabled(boolean enabled);

    public void setReportRevisionChange(boolean report); 

    public boolean isReportRevisionChange();
    
    public void setCommitMissingFiles(boolean commitMissingFiles);
    
    public boolean isCommitMissingFiles();
    
    public void setTouchUnresolved(boolean touchUnresolved);
    
    public boolean isTouchUnresolved();

    public void setPrompt(ICredentialsPrompt prompt);
    
    public ICredentialsPrompt getPrompt();
    

    public void lock(String []path, String comment, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;

    public void unlock(String []path, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
    
	public void add(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public long []commit(String []path, String message, boolean recurse, boolean noUnlock, /*boolean atomic, */ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void relocate(String from, String to, String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void cleanup(String path, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public Status []status(String path, boolean descend, boolean onServer, boolean getAll, boolean noIgnore, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public Status []status(final String path, boolean descend, boolean onServer, boolean getAll, boolean noIgnore, boolean collectParentExternals, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void merge(String path1, Revision revision1, String path2,
            Revision revision2, String localPath, boolean force,
            boolean recurse, boolean ignoreAncestry, boolean dryRun,
            ISVNProgressMonitor monitor) throws ClientWrapperException;

	public Status[] merge(String url, Revision peg, Revision from, Revision to, String mergePath, Status[] mergeStatus, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public Status[] mergeStatus(String url, Revision peg, Revision from, Revision to, String path, Revision lastMerged, boolean recurse, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void revert(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public long doSwitch(String path, String url, Revision revision, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public long []update(String []path, Revision revision, boolean recurse, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void doImport(String path, String url, String message, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public Status []diffStatus(String url1, Revision pegRevision1, Revision revision1, String url2, Revision pegRevision2, Revision revision2, boolean recurse, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException;

	public Info2 []info2(String pathOrUrl, Revision revision, Revision pegRevision, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public byte []fileContent(String path, Revision revision, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void streamFileContent(String path, Revision revision, Revision pegRevision, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws ClientWrapperException;

	public long checkout(String moduleName, String destPath, Revision revision, Revision pegRevision, boolean recurse, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void diff(String target1, Revision revision1, Revision peg1, String target2, Revision revision2, Revision peg2, 
            String outFileName, boolean recurse, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, 
            boolean diffUnversioned, boolean relativePath, 
            ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public long doExport(String srcPath, String destPath, Revision revision,
            Revision pegRevision, boolean force, boolean ignoreExternals,
            boolean recurse, String nativeEOL, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void copy(String srcPath, String destPath, String message, Revision revision, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void remove(String []path, String message, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void mkdir(String []path, String message, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public LogMessage []logMessages(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean stopOnCopy, boolean discoverPath, long limit, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void blame(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, BlameCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void move(String srcPath, String destPath, String message, Revision revision, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public DirEntry []list(String url, Revision revision, Revision pegRevision, boolean recurse, boolean fetchLocks, ISVNProgressMonitor monitor) throws ClientWrapperException;

	
	public void resolved(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public PropertyData []properties(String path, Revision revision, Revision peg, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public PropertyData propertyGet(String path, String name, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void propertySet(String path, String name, byte []value, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void propertySet(String path, String name, String value, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	public void propertyRemove(String path, String name, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException;
	
	
	public void dispose();
}
