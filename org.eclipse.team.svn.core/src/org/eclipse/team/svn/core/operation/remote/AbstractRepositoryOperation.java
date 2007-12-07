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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;

/**
 * Abstract repository operation implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractRepositoryOperation extends AbstractActionOperation {
	private IRepositoryResource []resources;
	private IRepositoryResourceProvider provider;
	
	public AbstractRepositoryOperation(String operationName, IRepositoryResource []resources) {
		super(operationName);
		this.resources = resources;
	}
	
	public AbstractRepositoryOperation(String operationName, IRepositoryResourceProvider provider) {
		super(operationName);
		this.provider = provider;
	}
	
	protected IRepositoryResource []operableData() {
		return this.resources == null ? this.provider.getRepositoryResources() : this.resources;
	}
	
}
