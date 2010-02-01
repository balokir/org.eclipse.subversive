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
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.operation.ShowRevisionGraphUtility.ISVNLogEntryProvider;

/**
 * Create revision graph model
 * 
 * @author Igor Burilo
 */
public class CreateRevisionGraphModelOperation extends AbstractActionOperation {	
	
	protected ISVNLogEntryProvider provider;
	protected SVNLogEntry[] rawEntries;
	protected IRepositoryResource resource;	
	
	//protected SVNLogEntry[] entries;	
	//protected RevisionNode startNode;
	
	protected PathRevision model;
	
	public CreateRevisionGraphModelOperation(SVNLogEntry[] entries, IRepositoryResource resource) {
		this(resource);
		this.rawEntries = entries;
	}
	
	public CreateRevisionGraphModelOperation(ISVNLogEntryProvider provider, IRepositoryResource resource) {
		this(resource);
		this.provider = provider;		
	}
	
	private CreateRevisionGraphModelOperation(IRepositoryResource resource) {
		super("CreateRevisionGraphModelOperation");
		this.resource = resource;		
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {															
		String url = this.resource.getUrl();
		String rootUrl = this.resource.getRepositoryLocation().getRepositoryRootUrl();	
	
		String path = url.substring(rootUrl.length());
		
		CreateRevisionGraphModelOperationHelper helper = new CreateRevisionGraphModelOperationHelper(this.operableData(), path, this.resource.getSelectedRevision(), this.resource);
		helper.runImpl(monitor);				
				
		this.model = helper.getResultNode();
	}
	
	protected SVNLogEntry[] operableData() {
		return this.rawEntries == null ? this.provider.getLogEntries() : this.rawEntries;
	}
	
	public PathRevision getModel() {
		return this.model;
	}
}
