/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.junit.Test;

/**
 * Tests for the abstract class.
 * 
 * @author Alessandro Nistico
 */
public class AbstractOperationTest {

	@Test
	public void testSetOperationName() {
		AbstractActionOperation op = new AbstractActionOperation("old name", SVNMessages.class) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return null;
			}
		};
		assertEquals("old name", op.getOperationName());
		op.setOperationName("new name");
		assertEquals("new name", op.getOperationName());
	}

	@Test
	public void testGetExecutionState() {
		// test the failure of an execution
		AbstractActionOperation op = new AbstractActionOperation("name", SVNMessages.class) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return null;
			}
		};
		op.run(new NullProgressMonitor());
		assertEquals(AbstractActionOperation.ERROR, op.getExecutionState());
	}

	@Test
	public void testRun() {
		// test the cancellation of an execution
		AbstractActionOperation op = new AbstractActionOperation("name", SVNMessages.class) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				monitor.setCanceled(true);
				reportStatus(new IStatus() {
					public IStatus[] getChildren() {
						return null;
					}

					public int getCode() {
						return 0;
					}

					public Throwable getException() {
						return new SVNConnectorCancelException("Cancelled");
					}

					public String getMessage() {
						return null;
					}

					public String getPlugin() {
						return null;
					}

					public int getSeverity() {
						return IStatus.CANCEL;
					}

					public boolean isMultiStatus() {
						return false;
					}

					public boolean isOK() {
						return false;
					}

					public boolean matches(int severityMask) {
						return false;
					}
				});
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return null;
			}
		};

		op.setConsoleStream(new IConsoleStream() {
			public void doComplexWrite(Runnable runnable) {
			}

			public void markCancelled() {
			}

			public void markEnd() {
			}

			public void markStart(String data) {
			}

			public void write(int severity, String data) {
			}
		});
		op.run(new NullProgressMonitor());
	}

	@Test
	public void testGetShortErrorMessage() {
		String expected = "SVN: 'op name' operation finished with error: Throwable";
		class MockAbstractActionOperation extends AbstractActionOperation {
			public MockAbstractActionOperation() {
				super("op name", SVNMessages.class);
			}

			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return null;
			}

			@Override
			public String getShortErrorMessage(Throwable t) {
				return super.getShortErrorMessage(t);
			}
		}
		;
		MockAbstractActionOperation op = new MockAbstractActionOperation();
		assertEquals(expected, op.getShortErrorMessage(new Throwable("Throwable")));
	}
}
