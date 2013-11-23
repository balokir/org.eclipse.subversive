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

import java.io.FileOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-380 defect (File updating 
 * error when repository contains an empty copy of it) 
 *
 * @author Sergiy Logvin
 */
public class PLC380Test extends TestWorkflow {
    public void testPLC380() {        
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC380Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream (getSecondProject().getLocation().toString() + "/123");
                            fos.write("some contents".getBytes());                            
                        }                        
                        finally {
                            fos.close();
                        }
                        new ShareProjectOperation(new IProject[] {getSecondProject()}, getLocation(), null, "Share Project test").run(monitor);
                        IResource []forAddition = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_NEW);
                        new AddToSVNOperation(forAddition).run(monitor);
                        IResource []forCommit = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_ADDED);
                        new CommitOperation(forCommit, "test PLC380", false).run(monitor);
                        new CheckoutAsOperation("TestProject", SVNUtility.getProposedTrunk(getLocation()).asRepositoryContainer(getSecondProject().getName(), false), SVNDepth.INFINITY, true).run(monitor);                        
                        try {
                            fos = new FileOutputStream (getFirstProject().getLocation().toString() + "/123");
                            fos.write("".getBytes());                             
                        }                        
                        finally {
                            fos.close();
                        }
                        new CommitOperation(new IResource[] {getFirstProject().getFile("123")}, "test PLC380", false).run(monitor);
                        new UpdateOperation(new IResource[] {ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123")}, true).run(monitor);                        
                    }
                };
            }
        }.testOperation();
    }
}