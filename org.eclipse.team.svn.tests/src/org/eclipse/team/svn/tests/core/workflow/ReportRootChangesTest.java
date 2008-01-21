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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RemoteStatusOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Test of the changes report for WC root 
 *
 * @author Sergiy Logvin
 */
public class ReportRootChangesTest extends TestWorkflow {
    public void testReportRootChanges() {
    	if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.REPORT_REVISION_CHANGE) == 0) {
    		return;
    	}
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("ReportRootChangesTest") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {  
                    	SVNRemoteStorage storage = SVNRemoteStorage.instance(); 
                        IRepositoryResource remote = storage.asRepositoryResource(getSecondProject());
                        new DeleteResourcesOperation(new IRepositoryResource[] {remote}, "test").run(monitor);
                        RemoteStatusOperation rStatusOp = new RemoteStatusOperation(new IResource[] {getSecondProject()});
                        ProgressMonitorUtility.doTaskExternalDefault(rStatusOp, new NullProgressMonitor());
                		SVNChangeStatus []statuses = (SVNChangeStatus [])rStatusOp.getStatuses();               		
                		int counter = 0;
                		for (int i = 0; i < statuses.length; i++) {
                		    if (statuses[i].path.equals(getSecondProject().getLocation().toString()) && 
               		            statuses[i].repositoryTextStatus == SVNEntryStatus.Kind.DELETED) {           		        
                		        counter = -1;
                		        break;          		        
                		    }
            		        counter++;
                		}
                		if (counter == statuses.length) {
                		    assertTrue("ReportRootChangesTest", false);
                		}
                    }
                };
            };
        }.testOperation();
    }   
}

