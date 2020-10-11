package org.eclipse.team.svn.tests.core.misc;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * @author Alexander Gurov
 */
public abstract class AbstractLockingTestOperation extends AbstractActionOperation {
	public AbstractLockingTestOperation(String operationName) {
		super(operationName, SVNMessages.class);
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return MultiRule.combine(TestUtil.getFirstProject(), TestUtil.getSecondProject());
	}

}
