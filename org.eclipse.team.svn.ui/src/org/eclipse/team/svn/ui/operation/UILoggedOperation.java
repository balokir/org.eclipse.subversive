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

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorAuthenticationException;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.HiddenException;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.debugmail.Reporter;
import org.eclipse.team.svn.ui.dialog.AdvancedDialog;
import org.eclipse.team.svn.ui.extension.factory.IMailSettingsProvider;
import org.eclipse.team.svn.ui.panel.reporting.ErrorCancelPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
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
    	new Thread() {
    		public void run() {
    			final Shell shell = UIMonitorUtility.getShell();
    			shell.getDisplay().syncExec(new Runnable() {
    	            public void run() {
    	            	boolean showCheckBox = SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ENABLED_NAME);
    	            	boolean doNotShowAgain = UILoggedOperation.showErrorImpl(
    	            			shell, 
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
    		}
    	}.start();
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
    	final ErrorCancelPanel panel;
    	//For example, if there is an NPE in the JavaSVN code or in our code - add option "Send Report" to the ErrorDialog
        //also interesting problems can be located before/after ClientCancelException, we shouldn't ignore that
    	boolean sendReport = isReportingAllowed && SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ENABLED_NAME) && Reporter.checkStatus(errorStatus, new ErrorReasonVisitor());
    	if (originalReport == null) {
            panel = new ErrorCancelPanel(operationName, errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, sendReport, optionName, errorStatus, pluginID);
    	}
    	else {
    		panel = new ErrorCancelPanel(operationName, errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, sendReport, optionName, errorStatus, pluginID, originalReport);
    	}
        AdvancedDialog dialog = new AdvancedDialog(shell, panel, sendReport ? 1 : 0);
        if (dialog.open() == 0 && sendReport) {
			UIMonitorUtility.doTaskNowDefault(shell, new AbstractActionOperation("Operation.MailSending") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					try {
						Reporter.sendReport(panel.getMailSettingsProvider(), errorStatus, pluginID, operationName, panel.getComment(), panel.getEmail(), panel.getName(), panel.getReportId());	
					}
					catch (Exception ex) {
						UILoggedOperation.showSendingError(ex, shell, panel.getMailSettingsProvider(), panel.getReport());
					}
				}
			}, false);
		}
		return panel.doNotShowAgain();
    }
    
    public static void showSendingError(final Throwable ex, final Shell shell, final IMailSettingsProvider provider, final String originalReport) {
		if (SVNTeamPreferences.getMailReporterBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME)) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					String email = provider.getEmailTo();
					boolean doNotShowAgain = UILoggedOperation.showErrorImpl(
							shell, 
							SVNTeamPlugin.NATURE_ID, 
							SVNTeamUIPlugin.instance().getResource("UILoggedOperation.SendReport.Error.Title"), 
							new Status(IStatus.ERROR, SVNTeamPlugin.NATURE_ID, IStatus.OK, MessageFormat.format(SVNTeamUIPlugin.instance().getResource("UILoggedOperation.SendReport.Error.Message"), new String[] {email}), ex), 
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
    
	protected static class ErrorReasonVisitor implements Reporter.IStatusVisitor {
		public boolean visit(IStatus status) {
			Throwable t = status.getException();
			if (t == null || t instanceof OperationCanceledException) {
				return false;
			}
			if (t instanceof SVNConnectorException) {
				if (t instanceof SVNConnectorCancelException ||
					t instanceof SVNConnectorAuthenticationException) {
					return false;
				}
				return ((SVNConnectorException)t).isRuntime();
			}
			return !(t instanceof UnreportableException);
		}
	}
	
}
