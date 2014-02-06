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

package org.eclipse.team.svn.tests.core.file.refactor;

import java.io.File;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.refactor.MoveOperation;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Move Operation Test
 * 
 * @author Sergiy Logvin
 */
public class MoveOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
		return new MoveOperation(new File[] {new File(this.getFirstFolder().getPath() + "/build.properties")}, new File (this.getFirstFolder().getPath() + "/src"), true);
	}
}
