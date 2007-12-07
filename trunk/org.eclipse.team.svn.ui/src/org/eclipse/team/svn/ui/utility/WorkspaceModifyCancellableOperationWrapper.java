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

package org.eclipse.team.svn.ui.utility;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * This wrapper allow to run operations that potentially can modify workspace
 * 
 * @author Alexander Gurov
 */
public class WorkspaceModifyCancellableOperationWrapper extends WorkspaceModifyOperation implements ICancellableOperationWrapper {
	protected IProgressMonitor attachedMonitor;
	protected IActionOperation operation;
	
	public WorkspaceModifyCancellableOperationWrapper(IActionOperation operation) {
		super(operation.getSchedulingRule());
		this.operation = operation;
		this.attachedMonitor = new NullProgressMonitor();
	}
	
	public void setCancelled(boolean cancelled) {
		this.attachedMonitor.setCanceled(cancelled);
	}
	
	public boolean isCancelled() {
		return this.attachedMonitor.isCanceled();
	}
	
	public IActionOperation getOperation() {
		return this.operation;
	}
	
	public String getOperationName() {
		return this.operation.getOperationName();
	}

	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		monitor.setCanceled(this.attachedMonitor.isCanceled());
		this.attachedMonitor = monitor;
		// wrap external monitor and make instance of SubProgressMonitorWithInfo
		ProgressMonitorUtility.doTaskExternal(this.operation, this.attachedMonitor, null);
	}
	
}
