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

package org.eclipse.team.svn.core.operation.remote.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Add revision link to repository location
 * 
 * @author Alexander Gurov
 */
public class AddRevisionLinkOperation extends AbstractRepositoryOperation {
	protected Revision revision;
	
	public AddRevisionLinkOperation(IRepositoryResource resource, long revision) {
		this(resource, Revision.getInstance(revision));
	}
	
	public AddRevisionLinkOperation(IRepositoryResource resource, Revision revision) {
		super("Operation.AddRevisionLink", new IRepositoryResource[] {resource});
		this.revision = revision;
	}
	
	public AddRevisionLinkOperation(IRepositoryResourceProvider provider, long revision) {
		this(provider, Revision.getInstance(revision));
	}
	
	public AddRevisionLinkOperation(IRepositoryResourceProvider provider, Revision revision) {
		super("Operation.AddRevisionLink", provider);
		this.revision = revision;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource source = resources[0];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IRepositoryLocation location = source.getRepositoryLocation();
					IRepositoryResource target = SVNUtility.copyOf(source);
					
					Revision selectedRevision = AddRevisionLinkOperation.this.revision;
					if (selectedRevision.equals(Revision.HEAD)) {
						selectedRevision = Revision.getInstance(source.getRevision());
					}
					
					Revision pegRevision = source.getPegRevision();
					if (pegRevision.equals(Revision.HEAD)) {
						pegRevision = Revision.getInstance(location.getRepositoryRoot().getRevision());
					}
					
					target.setSelectedRevision(selectedRevision);
					target.setPegRevision(pegRevision);
					
					location.addRevisionLink(target);
				}
			}, monitor, resources.length);
		}
	}
	
}
