/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file;

import org.eclipse.team.svn.tests.core.file.management.CleanupOperationTest;
import org.eclipse.team.svn.tests.core.file.management.DisconnectOperationTest;
import org.eclipse.team.svn.tests.core.file.management.RelocateOperationTest;
import org.eclipse.team.svn.tests.core.file.management.ShareOperationTest;
import org.eclipse.team.svn.tests.core.file.property.GetPropertyOpertionTest;
import org.eclipse.team.svn.tests.core.file.property.RemovePropertyOperationTest;
import org.eclipse.team.svn.tests.core.file.property.SetPropertyOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.CopyOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.DeleteOperationTest;
import org.eclipse.team.svn.tests.core.file.refactor.MoveOperationTest;

/**
 * File operations test
 * 
 * @author Elena Matokhina
 */
public class FileTest extends TestWorkflow {
	
	public void testFile() {
		new DisconnectOperationTest().testOperation();
		new ShareOperationTest().testOperation();
		new AddToSVNOperationTest().testOperation();
		new AddToSVNIgnoreOperationTest().testOperation();
		new CommitOperationTest().testOperation();
		new BranchTagOperationTest().testOperation();
		new SwitchOperationTest().testOperation();
		new CleanupOperationTest().testOperation();
		new GetAllFilesOperationTest().testOperation();
		new CheckoutAsOperationTest().testOperation();
		new SetPropertyOperationTest().testOperation();
		new GetPropertyOpertionTest(false).testOperation();
		new RemovePropertyOperationTest().testOperation();
		new GetPropertyOpertionTest(true).testOperation();
		new MultipleCommitTest().testOperation();
		new RelocateOperationTest().testOperation();
		new CopyOperationTest().testOperation();
		new MoveOperationTest().testOperation();
		new DeleteOperationTest().testOperation();
		new CreatePatchOperationTest().testOperation();
		new GetFileContentOperationTest().testOperation();
//		new JavaHLMergeOperationTest().testOperation();
		new LocalStatusOperationTest().testOperation();
		new LockOperationTest().testOperation();
		new UnlockOperationTest().testOperation();
//		new MarkResolvedOperationTest().testOperation();
		new RemoteStatusOperationTest().testOperation();
		new RevertOperationTest().testOperation();
		new UpdateOperationTest().testOperation();
	}

}
