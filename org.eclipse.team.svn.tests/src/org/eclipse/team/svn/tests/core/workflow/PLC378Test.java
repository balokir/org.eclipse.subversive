/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.workflow;

import java.io.FileOutputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-378 defect (Incorrect commit of 
 * the file with already existing name) 
 *
 * @author Sergiy Logvin
 */
public class PLC378Test extends TestWorkflow {
    public void testPLC378() {  
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC378Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        new CheckoutAsOperation("CopyProject", SVNUtility.getProposedTrunk(getLocation()).asRepositoryContainer(getFirstProject().getName(), false), true).run(monitor);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream (getFirstProject().getLocation().toString() + "/testFile");
                            fos.write("testFile contents".getBytes());                            
                        }                        
                        finally {
                            fos.close();
                        } 
                        IResource []forAddition = new IResource[] {getFirstProject().getFile("testFile")};
                        new AddToSVNOperation(forAddition).run(monitor);
                        IResource []forCommit = new IResource[] {getFirstProject().getFile("testFile")};
                        new CommitOperation(forCommit, "PLC378Test", false).run(monitor); 
                                                
                        try {
                            fos = new FileOutputStream (getSecondProject().getLocation().toString() + "/testFile");
                            fos.write("some other testFile contents".getBytes());                             
                        }                        
                        finally {
                            fos.close();
                        }
                        forAddition = new IResource[] {getSecondProject().getFile("testFile")};
                        new AddToSVNOperation(forAddition).run(monitor);
                        forCommit = new IResource[] {getSecondProject().getFile("testFile")};
                        new CommitOperation(forCommit, "PLC378Test", false).run(monitor);
                        IResource []forUpdate = new IResource[] {getSecondProject().getFile("testFile")};
                        new UpdateOperation(forUpdate, true).run(monitor);                        
                    }
                };
            }
        }.testOperation();
    }
}