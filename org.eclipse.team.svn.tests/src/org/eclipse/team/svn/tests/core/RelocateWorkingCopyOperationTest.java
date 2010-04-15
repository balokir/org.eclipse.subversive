/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.management.FindRelatedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.management.RelocateWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;


/**
 * RelocateWorkingCopyOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class RelocateWorkingCopyOperationTest extends AbstractOperationTestCase {
    protected IActionOperation getOperation() { 
        return new AbstractLockingTestOperation("RelocateWorkingCopyOperation Test") {
            protected void runImpl(IProgressMonitor monitor) throws Exception { 
                IRepositoryLocation newLocation = RelocateWorkingCopyOperationTest.this.getLocation();
                String old = newLocation.getUrl();
                newLocation.setUrl("http://testurl");
                
                CompositeOperation op = new CompositeOperation("Relocate Test", SVNMessages.class);
                FindRelatedProjectsOperation scannerOp = new FindRelatedProjectsOperation(newLocation);
                op.add(scannerOp);
                op.add(new RelocateWorkingCopyOperation(scannerOp, newLocation), new IActionOperation[] {scannerOp});
                op.run(monitor);
                IRepositoryResource remote = null;
                try {
                	remote = SVNRemoteStorage.instance().asRepositoryResource(RelocateWorkingCopyOperationTest.this.getFirstProject().getFolder("src"));
                }
                catch (IllegalArgumentException ex) {
                	// do nothing
                }
                assertFalse("RelocateWorkingCopyOperation Test",  remote != null && remote.exists());
                
                newLocation.setUrl(old);
                op = new CompositeOperation("Relocate Test", SVNMessages.class);
                scannerOp = new FindRelatedProjectsOperation(newLocation);
                op.add(scannerOp);
                op.add(new RelocateWorkingCopyOperation(scannerOp, newLocation), new IActionOperation[] {scannerOp});
                op.run(monitor);
                remote = SVNRemoteStorage.instance().asRepositoryResource(RelocateWorkingCopyOperationTest.this.getFirstProject().getFolder("src"));
                assertTrue("RelocateWorkingCopyOperation Test",  remote != null && remote.exists());
            };
        };
    }
    
}
