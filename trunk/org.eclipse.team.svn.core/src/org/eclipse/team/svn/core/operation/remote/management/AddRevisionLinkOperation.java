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
import org.eclipse.team.svn.core.client.RevisionKind;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Add revision link to repository location
 * 
 * @author Alexander Gurov
 */
public class AddRevisionLinkOperation extends AbstractRepositoryOperation {
	protected Revision revision;
	
	public AddRevisionLinkOperation(IRepositoryResource resource, Revision revision) {
		super("Operation.AddRevisionLink", new IRepositoryResource[] {resource});
		this.revision = revision;
	}
	
	public AddRevisionLinkOperation(IRepositoryResource resource, long revision) {
		this(resource, Revision.getInstance(revision));
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource source = this.operableData()[0];
		IRepositoryLocation location = source.getRepositoryLocation();
		String url = source.getUrl();
		IRepositoryResource target = source instanceof IRepositoryContainer ? (IRepositoryResource)location.asRepositoryContainer(url, false) : location.asRepositoryFile(url, false);
		target.setSelectedRevision(this.revision);
		Revision pegRevision = source.getPegRevision();
		if (pegRevision.getKind() == RevisionKind.head) {
			pegRevision = Revision.getInstance(source.getRepositoryLocation().getRepositoryRoot().getRevision());
		}
		target.setPegRevision(pegRevision);
		location.addRevisionLink(target);
	}
	
}
