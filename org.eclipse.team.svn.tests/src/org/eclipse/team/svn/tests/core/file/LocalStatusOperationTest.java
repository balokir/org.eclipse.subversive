/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file;

import java.io.File;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.LocalStatusOperation;

/**
 * Local Status Operation Test
 * 
 * @author Sergiy Logvin
 */
public class LocalStatusOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new LocalStatusOperation(new File[] {this.getFirstFolder()}, true);
	}

}
