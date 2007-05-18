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

package org.eclipse.team.svn.tests.core.file.refactor;

import java.io.File;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.refactor.CopyOperation;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Copy operation test
 * 
 * @author Elena Matokhina
 */
public class CopyOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new CopyOperation(new File[] {new File(this.getFirstFolder().getPath() + "/maven.xml")}, new File (this.getFirstFolder().getPath() + "/src"), true);
	}

}
