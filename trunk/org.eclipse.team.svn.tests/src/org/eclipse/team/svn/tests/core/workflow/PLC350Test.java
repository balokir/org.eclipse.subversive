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

package org.eclipse.team.svn.tests.core.workflow;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryFolder;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-350 defect (Branch and tag
 * operations fail for development roots (HEAD, BRANCHES, TAGS) )
 * 
 * @author Sergiy Logvin 
 */
public class PLC350Test extends TestWorkflow {
    public void testPLC350() {
        new ShareNewProjectOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC350Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        IRepositoryResource branchedTrunk = new SVNRepositoryFolder(getLocation(), SVNUtility.getProposedBranchesLocation(getLocation()) + "/trunk", SVNRevision.HEAD);
		                IRepositoryResource taggedTrunk = new SVNRepositoryFolder(getLocation(), SVNUtility.getProposedTagsLocation(getLocation()) + "/trunk", SVNRevision.HEAD);
		                if (branchedTrunk.exists()) {
		                    FileUtility.deleteRecursive(new File (branchedTrunk.getUrl()));
		                }
		                if (taggedTrunk.exists()) {
		                    FileUtility.deleteRecursive(new File (taggedTrunk.getUrl()));
		                }
		                IRepositoryResource branchTagResource = SVNUtility.getProposedTrunk(getLocation());
		                new PreparedBranchTagOperation("Branch", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedBranches(getLocation()), "test branch").run(monitor);
		                branchedTrunk = new SVNRepositoryFolder(getLocation(), SVNUtility.getProposedBranchesLocation(getLocation()) + "/trunk", SVNRevision.HEAD);
		                assertTrue("PLC350Test", branchedTrunk.exists());
		                new PreparedBranchTagOperation("Tag", new IRepositoryResource[] {branchTagResource}, SVNUtility.getProposedTags(getLocation()), "test tag").run(monitor);
		                taggedTrunk = new SVNRepositoryFolder(getLocation(), SVNUtility.getProposedTagsLocation(getLocation()) + "/trunk", SVNRevision.HEAD);
		                assertTrue("PLC350Test", taggedTrunk.exists());
                    }
                };
            }
        }.testOperation();           
    }
}
