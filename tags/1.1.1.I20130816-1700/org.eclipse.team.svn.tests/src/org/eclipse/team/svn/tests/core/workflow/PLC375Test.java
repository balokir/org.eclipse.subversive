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

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-375 defect (Committing new folder 
 * with the name of deleted file finishes with error) 
 *
 * @author Sergiy Logvin
 */
public class PLC375Test extends TestWorkflow {
    public void testPLC375() {
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC375Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream (getSecondProject().getLocation().toString() + "/123");
                            fos.write(12345);                             
                        }                        
                        finally {
                            fos.close();
                        }
                        IResource []forCommit = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_ADDED);
                        new CommitOperation(forCommit, "test PLC375", false).run(monitor);
                        FileUtility.deleteRecursive(getSecondProject().getFile("123").getLocation().toFile());
                        forCommit = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_ANY_CHANGE);
                        new CommitOperation(forCommit, "test PLC375", false).run(monitor);
                        File dir = new File(getSecondProject().getLocation().toString() + "/123");
                        dir.mkdir();
                        forCommit = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_ADDED);
                        new CommitOperation(forCommit, "test PLC375", false).run(monitor);
                    }
                };
            }
        }.testOperation();
    }
}