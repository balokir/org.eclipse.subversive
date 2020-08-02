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

import org.eclipse.team.svn.tests.core.CoreTest;
import org.eclipse.team.svn.tests.core.workflow.CommitUpdateTest;
import org.eclipse.team.svn.tests.core.workflow.PLC312Test;
import org.eclipse.team.svn.tests.core.workflow.PLC314Test;
import org.eclipse.team.svn.tests.core.workflow.PLC350Test;
import org.eclipse.team.svn.tests.core.workflow.PLC366Test;
import org.eclipse.team.svn.tests.core.workflow.PLC375Test;
import org.eclipse.team.svn.tests.core.workflow.PLC378Test;
import org.eclipse.team.svn.tests.core.workflow.PLC379Test;
import org.eclipse.team.svn.tests.core.workflow.PLC380Test;
import org.eclipse.team.svn.tests.ui.SVNTeamMoveDeleteHookTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Default test suite factory
 * 
 * @author Alexander Gurov
 */
@RunWith(Suite.class)
@SuiteClasses({ CoreTest.class, CommitUpdateTest.class, SVNTeamMoveDeleteHookTest.class, PLC312Test.class,
		PLC314Test.class, PLC350Test.class, PLC366Test.class, PLC375Test.class, PLC378Test.class, PLC379Test.class,
		PLC380Test.class })
public class AllTests {
	// NIC conditional tests?
//	ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
//	boolean workbenchEnabled = "true".equals(bundle.getString("UI.WorkbenchEnabled"));
//
//
//	if (workbenchEnabled) {
//		suite.addTestSuite(RepositoryViewMenuEnablementTest.class);
//		suite.addTestSuite(JavaViewMenuEnablementTest.class);
//	}

}
