/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension.factory;

import java.io.OutputStream;

import org.eclipse.team.svn.core.client.ISVNAnnotationCallback;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.ISVNConflictResolutionCallback;
import org.eclipse.team.svn.core.client.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.client.ISVNEntryCallback;
import org.eclipse.team.svn.core.client.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.client.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.client.ISVNLogEntriesCallback;
import org.eclipse.team.svn.core.client.ISVNNotificationCallback;
import org.eclipse.team.svn.core.client.ISVNProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNPropertyCallback;
import org.eclipse.team.svn.core.client.SVNClientException;
import org.eclipse.team.svn.core.client.SVNEntryReference;
import org.eclipse.team.svn.core.client.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNMergeInfo;
import org.eclipse.team.svn.core.client.SVNProperty;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.SVNRevisionRange;
import org.eclipse.team.svn.core.utility.StringId;

/**
 * Each method call through this wrapper redefines thread name then restores it
 * 
 * @author Alexander Gurov
 */
public class ThreadNameModifier implements ISVNClient {
	protected ISVNClient client;

	public ThreadNameModifier(ISVNClient client) {
		this.client = client;
	}

	public void add(String path, int depth, boolean skipIgnores, boolean addParents, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.add(path, depth, skipIgnores, addParents, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void annotate(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, boolean ignoreMimeType, boolean includeMergedRevisions, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.annotate(reference, revisionStart, revisionEnd, ignoreMimeType, includeMergedRevisions, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.checkout(fromReference, destPath, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void cleanup(String path, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.cleanup(path, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []commit(String []path, String message, int depth, boolean noUnlock, boolean keepChangelist, String changelistName, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.commit(path, message, depth, noUnlock, keepChangelist, changelistName, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(String srcPath, String destPath, SVNRevision revision, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.copy(srcPath, destPath, revision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(SVNEntryRevisionReference []srcPath, String destPath, String message, boolean copyAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.copy(srcPath, destPath, message, copyAsChild, makeParents, withMergeHistory, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diff(reference1, reference2, outFileName, depth, ignoreAncestry, noDiffDeleted, force, diffUnversioned, relativePath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diff(reference, revision1, revision2, outFileName, depth, ignoreAncestry, noDiffDeleted, force, diffUnversioned, relativePath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diffStatus(reference1, reference2, depth, ignoreAncestry, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diffStatus(reference, revision1, revision2, depth, ignoreAncestry, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void dispose() {
		String oldName = this.overrideThreadName();
		try {
			this.client.dispose();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doExport(SVNEntryRevisionReference fromReference, String destPath, boolean force, boolean ignoreExternals, int depth, String nativeEOL, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.doExport(fromReference, destPath, force, ignoreExternals, depth, nativeEOL, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void doImport(String path, String url, String message, int depth, boolean noIgnore, boolean ignoreUnknownNodeTypes, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.doImport(path, url, message, depth, noIgnore, ignoreUnknownNodeTypes, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doSwitch(String path, SVNEntryRevisionReference toReference, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.doSwitch(path, toReference, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String getConfigDirectory() throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getConfigDirectory();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ISVNNotificationCallback getNotificationCallback() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getNotificationCallback();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ISVNCredentialsPrompt getPrompt() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getPrompt();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCommitMissingFiles() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isCommitMissingFiles();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCredentialsCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isCredentialsCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isReportRevisionChange() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isReportRevisionChange();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isSSLCertificateCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isSSLCertificateCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isTouchUnresolved() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isTouchUnresolved();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void list(SVNEntryRevisionReference reference, int depth, int direntFields, boolean fetchLocks, ISVNEntryCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.list(reference, depth, direntFields, fetchLocks, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void lock(String[] path, String comment, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.lock(path, comment, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void logEntries(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, boolean stopOnCopy, boolean discoverPath, boolean includeMergedRevisions, String[] revProps, long limit, ISVNLogEntriesCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.logEntries(reference, revisionStart, revisionEnd, stopOnCopy, discoverPath, includeMergedRevisions, revProps, limit, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.merge(reference1, reference2, localPath, force, depth, ignoreAncestry, dryRun, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.merge(reference, revisions, localPath, force, depth, ignoreAncestry, dryRun, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String mergePath, SVNEntryStatus[] mergeStatus, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.merge(reference, revisions, mergePath, mergeStatus, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange []revisions, String path, int depth, boolean ignoreAncestry, ISVNEntryStatusCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.mergeStatus(reference, revisions, path, depth, ignoreAncestry, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mkdir(String []path, String message, boolean makeParents, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.mkdir(path, message, makeParents, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.move(srcPath, dstPath, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String []srcPath, String dstPath, String message, boolean force, boolean moveAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.move(srcPath, dstPath, message, force, moveAsChild, makeParents, withMergeHistory, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setNotificationCallback(ISVNNotificationCallback notify) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setNotificationCallback(notify);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPassword(String password) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setPassword(password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty propertyGet(SVNEntryRevisionReference reference, String name, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.propertyGet(reference, name, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertyRemove(String path, String name, int depth, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertyRemove(path, name, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, byte []value, int depth, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertySet(path, name, value, depth, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, String value, int depth, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertySet(path, name, value, depth, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.relocate(from, to, path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void remove(String []path, String message, boolean force, boolean keepLocal, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.remove(path, message, force, keepLocal, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void resolved(String path, int depth, int conflictResult, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.resolved(path, depth, conflictResult, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void revert(String path, int depth, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.revert(path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setClientSSLCertificate(String certPath, String passphrase) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setClientSSLCertificate(certPath, passphrase);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCommitMissingFiles(boolean commitMissingFiles) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setCommitMissingFiles(commitMissingFiles);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCredentialsCacheEnabled(boolean cacheCredentials) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setCredentialsCacheEnabled(cacheCredentials);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPrompt(ISVNCredentialsPrompt prompt) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setPrompt(prompt);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setProxy(String host, int port, String userName, String password) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setProxy(host, port, userName, password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setReportRevisionChange(boolean report) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setReportRevisionChange(report);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSHCredentials(userName, privateKeyPath, passphrase, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String password, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSHCredentials(userName, password, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSLCertificateCacheEnabled(boolean enabled) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSLCertificateCacheEnabled(enabled);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setTouchUnresolved(boolean touchUnresolved) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setTouchUnresolved(touchUnresolved);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void status(String path, int depth, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.status(path, depth, onServer, getAll, noIgnore, ignoreExternals, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void streamFileContent(SVNEntryRevisionReference reference, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.streamFileContent(reference, bufferSize, stream, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void unlock(String[] path, boolean force, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.unlock(path, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []update(String []path, SVNRevision revision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.update(path, revision, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setUsername(String username) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setUsername(username);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void addToChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.addToChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNRevisionRange[] getAvailableMerges(SVNEntryReference reference, String mergeSource, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getAvailableMerges(reference, mergeSource, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] getChangelist(String changelist, String rootPath, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getChangelist(changelist, rootPath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getMergeInfo(reference, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void info(SVNEntryRevisionReference reference, int depth, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.info(reference, depth, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void properties(SVNEntryRevisionReference reference, int depth, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.properties(reference, depth, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeFromChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.removeFromChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConfigDirectory(String configDir) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			this.client.setConfigDirectory(configDir);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConflictResolver(ISVNConflictResolutionCallback listener) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setConflictResolver(listener);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNClientException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.suggestMergeSources(reference, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	protected String overrideThreadName() {
		Thread current = Thread.currentThread();
		String oldName = current.getName();
		current.setName(StringId.generateRandom("SVN", 5));
		return oldName;
	}

	protected void restoreThreadName(String oldName) {
		Thread.currentThread().setName(oldName);
	}

}
