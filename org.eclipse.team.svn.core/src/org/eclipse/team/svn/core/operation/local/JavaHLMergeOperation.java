/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * JavaHL-mode merge implementation
 * 
 * @author Alexander Gurov
 */
public class JavaHLMergeOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryResourceProvider fromStart;
	protected IRepositoryResourceProvider fromEnd;
	protected boolean dryRun;
	protected boolean ignoreAncestry;
	
	protected ISVNProgressMonitor externalMonitor;
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResource []fromStart, IRepositoryResource []fromEnd, boolean dryRun, boolean ignoreAncestry) {
		this(localTo, new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(fromStart), new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(fromEnd), dryRun, ignoreAncestry);
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider fromStart, IRepositoryResourceProvider fromEnd, boolean dryRun, boolean ignoreAncestry) {
		super("Operation.JavaHLMerge", localTo);
		this.fromStart = fromStart;
		this.fromEnd = fromEnd;
		this.dryRun = dryRun;
		this.ignoreAncestry = ignoreAncestry;
	}
	
	public int getOperationWeight() {
		return 19;
	}
	
	public void setExternalMonitor(ISVNProgressMonitor externalMonitor) {
		this.externalMonitor = externalMonitor;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		IRepositoryResource []fromStart = this.fromStart.getRepositoryResources();
		IRepositoryResource []fromEnd = this.fromEnd.getRepositoryResources();

		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource resource = resources[i];
			final IRepositoryResource from1 = fromStart[i];
			final IRepositoryResource from2 = fromEnd[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					JavaHLMergeOperation.this.doMerge(resource, from1, from2, monitor);
				}
			}, monitor, resources.length);
		}
	}
	
	protected void doMerge(IResource resource, IRepositoryResource from1, IRepositoryResource from2, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = from1.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		proxy.setTouchUnresolved(true);
		try {
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			SVNEntryRevisionReference ref1 = SVNUtility.getEntryRevisionReference(from1);
			SVNEntryRevisionReference ref2 = SVNUtility.getEntryRevisionReference(from2);
			long options = this.ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE;
			options |= this.dryRun ? ISVNConnector.Options.SIMULATE : ISVNConnector.Options.NONE;
			if (SVNUtility.useSingleReferenceSignature(ref1, ref2)) {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge -r " + from1.getSelectedRevision() + ":" + from2.getSelectedRevision() + "\"" + from1.getUrl() + "@" + from1.getPegRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.dryRun ? " --dry-run" : "") + (this.ignoreAncestry ? " --ignore-ancestry" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.merge(ref1, new SVNRevisionRange[] {new SVNRevisionRange(from1.getSelectedRevision(), from2.getSelectedRevision())}, wcPath, Depth.INFINITY, options, new MergeProgressMonitor(this, monitor, null));
			}
			else {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge \"" + from1.getUrl() + "@" + from1.getSelectedRevision() + "\" \"" + from2.getUrl() + "@" + from2.getSelectedRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.dryRun ? " --dry-run" : "") + (this.ignoreAncestry ? " --ignore-ancestry" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.merge(ref1, ref2, wcPath, Depth.INFINITY, options, new MergeProgressMonitor(this, monitor, null));
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
