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

package org.eclipse.team.svn.tests.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test operation workflow
 * 
 * @author Alexander Gurov
 * @author Nicolas Peifer
 */
@RunWith(Parameterized.class)
public class ParameterizedWorkflowTest {

	@Parameter
	public ActionOperationWorkflow workflow;

	@Parameters(name = "#{index}: {0}")
	public static Collection<ActionOperationWorkflow> createTestData() throws Exception {
		ActionOperationWorkflowBuilder workflowBuilder = new ActionOperationWorkflowBuilder();
		List<ActionOperationWorkflow> result = new ArrayList<ActionOperationWorkflow>();
		result.add(workflowBuilder.buildCoreWorkflow());
		result.add(workflowBuilder.buildCommitUpdateWorkflow());
		result.add(workflowBuilder.buildPlc312Workflow());
		result.add(workflowBuilder.buildPlc314Workflow());
		result.add(workflowBuilder.buildPlc350Workflow());
		result.add(workflowBuilder.buildPlc366Workflow());
		result.add(workflowBuilder.buildPlc375Workflow());
		result.add(workflowBuilder.buildPlc378Workflow());
		result.add(workflowBuilder.buildPlc379Workflow());
		result.add(workflowBuilder.buildPlc380Workflow());
		result.add(workflowBuilder.buildFileWorkflow());
		return result;
	}

	@Test
	public void test() {
		workflow.execute();
	}

}
