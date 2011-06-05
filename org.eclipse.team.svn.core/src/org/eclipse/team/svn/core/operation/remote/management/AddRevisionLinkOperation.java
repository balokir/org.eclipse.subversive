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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.core.resource.IRevisionLinkProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Add revision link to repository location
 * 
 * @author Alexander Gurov
 */
public class AddRevisionLinkOperation extends AbstractActionOperation {
	
	private IRevisionLink []links;
	private IRevisionLinkProvider provider;	
	protected SVNRevision revision;
	
	public AddRevisionLinkOperation(IRevisionLink link, long revision) {
		this(link, SVNRevision.fromNumber(revision));
	}
	
	public AddRevisionLinkOperation(IRevisionLink link, SVNRevision revision) {
		this();
		this.links = new IRevisionLink[]{link};
		this.revision = revision;
	}
	
	public AddRevisionLinkOperation(IRevisionLinkProvider provider, long revision) {
		this(provider, SVNRevision.fromNumber(revision));
	}
	
	public AddRevisionLinkOperation(IRevisionLinkProvider provider, SVNRevision revision) {
		this();
		this.provider = provider;
		this.revision = revision;
	}	
	
	public AddRevisionLinkOperation() {
		super("Operation_AddRevisionLink", SVNMessages.class); //$NON-NLS-1$
	}
		
	protected IRevisionLink []operableData() {
		return this.links == null ? this.provider.getRevisionLinks() : this.links;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRevisionLink []links = this.operableData();
		for (int i = 0; i < links.length && !monitor.isCanceled(); i++) {
			final IRevisionLink source = links[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {		
					IRepositoryResource sourceResource = source.getRepositoryResource(); 
					IRepositoryLocation location = sourceResource.getRepositoryLocation();
					IRepositoryResource targetResource = SVNUtility.copyOf(sourceResource);
					
					SVNRevision selectedRevision = AddRevisionLinkOperation.this.revision == null ? sourceResource.getSelectedRevision() : AddRevisionLinkOperation.this.revision;
					if (selectedRevision.equals(SVNRevision.HEAD)) {
						long revision = sourceResource.getRevision();
						if (revision == SVNRevision.INVALID_REVISION_NUMBER) { // failed: no network connection
							return;
						}
						selectedRevision = SVNRevision.fromNumber(revision);
					}
					
					SVNRevision pegRevision = sourceResource.getPegRevision();
					if (pegRevision.equals(SVNRevision.HEAD)) {
						long revision = location.getRepositoryRoot().getRevision();
						if (revision == SVNRevision.INVALID_REVISION_NUMBER) { // failed: no network connection
							return;
						}
						pegRevision = SVNRevision.fromNumber(revision);
					}
					
					targetResource.setSelectedRevision(selectedRevision);
					targetResource.setPegRevision(pegRevision);
					
					IRevisionLink link = SVNUtility.createRevisionLink(targetResource);
					link.setComment(source.getComment());
					location.addRevisionLink(link);
				}
			}, monitor, links.length);
		}
	}
	
}
