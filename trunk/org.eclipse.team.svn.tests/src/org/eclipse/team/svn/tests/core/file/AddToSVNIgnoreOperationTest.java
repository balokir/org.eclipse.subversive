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

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Add to SVN ignore operation test
 * 
 * @author Elena Matokhina
 */
public class AddToSVNIgnoreOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		try {
			FileUtility.copyFile(new File(this.getFirstFolder().getPath() + "/src"), new File(this.getSecondFolder().getPath() + "/bumprev.sh"), new NullProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new AddToSVNIgnoreOperation(new File[] {new File (this.getFirstFolder().getPath() + "/src/bumprev.sh")}, IRemoteStorage.IGNORE_NAME, "");
	}

}
