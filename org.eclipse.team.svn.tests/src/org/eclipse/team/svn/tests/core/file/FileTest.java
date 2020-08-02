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

import org.eclipse.team.svn.tests.core.file.management.CleanupOperationTest;
import org.eclipse.team.svn.tests.core.file.management.DisconnectOperationTest;
import org.eclipse.team.svn.tests.core.file.management.RelocateOperationTest;
import org.eclipse.team.svn.tests.core.file.management.ShareOperationTest;
import org.eclipse.team.svn.tests.core.file.property.RemovePropertyOperationTest;
import org.eclipse.team.svn.tests.core.file.property.SetPropertyOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.CopyOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.DeleteOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.MoveOperationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * File operations test
 * 
 * @author Sergiy Logvin
 */
@RunWith(Suite.class)
@SuiteClasses({ DisconnectOperationTest.class, ShareOperationTest.class, AddToSVNOperationTest.class,
		AddToSVNIgnoreOperationTest.class, CommitOperationTest.class, BranchTagOperationTest.class,
		SwitchOperationTest.class, CleanupOperationTest.class, GetAllFilesOperationTest.class,
		CheckoutAsOperationTest.class, SetPropertyOperationTest.class,
//		NIC GetPropertyOpertionTest(false).class,
		RemovePropertyOperationTest.class,
//		NIC GetPropertyOpertionTest(true).class,
		MultipleCommitTest.class, RelocateOperationTest.class, CopyOperationTest.class, MoveOperationTest.class,
		DeleteOperationTest.class, CreatePatchOperationTest.class, GetFileContentOperationTest.class,
		LocalStatusOperationTest.class, LockOperationTest.class, UnlockOperationTest.class,
		RemoteStatusOperationTest.class, RevertOperationTest.class, UpdateOperationTest.class })
public class FileTest extends TestWorkflow {
// test suite - no further code required
}
