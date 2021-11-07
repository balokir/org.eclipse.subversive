package org.eclipse.team.svn.tests.workflow;

import static org.junit.Assert.fail;

import java.util.function.Supplier;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;

public class ActionOperationWorkflow {
	private Supplier<IActionOperation>[] actionOperations;

	public ActionOperationWorkflow(Supplier<IActionOperation>... actionOperations) {
		this.actionOperations = actionOperations;
	}

	public void execute() {
		for (Supplier<IActionOperation> operation : actionOperations) {
			executeAndValidate(operation.get());
		}
	}

	private void executeAndValidate(IActionOperation op) {
		IStatus operationStatus = op.run(new NullProgressMonitor()).getStatus();
		if (!operationStatus.isOK()) {
			String trace = ReportPartsFactory.getStackTrace(operationStatus);
			fail(operationStatus.getMessage() + trace);
		}
	}
}
