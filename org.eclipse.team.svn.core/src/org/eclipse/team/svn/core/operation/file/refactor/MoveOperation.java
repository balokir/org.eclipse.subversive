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

package org.eclipse.team.svn.core.operation.file.refactor;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Move resources between folders in WC
 * 
 * @author Alexander Gurov
 */
public class MoveOperation extends AbstractFileOperation {
	protected File localTo;
	protected boolean forceNonSVN;
	
	public MoveOperation(File []files, File localTo, boolean forceNonSVN) {
		super("Operation_MoveFile", files); //$NON-NLS-1$
		this.localTo = localTo;
		this.forceNonSVN = forceNonSVN;
	}

	public MoveOperation(IFileProvider provider, File localTo, boolean forceNonSVN) {
		super("Operation_MoveFile", provider); //$NON-NLS-1$
		this.localTo = localTo;
		this.forceNonSVN = forceNonSVN;
	}

	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule parentRule = super.getSchedulingRule();
		return MultiRule.combine(new LockingRule(this.localTo), parentRule);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		// allows moving of child and parent resources at the same time
		FileUtility.reorder(files, false);
		
		IRepositoryResource remoteTo = SVNFileStorage.instance().asRepositoryResource(this.localTo, true);
		IRepositoryLocation location = remoteTo == null ? null : remoteTo.getRepositoryLocation();
		final ISVNConnector proxy = location == null ? null : location.acquireSVNProxy();
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(current, true);
					File checked = MoveOperation.this.getRenameTo(current);
					if (remote == null) {
						MoveOperation.this.nonSVNMove(checked, current, monitor);
					}
					else if (proxy == null || MoveOperation.this.forceNonSVN) {
						MoveOperation.this.nonSVNCopy(current, monitor);
						ProgressMonitorUtility.doTaskExternal(new DeleteOperation(new File[] {current}), monitor);
					}
					else {
						proxy.move(new String[] {current.getAbsolutePath()}, checked.getAbsolutePath(), ISVNConnector.Options.FORCE, new SVNProgressMonitor(MoveOperation.this, monitor, null));
					}
				}
			}, monitor, files.length);
		}
		
		if (location != null) {
			location.releaseSVNProxy(proxy);
		}
	}

	protected File getRenameTo(File what) {
		File checked = new File(this.localTo.getAbsolutePath() + "/" + what.getName()); //$NON-NLS-1$
		if (checked.exists()) {
			String message = this.getNationalizedString("Error_AlreadyExists"); //$NON-NLS-1$
			throw new UnreportableException(MessageFormat.format(message, new Object[] {checked.getAbsolutePath()}));
		}
		return checked;
	}
	
	protected void nonSVNMove(File renameTo, File what, IProgressMonitor monitor) throws Exception {
		if (!what.renameTo(renameTo)) {
			this.nonSVNCopy(what, monitor);
			FileUtility.deleteRecursive(what, monitor);
		}
	}

	protected void nonSVNCopy(File what, IProgressMonitor monitor) throws Exception {
		FileUtility.copyAll(this.localTo, what, FileUtility.COPY_NO_OPTIONS, new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.getName().equals(SVNUtility.getSVNFolderName());
			}
		}, monitor);
	}
	
}
