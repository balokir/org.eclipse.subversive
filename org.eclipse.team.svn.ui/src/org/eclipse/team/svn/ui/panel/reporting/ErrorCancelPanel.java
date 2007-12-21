/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.reporting;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ReportingComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Error or cancel panel
 *
 * @author Sergiy Logvin
 */
public class ErrorCancelPanel extends AbstractDialogPanel {
	protected static final int ERROR_PANEL_TYPE = 0;
	protected static final int CANCEL_PANEL_TYPE = 1;
	
	protected ReportingComposite reportingComposite;

	protected Text errorTextField;
	protected String optionName;
	protected String simpleMessage;
	protected String advancedMessage;
	protected int panelType;
	protected boolean sendMail;
	
	protected IStatus errorStatus;
	protected String plugin;
	
	protected String originalReport;
	
    public ErrorCancelPanel(String title, int numberOfErrors, String simpleMessage, String advancedMessage, boolean sendMail, String optionName) {
    	this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title, simpleMessage, advancedMessage, sendMail, optionName);
    }
    
    public ErrorCancelPanel(String title, String simpleMessage, String advancedMessage, boolean sendMail, String optionName) {
    	this(ErrorCancelPanel.CANCEL_PANEL_TYPE, 0, title, simpleMessage, advancedMessage, sendMail, optionName);
    }
    
    public ErrorCancelPanel(String title, int numberOfErrors, String simpleMessage, String advancedMessage, boolean sendMail, String optionName, IStatus errorStatus, String plugin) {
    	this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title, simpleMessage, advancedMessage, sendMail, optionName);
    	this.errorStatus = errorStatus;
    	this.plugin = plugin;
    }
    
    public ErrorCancelPanel(String title, int numberOfErrors, String simpleMessage, String advancedMessage, boolean sendMail, String optionName, IStatus errorStatus, String plugin, String originalReport) {
    	this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title, simpleMessage, advancedMessage, sendMail, optionName);
    	this.errorStatus = errorStatus;
    	this.plugin = plugin;
    	this.originalReport = originalReport;
    }
    
    protected ErrorCancelPanel(int panelType, int numberOfErrors, String title, String simpleMessage, String advancedMessage, boolean sendMail, String optionName) {
    	super(sendMail ? new String[] {SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.Send"), SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.DontSend")} : new String[] {IDialogConstants.OK_LABEL});
    	this.panelType = panelType;
    	this.sendMail = sendMail;
    	this.dialogTitle = SVNTeamUIPlugin.instance().getResource(panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Title.Failed" : "ErrorCancelPanel.Title.Cancelled");
    	if (title == null || title.length() == 0) {
    		this.dialogDescription = SVNTeamUIPlugin.instance().getResource(panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Description.Failed.Empty" : "ErrorCancelPanel.Description.Cancelled.Empty");
    	}
    	else {
    		this.dialogDescription = SVNTeamUIPlugin.instance().getResource(panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Description.Failed" : "ErrorCancelPanel.Description.Cancelled");
    		this.dialogDescription = MessageFormat.format(this.dialogDescription, new String[] {title});
    	}
		if (sendMail) {
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.Message.Send");
		} 
		else {
			if (panelType == ErrorCancelPanel.ERROR_PANEL_TYPE) {
				if (numberOfErrors == 1) {
					this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.Message.DontSend.Single");
				}
				else {
					this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.Message.DontSend.Multi");
					this.defaultMessage = MessageFormat.format(this.defaultMessage, new String[] {String.valueOf(numberOfErrors)});
				}
			}
			else {
				this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.Message.DontSend");
			}
		}
		
		this.simpleMessage = simpleMessage == null ? SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.NoInfo") : simpleMessage;
		this.advancedMessage = advancedMessage == null ? SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.NoAdvancedInfo") : advancedMessage;
		this.optionName = optionName;
    }
    
    public IReporter getReporter() {
    	return this.reportingComposite == null ? ReportingComposite.getDefaultReporter(this.panelType == ErrorCancelPanel.ERROR_PANEL_TYPE, this.errorStatus) : this.reportingComposite.getReporter();
    }

    public boolean doNotShowAgain() {
    	return this.reportingComposite != null ? this.reportingComposite.isNotShowAgain() : false;
    }
    
    public void createControlsImpl(Composite parent) {
    	GridData data = null;
    	this.errorTextField = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 100;
		this.errorTextField.setLayoutData(data);
		this.errorTextField.setEditable(false);
		this.errorTextField.setText(this.simpleMessage + "\n" + this.advancedMessage);
		
		if (this.sendMail) {
	    	final Composite mailComposite = new Composite(parent, SWT.FILL);
	    	GridLayout layout = new GridLayout();
	    	layout.marginWidth = 0;
	    	data = new GridData(GridData.FILL_BOTH);
	    	mailComposite.setLayout(layout);
	    	mailComposite.setLayoutData(data);
	    	
	    	Label separator = new Label(mailComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
			data = new GridData(GridData.FILL_HORIZONTAL);
			separator.setLayoutData(data);
			
	    	this.reportingComposite = new ReportingComposite(parent, this.dialogTitle, this.plugin, this.errorStatus, this.optionName, true, this);
			data = new GridData(GridData.FILL_BOTH);
			this.reportingComposite.setLayoutData(data);
		}
		else {
			if (this.originalReport != null) {
				Button viewButton = new Button(parent, SWT.PUSH);
				viewButton.setText(SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.OriginalReport"));
				data = new GridData();
				data.widthHint = DefaultDialog.computeButtonWidth(viewButton);
				viewButton.setLayoutData(data);
				viewButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						PreviewReportPanel panel = new PreviewReportPanel(SVNTeamUIPlugin.instance().getResource("ErrorCancelPanel.OriginalReportPreview"), ErrorCancelPanel.this.originalReport);
						DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getDisplay().getActiveShell(), panel);
						dialog.open();
					}
				});
			}
		}
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.errorDialogContext";
	}
    
    protected void saveChangesImpl() {
    	if (this.sendMail) {
    		this.reportingComposite.saveChanges();
    	}
    }

    protected void cancelChangesImpl() {
    	if (this.sendMail) {
    		this.reportingComposite.cancelChanges();
    	}
    }
    
    protected void showDetails() {
    	
    }
    
    public void postInit() {
    	super.postInit();
    }
    
    public Point getPrefferedSizeImpl() {
        return new Point(640, SWT.DEFAULT);
    }
    
    public String getImagePath() {
    	return "icons/dialogs/" + (this.panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "operation_error.gif" : "select_revision.gif");
    }
    
}
