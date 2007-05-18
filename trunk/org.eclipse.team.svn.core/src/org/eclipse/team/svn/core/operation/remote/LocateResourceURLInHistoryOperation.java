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
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.RevisionKind;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;

/**
 * Locate resource URL that corresponds to revision
 * 
 * @author Alexander Gurov
 */
public class LocateResourceURLInHistoryOperation extends AbstractRepositoryOperation implements IRepositoryResourceProvider {
	protected IRepositoryResource []converted;

	public LocateResourceURLInHistoryOperation(IRepositoryResource []resources) {
		super("Operation.LocateURLInHistory", resources);
	}

	public LocateResourceURLInHistoryOperation(IRepositoryResourceProvider provider) {
		super("Operation.LocateURLInHistory", provider);
	}

	public IRepositoryResource []getRepositoryResources() {
		return this.converted;
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		this.converted = new IRepositoryResource[resources.length];
		System.arraycopy(resources, 0, this.converted, 0, resources.length);
		
		for (int i = 0; i < resources.length; i++) {
			final int idx = i;
			if (this.converted[i].getSelectedRevision().getKind() == RevisionKind.number) {
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						LocateResourceURLInHistoryOperation.this.converted[idx] = 
							LocateResourceURLInHistoryOperation.this.processEntry(LocateResourceURLInHistoryOperation.this.converted[idx], monitor);
					}
				}, monitor, resources.length);
			}
		}
	}

	protected IRepositoryResource processEntry(IRepositoryResource current, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = current.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			LogMessage []msgs = GetLogMessagesOperation.getMessagesImpl(proxy, current, Revision.getInstance(0), current.getPegRevision(), 1, true, this, monitor);
			if (msgs != null && msgs.length > 0 && msgs[0] != null) {
				ChangePath []paths = msgs[0].changedPaths;
				if (paths == null) {
					return current;
				}
				String pattern = current.getUrl().substring(location.getRepositoryRoot().getUrl().length());
				int idx = -1;
				for (int i = 0; i < paths.length; i++) {
					if (pattern.startsWith(paths[i].path)) {
						if (paths[i].copySrcPath != null) {
							idx = i;
						}
						break;
					}
				}
				if (idx == -1) {
					return current;
				}
				String copiedFrom = paths[idx].copySrcPath;
				if (copiedFrom == null) {
					return current;
				}
				copiedFrom = location.getRepositoryRoot().getUrl() + copiedFrom + pattern.substring(paths[idx].path.length());
				
				long rev = paths[idx].copySrcRevision;
				Revision searchRevision = current.getSelectedRevision();
				long searchRev = ((Revision.Number)searchRevision).getNumber();
				if (rev < searchRev) {
					return current;
				}
				
				Revision revison = Revision.getInstance(rev);
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
