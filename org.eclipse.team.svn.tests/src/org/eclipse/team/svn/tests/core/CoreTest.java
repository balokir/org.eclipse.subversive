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

package org.eclipse.team.svn.tests.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Core functionality test
 * 
 * @author Alexander Gurov
 */
@RunWith(Suite.class)
@SuiteClasses({ AbstractGetFileContentOperationTest.class, AbstractOperationTest.class,
		RepositoryLocationsManagementTest.class, ShareNewProjectOperationTest.class, FileUtilityTest.class,
		SVNUtilityTest.class, AddOperationTest.class, AddToSVNIgnoreOperationTest.class, CommitOperationTest.class,
		BranchTagOperationTest.class, SwitchOperationTest.class, CheckoutOperationTest.class,
		CleanupOperationTest.class, GetAllResourcesOperationTest.class, ClearLocalStatusesOperationTest.class,
		GetLogMessagesOperationTest.class, RemoteStatusOperationTest.class, RevertOperationTest.class,
		CreateRemoteFolderOperationTest.class, RenameRemoteResourceOperationTest.class,
		GetFileContentOperationTest.class, GetRemoteContentsOperationTest.class,
		GetResourceAnnotationOperationTest.class, InfoOperationTest.class, MoveLocalResourceOperationTest.class,
		DeleteRemoteResourceOperationTest.class, UpdateOperationTest.class, DisconnectWithoutDropOperationTest.class,
		ReconnectExistingProjectOperationTest.class, CopyLocalResourceOperationTest.class,
		DeleteLocalResourceOperationTest.class, DisconnectOperationTest.class,
		DiscardRepositoryLocationsOperationTest.class })
public class CoreTest extends TestWorkflow {
	// test suite (no further code required)
}
