/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import java.util.List;
import java.util.Map;

import org.eclipse.team.svn.core.connector.ISVNAnnotationCallback;
import org.eclipse.team.svn.core.connector.ISVNCallListener;
import org.eclipse.team.svn.core.connector.ISVNChangeListCallback;
import org.eclipse.team.svn.core.connector.ISVNConflictResolutionCallback;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.ISVNDiffStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNImportFilterCallback;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.ISVNMergeStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.ISVNPatchCallback;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNPropertyCallback;
import org.eclipse.team.svn.core.connector.SVNConflictResolution.Choice;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNExternalReference;
import org.eclipse.team.svn.core.connector.SVNMergeInfo;
import org.eclipse.team.svn.core.connector.SVNMergeInfo.LogKind;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.configuration.ISVNConfigurationEventHandler;
import org.eclipse.team.svn.core.utility.StringId;

/**
 * Each method call through this wrapper redefines thread name then restores it
 * 
 * Replace CRLF,CR to LF for svn:log property
 *   
 * @author Alexander Gurov
 */
public class ThreadNameModifier implements ISVNConnector {
	protected ISVNConnector connector;

	public ThreadNameModifier(ISVNConnector connector) {
		this.connector = connector;
	}

	public void addCallListener(ISVNCallListener listener) {
		this.connector.addCallListener(listener);
	}
	
	public void removeCallListener(ISVNCallListener listener) {
		this.connector.removeCallListener(listener);
	}
	
	public void add(String path, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.add(path, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void annotate(SVNEntryReference reference, SVNRevisionRange revisionRange, long options, long diffOptions, ISVNAnnotationCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.annotate(reference, revisionRange, options, diffOptions, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long checkout(SVNEntryRevisionReference fromReference, String destPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.checkout(fromReference, destPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void cleanup(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.cleanup(path, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void commit(String []path, String message, String[] changelistNames, SVNDepth depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.commit(path, this.processSVNLogProperty(message), changelistNames, depth, options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copyLocal(SVNEntryRevisionReference []srcPaths, String destPath, long options, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copyLocal(srcPaths, destPath, options, externalsToPin, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copyRemote(SVNEntryRevisionReference []srcPaths, String destPath, String message, long options, Map revProps, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copyRemote(srcPaths, destPath, this.processSVNLogProperty(message), options, revProps, externalsToPin, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String relativeToDir, OutputStream stream, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffTwo(reference1, reference2, relativeToDir, stream, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevisionRange revision1, String relativeToDir, String fileName, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(reference, revision1, relativeToDir, fileName, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String relativeToDir, String fileName, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffTwo(reference1, reference2, relativeToDir, fileName, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevisionRange revision1, String relativeToDir, OutputStream stream, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(reference, revision1, relativeToDir, stream, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatusTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, SVNDepth depth, long options, String[] changelistNames, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatusTwo(reference1, reference2, depth, options, changelistNames, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryReference reference, SVNRevisionRange revision1, SVNDepth depth, long options, String[] changelistNames, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatus(reference, revision1, depth, options, changelistNames, cb, monitor);
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

	public long exportTo(SVNEntryRevisionReference fromReference, String destPath, String nativeEOL, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.exportTo(fromReference, destPath, nativeEOL, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void importTo(String path, String url, String message, SVNDepth depth, long options, Map revProps, ISVNImportFilterCallback filter, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.importTo(path, url, this.processSVNLogProperty(message), depth, options, revProps, filter, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long switchTo(String path, SVNEntryRevisionReference toReference, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.switchTo(path, toReference, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConfigurationEventHandler(ISVNConfigurationEventHandler configHandler) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setConfigurationEventHandler(configHandler);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ISVNConfigurationEventHandler getConfigurationEventHandler() throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getConfigurationEventHandler();
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

	public boolean isSSLCertificateCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.isSSLCertificateCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listEntries(SVNEntryRevisionReference reference, SVNDepth depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listEntries(reference, depth, direntFields, options, cb, monitor);
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

	public void listHistoryLog(SVNEntryReference reference, SVNRevisionRange []revisionRanges, String[] revProps, long limit, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listHistoryLog(reference, revisionRanges, revProps, limit, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeTwo(reference1, reference2, localPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference, revisions, localPath, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void mergeReintegrate(SVNEntryReference reference, String localPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeReintegrate(reference, localPath, options, monitor);
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

	public void merge(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference1, reference2, mergePath, mergeStatus, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange []revisions, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeStatus(reference, revisions, path, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeStatus(reference1, reference2, path, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void merge(SVNEntryReference reference, String mergePath, SVNMergeStatus[] mergeStatus, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(reference, mergePath, mergeStatus, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeStatus(SVNEntryReference reference, String mergePath, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeStatus(reference, mergePath, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void mkdir(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mkdir(path, this.processSVNLogProperty(message), options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void moveLocal(String[] srcPaths, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.moveLocal(srcPaths, dstPath, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void moveRemote(String[] srcPaths, String dstPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.moveRemote(srcPaths, dstPath, this.processSVNLogProperty(message), options, revProps, monitor);
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

	public SVNProperty getProperty(SVNEntryRevisionReference reference, String name, String[] changeLists, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getProperty(reference, name, changeLists, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPropertyLocal(String []path, SVNProperty property, SVNDepth depth, long options, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPropertyLocal(path, property, depth, options, changelistNames, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPropertyRemote(SVNEntryReference reference, SVNProperty property, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPropertyRemote(reference, property, message, options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void relocate(String from, String to, String path, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.relocate(from, to, path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeLocal(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeLocal(path, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeRemote(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeRemote(path, this.processSVNLogProperty(message), options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void resolve(String path, Choice conflictResult, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.resolve(path, conflictResult, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void revert(String []paths, SVNDepth depth, String []changeLists, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.revert(paths, depth, changeLists, options, monitor);
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

	public void status(String path, SVNDepth depth, long options, String[] changelistNames, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.status(path, depth, options, changelistNames, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty []streamFileContent(SVNEntryRevisionReference reference, long options, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.streamFileContent(reference, options, stream, monitor);
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

	public long []update(String []path, SVNRevision revision, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
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

	public void addToChangeList(String[] paths, String changelist, SVNDepth depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.addToChangeList(paths, changelist, depth, changelistNames, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listMergeInfoLog(LogKind logKind, SVNEntryReference reference, SVNEntryReference mergeSourceReference, SVNRevisionRange mergeSourceRange, String[] revProps, SVNDepth depth, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listMergeInfoLog(logKind, reference, mergeSourceReference, mergeSourceRange, revProps, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void dumpChangeLists(String[] changeLists, String rootPath, SVNDepth depth, ISVNChangeListCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.dumpChangeLists(changeLists, rootPath, depth, cb, monitor);
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

	public void getInfo(SVNEntryRevisionReference reference, SVNDepth depth, long options, String []changeLists, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.getInfo(reference, depth, options, changeLists, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listProperties(SVNEntryRevisionReference reference, SVNDepth depth, String[] changelistNames, long options, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listProperties(reference, depth, changelistNames, options, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeFromChangeLists(String[] paths, SVNDepth depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeFromChangeLists(paths, depth, changelistNames, monitor);
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

	public ISVNConflictResolutionCallback getConflictResolver() {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getConflictResolver();
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

	public SVNProperty []listRevisionProperties(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.listRevisionProperties(reference, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty getRevisionProperty(SVNEntryReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getRevisionProperty(reference, name, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, String originalValue, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setRevisionProperty(reference, property, originalValue, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void upgrade(String path, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.upgrade(path, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void vacuum(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.vacuum(path, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void patch(String patchPath, String targetPath, int stripCount, long options, ISVNPatchCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.patch(patchPath, targetPath, stripCount, options, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	protected String overrideThreadName() {
		Thread current = Thread.currentThread();
		String oldName = current.getName();
		current.setName(StringId.generateRandom("SVN", 5)); //$NON-NLS-1$
		return oldName;
	}

	protected void restoreThreadName(String oldName) {
		Thread.currentThread().setName(oldName);
	}

	/**
	 * Replace CRLF,CR to LF
	 * 
	 * @param str
	 * @return
	 */
	protected String processSVNLogProperty(String str) {
		return str != null ? str.replaceAll("\r\n|\r", "\n") : null;
	}

}
