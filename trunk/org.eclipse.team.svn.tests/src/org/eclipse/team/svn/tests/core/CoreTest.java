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

/**
 * Core functionality test
 * 
 * @author Alexander Gurov
 */
public class CoreTest extends TestWorkflow {
	public void testCore() {
		new AbstractNonLockingOperationTest() {}.testGetSchedulingRule();
		new AbstractGetFileContentOperationTest() {}.testGetSetContent();
		new AbstractOperationTest() {}. allTests();
		new RepositoryLocationsManagementTest() {}.testLocationsManagement();
		new ShareNewProjectOperationTest() {}.testOperation();
		new FileUtilityTest() {}.testOperation();
		new SVNUtilityTest() {}.testOperation();
		new AddOperationTest() {}.testOperation();
		new AddToSVNIgnoreOperationTest() {}.testOperation();
		new CommitOperationTest() {}.testOperation();
		new BranchTagOperationTest() {}.testOperation();
		new SwitchOperationTest() {}.testOperation();
		new CheckoutOperationTest() {}.testOperation();
		new CleanupOperationTest() {}.testOperation();
		new GetAllResourcesOperationTest() {}.testOperation();
		new ClearLocalStatusesOperationTest() {}.testOperation();
		new GetLogMessagesOperationTest() {}.testOperation();
		new RemoteStatusOperationTest() {}.testOperation();
		new RevertOperationTest() {}.testOperation();
		new CreateRemoteFolderOperationTest() {}.testOperation();
		new RenameRemoteResourceOperationTest() {}.testOperation();
		new GetFileContentOperationTest() {}.testOperation();
		new GetRemoteContentsOperationTest() {}.testOperation();
		new GetResourceAnnotationOperationTest() {}.testOperation();
		new InfoOperationTest() {}.testOperation();
		new MoveLocalResourceOperationTest() {}.testOperation();
		new DeleteRemoteResourceOperationTest() {}.testOperation();
		new UpdateOperationTest() {}.testOperation();
		new DisconnectWithoutDropOperationTest() {}.testOperation();
		new ReconnectExistingProjectOperationTest() {}.testOperation();
		new CopyLocalResourceOperationTest() {}.testOperation();
		new DeleteLocalResourceOperationTest() {}.testOperation();
//		new RelocateWorkingCopyOperationTest() {}.testOperation(); // requires UI interaction due to crash recovery API
		new DisconnectOperationTest() {}.testOperation();
		new DiscardRepositoryLocationsOperationTest() {}.testOperation();
	}

}
