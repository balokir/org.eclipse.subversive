/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * Synchronize model operation wrapper to provide a probability of running the operations
 * by our code.
 * 
 * @author Igor Burilo
 */
public class FilteredSynchronizeModelOperation extends SynchronizeModelOperation {

	protected IActionOperation executable;
	
	public FilteredSynchronizeModelOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, IActionOperation executable) {
		super(configuration, elements);
		this.executable = executable;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (this.executable != null) {
		    ProgressMonitorUtility.doTaskExternal(this.executable, monitor);
		}
	}
	
	protected boolean canRunAsJob() {
		return true;
	}
	
	protected String getJobName() {
		return this.executable == null ? super.getJobName() : this.executable.getOperationName();
	}

}
