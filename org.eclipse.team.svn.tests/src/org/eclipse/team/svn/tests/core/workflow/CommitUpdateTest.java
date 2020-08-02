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

package org.eclipse.team.svn.tests.core.workflow;

import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.CreateRemoteFolderOperationTest;
import org.eclipse.team.svn.tests.core.DisconnectOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;
import org.eclipse.team.svn.tests.core.UpdateOperationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for the one of possible consecution of the core operations
 *
 * @author Sergiy Logvin
 */
@RunWith(Suite.class)
@SuiteClasses({ ShareNewProjectOperationTest.class, AddOperationTest.class, CommitOperationTest.class,
		CreateRemoteFolderOperationTest.class, UpdateOperationTest.class, DisconnectOperationTest.class })
public class CommitUpdateTest extends TestWorkflow {

	// NIC what about this test case?
//		new AbstractOperationTestCase() {
//			@Override
//			protected IActionOperation getOperation() {
//				return new AbstractLockingTestOperation("CommitUpdateWorkflowTest") {
//					@Override
//					protected void runImpl(IProgressMonitor monitor) throws Exception {
//						File testFolder = getFirstProject().getFolder("src/testFolder").getLocation().toFile();
//						assertTrue("CommitUpdateWorkflowTest", testFolder.exists());
//					}
//				};
//			}
//		}.testOperation();

}
