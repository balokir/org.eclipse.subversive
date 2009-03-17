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

package org.eclipse.team.svn.core.operation.remote.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
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
	protected SVNRevision revision;
	
	public AddRevisionLinkOperation(IRepositoryResource resource, long revision) {
		this(resource, SVNRevision.fromNumber(revision));
	}
	
	public AddRevisionLinkOperation(IRepositoryResource resource, SVNRevision revision) {
		super("Operation_AddRevisionLink", new IRepositoryResource[] {resource}); //$NON-NLS-1$
		this.revision = revision;
	}
	
	public AddRevisionLinkOperation(IRepositoryResourceProvider provider, long revision) {
		this(provider, SVNRevision.fromNumber(revision));
	}
	
	public AddRevisionLinkOperation(IRepositoryResourceProvider provider, SVNRevision revision) {
		super("Operation_AddRevisionLink", provider); //$NON-NLS-1$
		this.revision = revision;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource source = resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IRepositoryLocation location = source.getRepositoryLocation();
					IRepositoryResource target = SVNUtility.copyOf(source);
					
					SVNRevision selectedRevision = AddRevisionLinkOperation.this.revision == null ? source.getSelectedRevision() : AddRevisionLinkOperation.this.revision;
					if (selectedRevision.equals(SVNRevision.HEAD)) {
						selectedRevision = SVNRevision.fromNumber(source.getRevision());
					}
					
					SVNRevision pegRevision = source.getPegRevision();
					if (pegRevision.equals(SVNRevision.HEAD)) {
						pegRevision = SVNRevision.fromNumber(location.getRepositoryRoot().getRevision());
					}
					
					target.setSelectedRevision(selectedRevision);
					target.setPegRevision(pegRevision);
					
					location.addRevisionLink(target);
				}
			}, monitor, resources.length);
		}
	}
	
}
