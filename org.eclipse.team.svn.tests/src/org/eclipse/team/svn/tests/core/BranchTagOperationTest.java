/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * BranchTagOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class BranchTagOperationTest extends AbstractOperationTestCase {
    protected IActionOperation getOperation() {
        SVNRemoteStorage storage = SVNRemoteStorage.instance(); 
        IRepositoryResource branchTagResource = storage.asRepositoryResource(this.getFirstProject());
        PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation("Branch", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedBranches(this.getLocation()), "test branch", false);
        CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
        op.add(mainOp);
        op.add(new PreparedBranchTagOperation("Tag", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedTags(this.getLocation()), "test branch", false));
        return op;
	}
    
}
