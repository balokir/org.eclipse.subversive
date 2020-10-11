package org.eclipse.team.svn.tests.workflow;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.tests.core.misc.TestUtil;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;

public class ActionOperationWorkflow {
	private IActionOperation[] actionOperations;

	public ActionOperationWorkflow(IActionOperation... actionOperations) {
		this.actionOperations = actionOperations;
	}

	public void execute() {
		TestUtil.refreshProjects();
		for (IActionOperation operation : actionOperations) {
			executeAndValidate(operation);
		}
	}

	private void executeAndValidate(IActionOperation op) {
		IStatus operationStatus = op.run(new NullProgressMonitor()).getStatus();
		if (operationStatus.isOK()) {
			assertTrue(op.getOperationName(), true);
		} else {
			String trace = ReportPartsFactory.getStackTrace(operationStatus);
			assertTrue(operationStatus.getMessage() + trace, false);
		}
	}
}
