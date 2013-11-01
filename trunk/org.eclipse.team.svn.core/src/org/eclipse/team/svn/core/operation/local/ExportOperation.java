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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Export local resources
 * 
 * @author Alexander Gurov
 */
public class ExportOperation extends AbstractWorkingCopyOperation {
	protected SVNRevision revision;
	protected String path;
	protected boolean ignoreExternals;
	
	public ExportOperation(IResource[] resources, String path, SVNRevision revision, boolean ignoreExternals) {
		super("Operation_ExportRevision", SVNMessages.class, resources); //$NON-NLS-1$
		this.revision = revision;
		this.path = path;
		this.ignoreExternals = ignoreExternals;
	}

	public ExportOperation(IResourceProvider provider, String path, SVNRevision revision, boolean ignoreExternals) {
		super("Operation_ExportRevision", SVNMessages.class, provider); //$NON-NLS-1$
		this.revision = revision;
		this.path = path;
		this.ignoreExternals = ignoreExternals;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource current = resources[i];
			final IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(current);
			final ISVNConnector proxy = location.acquireSVNProxy();
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					String wcPath = FileUtility.getWorkingCopyPath(current);
					String targetPath = ExportOperation.this.path + "/" + current.getName(); //$NON-NLS-1$
					ExportOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export \"" + wcPath + "\" -r " + ExportOperation.this.revision.toString() + SVNUtility.getIgnoreExternalsArg(ExportOperation.this.ignoreExternals) + " \"" + FileUtility.normalizePath(targetPath) + "\" --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					long options = ISVNConnector.Options.FORCE;
					if (ExportOperation.this.ignoreExternals) {
						options |= ISVNConnector.Options.IGNORE_EXTERNALS;
					}
					proxy.exportTo(new SVNEntryRevisionReference(wcPath, null, ExportOperation.this.revision), targetPath, null, Depth.INFINITY, options, new SVNProgressMonitor(ExportOperation.this, monitor, null));
				}
			}, monitor, resources.length);
			
			location.releaseSVNProxy(proxy);
		}
	}

	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {FileUtility.getNamesListAsString(this.operableData())});
	}
	
}
