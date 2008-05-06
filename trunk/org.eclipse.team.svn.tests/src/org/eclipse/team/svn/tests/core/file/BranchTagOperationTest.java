/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file;

import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Branch and tag operations test
 * 
 * @author Sergiy Logvin
 */
public class BranchTagOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		SVNFileStorage storage = SVNFileStorage.instance(); 
        IRepositoryResource branchTagResource = storage.asRepositoryResource(this.getFirstFolder(), true);
        PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation("Branch", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedBranches(this.getLocation()), "test branch");
        CompositeOperation op = new CompositeOperation(mainOp.getId());
        op.add(mainOp);
        op.add(new PreparedBranchTagOperation("Tag", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedTags(this.getLocation()), "test branch"));
        return op;
	}

}
