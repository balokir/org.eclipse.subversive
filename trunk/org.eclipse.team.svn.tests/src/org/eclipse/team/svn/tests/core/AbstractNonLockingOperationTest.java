/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.tests.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;

import junit.framework.TestCase;

/**
 * @author Alessandro Nistico
 * 
 */
public abstract class AbstractNonLockingOperationTest extends TestCase {
	public void testGetSchedulingRule() {
		AbstractNonLockingOperation anlop = new AbstractNonLockingOperation(
				"Test Non Locking") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			}
		};
		ISchedulingRule testRule = new ISchedulingRule() {
			public boolean contains(ISchedulingRule rule) {
				return false;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return false;
			}
		};
		ISchedulingRule rule = anlop.getSchedulingRule();
		assertFalse(rule.isConflicting(null));
		assertFalse(rule.isConflicting(testRule));
		assertTrue(rule.isConflicting(rule));

		assertFalse(rule.contains(null));
		assertFalse(rule.contains(testRule));
		assertTrue(rule.contains(rule));
	}
}
