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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.CommitOperation;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Commit operation test
 * 
 * @author Elena Matokhina
 */
public class CommitOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		List toCommit = new ArrayList();
		File []files = this.getFirstFolder().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!CommitOperationTest.isSVNInternals(files[i])) {
				toCommit.add(files[i]);
			}
		}
		files = this.getSecondFolder().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!CommitOperationTest.isSVNInternals(files[i])) {
				toCommit.add(files[i]);
			}
		}
		
		return new CommitOperation((File[])toCommit.toArray(new File[toCommit.size()]), "test commit", true, false);
	}
	
	public static boolean isSVNInternals(File file) {
		if (SVNUtility.getSVNFolderName().equals(file.getName())) {
			return true;
		}
		File parent = file.getParentFile();
		return parent == null ? false : CommitOperationTest.isSVNInternals(parent);
	}
	

}
