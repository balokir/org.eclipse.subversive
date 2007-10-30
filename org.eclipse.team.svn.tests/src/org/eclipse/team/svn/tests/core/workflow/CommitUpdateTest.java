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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.CreateRemoteFolderOperationTest;
import org.eclipse.team.svn.tests.core.DisconnectOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;
import org.eclipse.team.svn.tests.core.UpdateOperationTest;

/**
 * Test for the one of possible consecution of the core operations 
 *
 * @author Sergiy Logvin
 */
public class CommitUpdateTest extends TestWorkflow {
    
    public void testCommitUpdateWorkflow() {
        new ShareNewProjectOperationTest() {}.testOperation();
		new AddOperationTest() {}.testOperation();
		new CommitOperationTest() {}.testOperation();
		new CreateRemoteFolderOperationTest() {}.testOperation();
		new UpdateOperationTest() {}.testOperation();
		new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("CommitUpdateWorkflowTest") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        File testFolder = getFirstProject().getFolder("src/testFolder").getLocation().toFile();
                        CommitUpdateTest.assertTrue("CommitUpdateWorkflowTest", testFolder.exists());                        
                    }                    
                };
            }		    
		}.testOperation();
		new DisconnectOperationTest() {}.testOperation();
    }

}
