/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.test.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.CommitOperation;
import org.eclipse.team.svn.core.operation.common.IActionOperation;
import org.eclipse.team.svn.test.TestPlugin;

/**
 * CommitOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class CommitOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		IProject prj = this.getProject();
		
		IFile file = prj.getFile(TestPlugin.instance().getResourceBundle().getString("File.AdditionTest"));
		
		return new CommitOperation(new IResource[] {file}, "test commit");
	}

}
