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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * This operation find all children including deleted for the resource specified
 * 
 * @author Alexander Gurov
 */
public class GetAllResourcesOperation extends AbstractActionOperation {
	protected IContainer container;
	protected IResource []children;

	public GetAllResourcesOperation(IContainer container) {
		super("Operation.GetResourceList");
		this.container = container;
		this.children = new IResource[0];
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []retVal = SVNRemoteStorage.instance().getRegisteredChildren(this.container);
		if (retVal != null) {
			this.children = retVal;
		}
	}
	
	public IResource []getChildren() {
		return this.children;
	}

}
