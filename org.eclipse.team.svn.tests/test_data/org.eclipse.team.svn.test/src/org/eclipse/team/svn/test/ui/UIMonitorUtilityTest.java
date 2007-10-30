/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.test.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.team.svn.core.operation.common.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.common.IActionOperation;
import org.eclipse.team.svn.core.operation.common.LoggedOperation;
import org.eclipse.team.svn.test.TestPlugin;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.WorkspaceModifyOperationWrapperFactory;

import junit.framework.TestCase;

/**
 * UIMonitorUtility test
 * 
 * @author Alexander Gurov
 */
public class UIMonitorUtilityTest extends TestCase {

	public void testDoTaskScheduledAsTeamWorkspaceModify() {
		IWorkbenchPart part = TestPlugin.instance().getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		UIMonitorUtility.doTaskScheduled(part, new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsTeamDefault() {
		IWorkbenchPart part = TestPlugin.instance().getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		UIMonitorUtility.doTaskScheduled(part, new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsJobWorkspaceModify() {
		UIMonitorUtility.doTaskScheduled(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskScheduledAsJobDefault() {
		UIMonitorUtility.doTaskScheduled(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskNowWorkspaceModify() {
		Shell shell = TestPlugin.instance().getWorkbench().getDisplay().getActiveShell();
		UIMonitorUtility.doTaskNow(shell, new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		false, 
		new WorkspaceModifyOperationWrapperFactory() {
	        public IActionOperation getLogged(IActionOperation operation) {
	            return new LoggedOperation(operation);
	        }
	    });
	}

	public void testDoTaskNowDefault() {
		Shell shell = TestPlugin.instance().getWorkbench().getDisplay().getActiveShell();
		UIMonitorUtility.doTaskNow(shell, new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		false, 
		new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskBusyDefault() {
		UIMonitorUtility.doTaskBusy(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskBusyWorkspaceModify() {
		UIMonitorUtility.doTaskBusy(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new WorkspaceModifyOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskExternalDefault() {
		UIMonitorUtility.doTaskExternal(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new NullProgressMonitor(), 
		new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
                return new LoggedOperation(operation);
            }
        });
	}

	public void testDoTaskExternalWorkspaceModify() {
		UIMonitorUtility.doTaskExternal(new AbstractActionOperation("Test") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}
		}, 
		new NullProgressMonitor(), 
		new WorkspaceModifyOperationWrapperFactory() {
	        public IActionOperation getLogged(IActionOperation operation) {
	            return new LoggedOperation(operation);
	        }
	    });
	}

}
