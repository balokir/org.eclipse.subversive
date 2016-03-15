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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
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
	protected IRepositoryResourceProvider from;
	protected IRepositoryResourceProvider fromEnd;
	protected SVNRevisionRange []revisions;
	protected SVNDepth depth;
	protected long options;
	
	protected ISVNProgressMonitor externalMonitor;
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider from, SVNRevisionRange []revisions, boolean dryRun, boolean ignoreAncestry, SVNDepth depth) {
		this(localTo, from, revisions, depth, 
			(dryRun ? ISVNConnector.Options.SIMULATE : ISVNConnector.Options.NONE) | 
			(ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE));
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider fromStart, IRepositoryResourceProvider fromEnd, boolean dryRun, boolean ignoreAncestry, SVNDepth depth) {
		this(localTo, fromStart, fromEnd, depth, 
			(dryRun ? ISVNConnector.Options.SIMULATE : ISVNConnector.Options.NONE) | 
			(ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE));
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider from, boolean dryRun) {
		this(localTo, from, ISVNConnector.Options.SIMULATE);
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider from, SVNRevisionRange []revisions, SVNDepth depth, long options) {
		super("Operation_JavaHLMerge", SVNMessages.class, localTo); //$NON-NLS-1$
		this.from = from;
		this.revisions = revisions;
		this.options = options & ISVNConnector.CommandMasks.MERGE;
		this.depth = depth;
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider fromStart, IRepositoryResourceProvider fromEnd, SVNDepth depth, long options) {
		super("Operation_JavaHLMerge", SVNMessages.class, localTo); //$NON-NLS-1$
		this.from = fromStart;
		this.fromEnd = fromEnd;
		this.options = options & ISVNConnector.CommandMasks.MERGE;
		this.depth = depth;
	}
	
	public JavaHLMergeOperation(IResource []localTo, IRepositoryResourceProvider from, long options) {
		super("Operation_JavaHLMerge", SVNMessages.class, localTo); //$NON-NLS-1$
		this.from = from;
		this.options = options & ISVNConnector.CommandMasks.MERGE;
	}
	
	public void setRecordOnly(boolean recordOnly) {
		this.options &= ~ISVNConnector.Options.RECORD_ONLY;
		this.options |= recordOnly ? ISVNConnector.Options.RECORD_ONLY : ISVNConnector.Options.NONE;
	}
	
	public int getOperationWeight() {
		return 19;
	}
	
	public void setExternalMonitor(ISVNProgressMonitor externalMonitor) {
		this.externalMonitor = externalMonitor;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		IRepositoryResource []fromStart = this.from.getRepositoryResources();
		IRepositoryResource []fromEnd = this.fromEnd != null ? this.fromEnd.getRepositoryResources() : null;

		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource resource = resources[i];
			final IRepositoryResource from1 = fromStart[i];
			final IRepositoryResource from2 = fromEnd == null ? null : fromEnd[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					if (from2 != null) {
						JavaHLMergeOperation.this.doMerge2URL(resource, from1, from2, monitor);
					}
					else if (JavaHLMergeOperation.this.revisions != null) {
						JavaHLMergeOperation.this.doMerge1URL(resource, from1, monitor);
					}
					else {
						JavaHLMergeOperation.this.doMergeReintegrate(resource, from1, monitor);
					}
				}
			}, monitor, resources.length);
		}
	}
	
	protected void doMerge1URL(IResource resource, IRepositoryResource from, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = from.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		try {
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			SVNEntryReference ref1 = SVNUtility.getEntryReference(from);
			String changes = ""; //$NON-NLS-1$
			String ranges = ""; //$NON-NLS-1$
			for (SVNRevisionRange range : this.revisions) {
				if (range.from.equals(range.to)) {
					changes += changes.length() > 0 ? ("," + range.from.toString()) : range.from.toString(); //$NON-NLS-1$
				}
				else {
					ranges += " -r " + range.from.toString() + ":" + range.to.toString(); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (changes.length() > 0) {
				changes = " -c " + changes; //$NON-NLS-1$
			}
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge" + changes + ranges + " \"" + from.getUrl() + "@" + from.getPegRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + SVNUtility.getDepthArg(this.depth, ISVNConnector.Options.NONE) + ISVNConnector.Options.asCommandLine(this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			proxy.merge(ref1, this.revisions, wcPath, this.depth, this.options, new MergeProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected void doMerge2URL(IResource resource, IRepositoryResource from1, IRepositoryResource from2, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = from1.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		try {
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			SVNEntryRevisionReference ref1 = SVNUtility.getEntryRevisionReference(from1);
			SVNEntryRevisionReference ref2 = SVNUtility.getEntryRevisionReference(from2);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge \"" + from1.getUrl() + "@" + from1.getSelectedRevision() + "\" \"" + from2.getUrl() + "@" + from2.getSelectedRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + SVNUtility.getDepthArg(this.depth, ISVNConnector.Options.NONE) + ISVNConnector.Options.asCommandLine(this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			proxy.mergeTwo(ref1, ref2, wcPath, this.depth, this.options, new MergeProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected void doMergeReintegrate(IResource resource, IRepositoryResource from1, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = from1.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		try {
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			SVNEntryReference ref1 = SVNUtility.getEntryReference(from1);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn merge --reintegrate \"" + from1.getUrl() + "@" + from1.getPegRevision() + "\" \"" + FileUtility.normalizePath(wcPath) + "\"" + ISVNConnector.Options.asCommandLine(this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			proxy.mergeReintegrate(ref1, wcPath, this.options, new MergeProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected class MergeProgressMonitor extends SVNProgressMonitor {
		public MergeProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root, (JavaHLMergeOperation.this.options & ISVNConnector.Options.SIMULATE) == 0);
		}
		
		public void progress(int current, int total, ItemState state) {
			super.progress(current, total, state);
			if (JavaHLMergeOperation.this.externalMonitor != null) {
				JavaHLMergeOperation.this.externalMonitor.progress(current, total, state);
			}
		}
	}
	
}
