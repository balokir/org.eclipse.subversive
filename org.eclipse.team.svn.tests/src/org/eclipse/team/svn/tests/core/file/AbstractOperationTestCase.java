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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;

/**
 * Abstract file operation test
 * 
 * @author Sergiy Logvin
 */
public abstract class AbstractOperationTestCase extends TestCase {
	public void testOperation() {
		this.assertOperation(this.getOperation());
	}
	
	protected abstract IActionOperation getOperation();
	
	protected File getFirstFolder() {
		return TestWorkflow.FIRST_FOLDER;
	}
	
	protected File getSecondFolder() {
		return TestWorkflow.SECOND_FOLDER;
	}
	
	protected File[] getBothFolders() {
		return new File[] {this.getFirstFolder(), this.getSecondFolder()};
	}
	
	protected File[] getListFiles() {
		return TestWorkflow.FIRST_FOLDER.getParentFile().listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.getName().equals(".metadata");
			}
		});
	}
	
	protected File[] getListFilesRecursive() {
		List<File> allFiles = new ArrayList<File>();
		this.getFilesRecursiveImpl(this.getListFiles(), allFiles);
		return allFiles.toArray(new File[allFiles.size()]);
	}
	
	protected void getFilesRecursiveImpl(File[] roots, List<File> allFiles) {
		for (int i = 0; i < roots.length; i++) {
			allFiles.add(roots[i]);
			if (roots[i].isDirectory()) {
				this.getFilesRecursiveImpl(roots[i].listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return !pathname.getName().equals(SVNUtility.getSVNFolderName());
					}
				}), allFiles);
			}
		}
	}
	
	protected IRepositoryLocation getLocation() {
		return SVNFileStorage.instance().getRepositoryLocations()[0];
	}
	
	protected void assertOperation(IActionOperation op) {
		IStatus operationStatus = op.run(new NullProgressMonitor()).getStatus();
		if (operationStatus.isOK()) {
			assertTrue(op.getOperationName(), true);
		}
		else {
			String trace = ReportPartsFactory.getStackTrace(operationStatus);
			assertTrue(operationStatus.getMessage() + trace, false);
		}		
	}

}
