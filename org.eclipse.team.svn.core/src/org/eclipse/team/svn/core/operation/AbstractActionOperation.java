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

package org.eclipse.team.svn.core.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Abstract IActionOperation implementation provides default implementation of the status processing and console support
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractActionOperation implements IActionOperation {
	private final MultiStatus status = new MultiStatus(SVNTeamPlugin.NATURE_ID, IStatus.OK, "", null);
	protected String nameId;
	protected String name;
	protected boolean isExecuted;
	protected IConsoleStream consoleStream;
	
	public AbstractActionOperation(String operationName) {
		this.isExecuted = false;
		this.setOperationName(operationName);
		this.updateStatusMessage();
	}
	
	public int getOperationWeight() {
		return IActionOperation.DEFAULT_WEIGHT;
	}
	
	public IConsoleStream getConsoleStream() {
		return this.consoleStream;
	}
	
	public void setConsoleStream(IConsoleStream stream) {
		this.consoleStream = stream;
	}

	public IStatus getStatus() {
		return this.status;
	}

	public ISchedulingRule getSchedulingRule() {
		// do not lock anything by default 
		return null;
	}
	
	public int getExecutionState() {
		if (this.status.isOK()) {
			return this.isExecuted ? IActionOperation.OK : IActionOperation.NOTEXECUTED;
		}
		return AbstractActionOperation.ERROR;
	}

	public final IActionOperation run(IProgressMonitor monitor) {
		try {
			this.updateStatusMessage();
			this.isExecuted = true;
			if (this.consoleStream != null) {
				this.consoleStream.markStart(this.name);
			}
			this.runImpl(monitor);
			if (monitor.isCanceled()) {
				boolean hasCanceledException = false;	
				// We have a multi status, so we iterate the children
				IStatus[] children = this.status.getChildren();
				for (int i = 0; i < children.length; i++) {
					Throwable exception = children[i].getException();
					if (exception instanceof SVNConnectorCancelException || 
						exception instanceof ActivityCancelledException) {
						hasCanceledException = true;
						break;
					}
				}
				if (!hasCanceledException) {
					throw new ActivityCancelledException();
				}
			}
		}
		catch (Throwable t) {
			this.reportError(t);
		}
		finally {
			if (this.consoleStream != null) {
				this.consoleStream.markEnd();
			}
		}
		
		return this;
	}
	
	public void setOperationName(String name) {
		this.nameId = name;
		this.name = this.getNationalizedString(name);
	}
	
	public String getOperationName() {
		return this.name;
	}
	
	public String getId() {
		return this.nameId;
	}
	
	protected abstract void runImpl(IProgressMonitor monitor) throws Exception;
	
	protected void writeCancelledToConsole() {
		if (this.consoleStream != null) {
			this.consoleStream.markCancelled();
		}
	}
	
	protected void writeToConsole(int severity, String data) {
		if (this.consoleStream != null) {
			this.consoleStream.write(severity, data);
		}
	}
	
	protected void complexWriteToConsole(Runnable runnable) {
		if (this.consoleStream != null) {
			this.consoleStream.doComplexWrite(runnable);
		}
	}
	
	protected void protectStep(IUnprotectedOperation step, IProgressMonitor monitor, int subTaskCnt) {
		this.protectStep(step, monitor, IActionOperation.DEFAULT_WEIGHT * subTaskCnt, IActionOperation.DEFAULT_WEIGHT);
	}
	
	protected void protectStep(IUnprotectedOperation step, IProgressMonitor monitor, int totalWeight, int currentWeight) {
		try {
			ProgressMonitorUtility.doSubTask(this, step, monitor, totalWeight, currentWeight);
		}
		catch (Throwable t) {
			this.reportError(t);
		}
	}
	
	protected void reportError(Throwable t) {
		if (t instanceof SVNConnectorCancelException ||
			t instanceof ActivityCancelledException) {
			this.writeCancelledToConsole();
		}
		else {
			this.writeToConsole(IConsoleStream.LEVEL_ERROR, (t.getMessage() != null ? t.getMessage() : this.getShortErrorMessage(t)) + "\n");
		}
		
		this.reportStatus(new Status(IStatus.ERROR, SVNTeamPlugin.NATURE_ID, IStatus.OK, this.getShortErrorMessage(t), t));
	}
	
	protected String getShortErrorMessage(Throwable t) {
		String key = this.nameId + ".Error";
		String retVal = this.getNationalizedString(key);
		if (retVal.equals(key)) {
			return this.status.getMessage() + ": " + t.getMessage();
		}
		return retVal;
	}
	
	protected void reportStatus(IStatus st) {
		if (st.getSeverity() != IStatus.OK) {
			this.status.merge(st);
		}
	}
	
	protected String getOperationResource(String key) {
		return this.getNationalizedString(this.nameId + "." + key);
	}
	
	protected final String getNationalizedString(String key) {
		String retVal = SVNTeamPlugin.instance().getResource(key);
		if (retVal.equals(key)) {
			return CoreExtensionsManager.instance().getOptionProvider().getResource(key);
		}
		return retVal;
	}
	
	private void updateStatusMessage() {
		String key = this.nameId + ".Id";
		String prefix = this.getNationalizedString(key);
		prefix = prefix.equals(key) ? "" : (prefix + ": ");
		String errMessage = SVNTeamPlugin.instance().getResource("Operation.Error.LogHeader", new String[] {prefix + this.name});
		this.status.setMessage(errMessage);
	}
	
	/**
	 * Give us an accessible version of MultiStatus to be able to update the message.
	 * @author Alessandro Nistico
	 */
	private static class MultiStatus extends org.eclipse.core.runtime.MultiStatus {
		public MultiStatus(String pluginId, int code, IStatus[] newChildren, String message, Throwable exception) {
			super(pluginId, code, newChildren, message, exception);
		}

		public MultiStatus(String pluginId, int code, String message, Throwable exception) {
			super(pluginId, code, message, exception);
		}
		
		public void setMessage(String message) {
			super.setMessage(message);
		}
	}
	
}
