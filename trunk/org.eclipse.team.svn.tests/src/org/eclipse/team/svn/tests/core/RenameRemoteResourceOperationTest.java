/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.RenameResourceOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * RenameRemoteResourceOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class RenameRemoteResourceOperationTest extends AbstractOperationTestCase{
    protected IActionOperation getOperation() {
        SVNRemoteStorage storage = SVNRemoteStorage.instance();
        IRepositoryResource forRename = storage.asRepositoryResource(this.getFirstProject().getFolder("src/testFolder"));
		return new RenameResourceOperation(forRename, "testFolder2", "RenameRemoteResourceOperation Test");		
    }
    
}
