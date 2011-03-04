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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
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
	private final MultiStatus status = new MultiStatus(SVNTeamPlugin.NATURE_ID, IStatus.OK, "", null); //$NON-NLS-1$
	protected String nameId;
	protected String name;
	protected boolean isExecuted;
	protected IConsoleStream consoleStream;
	protected Class<? extends NLS> messagesClass;
	
	public AbstractActionOperation(String operationName, Class<? extends NLS> messagesClass) {
		this.isExecuted = false;
		this.messagesClass = messagesClass;
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
			this.reportStatus(IStatus.ERROR, null, t);
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
	
	public Class<? extends NLS> getMessagesClass() {
		return this.messagesClass;
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
			this.reportStatus(IStatus.ERROR, null, t);
		}
	}
	
	protected void reportWarning(String message, Throwable t) { //$NON-NLS-1$
		this.reportStatus(IStatus.WARNING, message, t);
	}
	
	protected void reportError(Throwable t) {
		this.reportStatus(IStatus.ERROR, null, t);
	}
	
	public void reportStatus(int severity, String message, Throwable t) {
		String msg = message != null ? message : this.getShortErrorMessage(t);
		if (severity == IStatus.ERROR) {
			if (t instanceof SVNConnectorCancelException || t instanceof ActivityCancelledException) {
				this.writeCancelledToConsole();
			}
			else {
				this.writeToConsole(IConsoleStream.LEVEL_ERROR, msg + "\n"); //$NON-NLS-1$
			}
		}
		else if (severity == IStatus.WARNING) {
			this.writeToConsole(IConsoleStream.LEVEL_WARNING, message + "\n");			
		}
		this.reportStatus(new Status(severity, SVNTeamPlugin.NATURE_ID, IStatus.OK, msg, t));
	}
	
	protected String getShortErrorMessage(Throwable t) {
		String key = this.nameId + "_Error"; //$NON-NLS-1$
		String retVal = this.getNationalizedString(key);
		if (retVal.equals(key)) {
			return this.status.getMessage() + ": " + (t == null ? "<null>" : t.getMessage()); //$NON-NLS-1$ $NON-NLS-2$
		}
		return retVal;
	}
	
	protected void reportStatus(IStatus st) {
		if (st.getSeverity() != IStatus.OK) {
			this.status.merge(st);
		}
	}
	
	protected String getOperationResource(String key) {
		return this.getNationalizedString(this.nameId + "_" + key); //$NON-NLS-1$
	}
	
	protected final String getNationalizedString(String key) {		
		String retVal = BaseMessages.getErrorString(key, this.messagesClass);
		if (retVal.equals(key)) {
			return CoreExtensionsManager.instance().getOptionProvider().getResource(key);
		}
		return retVal;
	}
	
	private void updateStatusMessage() {
		String key = this.nameId + "_Id"; //$NON-NLS-1$
		String prefix = this.getNationalizedString(key);
		prefix = prefix.equals(key) ? "" : (prefix + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		String errMessage = SVNMessages.format(SVNMessages.Operation_Error_LogHeader, new String[] {prefix + this.name});
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
		
		public void merge(IStatus status) {
			super.merge(status);
			if (status.getSeverity() != IStatus.OK) {
				this.setSeverity(this.getSeverity() | status.getSeverity());
			}
		}
	}
	
}
