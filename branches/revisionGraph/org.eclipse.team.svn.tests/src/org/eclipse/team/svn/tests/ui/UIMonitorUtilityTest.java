/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.ui;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.WorkspaceModifyOperationWrapperFactory;
import org.eclipse.ui.IWorkbenchPart;

/**
 * UIMonitorUtility test
 * 
 * @author Alexander Gurov
 */
public class UIMonitorUtilityTest extends TestCase {
	public void testDoTaskScheduledAsTeamWorkspaceModify() {
		IWorkbenchPart part = TestPlugin.instance().getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		UIMonitorUtility.doTaskScheduled(part, new TestFailureOperation(), new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsTeamDefault() {
		IWorkbenchPart part = TestPlugin.instance().getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		UIMonitorUtility.doTaskScheduled(part, new TestFailureOperation(), new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsJobWorkspaceModify() {
		UIMonitorUtility.doTaskScheduled(new TestFailureOperation(), new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsJobDefault() {
		UIMonitorUtility.doTaskScheduled(new TestFailureOperation(), new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskNowWorkspaceModify() {
		Shell shell = TestPlugin.instance().getWorkbench().getDisplay().getActiveShell();
		UIMonitorUtility.doTaskNow(shell, new TestFailureOperation(), false, new WorkspaceModifyOperationWrapperFactory() {
	        public IActionOperation getLogged(IActionOperation operation) {
	            return new LoggedOperation(operation);
	        }
	    });
	}

	public void testDoTaskNowDefault() {
		Shell shell = TestPlugin.instance().getWorkbench().getDisplay().getActiveShell();
		UIMonitorUtility.doTaskNow(shell, new TestFailureOperation(), false, new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskBusyDefault() {
		UIMonitorUtility.doTaskBusy(new TestFailureOperation(), new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskBusyWorkspaceModify() {
		UIMonitorUtility.doTaskBusy(new TestFailureOperation(), new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	protected class TestFailureOperation extends AbstractActionOperation {
		public TestFailureOperation() {
			super("Test");
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			throw new Exception();
		}
		
	}
	
}
