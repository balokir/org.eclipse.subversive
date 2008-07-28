/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Operation organize export local folder to repository
 * 
 * @author Sergiy Logvin
 */
public class ExportOperation extends AbstractRepositoryOperation {
	protected String path;
	protected int depth;
	
	public ExportOperation(IRepositoryResource []resources, String path, int depth) {
		super("Operation.ExportRevision", resources);
		this.path = path;
		this.depth = depth;
	}
	
	public ExportOperation(IRepositoryResourceProvider provider, String path, int depth) {
		super("Operation.ExportRevision", provider);
		this.path = path;
		this.depth = depth;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			IRepositoryLocation location = resources[i].getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			final String path = this.path + "/" + resources[i].getName();
			final SVNEntryRevisionReference entryRef = SVNUtility.getEntryRevisionReference(resources[i]);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export \"" + resources[i].getUrl() + "@" + resources[i].getPegRevision() + "\" -r " + resources[i].getSelectedRevision() + " \"" + FileUtility.normalizePath(path) + "\"" + SVNUtility.getDepthArg(ExportOperation.this.depth) + " --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.doExport(entryRef, path, null, ExportOperation.this.depth, ISVNConnector.Options.FORCE, new SVNProgressMonitor(ExportOperation.this, monitor, null));
				}
			}, monitor, resources.length);
			
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getUrl()});
	}
	
}
