/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.workflow;

import java.io.FileOutputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-379 defect (Incorrect commit of 
 * the file with the name added to svn:ignore in another workspace) 
 *
 * @author Elena Matokhina
 */
public class PLC379Test  extends TestWorkflow {
    public void testPLC379() {  
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC379Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        new CheckoutAsOperation("TestProject", SVNUtility.getProposedTrunk(getLocation()).asRepositoryContainer(getSecondProject().getName(), false), true).run(monitor);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream (getFirstProject().getLocation().toString() + "/123");
                            fos.write("some contents".getBytes());                            
                        }                        
                        finally {
                            fos.close();
                        } 
                        IResource []forAddition = new IResource[] {getFirstProject().getFile("123")};
                        new AddToSVNOperation(forAddition).run(monitor);
                        IResource []forCommit = new IResource[] {getFirstProject().getFile("123")};
                        new CommitOperation(forCommit, "PLC379Test", false).run(monitor); 
                                                
                        try {
                            fos = new FileOutputStream (ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getLocation().toString() + "/123");
                            fos.write("some other contents".getBytes());                             
                        }                        
                        finally {
                            fos.close();
                        }
                        IResource[] ignoreResource = new IResource[] {ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123")};
                        new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
                        IResource []forUpdate = new IResource[] {ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123")};
                        new UpdateOperation(forUpdate, true).run(monitor);                        
                    }
                };
            }
        }.testOperation();
    }
}