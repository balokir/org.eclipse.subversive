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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
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
public class GetRemoteContentsOperation extends AbstractActionOperation {
	protected IResource resource;
	protected IRepositoryResource remote;

	public GetRemoteContentsOperation(IResource resource, IRepositoryResource remote) {
		super("Operation.GetContent");
		this.resource = resource;
		this.remote = remote;
	}

	public ISchedulingRule getSchedulingRule() {
		return this.resource;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = this.remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String wcPath = FileUtility.getWorkingCopyPath(this.resource);
			if (this.remote instanceof IRepositoryFile) {
				FileOutputStream stream = null;
				try {
					String url = SVNUtility.encodeURL(this.remote.getUrl());
					this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn cat " + url + "@" + this.remote.getPegRevision() + " -r " + this.remote.getSelectedRevision() + FileUtility.getUsernameParam(location.getUsername()) + "\n");
					stream = new FileOutputStream(wcPath);
					proxy.streamFileContent(SVNUtility.getEntryRevisionReference(this.remote), 2048, stream, new SVNProgressMonitor(this, monitor, null));
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
				String url = SVNUtility.encodeURL(this.remote.getUrl());
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export " + url + "@" + this.remote.getPegRevision() + " -r " + this.remote.getSelectedRevision() + " \"" + wcPath + "\" --force " + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.doExport(SVNUtility.getEntryRevisionReference(this.remote), wcPath, ISVNConnector.Options.FORCE, Depth.INFINITY, null, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.resource.getName()});
	}

}
