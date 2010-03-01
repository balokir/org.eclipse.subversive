/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;

/**
 *  As we don't know whether revision edit part is a file or directory, we need to contact repository
 *	to detect it in order to create repository resources 
 *
 * @author Igor Burilo
 */
public class GetRepositoryResourcesOperation extends AbstractActionOperation implements IRepositoryResourceProvider {

	protected RevisionEditPart[] editParts;		
	
	protected IRepositoryResource[] resources;
	
	public GetRepositoryResourcesOperation(RevisionEditPart[] editParts) {
		super("Get Repository Resources Operation");
		this.editParts = editParts;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.resources = new IRepositoryResource[this.editParts.length];
		for (int i = 0; i  < this.editParts.length; i ++) {
			RevisionEditPart editPart = this.editParts[i];
			
			RevisionNode revisionNode = editPart.getCastedModel();
			RevisionRootNode rootNode = editPart.getRevisionRootNode();		
			
			IRepositoryLocation location = rootNode.getRepositoryLocation();
			SVNRevision svnRevision = SVNRevision.fromNumber(revisionNode.pathRevision.getRevision());
			String url = rootNode.getRevisionFullPath(revisionNode); 				
			SVNEntryRevisionReference reference =  new SVNEntryRevisionReference(SVNUtility.encodeURL(url), svnRevision, svnRevision);								
			this.resources[i] = SVNRemoteStorage.instance().asRepositoryResource(location, reference, new SVNProgressMonitor(this, monitor, null));
			this.resources[i].setSelectedRevision(svnRevision);
			this.resources[i].setPegRevision(svnRevision);
		}			
	}

	public IRepositoryResource[] getRepositoryResources() {
		return this.resources;
	}		
}
