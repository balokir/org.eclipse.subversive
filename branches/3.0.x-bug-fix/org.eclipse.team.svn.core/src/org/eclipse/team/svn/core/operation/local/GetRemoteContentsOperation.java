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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Get remote resource contents without working copy modification
 * 
 * @author Alexander Gurov
 */
public class GetRemoteContentsOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryResourceProvider provider;
	protected HashMap<String, String> remotePath2localPath;
	protected long options;
	
	public GetRemoteContentsOperation(IResource [] resources, final IRepositoryResource []remoteResources, HashMap<String, String> remotePath2localPath, boolean ignoreExternals) {
		this (resources, new IRepositoryResourceProvider() {
			public IRepositoryResource[] getRepositoryResources() {
				return remoteResources;
			}
		}, remotePath2localPath, ignoreExternals);
	}
	
	public GetRemoteContentsOperation(IResource [] resources, IRepositoryResourceProvider provider, HashMap<String, String> remotePath2localPath, boolean ignoreExternals) {
		this(resources, provider, remotePath2localPath, ISVNConnector.Options.FORCE | (ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE));
	}

	public GetRemoteContentsOperation(IResource [] resources, IRepositoryResourceProvider provider, HashMap<String, String> remotePath2localPath, long options) {
		super("Operation_GetContent", SVNMessages.class, resources); //$NON-NLS-1$
		this.provider = provider;
		this.remotePath2localPath = remotePath2localPath;
		this.options = options & ISVNConnector.CommandMasks.EXPORT;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource [] remoteResources = this.provider.getRepositoryResources();
		for (int i = 0; i < remoteResources.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource remote = remoteResources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					GetRemoteContentsOperation.this.doGet(remote, monitor);
				}
			}, monitor, remoteResources.length);
		}
	}
	
	protected void doGet(IRepositoryResource remote, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String url = SVNUtility.encodeURL(remote.getUrl());
			String wcPath = this.remotePath2localPath.get(url);
			if (remote instanceof IRepositoryFile) {
				File parent = new File(wcPath.substring(0, wcPath.lastIndexOf("/"))); //$NON-NLS-1$
				if (!parent.exists()) {
					parent.mkdirs();
				}
				File file = new File(wcPath);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream stream = null;
				try {
					this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn cat " + url + "@" + remote.getPegRevision() + " -r " + remote.getSelectedRevision() + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					stream = new FileOutputStream(wcPath);
					proxy.streamFileContent(SVNUtility.getEntryRevisionReference(remote), 2048, stream, new SVNProgressMonitor(this, monitor, null));
				}
				catch (FileNotFoundException e) {
					//skip read-only files
				}
				finally {
					if (stream != null) {
						try {stream.close();} catch (Exception ex) {}
					}
				}
			}
			else {
				File directory = new File(wcPath);
				if (!directory.exists()) {
					directory.mkdirs();
				}
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export " + url + "@" + remote.getPegRevision() + " -r " + remote.getSelectedRevision() + ISVNConnector.Options.asCommandLine(this.options) + " \"" + wcPath + "\" " + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				proxy.exportTo(SVNUtility.getEntryRevisionReference(remote), wcPath, null, SVNDepth.INFINITY, this.options, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {FileUtility.getNamesListAsString(this.provider.getRepositoryResources())});
	}

}
