/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;

/**
 * RevertOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class RevertOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
		IProject prj = this.getFirstProject();
		
		IFile file = prj.getFile("testProject.xml");
		
		try {
			file.appendContents(new ByteArrayInputStream("data".getBytes()), true, false, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		
		prj = this.getSecondProject();
		IFile file1 = prj.getFile("site.xml");
		
		try {
		    file1.appendContents(new ByteArrayInputStream("data".getBytes()), true, false, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		
		return new RevertOperation(new IResource[] {file, file1}, false);
	}

}
