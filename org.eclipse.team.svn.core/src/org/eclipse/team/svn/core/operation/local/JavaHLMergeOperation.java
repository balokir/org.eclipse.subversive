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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.ISVNProgressMonitor;
import org.eclipse.team.svn.core.client.RevisionRange;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * JavaHL-mode merge implementation
 * 
 * @author Alexander Gurov
 */
public class JavaHLMergeOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryResource []from1;
	protected IRepositoryResource []from2;
	protected boolean dryRun;
	protected boolean ignoreAncestry;
	
	protected ISVNProgressMonitor externalMonitor;
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResource []from1, IRepositoryResource []from2, boolean dryRun) {
		this(localTo, from1, from2, dryRun, false);
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResource []from1, IRepositoryResource []from2, boolean dryRun, boolean ignoreAncestry) {
		super("Operation.JavaHLMerge", localTo);
		this.from1 = from1;
		this.from2 = from2;
		this.dryRun = dryRun;
		this.ignoreAncestry = ignoreAncestry;
	}
	
	public void setExternalMonitor(ISVNProgressMonitor externalMonitor) {
		this.externalMonitor = externalMonitor;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource resource = resources[i];
			final IRepositoryResource from1 = this.from1[i];
			final IRepositoryResource from2 = this.from2[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					JavaHLMergeOperation.this.doMerge(resource, from1, from2, monitor);
				}
			}, monitor, resources.length);
		}
	}
	
	protected void doMerge(IResource resource, IRepositoryResource from1, IRepositoryResource from2, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = from1.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		
		proxy.setTouchUnresolved(true);
		try {
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			if (from1.getUrl().equals(from2.getUrl())) {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge -r " + from1.getSelectedRevision() + ":" + from2.getSelectedRevision() + "\"" + from1.getUrl() + "@" + from1.getPegRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.dryRun ? " --dry-run" : "") + (this.ignoreAncestry ? " --ignore-ancestry" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.merge(SVNUtility.getEntryReference(from1), new RevisionRange[] {new RevisionRange(from1.getSelectedRevision(), from2.getSelectedRevision())}, wcPath, false, Depth.INFINITY, this.ignoreAncestry, this.dryRun, new MergeProgressMonitor(this, monitor, null));
			}
			else {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge \"" + from1.getUrl() + "@" + from1.getSelectedRevision() + "\" \"" + from2.getUrl() + "@" + from2.getSelectedRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.dryRun ? " --dry-run" : "") + (this.ignoreAncestry ? " --ignore-ancestry" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.merge(SVNUtility.getEntryReference(from1), SVNUtility.getEntryReference(from2), wcPath, false, Depth.INFINITY, this.ignoreAncestry, this.dryRun, new MergeProgressMonitor(this, monitor, null));
			}
		}
		finally {
			proxy.setTouchUnresolved(false);
			location.releaseSVNProxy(proxy);
		}
	}

	protected class MergeProgressMonitor extends SVNProgressMonitor {
		public MergeProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root, !JavaHLMergeOperation.this.dryRun);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
			if (JavaHLMergeOperation.this.externalMonitor != null) {
				JavaHLMergeOperation.this.externalMonitor.progress(current, total, state);
			}
		}
	}
	
}
