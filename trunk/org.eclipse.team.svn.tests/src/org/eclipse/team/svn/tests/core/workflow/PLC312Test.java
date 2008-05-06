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

package org.eclipse.team.svn.tests.core.workflow;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;
/**
 * Reproducing steps, which are described in PLC-312 defect (Exception in 
 * Add to SVN ignore operation) 
 *
 * @author Sergiy Logvin
 */
public class PLC312Test extends TestWorkflow {
    public void testPLC312() {
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC312Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {                        
                        FileUtility.copyAll(getFirstProject().getFolder("src").getLocation().toFile(), getSecondProject().getFolder("web").getLocation().toFile(), monitor);
                        IResource[] ignoreResource = new IResource[] {getFirstProject().getFile("src/web/site.css")};
                        new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
                    };
                };
            }            
        }.testOperation();
    }
}
