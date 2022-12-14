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
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.configuration.ISVNConfigurationEventHandler;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.StringId;

/**
 * This class servers as facade for {@link ISVNConnector}. During operation, the
 * thread name is modified in order to reflect the current operation.
 * 
 * NIC misleading name. better: SvnConnectorFacade
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
			this.connector.add(FileUtility.normalizePathJavaHL(path), depth, options, monitor);
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
			return this.connector.checkout(ThreadNameModifier.normalizePathJavaHL(fromReference), FileUtility.normalizePathJavaHL(destPath), depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void cleanup(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.cleanup(FileUtility.normalizePathJavaHL(path), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void commit(String []path, String message, String[] changelistNames, SVNDepth depth, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.commit(FileUtility.normalizePathsJavaHL(path), this.processSVNLogProperty(message), changelistNames, depth, options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copyLocal(SVNEntryRevisionReference []srcPaths, String destPath, long options, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copyLocal(ThreadNameModifier.normalizePathJavaHL(srcPaths), FileUtility.normalizePathJavaHL(destPath), options, externalsToPin, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copyRemote(SVNEntryRevisionReference []srcPaths, String destPath, String message, long options, Map revProps, Map<String, List<SVNExternalReference>> externalsToPin, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.copyRemote(ThreadNameModifier.normalizePathJavaHL(srcPaths), FileUtility.normalizePathJavaHL(destPath), this.processSVNLogProperty(message), options, revProps, externalsToPin, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String relativeToDir, OutputStream stream, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffTwo(ThreadNameModifier.normalizePathJavaHL(reference1), ThreadNameModifier.normalizePathJavaHL(reference2), FileUtility.normalizePathJavaHL(relativeToDir), stream, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevisionRange revision1, String relativeToDir, String fileName, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(ThreadNameModifier.normalizePathJavaHL(reference), revision1, FileUtility.normalizePathJavaHL(relativeToDir), fileName, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String relativeToDir, String fileName, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffTwo(ThreadNameModifier.normalizePathJavaHL(reference1), ThreadNameModifier.normalizePathJavaHL(reference2), FileUtility.normalizePathJavaHL(relativeToDir), fileName, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(SVNEntryReference reference, SVNRevisionRange revision1, String relativeToDir, OutputStream stream, SVNDepth depth, long options, String[] changelistNames, long outputOptions, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diff(ThreadNameModifier.normalizePathJavaHL(reference), revision1, FileUtility.normalizePathJavaHL(relativeToDir), stream, depth, options, changelistNames, outputOptions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatusTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, SVNDepth depth, long options, String[] changelistNames, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatusTwo(ThreadNameModifier.normalizePathJavaHL(reference1), ThreadNameModifier.normalizePathJavaHL(reference2), depth, options, changelistNames, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(SVNEntryReference reference, SVNRevisionRange revision1, SVNDepth depth, long options, String[] changelistNames, ISVNDiffStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.diffStatus(ThreadNameModifier.normalizePathJavaHL(reference), revision1, depth, options, changelistNames, cb, monitor);
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
			return this.connector.exportTo(ThreadNameModifier.normalizePathJavaHL(fromReference), FileUtility.normalizePathJavaHL(destPath), nativeEOL, depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void importTo(String path, String url, String message, SVNDepth depth, long options, Map revProps, ISVNImportFilterCallback filter, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.importTo(FileUtility.normalizePathJavaHL(path), url, this.processSVNLogProperty(message), depth, options, revProps, filter, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long switchTo(String path, SVNEntryRevisionReference toReference, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.switchTo(FileUtility.normalizePathJavaHL(path), ThreadNameModifier.normalizePathJavaHL(toReference), depth, options, monitor);
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

	public void listEntries(SVNEntryRevisionReference reference, SVNDepth depth, int direntFields, long options, ISVNEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listEntries(ThreadNameModifier.normalizePathJavaHL(reference), depth, direntFields, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void lock(String[] path, String comment, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.lock(FileUtility.normalizePathsJavaHL(path), comment, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listHistoryLog(SVNEntryReference reference, SVNRevisionRange []revisionRanges, String[] revProps, long limit, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listHistoryLog(ThreadNameModifier.normalizePathJavaHL(reference), revisionRanges, revProps, limit, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mergeTwo(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeTwo(ThreadNameModifier.normalizePathJavaHL(reference1), ThreadNameModifier.normalizePathJavaHL(reference2), FileUtility.normalizePathJavaHL(localPath), depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(SVNEntryReference reference, SVNRevisionRange []revisions, String localPath, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.merge(ThreadNameModifier.normalizePathJavaHL(reference), revisions, FileUtility.normalizePathJavaHL(localPath), depth, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void mergeReintegrate(SVNEntryReference reference, String localPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mergeReintegrate(ThreadNameModifier.normalizePathJavaHL(reference), FileUtility.normalizePathJavaHL(localPath), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void mkdir(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.mkdir(FileUtility.normalizePathsJavaHL(path), this.processSVNLogProperty(message), options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void moveLocal(String[] srcPaths, String dstPath, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.moveLocal(FileUtility.normalizePathsJavaHL(srcPaths), FileUtility.normalizePathJavaHL(dstPath), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void moveRemote(String[] srcPaths, String dstPath, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.moveRemote(FileUtility.normalizePathsJavaHL(srcPaths), FileUtility.normalizePathJavaHL(dstPath), this.processSVNLogProperty(message), options, revProps, monitor);
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
			return this.connector.getProperty(ThreadNameModifier.normalizePathJavaHL(reference), name, changeLists, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPropertyLocal(String []path, SVNProperty property, SVNDepth depth, long options, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPropertyLocal(FileUtility.normalizePathsJavaHL(path), property, depth, options, changelistNames, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPropertyRemote(SVNEntryReference reference, SVNProperty property, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setPropertyRemote(ThreadNameModifier.normalizePathJavaHL(reference), property, message, options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void relocate(String from, String to, String path, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.relocate(from, to, FileUtility.normalizePathJavaHL(path), depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeLocal(String []path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeLocal(FileUtility.normalizePathsJavaHL(path), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeRemote(String []path, String message, long options, Map revProps, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeRemote(FileUtility.normalizePathsJavaHL(path), this.processSVNLogProperty(message), options, revProps, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void resolve(String path, Choice conflictResult, SVNDepth depth, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.resolve(FileUtility.normalizePathJavaHL(path), conflictResult, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void revert(String []paths, SVNDepth depth, String []changeLists, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.revert(FileUtility.normalizePathsJavaHL(paths), depth, changeLists, options, monitor);
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

	public void status(String path, SVNDepth depth, long options, String[] changelistNames, ISVNEntryStatusCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.status(FileUtility.normalizePathJavaHL(path), depth, options, changelistNames, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty []streamFileContent(SVNEntryRevisionReference reference, long options, OutputStream stream, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.streamFileContent(ThreadNameModifier.normalizePathJavaHL(reference), options, stream, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void unlock(String[] path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.unlock(FileUtility.normalizePathsJavaHL(path), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []update(String []path, SVNRevision revision, SVNDepth depth, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.update(FileUtility.normalizePathsJavaHL(path), revision, depth, options, monitor);
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
			this.connector.addToChangeList(FileUtility.normalizePathsJavaHL(paths), changelist, depth, changelistNames, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listMergeInfoLog(LogKind logKind, SVNEntryReference reference, SVNEntryReference mergeSourceReference, SVNRevisionRange mergeSourceRange, String[] revProps, SVNDepth depth, long options, ISVNLogEntryCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listMergeInfoLog(logKind, ThreadNameModifier.normalizePathJavaHL(reference), mergeSourceReference, mergeSourceRange, revProps, depth, options, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void dumpChangeLists(String[] changeLists, String rootPath, SVNDepth depth, ISVNChangeListCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.dumpChangeLists(changeLists, FileUtility.normalizePathJavaHL(rootPath), depth, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNMergeInfo getMergeInfo(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getMergeInfo(ThreadNameModifier.normalizePathJavaHL(reference), monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void getInfo(SVNEntryRevisionReference reference, SVNDepth depth, long options, String []changeLists, ISVNEntryInfoCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.getInfo(ThreadNameModifier.normalizePathJavaHL(reference), depth, options, changeLists, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void listProperties(SVNEntryRevisionReference reference, SVNDepth depth, String[] changelistNames, long options, ISVNPropertyCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.listProperties(ThreadNameModifier.normalizePathJavaHL(reference), depth, changelistNames, options, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeFromChangeLists(String[] paths, SVNDepth depth, String[] changelistNames, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.removeFromChangeLists(FileUtility.normalizePathsJavaHL(paths), depth, changelistNames, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConfigDirectory(String configDir) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setConfigDirectory(FileUtility.normalizePathJavaHL(configDir));
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
			return this.connector.suggestMergeSources(ThreadNameModifier.normalizePathJavaHL(reference), monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty []listRevisionProperties(SVNEntryReference reference, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.listRevisionProperties(ThreadNameModifier.normalizePathJavaHL(reference), monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public SVNProperty getRevisionProperty(SVNEntryReference reference, String name, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			return this.connector.getRevisionProperty(ThreadNameModifier.normalizePathJavaHL(reference), name, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setRevisionProperty(SVNEntryReference reference, SVNProperty property, String originalValue, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.setRevisionProperty(ThreadNameModifier.normalizePathJavaHL(reference), property, originalValue, options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void upgrade(String path, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.upgrade(FileUtility.normalizePathJavaHL(path), monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void vacuum(String path, long options, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.vacuum(FileUtility.normalizePathJavaHL(path), options, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}
	
	public void patch(String patchPath, String targetPath, int stripCount, long options, ISVNPatchCallback callback, ISVNProgressMonitor monitor) throws SVNConnectorException {
		String oldName = this.overrideThreadName();
		try {
			this.connector.patch(FileUtility.normalizePathJavaHL(patchPath), FileUtility.normalizePathJavaHL(targetPath), stripCount, options, callback, monitor);
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

	protected static SVNEntryReference normalizePathJavaHL(SVNEntryReference reference) {
		if (reference instanceof SVNEntryRevisionReference) {
			return ThreadNameModifier.normalizePathJavaHL((SVNEntryRevisionReference)reference);
		}
		if (reference instanceof SVNExternalReference) {
			return ThreadNameModifier.normalizePathJavaHL((SVNExternalReference)reference);
		}
		if (reference.path.indexOf("://") == -1) {
			return new SVNEntryReference(FileUtility.normalizePathJavaHL(reference.path), reference.pegRevision);
		}
		return reference; 
	}

	protected static SVNExternalReference normalizePathJavaHL(SVNExternalReference reference) {
		if (reference.path.indexOf("://") == -1) { 
			return new SVNExternalReference(reference.target, FileUtility.normalizePathJavaHL(reference.path), reference.pegRevision, reference.revision);
		}
		return reference; 
	}

	protected static SVNEntryRevisionReference[] normalizePathJavaHL(SVNEntryRevisionReference[] references) {
		SVNEntryRevisionReference[] retVal = new SVNEntryRevisionReference[references.length];
		for (int i = 0; i < references.length; i++) {
			retVal[i] = ThreadNameModifier.normalizePathJavaHL(references[i]);
		}
		return retVal; 
	}

	protected static SVNEntryRevisionReference normalizePathJavaHL(SVNEntryRevisionReference reference) {
		if (reference.path.indexOf("://") == -1) {//reference.revision.getKind() == SVNRevision.Kind.WORKING && 
			return new SVNEntryRevisionReference(FileUtility.normalizePathJavaHL(reference.path), reference.pegRevision, reference.revision);
		}
		return reference; 
	}
}
