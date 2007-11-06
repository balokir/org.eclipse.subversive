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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.IStatusCallback;
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
	protected Status []statuses;
	protected boolean recursive;

	public AbstractStatusOperation(String operationName, File []files, boolean recursive) {
		super(operationName, files);
		this.recursive = recursive;
	}

	public AbstractStatusOperation(String operationName, IFileProvider provider, boolean recursive) {
		super(operationName, provider);
		this.recursive = recursive;
	}

	public Status []getStatuses() {
		return this.statuses;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		final List result = new ArrayList();
		IStatusCallback cb = new IStatusCallback() {
			public void nextStatus(Status status) {
				result.add(status);
			}
		};
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(files[i], false);
			
			IRepositoryLocation location = remote.getRepositoryLocation();
			ISVNClientWrapper proxy = location.acquireSVNProxy();

			this.reportStatuses(proxy, cb, files[i], monitor, files.length);
			
			location.releaseSVNProxy(proxy);
		}
		this.statuses = (Status [])result.toArray(new Status[result.size()]);
	}

	protected void reportStatuses(final ISVNClientWrapper proxy, final IStatusCallback cb, final File current, IProgressMonitor monitor, int tasks) {
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				proxy.status(
						current.getAbsolutePath(), 
						Depth.infinityOrImmediates(AbstractStatusOperation.this.recursive), AbstractStatusOperation.this.isRemote(), false, false, false, cb, 
						new SVNProgressMonitor(AbstractStatusOperation.this, monitor, null, false));
			}
		}, monitor, tasks);
	}
	
	protected abstract boolean isRemote();
	
}
