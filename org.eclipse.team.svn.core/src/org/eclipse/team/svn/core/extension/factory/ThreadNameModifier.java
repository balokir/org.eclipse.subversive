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

import org.eclipse.team.svn.core.connector.ISVNAnnotationCallback;
import org.eclipse.team.svn.core.connector.ISVNConflictResolutionCallback;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.ISVNDiffStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNMergeStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNPropertyCallback;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNMergeInfo;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.utility.StringId;

/**
 * Each method call through this wrapper redefines thread name then restores it
 * 
 * @author Alexander Gurov
 */
public class ThreadNameModifier implements ISVNConnector {
	protected ISVNConnector connector;

	public ThreadNameModifier(ISVNConnector connector) {
		this.connector = connector;
	}

	public void add(String path, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.add(path, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void annotate(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, long options, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.annotate(reference, revisionStart, revisionEnd, options, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.checkout(fromReference, destPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void cleanup(String path, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.cleanup(path, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []commit(String []path, String message, int depth, long options, String changelistName, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.commit(path, message, depth, options, changelistName, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(String srcPath, String destPath, SVNRevision revision, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copy(srcPath, destPath, revision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(SVNEntryRevisionReference []srcPath, String destPath, String message, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copy(srcPath, destPath, message, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String relativeToDir, String outFileName, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(reference1, reference2, relativeToDir, outFileName, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, String relativeToDir, String outFileName, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(reference, revision1, revision2, relativeToDir, outFileName, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, int depth, long options, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatus(reference1, reference2, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryReference reference, SVNRevision revision1, SVNRevision revision2, int depth, long options, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatus(reference, revision1, revision2, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void dispose() {
		String oldName = this.overrideThreadName();
		try {
			this.connector.dispose();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doExport(SVNEntryRevisionReference fromReference, String destPath, long options, int depth, String nativeEOL, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.doExport(fromReference, destPath, options, depth, nativeEOL, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void doImport(String path, String url, String message, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.doImport(path, url, message, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doSwitch(String path, SVNEntryRevisionReference toReference, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.doSwitch(path, toReference, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String getConfigDirectory() throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getConfigDirectory();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ISVNNotificationCallback getNotificationCallback() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getNotificationCallback();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ISVNCredentialsPrompt getPrompt() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getPrompt();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCommitMissingFiles() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isCommitMissingFiles();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCredentialsCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isCredentialsCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isReportRevisionChange() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isReportRevisionChange();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isSSLCertificateCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isSSLCertificateCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isTouchUnresolved() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isTouchUnresolved();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void list(SVNEntryRevisionReference reference, int depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.list(reference, depth, direntFields, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void lock(String[] path, String comment, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.lock(path, comment, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void logEntries(SVNEntryReference reference, SVNRevision revisionStart, SVNRevision revisionEnd, long options, String[] revProps, long limit, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.logEntries(reference, revisionStart, revisionEnd, options, revProps, limit, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference1, reference2, localPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String localPath, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference, revisions, localPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference, revisions, mergePath, mergeStatus, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange []revisions, String path, int depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeStatus(reference, revisions, path, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mkdir(String []path, String message, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mkdir(path, message, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String srcPath, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.move(srcPath, dstPath, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String []srcPath, String dstPath, String message, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.move(srcPath, dstPath, message, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setNotificationCallback(ISVNNotificationCallback notify) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setNotificationCallback(notify);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPassword(String password) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPassword(password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty propertyGet(SVNEntryRevisionReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.propertyGet(reference, name, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertyRemove(String path, String name, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.propertyRemove(path, name, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, byte []value, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.propertySet(path, name, value, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, String value, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.propertySet(path, name, value, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void relocate(String from, String to, String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.relocate(from, to, path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void remove(String []path, String message, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.remove(path, message, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void resolved(String path, int depth, int conflictResult, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.resolved(path, depth, conflictResult, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void revert(String path, int depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.revert(path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setClientSSLCertificate(String certPath, String passphrase) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setClientSSLCertificate(certPath, passphrase);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCommitMissingFiles(boolean commitMissingFiles) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setCommitMissingFiles(commitMissingFiles);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCredentialsCacheEnabled(boolean cacheCredentials) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setCredentialsCacheEnabled(cacheCredentials);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPrompt(ISVNCredentialsPrompt prompt) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPrompt(prompt);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setProxy(String host, int port, String userName, String password) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setProxy(host, port, userName, password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setReportRevisionChange(boolean report) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setReportRevisionChange(report);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setSSHCredentials(userName, privateKeyPath, passphrase, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String password, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setSSHCredentials(userName, password, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSLCertificateCacheEnabled(boolean enabled) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setSSLCertificateCacheEnabled(enabled);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setTouchUnresolved(boolean touchUnresolved) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setTouchUnresolved(touchUnresolved);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void status(String path, int depth, long options, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.status(path, depth, options, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void streamFileContent(SVNEntryRevisionReference reference, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.streamFileContent(reference, bufferSize, stream, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void unlock(String[] path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.unlock(path, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []update(String []path, SVNRevision revision, int depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.update(path, revision, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setUsername(String username) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setUsername(username);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void addToChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.addToChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNRevisionRange[] getAvailableMerges(SVNEntryReference reference, String mergeSource, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getAvailableMerges(reference, mergeSource, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] getChangelist(String changelist, String rootPath, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getChangelist(changelist, rootPath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getMergeInfo(reference, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void info(SVNEntryRevisionReference reference, int depth, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.info(reference, depth, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void properties(SVNEntryRevisionReference reference, int depth, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.properties(reference, depth, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeFromChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeFromChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConfigDirectory(String configDir) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setConfigDirectory(configDir);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConflictResolver(ISVNConflictResolutionCallback listener) {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setConflictResolver(listener);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] suggestMergeSources(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.suggestMergeSources(reference, monitor);
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
