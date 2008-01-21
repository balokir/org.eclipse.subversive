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

package org.eclipse.team.svn.core.operation.file.refactor;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Copy files between working copies
 * 
 * @author Alexander Gurov
 */
public class CopyOperation extends AbstractFileOperation {
	protected File localTo;
	protected boolean forceNonSVN;
	
	public CopyOperation(File []files, File localTo, boolean forceNonSVN) {
		super("Operation.CopyFile", files);
		this.localTo = localTo;
		this.forceNonSVN = forceNonSVN;
	}

	public CopyOperation(IFileProvider provider, File localTo, boolean forceNonSVN) {
		super("Operation.CopyFile", provider);
		this.localTo = localTo;
		this.forceNonSVN = forceNonSVN;
	}

	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule parentRule = super.getSchedulingRule();
		return MultiRule.combine(new LockingRule(this.localTo), parentRule);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		IRepositoryResource remoteTo = SVNFileStorage.instance().asRepositoryResource(this.localTo, true);
		IRepositoryLocation location = remoteTo == null ? null : remoteTo.getRepositoryLocation();
		final ISVNConnector proxy = location == null ? null : location.acquireSVNProxy();
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(current, true);
					File checked = CopyOperation.this.getCopyTo(current);
					if (remote == null || proxy == null || CopyOperation.this.forceNonSVN) {
						CopyOperation.this.nonSVNCopy(current, monitor);
					}
					else {
						proxy.copy(new String[] {current.getAbsolutePath()}, checked.getAbsolutePath(), SVNRevision.WORKING, new SVNProgressMonitor(CopyOperation.this, monitor, null));
					}
				}
			}, monitor, files.length);
		}
		
		if (location != null) {
			location.releaseSVNProxy(proxy);
		}
	}

	protected File getCopyTo(File what) {
		File checked = new File(this.localTo.getAbsolutePath() + "/" + what.getName());
		if (checked.exists()) {
			String message = this.getNationalizedString("Error.AlreadyExists");
			throw new UnreportableException(MessageFormat.format(message, new Object[] {checked.getAbsolutePath()}));
		}
		return checked;
	}
	
	protected void nonSVNCopy(File what, IProgressMonitor monitor) throws Exception {
		FileUtility.copyAll(this.localTo, what, FileUtility.COPY_NO_OPTIONS, new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.getName().equals(SVNUtility.getSVNFolderName());
			}
		}, monitor);
	}
	
}
