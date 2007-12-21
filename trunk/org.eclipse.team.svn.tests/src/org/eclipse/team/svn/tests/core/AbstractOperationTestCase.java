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

package org.eclipse.team.svn.tests.core;

import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;

/**
 * Abstract operation test
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractOperationTestCase extends TestCase {
	
	public void testSetOperationName() {
		AbstractActionOperation op = new AbstractActionOperation("old name") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			}

			public ISchedulingRule getSchedulingRule() {
				return null;
			}			
		};
		assertEquals("old name", op.getOperationName());
		op.setOperationName("new name");
		assertEquals("new name", op.getOperationName());
	}
	
	public void testGetExecutionState() {
		// test the failure of an execution
		AbstractActionOperation op = new AbstractActionOperation("old name") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				throw new Exception();
			}

			public ISchedulingRule getSchedulingRule() {
				return null;
			}			
		};
		op.run(new NullProgressMonitor());
		assertEquals(AbstractActionOperation.ERROR, op.getExecutionState());
	}
	
	public void testOperation() {
		this.refreshProjects();
		
		this.assertOperation(getOperation());
	}
		
	protected abstract IActionOperation getOperation();
	
	protected IProject getFirstProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project1.Name"));
	}
	
	protected IProject getSecondProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project2.Name"));
	}
	
	protected IRepositoryLocation getLocation() {
		return SVNRemoteStorage.instance().getRepositoryLocations()[0];
	}
	
	protected void assertOperation(IActionOperation op) {
		IStatus operationStatus = op.run(new NullProgressMonitor()).getStatus();
		if (operationStatus.isOK()) {
			assertTrue(op.getOperationName(), true);
		}
		else {
			String trace = ReportPartsFactory.getStackTrace(operationStatus);
			assertTrue(operationStatus.getMessage() + trace, false);
		}		
	}
	
	protected void refreshProjects() {
		final boolean []refreshDone = new boolean[1];
		IResourceStatesListener listener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				synchronized (AbstractOperationTestCase.this) {
					refreshDone[0] = true;
					AbstractOperationTestCase.this.notify();
				}
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, listener);
		try {
			if (!this.getFirstProject().isSynchronized(IResource.DEPTH_INFINITE) || !this.getSecondProject().isSynchronized(IResource.DEPTH_INFINITE)) {
				this.getFirstProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				this.getSecondProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				
				synchronized (this) {
					if (!refreshDone[0]) {
						this.wait(120000);
					}
					if (!refreshDone[0]) {
						throw new RuntimeException("No refresh event is generated !");
					}
				}
			}
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, listener);
		}
	}
	
	protected abstract class AbstractLockingTestOperation extends AbstractActionOperation {
		public AbstractLockingTestOperation(String operationName) {
			super(operationName);
		}

		public ISchedulingRule getSchedulingRule() {
			return MultiRule.combine(AbstractOperationTestCase.this.getFirstProject(), AbstractOperationTestCase.this.getSecondProject());
		}
		
	}
	
}
