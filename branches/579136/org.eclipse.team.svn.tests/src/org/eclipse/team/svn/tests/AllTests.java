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

package org.eclipse.team.svn.tests;

import org.eclipse.team.svn.tests.core.AbstractOperationTest;
import org.eclipse.team.svn.tests.core.RepositoryLocationsManagementTest;
import org.eclipse.team.svn.tests.core.StateFilterTest;
import org.eclipse.team.svn.tests.ui.DecoratorVariablesTest;
import org.eclipse.team.svn.tests.ui.JavaViewMenuEnablementTest;
import org.eclipse.team.svn.tests.ui.RepositoryViewMenuEnablementTest;
import org.eclipse.team.svn.tests.ui.SVNTeamMoveDeleteHookTest;
import org.eclipse.team.svn.tests.ui.UIMonitorUtilityTest;
import org.eclipse.team.svn.tests.workflow.ParameterizedWorkflowTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Default test suite factory
 * 
 * @author Alexander Gurov
 * @author Nicolas Peifer
 */
@RunWith(Suite.class)
@SuiteClasses({ AbstractOperationTest.class, DecoratorVariablesTest.class, JavaViewMenuEnablementTest.class,
		 ParameterizedWorkflowTest.class, RepositoryLocationsManagementTest.class,
		RepositoryViewMenuEnablementTest.class, StateFilterTest.class, SVNTeamMoveDeleteHookTest.class,
		UIMonitorUtilityTest.class })
public class AllTests {
	// no implementation needed
}
