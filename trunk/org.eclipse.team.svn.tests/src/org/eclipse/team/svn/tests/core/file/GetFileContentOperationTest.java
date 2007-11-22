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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.GetFileContentOperation;

/**
 * Get File Content Operation Test
 * 
 * @author Sergiy Logvin
 */
public class GetFileContentOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		OutputStream out = null;
		try {
			out = new FileOutputStream(this.getFirstFolder().getPath() + "/maven.xml");
		}
		catch(IOException e) {
			AbstractOperationTestCase.assertFalse(e.getMessage(), true);
		}
		return new GetFileContentOperation(new File(this.getFirstFolder().getPath() + "/plugin.properties"), SVNRevision.HEAD, SVNRevision.HEAD, out);
	}

}
