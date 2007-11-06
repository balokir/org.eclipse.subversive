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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ChangePath;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.LogEntry;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Revision.Kind;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Locate resource URL that corresponds to revision
 * 
 * @author Alexander Gurov
 */
public class LocateResourceURLInHistoryOperation extends AbstractRepositoryOperation implements IRepositoryResourceProvider {
	protected IRepositoryResource []converted;
	protected boolean pegAsSelected;

	public LocateResourceURLInHistoryOperation(IRepositoryResource []resources, boolean pegAsSelected) {
		super("Operation.LocateURLInHistory", resources);
		this.pegAsSelected = pegAsSelected;
	}

	public LocateResourceURLInHistoryOperation(IRepositoryResourceProvider provider, boolean pegAsSelected) {
		super("Operation.LocateURLInHistory", provider);
		this.pegAsSelected = pegAsSelected;
	}

	public IRepositoryResource []getRepositoryResources() {
		return this.converted;
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		this.converted = new IRepositoryResource[resources.length];
		System.arraycopy(resources, 0, this.converted, 0, resources.length);
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final int idx = i;
			if (this.converted[i].getSelectedRevision().getKind() == Kind.NUMBER) {
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						IRepositoryResource result = LocateResourceURLInHistoryOperation.this.processEntry(LocateResourceURLInHistoryOperation.this.converted[idx], monitor);
						LocateResourceURLInHistoryOperation.this.converted[idx] = LocateResourceURLInHistoryOperation.this.converted[idx] == result ? SVNUtility.copyOf(result) : result;
						
						if (LocateResourceURLInHistoryOperation.this.pegAsSelected) {
							// when URL is corrected peg can be set to selected revision number
							LocateResourceURLInHistoryOperation.this.converted[idx].setPegRevision(LocateResourceURLInHistoryOperation.this.converted[idx].getSelectedRevision());
						}
					}
				}, monitor, resources.length);
			}
		}
	}

	protected IRepositoryResource processEntry(IRepositoryResource current, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = current.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			LogEntry []msgs = GetLogMessagesOperation.getMessagesImpl(proxy, current, Revision.fromNumber(0), current.getPegRevision(), ISVNClientWrapper.EMPTY_LOG_ENTRY_PROPS, 1, true, this, monitor);
			if (msgs != null && msgs.length > 0 && msgs[0] != null) {
				ChangePath []paths = msgs[0].changedPaths;
				if (paths == null) {
					return current;
				}
				String pattern = current.getUrl().substring(location.getRepositoryRoot().getUrl().length());
				int idx = -1;
				for (int i = 0; i < paths.length; i++) {
					if (pattern.startsWith(paths[i].path)) {
						if (paths[i].copiedFromPath != null) {
							idx = i;
						}
						break;
					}
				}
				if (idx == -1) {
					return current;
				}
				String copiedFrom = location.getRepositoryRoot().getUrl() + paths[idx].copiedFromPath + pattern.substring(paths[idx].path.length());
				
				long rev = paths[idx].copiedFromRevision;
				Revision searchRevision = current.getSelectedRevision();
				long searchRev = ((Revision.Number)searchRevision).getNumber();
				if (rev < searchRev) {
					return current;
				}
				
				Revision revison = Revision.fromNumber(rev);
				IRepositoryResource retVal = current instanceof IRepositoryFile ? (IRepositoryResource)location.asRepositoryFile(copiedFrom, false) : location.asRepositoryContainer(copiedFrom, false);
				retVal.setPegRevision(revison);
				retVal.setSelectedRevision(searchRevision);
				return this.processEntry(retVal, monitor);
			}
			return current;
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
