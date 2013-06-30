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

package org.eclipse.team.svn.tests.core.file.management;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.management.CleanupOperation;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Cleanup operation test
 * 
 * @author Sergiy Logvin
 */
public class CleanupOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new CleanupOperation(this.getBothFolders());
	}	

}
