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
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.cache.RevisionDataContainer;

/**
 * 
 * @author Igor Burilo
 */
public class PrepareRevisionDataOperation extends AbstractActionOperation {

	protected IRepositoryResource resource;
	
	protected RevisionDataContainer dataContainer;
	
	public PrepareRevisionDataOperation(IRepositoryResource resource) {
		super("PrepareRevisionDataOperation");
		this.resource = resource;
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.dataContainer = new RevisionDataContainer(RevisionGraphUtility.getCacheFolder(this.resource), this.resource);
		this.dataContainer.prepareData(monitor);
	}
	
	public RevisionDataContainer getDataContainer() {
		return this.dataContainer;
	}		
}
