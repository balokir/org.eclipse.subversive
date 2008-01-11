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

package org.eclipse.team.svn.ui.operation;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.compare.TwoWayResourceCompareInput;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Two way compare for repository resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class CompareRepositoryResourcesOperation extends AbstractActionOperation {
	protected IRepositoryResource next;
	protected IRepositoryResource prev;
	protected IRepositoryResourceProvider provider;

	public CompareRepositoryResourcesOperation(IRepositoryResource prev, IRepositoryResource next) {
		super("Operation.CompareRepository");
		this.prev = prev;
		this.next = next;
	}
	
	public CompareRepositoryResourcesOperation(IRepositoryResourceProvider provider) {
		this(null, null);
		this.provider = provider;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (this.provider != null) {
			IRepositoryResource []toCompare = this.provider.getRepositoryResources(); 
			this.prev = toCompare[0];
			this.next = toCompare[1];
		}
		
		LocateResourceURLInHistoryOperation op = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {this.next, this.prev}, true);
		ProgressMonitorUtility.doTaskExternal(op, monitor);
		this.next = op.getRepositoryResources()[0];
		this.prev = op.getRepositoryResources()[1];
		
		IRepositoryLocation location = this.prev.getRepositoryLocation();
		final ISVNConnector proxy = location.acquireSVNProxy();
		final ArrayList<SVNDiffStatus> statuses = new ArrayList<SVNDiffStatus>();
		
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				SVNEntryRevisionReference refPrev = SVNUtility.getEntryRevisionReference(CompareRepositoryResourcesOperation.this.prev);
				SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(CompareRepositoryResourcesOperation.this.next);
				if (SVNUtility.useSingleReferenceSignature(refPrev, refNext)) {
					SVNUtility.diffStatus(proxy, statuses, refPrev, refPrev.revision, refNext.revision, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareRepositoryResourcesOperation.this, monitor, null, false));
				}
				else {
					SVNUtility.diffStatus(proxy, statuses, refPrev, refNext, Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(CompareRepositoryResourcesOperation.this, monitor, null, false));
				}
			}
		}, monitor, 2);
		
		location.releaseSVNProxy(proxy);
		
		if (!monitor.isCanceled()) {
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					CompareConfiguration cc = new CompareConfiguration();
					cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, Boolean.FALSE);
					final TwoWayResourceCompareInput compare = new TwoWayResourceCompareInput(cc, CompareRepositoryResourcesOperation.this.next, CompareRepositoryResourcesOperation.this.prev, statuses);
					compare.initialize(monitor);
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							CompareUI.openCompareEditor(compare);
						}
					});
				}
			}, monitor, 2);
		}
	}
	
    protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.next.getName(), this.prev.getName()});
	}

}
