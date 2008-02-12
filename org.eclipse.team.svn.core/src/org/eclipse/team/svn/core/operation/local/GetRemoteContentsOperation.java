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

package org.eclipse.team.svn.core.operation.local;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Get remote resource contents without working copy modification
 * 
 * @author Alexander Gurov
 */
public class GetRemoteContentsOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryResource []remoteResources;

	public GetRemoteContentsOperation(IResource []resources, IRepositoryResource []remoteResources) {
		super("Operation.GetContent", resources);
		this.remoteResources = remoteResources;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource local = resources[i];
			final IRepositoryResource remote = this.remoteResources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					GetRemoteContentsOperation.this.doGet(local, remote, monitor);
				}
			}, monitor, resources.length);
		}
	}
	
	protected void doGet(IResource local, IRepositoryResource remote, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String wcPath = FileUtility.getWorkingCopyPath(local);
			String url = SVNUtility.encodeURL(remote.getUrl());
			if (remote instanceof IRepositoryFile) {
				FileOutputStream stream = null;
				try {
					this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn cat " + url + "@" + remote.getPegRevision() + " -r " + remote.getSelectedRevision() + FileUtility.getUsernameParam(location.getUsername()) + "\n");
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
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export " + url + "@" + remote.getPegRevision() + " -r " + remote.getSelectedRevision() + " \"" + wcPath + "\" --force " + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.doExport(SVNUtility.getEntryRevisionReference(remote), wcPath, null, Depth.INFINITY, ISVNConnector.Options.FORCE, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {FileUtility.getNamesListAsString(this.operableData())});
	}

}
