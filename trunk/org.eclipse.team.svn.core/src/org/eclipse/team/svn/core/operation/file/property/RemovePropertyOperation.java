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

package org.eclipse.team.svn.core.operation.file.property;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Remove resource property operation
 * 
 * @author Alexander Gurov
 */
public class RemovePropertyOperation extends AbstractFileOperation {
	protected boolean isRecursive;
	protected String []names;

	public RemovePropertyOperation(File []files, String []names, boolean isRecursive) {
		super("Operation.RemovePropertiesFile", files);
		this.names = names;
		this.isRecursive = isRecursive;
	}

	public RemovePropertyOperation(IFileProvider provider, String []names, boolean isRecursive) {
		super("Operation.RemovePropertiesFile", provider);
		this.names = names;
		this.isRecursive = isRecursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		if (this.isRecursive) {
			files = FileUtility.shrinkChildNodes(files, false);
		}
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(files[i], false);
			IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					for (int i = 0; i < RemovePropertyOperation.this.names.length && !monitor.isCanceled(); i++) {
					    final String name = RemovePropertyOperation.this.names[i];
					    RemovePropertyOperation.this.protectStep(new IUnprotectedOperation() {
			                public void run(IProgressMonitor monitor) throws Exception {
			        			proxy.propertyRemove(current.getAbsolutePath(), name, RemovePropertyOperation.this.isRecursive, new SVNProgressMonitor(RemovePropertyOperation.this, monitor, null));
			                }
			            }, monitor, RemovePropertyOperation.this.names.length);
					}
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

	protected ISchedulingRule getSchedulingRule(File file) {
		return file.isDirectory() ? new LockingRule(file) : super.getSchedulingRule(file);
	}
	
}
