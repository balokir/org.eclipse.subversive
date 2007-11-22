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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
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
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			SVNLogEntry []msgs;
			int index = 0;
			if (current.exists()) {
				msgs = GetLogMessagesOperation.getMessagesImpl(proxy, current, SVNRevision.fromNumber(0), current.getPegRevision(), ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, true, this, monitor);
			}
			else {
				msgs = GetLogMessagesOperation.getMessagesImpl(proxy, current.getParent(), SVNRevision.fromNumber(0), current.getPegRevision(), ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, true, this, monitor);
				if (msgs != null) {
					for (int j = 0; j < msgs.length; j++) {
						SVNLogPath []paths = msgs[j].changedPaths;
						if (paths != null) {
							for (int k = 0; k < paths.length; k++) {
								if (paths[k] != null && current.getUrl().endsWith(paths[k].path)) {
									index = j;
									break;
								}
							}
						}
					}
				}
			}
			if (msgs != null && msgs.length > index && msgs[index] != null) {
				SVNLogPath []paths = msgs[index].changedPaths;
				if (paths == null) {
					return current;
				}
				String pattern = current.getUrl().substring(location.getRepositoryRootUrl().length());
				int idx = -1;
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].action != 'D' && pattern.startsWith(paths[i].path)) {
						if (paths[i].copiedFromPath != null) {
							idx = i;
						}
						break;
					}
				}
				if (idx == -1) {
					return current;
				}
				String copiedFrom = location.getRepositoryRootUrl() + paths[idx].copiedFromPath + pattern.substring(paths[idx].path.length());
				
				long rev = paths[idx].copiedFromRevision;
				SVNRevision searchRevision = current.getSelectedRevision();
				long searchRev = ((SVNRevision.Number)searchRevision).getNumber();
				if (rev < searchRev) {
					return current;
				}
				
				SVNRevision revison = SVNRevision.fromNumber(rev);
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
