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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.operation.file.management.RelocateOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Relocate operation test
 * 
 * @author Sergiy Logvin
 */
public class RelocateOperationTest extends AbstractOperationTestCase {
	 protected void runImpl(IProgressMonitor monitor) throws Exception { 
         
     }

	protected IActionOperation getOperation() {
		return new AbstractFileOperation("Relocate", this.getBothFolders()) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNFileStorage storage = SVNFileStorage.instance();
				IRepositoryLocation newLocation = RelocateOperationTest.this.getLocation();
				String old = newLocation.getUrl();
				new RelocateOperation(this.operableData(), "http://testurl").run(monitor);
				IRepositoryResource remote = storage.asRepositoryResource(RelocateOperationTest.this.getFirstFolder(), true);
				new RelocateOperation(this.operableData(), old).run(monitor);
				remote = storage.asRepositoryResource(RelocateOperationTest.this.getFirstFolder(), true);
				assertTrue("Relocate Operation Test",  remote.exists());
			}
		};
	}
}
