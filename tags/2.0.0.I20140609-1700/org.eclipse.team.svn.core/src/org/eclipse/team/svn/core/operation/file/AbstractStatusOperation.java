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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNEntryInfoCallback;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Abstract status reporting operation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractStatusOperation extends AbstractFileOperation {
	protected SVNChangeStatus []statuses;
	protected boolean recursive;

	public AbstractStatusOperation(String operationName, Class<? extends NLS> messagesClass, File []files, boolean recursive) {
		super(operationName, messagesClass, files);
		this.recursive = recursive;
	}

	public AbstractStatusOperation(String operationName, Class<? extends NLS> messagesClass, IFileProvider provider, boolean recursive) {
		super(operationName, messagesClass, provider);
		this.recursive = recursive;
	}

	public SVNChangeStatus []getStatuses() {
		return this.statuses;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		final List<SVNChangeStatus> result = new ArrayList<SVNChangeStatus>();
		final List<SVNChangeStatus> conflicts = new ArrayList<SVNChangeStatus>();
		
		ISVNEntryStatusCallback cb = new ISVNEntryStatusCallback() {
			public void next(SVNChangeStatus status) {
				result.add(status);
				if (status.hasConflict && status.treeConflicts == null) {
					conflicts.add(status);
				}
			}
		};
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(files[i], false);
			
			IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();

			conflicts.clear();
			
			this.reportStatuses(proxy, cb, files[i], monitor, files.length);
			
			for (Iterator<SVNChangeStatus> it = conflicts.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				final SVNChangeStatus svnChangeStatus = it.next();
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						proxy.getInfo(new SVNEntryRevisionReference(svnChangeStatus.path), SVNDepth.EMPTY, null, new ISVNEntryInfoCallback() {
							public void next(SVNEntryInfo info) {
								svnChangeStatus.treeConflicts = info.treeConflicts;
							}
						}, new SVNProgressMonitor(AbstractStatusOperation.this, monitor, null, false));
					}
				}, monitor, files.length);
			}
			
			location.releaseSVNProxy(proxy);
		}
		this.statuses = result.toArray(new SVNChangeStatus[result.size()]);
	}

	protected void reportStatuses(final ISVNConnector proxy, final ISVNEntryStatusCallback cb, final File current, IProgressMonitor monitor, int tasks) {
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				proxy.status(
						current.getAbsolutePath(), 
						SVNDepth.infinityOrImmediates(AbstractStatusOperation.this.recursive), AbstractStatusOperation.this.isRemote() ? ISVNConnector.Options.SERVER_SIDE : ISVNConnector.Options.NONE, null, cb, 
						new SVNProgressMonitor(AbstractStatusOperation.this, monitor, null, false));
			}
		}, monitor, tasks);
	}
	
	protected abstract boolean isRemote();
	
}
