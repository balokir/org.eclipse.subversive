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

package org.eclipse.team.svn.tests.core;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.tests.core.file.FileOperationFactory;
import org.eclipse.team.svn.tests.core.misc.TestUtil;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Parameterized test for several {@link IActionOperation}s.
 * 
 * @author Alexander Gurov
 * @author Nicolas Peifer
 */
@RunWith(Parameterized.class)
public class ParameterizedOperationTest {

	@Parameter
	public IActionOperation operation;

	@Parameters(name = "#{index}: {0}")
	public static Collection<IActionOperation> createTestData() throws Exception {

		TestUtil.refreshProjects();
		Object[] operationFactories = { new FileOperationFactory(), new RemoteOperationFactory() };

		List<IActionOperation> result = new ArrayList<IActionOperation>();

		for (Object factory : operationFactories) {
			List<IActionOperation> testOperationsFrom = extractTestOperationsFrom(factory);
			// make sure that no refactoring destroys the reflection usage
			assertTrue(testOperationsFrom.size() > 0);
			result.addAll(testOperationsFrom);
		}

		return result;
	}

	private static List<IActionOperation> extractTestOperationsFrom(Object operationFactory)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<IActionOperation> result = new ArrayList<IActionOperation>();

		// retrieve all test operations by using reflection on the corresponding factory
		Method[] declaredMethods = operationFactory.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			result.add((IActionOperation) method.invoke(operationFactory));
		}

		return result;
	}

	@Test
	public void testOperation() {
		TestUtil.refreshProjects();

		this.assertOperation(operation);
	}

	protected void assertOperation(IActionOperation op) {
		IStatus operationStatus = op.run(new NullProgressMonitor()).getStatus();
		if (operationStatus.isOK()) {
			assertTrue(op.getOperationName(), true);
		} else {
			String trace = ReportPartsFactory.getStackTrace(operationStatus);
			assertTrue(operationStatus.getMessage() + trace, false);
		}
	}

}
