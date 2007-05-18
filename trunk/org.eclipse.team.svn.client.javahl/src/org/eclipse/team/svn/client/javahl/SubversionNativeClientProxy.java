/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Pavel Zuev - peg revisions for compare operation
 *******************************************************************************/

package org.eclipse.team.svn.client.javahl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.team.svn.core.ICredentialsPrompt;
import org.eclipse.team.svn.core.client.BlameCallback;
import org.eclipse.team.svn.core.client.ClientWrapperAuthenticationException;
import org.eclipse.team.svn.core.client.ClientWrapperCancelException;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ClientWrapperUnresolvedConflictException;
import org.eclipse.team.svn.core.client.DirEntry;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.ISVNProgressMonitor;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.NotifyInformation;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.RevisionKind;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.StatusKind;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.Notify2Composite;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.ConversionUtility;
import org.tigris.subversion.javahl.PromptUserPassword3;
import org.tigris.subversion.javahl.SVNClient;

/**
 * Native client library wrapper.
 * 
 * @author Alexander Gurov
 */
public class SubversionNativeClientProxy implements ISVNClientWrapper {
	protected SVNClient client;
	protected ICredentialsPrompt prompt;
	protected Notify2Composite composite;
	protected Notify2 installedNotify2;
	protected boolean commitMissingFiles;
	
	protected String sslCertificate;
	protected String sslPassphrase;

	public SubversionNativeClientProxy() {
		this.client = new SVNClient();
		this.client.notification2(ConversionUtility.convert(this.composite = new Notify2Composite()));
	}

	public String getConfigDirectory() throws ClientWrapperException {
		try {
			return this.client.getConfigDirectory();
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		// unreachable code
		return null;
	}
	
	public void add(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.add(path, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void blame(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, BlameCallback callback, ISVNProgressMonitor monitor)
			throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.blame(path, ConversionUtility.convert(pegRevision), ConversionUtility.convert(revisionStart), ConversionUtility.convert(revisionEnd), ConversionUtility.convert(callback));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public long checkout(String moduleName, String destPath, Revision revision, Revision pegRevision, boolean recurse,
			boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return this.client.checkout(moduleName, destPath, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision), recurse, ignoreExternals);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return 0;
	}

	public void cleanup(String path, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.cleanup(path);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public long[] commit(String[] path, String message, boolean recurse, boolean noUnlock, ISVNProgressMonitor monitor)
			throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			if (this.commitMissingFiles) {
				for (int i = 0; i < path.length && !monitor.isActivityCancelled(); i++) {
					if (!new File(path[i]).exists()) {
						this.client.remove(new String[] {path[i]}, null, true);
					}
				}
			}
			
			if (!recurse) {
				HashSet fullSet = new HashSet(Arrays.asList(path));
				HashSet deleted = new HashSet();
				FileUtility.sort(path);
				for (int i = 0; i < path.length && !monitor.isActivityCancelled(); i++) {
					File toCheck = new File(path[i]);
					if (toCheck.isDirectory()) {
						org.tigris.subversion.javahl.Status []st = this.client.status(path[i], false, false, false);
						if (st != null && st.length > 0) {
							for (int j = 0; j < st.length && !monitor.isActivityCancelled(); j++) {
								if (st[j].getTextStatus() == org.tigris.subversion.javahl.StatusKind.deleted && st[j].getPath().length() == path[i].length()) {
									deleted.add(path[i]);
									String root = path[i];
									while (i < path.length && !monitor.isActivityCancelled()) {
										if (path[i].startsWith(root) && (path[i].length() == root.length() || path[i].charAt(root.length()) == '\\' || path[i].charAt(root.length()) == '/')) {
											fullSet.remove(path[i]);
											i++;
										}
										else {
											i--;
											break;
										}
									}
									break;
								}
							}
						}
					}
				}
				if (deleted.size() > 0) {
					if (fullSet.size() == 0) {
						path = (String [])deleted.toArray(new String[deleted.size()]);
						recurse = true;
					}
					else {
						path = (String [])fullSet.toArray(new String[fullSet.size()]);
						String []deletedPath = (String [])deleted.toArray(new String[deleted.size()]);
						
						this.composite.add(wrapper);
						wrapper.start();
						return new long[] {
								this.client.commit(path, message, recurse, noUnlock),
								this.client.commit(deletedPath, message, true, noUnlock),
						};
					}
				}
			}
			
			this.composite.add(wrapper);
			wrapper.start();
			return new long[] {this.client.commit(path, message, recurse, noUnlock)};
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void copy(String srcPath, String destPath, String message, Revision revision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.copy(srcPath, destPath, message, ConversionUtility.convert(revision));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void diff(String target1, Revision revision1, Revision peg1, String target2, Revision revision2, Revision peg2, String outFileName, boolean recurse,
			boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath,
			ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			if (this.usePegSignature(target1, revision1, peg1, target2, revision2, peg2)) {
				this.client.diff(target1, ConversionUtility.convert(peg1), ConversionUtility.convert(revision1), ConversionUtility.convert(revision2), outFileName, recurse, ignoreAncestry, noDiffDeleted, force);
			}
			else {
				this.client.diff(target1, ConversionUtility.convert(revision1), target2, ConversionUtility.convert(revision2), outFileName, recurse, ignoreAncestry, noDiffDeleted, force);
			}
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public Status[] diffStatus(String url1, Revision pegRevision1, Revision revision1, String url2, Revision pegRevision2,
			Revision revision2, boolean recurse, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException {
		// extension method diffStatus is not supported. So, we emulate it for files
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		File tmp = null;
		try {
			tmp = File.createTempFile("diffStatus", ".tmp");
			this.composite.add(wrapper);
			wrapper.start();
			
			if (this.usePegSignature(url1, revision1, pegRevision1, url2, revision2, pegRevision2)) {
				this.client.diff(url1, ConversionUtility.convert(pegRevision1), ConversionUtility.convert(revision1), ConversionUtility.convert(revision2), tmp.getAbsolutePath(), recurse, ignoreAncestry, false, true);
			}
			else {
				this.client.diff(url1, ConversionUtility.convert(revision1), url2, ConversionUtility.convert(revision2), tmp.getAbsolutePath(), recurse, ignoreAncestry, false, true);
			}

			if (tmp.length() == 0) {
				return new Status[0];
			}
			return new Status[]{new Status(url1, url2, NodeKind.file, Revision.SVN_INVALID_REVNUM, Revision.SVN_INVALID_REVNUM, 0, null, StatusKind.modified, StatusKind.normal, StatusKind.normal, StatusKind.normal, false, false, null, null, null, null, Revision.SVN_INVALID_REVNUM, false, null, null, null, 0, null, Revision.SVN_INVALID_REVNUM, 0, NodeKind.file, null, null)};
		}
		catch (IOException ex) {
			throw new ClientWrapperException(ex, false);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			if (tmp != null) {
				tmp.delete();
			}
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		return null;
	}

	public void dispose() {
		this.client.dispose();
	}

	public long doExport(String srcPath, String destPath, Revision revision, Revision pegRevision, boolean force, boolean ignoreExternals,
			boolean recurse, String nativeEOL, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return this.client.doExport(srcPath, destPath, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision), force, ignoreExternals, recurse, nativeEOL);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return 0;
	}

	public void doImport(String path, String url, String message, boolean recurse, ISVNProgressMonitor monitor)
			throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.doImport(path, url, message, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public long doSwitch(String path, String url, Revision revision, boolean recurse, ISVNProgressMonitor monitor)
			throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return this.client.doSwitch(path, url, ConversionUtility.convert(revision), recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return 0;
	}

	public byte[] fileContent(String path, Revision revision, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return this.client.fileContent(path, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void streamFileContent(String path, Revision revision, Revision pegRevision, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.streamFileContent(path, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision), bufferSize, stream);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}
	
	public Notify2 getNotification2() {
		return this.installedNotify2;
	}

	public Info2[] info2(String pathOrUrl, Revision revision, Revision pegRevision, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return ConversionUtility.convert(this.client.info2(pathOrUrl, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision), recurse));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public boolean isCommitMissingFiles() {
		return this.commitMissingFiles;
	}

	public boolean isCredentialsCacheEnabled() {
		return true;
	}

	public boolean isReportRevisionChange() {
		return false;
	}

	public boolean isSSLCertificateCacheEnabled() {
		return true;
	}

	public boolean isTouchUnresolved() {
		return true;
	}

	public DirEntry[] list(String url, Revision revision, Revision pegRevision, boolean recurse, boolean fetchLocks, ISVNProgressMonitor monitor)
			throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			org.tigris.subversion.javahl.DirEntry []entries = this.client.list(url, ConversionUtility.convert(revision), ConversionUtility.convert(pegRevision), recurse);
			// fetch locks is not supported
			DirEntry []retVal = new DirEntry[entries.length];
			for (int i = 0; i < entries.length; i++) {
				retVal[i] = new DirEntry(entries[i].getLastChanged(), entries[i].getLastChangedRevisionNumber(), entries[i].getHasProps(), entries[i].getLastAuthor(), entries[i].getNodeKind(), entries[i].getSize(), entries[i].getPath(), null);
			}
			return retVal;
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void lock(String[] path, String comment, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.lock(path, comment, force);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public LogMessage[] logMessages(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean stopOnCopy,
			boolean discoverPath, long limit, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return ConversionUtility.convert(this.client.logMessages(path, ConversionUtility.convert(revisionStart), ConversionUtility.convert(revisionEnd), stopOnCopy, discoverPath, limit));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void merge(String path1, Revision revision1, String path2, Revision revision2, String localPath, boolean force, boolean recurse,
			boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.merge(path1, ConversionUtility.convert(revision1), path2, ConversionUtility.convert(revision2), localPath, force, recurse, ignoreAncestry, dryRun);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public Status[] merge(String url, Revision peg, Revision from, Revision to, String mergePath, Status[] mergeStatus, boolean force,
			ISVNProgressMonitor monitor) throws ClientWrapperException {
		// extended API is unsupported by native client 
		return null;
	}

	public Status[] mergeStatus(String url, Revision peg, Revision from, Revision to, String path, Revision lastMerged, boolean recurse,
			boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException {
		// extended API is unsupported by native client 
		return null;
	}

	public void mkdir(String[] path, String message, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.mkdir(path, message);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.move(srcPath, dstPath, null, force);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void move(String srcPath, String destPath, String message, Revision revision, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.move(srcPath, destPath, message, ConversionUtility.convert(revision), force);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void notification2(Notify2 notify) {
		if (this.installedNotify2 != null) {
			this.composite.remove(this.installedNotify2);
		}
		this.installedNotify2 = notify;
		if (this.installedNotify2 != null) {
			this.composite.add(this.installedNotify2);
		}
	}

	public void password(String password) {
		this.client.password(password == null ? "" : password);
	}

	public PropertyData[] properties(String path, Revision revision, Revision peg, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return ConversionUtility.convert(this.client.properties(path, ConversionUtility.convert(revision), ConversionUtility.convert(peg)));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public PropertyData propertyGet(String path, String name, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return ConversionUtility.convert(this.client.propertyGet(path, name));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void propertyRemove(String path, String name, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.propertyRemove(path, name, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void propertySet(String path, String name, byte[] value, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.propertySet(path, name, value, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void propertySet(String path, String name, String value, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.propertySet(path, name, value, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void resolved(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.resolved(path, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}
	
	public void relocate(String from, String to, String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.relocate(from, to, path, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void remove(String[] path, String message, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.remove(path, message, force);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void revert(String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			// FIXME replaced/prereplaced files cannot be reverted ???
			this.client.revert(path, recurse);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public void setClientSSLCertificate(String certPath, String passphrase) {
		this.sslCertificate = certPath == null || certPath.trim().length() == 0 ? null : certPath;
		this.sslPassphrase = passphrase;
	}

	public void setCommitMissingFiles(boolean commitMissingFiles) {
		this.commitMissingFiles = commitMissingFiles;
	}

	public void setCredentialsCacheEnabled(boolean cacheCredentials) {
		// credentials cache is always enabled for native client
	}

	public void setPrompt(ICredentialsPrompt prompt) {
		this.client.setPrompt(prompt == null ? null : new RepositoryInfoPrompt(this.prompt = prompt));
	}

	public ICredentialsPrompt getPrompt() {
		return this.prompt;
	}

	public void setProxy(String host, int port, String userName, String password) {
		// never works for native client

	}

	public void setReportRevisionChange(boolean report) {
		// native client never reports revision changes
	}

	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port) {
		// never works for native client

	}

	public void setSSHCredentials(String userName, String password, int port) {
		// never works for native client

	}

	public void setSSLCertificateCacheEnabled(boolean enabled) {
		// SSL certificate cache is always enabled for native client
	}

	public void setTouchUnresolved(boolean touchUnresolved) {
		// native client always merges conflicing resources
	}

	public Status[] status(String path, boolean descend, boolean onServer, boolean getAll, boolean noIgnore, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return ConversionUtility.convert(this.client.status(path, descend, onServer, getAll, noIgnore));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public Status[] status(String path, boolean descend, boolean onServer, boolean getAll, boolean noIgnore,
			boolean collectParentExternals, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			// FIXME collectParentExternals is not supported
			return ConversionUtility.convert(this.client.status(path, descend, onServer, getAll, noIgnore, ignoreExternals));
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void unlock(String[] path, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			this.client.unlock(path, force);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
	}

	public long[] update(String[] path, Revision revision, boolean recurse, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException {
		ProgressMonitorWrapper wrapper = new ProgressMonitorWrapper(monitor);
		try {
			this.composite.add(wrapper);
			wrapper.start();
			return this.client.update(path, ConversionUtility.convert(revision), recurse, ignoreExternals);
		}
		catch (ClientException ex) {
			this.handleClientException(ex);
		}
		finally {
			wrapper.interrupt();
			this.composite.remove(wrapper);
		}
		// unreachable code
		return null;
	}

	public void username(String username) {
		this.client.username(username == null ? "" : username);
	}

	protected void handleClientException(ClientException ex) throws ClientWrapperException {
		if (this.findConflict(ex)) {
			throw new ClientWrapperUnresolvedConflictException(ex.getMessage(), ex);
		}
		if (this.findCancel(ex)) {
			throw new ClientWrapperCancelException(ex.getMessage(), ex);
		}
		if (this.findAuthentication(ex)) {
			throw new ClientWrapperAuthenticationException(ex.getMessage(), ex);
		}
		throw new ClientWrapperException(ex.getMessage(), ex.getAprError(), ex, false);
	}
	
	protected boolean findConflict(ClientException t) {
    	return t.getAprError() == 160024;
	}
    
	protected boolean findAuthentication(ClientException t) {
    	return t.getAprError() == 170001;
	}
    
	protected boolean findCancel(ClientException t) {
    	return t.getAprError() == 200015;
	}
	
	protected boolean usePegSignature(String target1, Revision revision1, Revision peg1, String target2, Revision revision2, Revision peg2) {
		int kind1 = revision1.getKind();
		int kind2 = revision2.getKind();
		if ((kind1 == RevisionKind.base || kind1 == RevisionKind.working) && (kind2 == RevisionKind.base || kind2 == RevisionKind.working)) {
			return false;
		}
		return target1.equals(target2) && (peg1 == peg2 || (peg1 != null && peg1.equals(peg2)));
	}

	protected class ProgressMonitorWrapper extends Thread implements Notify2 {
		protected ISVNProgressMonitor monitor;
		protected int current;
		
		public ProgressMonitorWrapper(ISVNProgressMonitor monitor) {
			this.monitor = monitor;
			this.current = 0;
		}

		public void onNotify(NotifyInformation arg0) {
			this.monitor.progress(this.current++, ISVNProgressMonitor.TOTAL_UNKNOWN, new ISVNProgressMonitor.ItemState(arg0.path, arg0.action, arg0.kind, arg0.mimeType, arg0.contentState, arg0.propState, arg0.lockState, arg0.revision));
		}

		public void run() {
			try {
				while (!this.monitor.isActivityCancelled() && !this.isInterrupted()) {
					Thread.sleep(100);
				}
			}
			catch (InterruptedException ex) {

			}
			if (this.monitor.isActivityCancelled()) {
				try {SubversionNativeClientProxy.this.client.cancelOperation();} catch (Exception ex) {}
			}
		}
	}
    
	protected class RepositoryInfoPrompt implements PromptUserPassword3 {
		protected ICredentialsPrompt prompt;
		
		public RepositoryInfoPrompt(ICredentialsPrompt prompt) {
			this.prompt = prompt;
		}

	    public boolean prompt(String realm, String username) {
	        return this.prompt.prompt(null, realm);
		}
		
	    public boolean prompt(String realm, String username, boolean maySave) {
	        return this.prompt.prompt(null, realm);
		}

	    public int askTrustSSLServer(String info, boolean allowPermanently) {
	    	return this.prompt.askTrustSSLServer(null, info, allowPermanently);
		}

	    public String getUsername() {
	        return this.prompt.getUsername();
	    }
	    
	    public String getPassword() {
	        return this.prompt.getPassword();
	    }
	    
	    public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
	        return false;
	    }
	    
	    public String askQuestion(String realm, String question, boolean showAnswer, boolean maySave) {
	    	if (question.indexOf("certificate filename") != -1) {
	    		if (SubversionNativeClientProxy.this.sslCertificate != null) {
	    			String retVal = SubversionNativeClientProxy.this.sslCertificate;
	    			return retVal;
	    		}
	    		else if (!this.prompt.promptSSL(null, realm)) {
		    		return null;
		    	}
	    		else {
	    			SubversionNativeClientProxy.this.sslPassphrase = this.prompt.getSSLClientCertPassword();
	    			return this.prompt.getSSLClientCertPath();
	    		}
	    	}
	    	if (question.indexOf("certificate passphrase") != -1) {
	    		if (SubversionNativeClientProxy.this.sslPassphrase != null) {
	    			String retVal = SubversionNativeClientProxy.this.sslPassphrase;
	    			return retVal;
	    		}
	    		else if (!this.prompt.promptSSL(null, realm)) {
		    		return null;
		    	}
	    		else {
	    			return this.prompt.getSSLClientCertPassword();
	    		}
	    	}
	    	return null;
	    }
	    
	    public String askQuestion(String realm, String question, boolean showAnswer) {
	        return null;
	    }
	    
	    public boolean userAllowedSave() {
	    	return false;
	    }
	    
	}
	
}
