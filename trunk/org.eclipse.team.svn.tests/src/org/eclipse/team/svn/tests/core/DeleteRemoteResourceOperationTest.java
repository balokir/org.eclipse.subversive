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

package org.eclipse.team.svn.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * DeleteRemoteResourceOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class DeleteRemoteResourceOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    SVNRemoteStorage storage = SVNRemoteStorage.instance();

	    IProject prj = this.getFirstProject();
		IRepositoryResource remote1 = storage.asRepositoryResource(prj.getFile("testProject.xml"));
		IRepositoryResource remote2 = storage.asRepositoryResource(prj.getFolder("src/testFolder2"));
		
	    prj = this.getSecondProject();
		IRepositoryResource remote3 = storage.asRepositoryResource(prj.getFile("bumprev.sh"));		
		
		return new DeleteResourcesOperation(new IRepositoryResource[] {remote1, remote2, remote3}, "test delete");
	}

}
