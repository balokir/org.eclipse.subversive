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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNMergeStatusCallback;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge status operation implementation
 * 
 * @author Alexander Gurov
 */
public class MergeStatusOperation extends AbstractWorkingCopyOperation implements IRemoteStatusOperation {
	protected MergeSet info;
	protected SVNMergeStatus []retVal;
	
	public MergeStatusOperation(MergeSet info, IResource []resources) {
		super("Operation.MergeStatus", resources == null ? info.to : resources);
		this.info = info;
	}
	
	public IResource []getScope() {
		return this.info.to;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
		final ArrayList st = new ArrayList();
		
		HashSet resources = new HashSet(Arrays.asList(this.operableData()));
		
		for (int i = 0; i < this.info.to.length && !monitor.isCanceled(); i++) {
			if (resources.contains(this.info.to[i])) {
				final IRepositoryResource from = this.info.from[i];
				final ISVNConnector proxy = from.getRepositoryLocation().acquireSVNProxy();
				final String wcPath = FileUtility.getWorkingCopyPath(this.info.to[i]);
				
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						proxy.mergeStatus(SVNUtility.getEntryReference(from), new SVNRevisionRange [] {new SVNRevisionRange(MergeStatusOperation.this.info.start, from.getSelectedRevision())}, 
						    	wcPath, Depth.INFINITY, ISVNConnector.Options.FORCE, new ISVNMergeStatusCallback() {
									public void next(SVNMergeStatus status) {
										st.add(status);
									}
								}, 
								new SVNProgressMonitor(MergeStatusOperation.this, monitor, null));
					}
				}, monitor, this.info.to.length);
				
				from.getRepositoryLocation().releaseSVNProxy(proxy);
			}
		}
		this.info.addStatuses(retVal = (SVNMergeStatus [])st.toArray(new SVNMergeStatus[st.size()]));
    }

	public SVNEntryStatus[]getStatuses() {
		return this.retVal;
	}

    public void setPegRevision(IResourceChange change) {
        
    }
    
}
