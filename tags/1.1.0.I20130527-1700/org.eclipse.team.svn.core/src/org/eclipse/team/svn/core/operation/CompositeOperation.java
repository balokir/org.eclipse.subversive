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

package org.eclipse.team.svn.core.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Composite operation provide way to combine different operations
 * 
 * @author Alexander Gurov
 */
public class CompositeOperation extends AbstractActionOperation implements IConsoleStream {
	protected List<Pair> operations;
	protected boolean checkWarnings;
	protected int totalWeight;

	public CompositeOperation(String operationName, Class<? extends NLS> messagesClass) {
		this(operationName, messagesClass, false);
	}
	
	public CompositeOperation(String operationName, Class<? extends NLS> messagesClass, boolean checkWarnings) {
		super(operationName, messagesClass);
		this.operations = new ArrayList<Pair>();
		this.checkWarnings = checkWarnings;
		this.totalWeight = 0;
	}
	
	public boolean isEmpty() {
		return this.operations.size() == 0;
	}
	
	public void add(IActionOperation operation) {
		this.add(operation, null);
	}
	
	public void add(IActionOperation operation, IActionOperation []dependsOnOperation) {
		operation.setConsoleStream(this);
		this.operations.add(new Pair(operation, dependsOnOperation));
		this.totalWeight += operation.getOperationWeight();
	}
			
	public void remove(IActionOperation operation) {
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext();) {
			Pair pair = it.next();
			if (pair.operation == operation) {
				if (operation.getConsoleStream() == this) {
					operation.setConsoleStream(null);
				}
				it.remove();
				this.totalWeight -= operation.getOperationWeight();
				break;
			}
		}
	}
	
	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule retVal = null;
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext(); ) {
			Pair pair = it.next();
			retVal = MultiRule.combine(retVal, pair.operation.getSchedulingRule());
		}
		return retVal;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Pair pair = it.next();
			
			boolean errorFound = false;
			if (pair.dependsOnOperation != null) {
				for (int i = 0; i < pair.dependsOnOperation.length; i++) {
					if (pair.dependsOnOperation[i].getStatus().getSeverity() == IStatus.ERROR ||
						this.checkWarnings && pair.dependsOnOperation[i].getStatus().getSeverity() == IStatus.WARNING || 
						pair.dependsOnOperation[i].getExecutionState() == IActionOperation.NOTEXECUTED) {
						errorFound = true;
						break;
					}
				}
			}
			if (!errorFound) {
				ProgressMonitorUtility.doTask(pair.operation, monitor, this.totalWeight, pair.operation.getOperationWeight());
				this.reportStatus(pair.operation.getStatus());
			}
		}
	}

	protected class Pair {
		public IActionOperation operation;
		public IActionOperation []dependsOnOperation;
		
		public Pair(IActionOperation operation, IActionOperation []dependsOnOperation) {
			this.operation = operation;
			this.dependsOnOperation = dependsOnOperation;
		}
	}

	public void markEnd() {
		// do nothing
	}

	public void markStart(String data) {
		// do nothing
	}

	public void write(int severity, String data) {
		this.writeToConsole(severity, data);
	}

	public void doComplexWrite(Runnable runnable) {
		this.complexWriteToConsole(runnable);
	}
	
	public void markCancelled() {
		this.writeCancelledToConsole();
	}

}
