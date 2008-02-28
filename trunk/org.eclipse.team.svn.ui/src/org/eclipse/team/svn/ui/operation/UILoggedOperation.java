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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorAuthenticationException;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.HiddenException;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;
import org.eclipse.team.svn.ui.panel.reporting.ErrorCancelPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * UI LoggedOperation allow us to show error message after operation fails
 * 
 * @author Alexander Gurov
 */
public class UILoggedOperation extends LoggedOperation {
	
    public UILoggedOperation(IActionOperation op) {
        super(op);
    }
    
    public static void reportError(String where, Throwable t) {
		String errMessage = SVNTeamPlugin.instance().getResource("Operation.Error.LogHeader", new String[] {where});
	    MultiStatus status = new MultiStatus(SVNTeamPlugin.NATURE_ID, IStatus.OK, errMessage, null);
		Status st = 
			new Status(
					IStatus.ERROR, 
					SVNTeamPlugin.NATURE_ID, 
					IStatus.OK, 
					status.getMessage() + ": " + t.getMessage(), 
					t);
		status.merge(st);
		UILoggedOperation.logError(st);
		UILoggedOperation.showError(SVNTeamPlugin.NATURE_ID, where, st, true);
	}

    protected void handleError(IStatus errorStatus) {
    	super.handleError(errorStatus);
    	if (errorStatus.matches(IStatus.ERROR)) {
    		UILoggedOperation.showError(SVNTeamPlugin.NATURE_ID, this.getOperationName(), errorStatus, true);
    	}
    }
    
    public static void showError(final String pluginID, final String operationName, final IStatus errorStatus, final boolean isReportingAllowed) {
    	OperationErrorInfo errorInfo = UILoggedOperation.formatMessage(errorStatus, false);
        if (errorInfo == null) {
        	return;
        }
    	// release calling thread
		Job job = new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
    			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
    	            public void run() {
    	            	boolean showCheckBox = SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ENABLED_NAME);
    	            	boolean doNotShowAgain = UILoggedOperation.showErrorImpl(
    	            			UIMonitorUtility.getShell(), 
    	            			pluginID, 
    	            			operationName, 
    	            			errorStatus, 
    	            			isReportingAllowed, 
    	            			showCheckBox ? SVNTeamUIPlugin.instance().getResource("UILoggedOperation.DontAskSend") : null,
            					null);
    					if (showCheckBox && doNotShowAgain) {
    						SVNTeamPreferences.setMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ENABLED_NAME, false);
    					}
    	            }
    	        });
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
    }
    
    public static OperationErrorInfo formatMessage(IStatus status, boolean allowsCancelled) {
        if (!status.isMultiStatus()) {
        	Throwable ex = status.getException();
        	if (!allowsCancelled && (ex instanceof SVNConnectorCancelException || ex instanceof ActivityCancelledException || ex instanceof OperationCanceledException) || 
        		ex instanceof HiddenException) {
        		return null;
        	}
        	String simpleMsg = UILoggedOperation.getSimpleMessage(status);
        	if (ex instanceof SVNConnectorCancelException || 
        		ex instanceof ActivityCancelledException || 
        		ex instanceof OperationCanceledException || 
        		ex instanceof SVNConnectorAuthenticationException) {
        		return new OperationErrorInfo(simpleMsg, simpleMsg, ex, 1);
        	}
        	String advancedMsg = UILoggedOperation.getSingleStatusMessage(status);
           	return new OperationErrorInfo(simpleMsg, advancedMsg, ex, 1);
        }
        
        IStatus []children = status.getChildren();
        String advanceMess = "";
        String simpleMess = "";
        for (int i = 0; i < children.length; i++) {
            Throwable exception = children[i].getException();
        	if (!allowsCancelled && (exception instanceof SVNConnectorCancelException || exception instanceof ActivityCancelledException || exception instanceof OperationCanceledException) || 
        		exception instanceof HiddenException) {
        		continue;
        	}
        	String simpleMsg = UILoggedOperation.getSimpleMessage(children[i]);
        	String advancedMsg = UILoggedOperation.getSingleStatusMessage(children[i]);
        	advanceMess += advanceMess.length() == 0 ? advancedMsg : ("\n\n" + advancedMsg);
    		simpleMess += simpleMess.length() == 0 ? simpleMsg : ("\n" + simpleMsg);
        	if (exception instanceof SVNConnectorCancelException || 
        		exception instanceof ActivityCancelledException || 
        		exception instanceof OperationCanceledException || 
        		exception instanceof SVNConnectorAuthenticationException) {
            	return new OperationErrorInfo(simpleMess, advanceMess, exception, i + 1);
            }
        }
        
        return advanceMess.length() == 0 && simpleMess.length() == 0 ? null : new OperationErrorInfo(simpleMess, advanceMess, null, children.length);
    }
    
    protected static String getSimpleMessage(IStatus status) {
    	if (status.getException() instanceof SVNConnectorCancelException ||
    		status.getException() instanceof ActivityCancelledException ||
    		status.getException() instanceof OperationCanceledException) {
    		return SVNTeamUIPlugin.instance().getResource("UILoggedOperation.Cancelled");
    	}
    	
    	if (status.getException() instanceof SVNConnectorAuthenticationException) {
    		return SVNTeamUIPlugin.instance().getResource("UILoggedOperation.Authentication");
    	}
    	
    	return status.getMessage();
    }
    
    protected static String getSingleStatusMessage(IStatus status) {
    	if (status.getException() == null) {
    		if (status.getMessage() != null) {
    			return status.getMessage();
    		}
    		return SVNTeamUIPlugin.instance().getResource("UILoggedOperation.Unknown");
    	}
    	
    	if (status.getException() instanceof SVNConnectorCancelException ||
    		status.getException() instanceof ActivityCancelledException) {
    		return SVNTeamUIPlugin.instance().getResource("UILoggedOperation.Cancelled");
    	}
    	
    	if (status.getException().getMessage() == null) {
    		return status.getException().getClass().getName();
        }
    	
    	return status.getException().getMessage();
    }

    public static class OperationErrorInfo {
    	public String simpleMessage;
    	public String advancedMessage;
    	public Throwable exception;
    	public int numberOfErrors;
    	
    	public OperationErrorInfo(String simpleMessage, String advancedMessage, Throwable exception, int numberOfErrors) {
    		this.simpleMessage = simpleMessage;
    		this.advancedMessage = advancedMessage;
    		this.exception = exception;
    		this.numberOfErrors = numberOfErrors;
    	}
     }
    
    protected static boolean showErrorImpl(final Shell shell, final String pluginID, final String operationName, final IStatus errorStatus, boolean isReportingAllowed, String optionName, String originalReport) {
    	OperationErrorInfo errorInfo = UILoggedOperation.formatMessage(errorStatus, false);
    	if (errorInfo == null) {
    		// cancelled
    		return true;
    	}
    	final ErrorCancelPanel panel;
    	//For example, if there is an NPE in the JavaSVN code or in our code - add option "Send Report" to the ErrorDialog
        //also interesting problems can be located before/after ClientCancelException, we shouldn't ignore that
    	boolean sendReport = isReportingAllowed && SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ENABLED_NAME);
    	boolean isPlugInError = ReportPartsFactory.checkStatus(errorStatus, new ErrorReasonVisitor());
    	sendReport &= isPlugInError;
    	if (originalReport == null) {
            panel = new ErrorCancelPanel(operationName, errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, sendReport, isPlugInError, optionName, errorStatus, pluginID);
    	}
    	else {
    		panel = new ErrorCancelPanel(operationName, errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, sendReport, isPlugInError, optionName, errorStatus, pluginID, originalReport);
    	}
        DefaultDialog dialog = new DefaultDialog(shell, panel);
        if (dialog.open() == 0 && sendReport) {
			IReporter reporter = panel.getReporter();
			UIMonitorUtility.doTaskNow(shell, reporter, true, new DefaultOperationWrapperFactory() {
				protected IActionOperation wrappedOperation(IActionOperation operation) {
					return new LoggedOperation(operation);
				}
			});
			if (reporter.getExecutionState() != IActionOperation.OK) {
				UILoggedOperation.showSendingError(reporter.getStatus(), shell, reporter.getReportingDescriptor(), reporter.buildReport());
			}
		}
		return panel.doNotShowAgain();
    }
    
    public static void showSendingError(Throwable ex, Shell shell, IReportingDescriptor provider, String originalReport) {
    	UILoggedOperation.showSendingError(new Status(IStatus.ERROR, SVNTeamPlugin.NATURE_ID, IStatus.OK, SVNTeamUIPlugin.instance().getResource("UILoggedOperation.SendReport.Error.Message", new String[] {provider.getEmailTo()}), ex), shell, provider, originalReport);
	}
    
    public static void showSendingError(final IStatus st, final Shell shell, IReportingDescriptor provider, final String originalReport) {
		if (SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME)) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					boolean doNotShowAgain = UILoggedOperation.showErrorImpl(
							shell, 
							SVNTeamPlugin.NATURE_ID, 
							SVNTeamUIPlugin.instance().getResource("UILoggedOperation.SendReport.Error.Title"), 
							st, 
							false, 
							SVNTeamUIPlugin.instance().getResource("UILoggedOperation.SendReport.Error.DontShow"),
							originalReport);
					if (doNotShowAgain) {
						SVNTeamPreferences.setMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME, false);
					}
				}
			});
		}
	}
    
	protected static class ErrorReasonVisitor implements ReportPartsFactory.IStatusVisitor {
		public boolean visit(IStatus status) {
			Throwable t = status.getException();
			return !(t == null || t instanceof OperationCanceledException || t instanceof SVNConnectorException || t instanceof UnreportableException);
		}
	}
	
}
