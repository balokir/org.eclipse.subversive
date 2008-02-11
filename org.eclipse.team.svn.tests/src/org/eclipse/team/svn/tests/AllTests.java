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

package org.eclipse.team.svn.tests;

import java.util.ResourceBundle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
import org.eclipse.team.svn.tests.ui.JavaViewMenuEnablementTest;
import org.eclipse.team.svn.tests.ui.RepositoryViewMenuEnablementTest;
import org.eclipse.team.svn.tests.ui.SVNTeamMoveDeleteHookTest;

/**
 * Default test suite factory
 * 
 * @author Alexander Gurov
 */
public class AllTests extends TestCase {
	public static Test suite() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		boolean workbenchEnabled = "true".equals(bundle.getString("UI.WorkbenchEnabled"));

		TestSuite suite = new TestSuite("SVN Tests");

		suite.addTestSuite(CoreTest.class);
		suite.addTestSuite(CommitUpdateTest.class);
		suite.addTestSuite(SVNTeamMoveDeleteHookTest.class);
		if (workbenchEnabled) {
			suite.addTestSuite(RepositoryViewMenuEnablementTest.class);
			suite.addTestSuite(JavaViewMenuEnablementTest.class);
		}
		suite.addTestSuite(PLC312Test.class);
		suite.addTestSuite(PLC314Test.class);
		suite.addTestSuite(PLC350Test.class);
		suite.addTestSuite(PLC366Test.class);
		suite.addTestSuite(PLC375Test.class);
		suite.addTestSuite(PLC378Test.class);
		suite.addTestSuite(PLC379Test.class);
		suite.addTestSuite(PLC380Test.class);

		return suite;
	}

	public void testAll() {
		AllTests.suite().run(this.createResult());
	}

}
