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

package org.eclipse.team.svn.ui;

import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Remote resource transferrable implementation
 * 
 * @author Alexander Gurov
 */
public class RemoteResourceTransferrable {
	public static final int OP_NONE = 0xff;
	public static final int OP_COPY = 0;
	public static final int OP_CUT = 1;
	
	protected IRepositoryResource []resources;
	protected int operation;

	public RemoteResourceTransferrable(IRepositoryResource []resources, int operation) {
		this.resources = resources;
		this.operation = operation;
	}

	public int getOperationType() {
		return this.operation;
	}
	
	public IRepositoryResource []getResources() {
		return this.resources;
	}
	
}
