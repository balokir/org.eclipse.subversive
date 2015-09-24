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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.operation.local.refactor.CopyResourceOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-366 defect (Commit doesn't
 * work for folders with svn:ignore resources)
 * 
 * @author Sergiy Logvin 
 */
public class PLC366Test extends TestWorkflow {
    public void testPLC366() {
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC366Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {
                        new ShareProjectOperation(new IProject[] {getSecondProject()}, getLocation(), null, "Share Project test").run(monitor);
                        IResource []forAddition = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_NEW);
                        new AddToSVNOperation(forAddition).run(monitor);
                        IResource []forCommit = FileUtility.getResourcesRecursive(new IResource[] {getSecondProject()}, IStateFilter.SF_ADDED);
                        new CommitOperation(forCommit, "test PLC366", false, false).run(monitor);
                        IResource source = getSecondProject().getFile("site.xml");
                        IResource destination = getSecondProject().getFile("web/site.xml");
                        new CopyResourceOperation (source, destination).run(monitor);
                        new AddToSVNIgnoreOperation(new IResource [] {destination}, IRemoteStorage.IGNORE_NAME, "").run(monitor);
                        new CommitOperation(new IResource[] {getSecondProject().getFolder("web")}, "test PLC366", false, false).run(monitor);                       
                    }
                };
            }
        }.testOperation();
    }
}
