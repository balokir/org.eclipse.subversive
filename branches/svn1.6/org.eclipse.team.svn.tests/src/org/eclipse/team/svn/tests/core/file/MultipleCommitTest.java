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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.CommitOperation;
import org.eclipse.team.svn.core.operation.file.LocalStatusOperation;
import org.eclipse.team.svn.core.operation.file.RevertOperation;
import org.eclipse.team.svn.core.operation.file.property.SetPropertyOperation;
import org.eclipse.team.svn.tests.core.file.property.SetPropertyOperationTest;

/**
 * Multiple resources commit test
 * 
 * @author Sergiy Logvin
 */
public class MultipleCommitTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new AbstractFileOperation("Commit", this.getListFilesRecursive()) {

			protected void runImpl(IProgressMonitor monitor) throws Exception {
				new SetPropertyOperation(this.operableData(), 
						SetPropertyOperationTest.TEST_PROPERTY_NAME, 
						SetPropertyOperationTest.TEST_PROPERTY_VALUE.getBytes(),
						false).run(monitor);
				LocalStatusOperation op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length > 0);
				new RevertOperation(this.operableData(), true).run(monitor);
				op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length == 1);
				new SetPropertyOperation(this.operableData(), 
						SetPropertyOperationTest.TEST_PROPERTY_NAME, 
						SetPropertyOperationTest.TEST_PROPERTY_VALUE.getBytes(),
						false).run(monitor);
				op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length > 0);
				new CommitOperation(this.operableData(), "testCommit", false, false).run(monitor);
			}
		};
	}

}
