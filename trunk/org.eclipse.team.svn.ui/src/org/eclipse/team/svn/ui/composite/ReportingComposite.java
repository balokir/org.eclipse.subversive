/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.text.MessageFormat;
import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory.ReportType;
import org.eclipse.team.svn.ui.panel.reporting.PreviewErrorReportPanel;
import org.eclipse.team.svn.ui.panel.reporting.PreviewReportPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Reporting form composite
 * 
 * @author Alexander Gurov
 */
public class ReportingComposite extends Composite {
	public static final String MAIL_HISTORY = "mailHistory";
	public static final String USER_NAME_HISTORY = "userNameHistory";
	
	protected UserInputHistory mailHistory;
	protected UserInputHistory userNameHistory;
	
	protected Combo providersCombo;
	protected Text emailText;
	protected Text nameText;
	protected Text commentText;
	protected Button doNotShowAgainButton;
	protected Button previewButton;
	
	protected boolean doNotShowAgain;
	
	protected String reportType;
	protected boolean isError;
	protected String pluginId;
	protected IStatus status;
	protected IReportingDescriptor []providers;
	
	protected IReporter reporter;
	
	public ReportingComposite(Composite parent, String reportType, String pluginId, IStatus status, String optionName, boolean isError, IValidationManager manager) {
		this(parent, reportType, pluginId, status, optionName, isError, manager, false);
	}
	
	public ReportingComposite(Composite parent, String reportType, String pluginId, IStatus status, String optionName, boolean isError, IValidationManager manager, boolean doNotValidateComment) {
		super(parent, SWT.NONE);
		this.isError = isError;
		this.reportType = reportType;
		this.pluginId = pluginId;
		this.status = status;
		this.providers = ExtensionsManager.getInstance().getReportingDescriptors();
		this.reporter = ReportingComposite.getDefaultReporter(isError, status);
		this.createControls(optionName, manager, doNotValidateComment);
	}
	
	public static IReporter getDefaultReporter(boolean isError, IStatus status) {
		IReportingDescriptor []providers = ExtensionsManager.getInstance().getReportingDescriptors();
		if (providers.length > 0) {
			IReporter reporter = ExtensionsManager.getInstance().getReporter(providers[0], isError ? ReportType.BUG : ReportType.TIP);
			if (reporter != null) {
				reporter.setProblemStatus(status);
				return reporter;
			}
		}
		return null;
	}
	
	public IReporter getReporter() {
		return this.reporter;
	}

	public boolean isNotShowAgain() {
		return this.doNotShowAgain;
	}
    
	public void saveChanges() {
		if (this.commentText != null) {
			this.reporter.setUserComment(this.commentText.getText().trim());
		}
		
		if (this.emailText != null) {
	    	String email = this.emailText.getText().trim();
			this.reporter.setUserEMail(email);
	    	if (email.length() > 0) {
				this.mailHistory.addLine(email);
			} 
			else {
				this.mailHistory.clear();
			}
		}
		
    	if (this.nameText != null) {
        	String name = this.nameText.getText().trim();
    		this.reporter.setUserName(name);
    		if (name.length() > 0) {
    			this.userNameHistory.addLine(name);	
    		}
    		else {
    			this.userNameHistory.clear();
    		}
    	}
    	
		this.doNotShowAgain = this.doNotShowAgainButton.getSelection();
	}
	
	public void cancelChanges() {
		this.doNotShowAgain = this.doNotShowAgainButton.getSelection();
	}

	private void createControls(String optionName, IValidationManager manager, boolean doNotValidateComment) {
    	GridLayout layout = null;
    	GridData data = null;
    	
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		this.setLayout(layout);
		
		if (this.providers.length > 1 || this.reporter == null) {
			Label description1 = new Label(this, SWT.NONE);
			data = new GridData();
			description1.setLayoutData(data);
			description1.setText(SVNTeamUIPlugin.instance().getResource("ReportingComposite.Product"));
			
			this.providersCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.providersCombo.setLayoutData(data);
			FileUtility.sort(this.providers, new Comparator() {
				public int compare(Object arg0, Object arg1) {
					IReportingDescriptor first = (IReportingDescriptor)arg0;
					IReportingDescriptor second = (IReportingDescriptor)arg1;
					return first.getProductName().compareTo(second.getProductName());
				}
			});
			String []names = new String[this.providers.length];
			for (int i = 0; i < this.providers.length; i++) {
				names[i] = this.providers[i].getProductName();
			}
			this.providersCombo.setItems(names);
			this.providersCombo.select(0);
			this.providersCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int selectedProviderIdx = ReportingComposite.this.providersCombo.getSelectionIndex();
					ReportingComposite.this.setReporter(selectedProviderIdx);
				}
			});
		}
    	
		this.mailHistory = new UserInputHistory(ReportingComposite.MAIL_HISTORY, 1);
		this.userNameHistory = new UserInputHistory(ReportingComposite.USER_NAME_HISTORY, 1);
		String []mailName = this.mailHistory.getHistory();
		
		if (this.providers.length > 1 || this.reporter != null && !this.reporter.isCustomEditorSupported() || this.reporter == null) {
			Label description = new Label(this, SWT.WRAP);
			data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this, this.isError ? 4 : 3);
			data.horizontalSpan = 2;
			description.setLayoutData(data);
			description.setText(SVNTeamUIPlugin.instance().getResource(this.isError ? "ReportingComposite.ErrorHint" : "ReportingComposite.Hint"));
			
			Label description2 = new Label(this, SWT.NONE);
			data = new GridData();
			description2.setLayoutData(data);
			description2.setText(SVNTeamUIPlugin.instance().getResource("ReportingComposite.EMail"));
			
			this.emailText = new Text(this, SWT.BORDER);			
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.emailText.setLayoutData(data);
			this.emailText.setFocus();
			if (mailName != null && mailName.length > 0) {
				this.emailText.setText(mailName[0]);
			}
			
			Label description3 = new Label(this, SWT.NONE);
			data = new GridData();
			description3.setLayoutData(data);
			description3.setText(SVNTeamUIPlugin.instance().getResource("ReportingComposite.Name"));
			
			this.nameText = new Text(this, SWT.BORDER);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.nameText.setLayoutData(data);
			String []userName = this.userNameHistory.getHistory();
			if (userName != null && userName.length > 0) {
				this.nameText.setText(userName[0]);
			}
			
			Label commentLabel = new Label(this, SWT.LEFT);
			data = new GridData();
			data.horizontalSpan = 2;
			commentLabel.setLayoutData(data);
			commentLabel.setText(SVNTeamUIPlugin.instance().getResource("ReportingComposite.Comment"));
			
			this.commentText = new Text(this, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
			data = new GridData(GridData.FILL_BOTH);
			data.heightHint = 100;
			data.horizontalSpan = 2;
			this.commentText.setLayoutData(data);
			if (mailName != null && mailName.length > 0) {
				this.commentText.setFocus();
			}
			if (manager != null) {
				manager.attachTo(this.commentText, new AbstractVerifier() {
					protected String getWarningMessage(Control input) {
						return null;
					}
					protected String getErrorMessage(Control input) {
						if (ReportingComposite.this.getReporter() == null) {
							return SVNTeamUIPlugin.instance().getResource("ReportingComposite.Product.Verifier");
						}
						return null;
					}
				});
				if (!doNotValidateComment) {
					manager.attachTo(this.commentText, new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("ReportingComposite.Comment.Verifier")));
				}
			}
		}
		
		Composite buttonsComposite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = 2;
		buttonsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		buttonsComposite.setLayoutData(data);
		
		this.doNotShowAgainButton = new Button(buttonsComposite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.doNotShowAgainButton.setLayoutData(data);
		this.doNotShowAgainButton.setSelection(false);
		if (optionName != null) {
			this.doNotShowAgainButton.setText(optionName);
		}
		else {
			this.doNotShowAgainButton.setVisible(false);
		}
		
		if (this.providers.length > 1 || this.reporter != null && !this.reporter.isCustomEditorSupported() || this.reporter == null) {
			this.previewButton = new Button(buttonsComposite, SWT.PUSH);
			data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL);
			this.previewButton.setText(SVNTeamUIPlugin.instance().getResource("ReportingComposite.Preview"));
			data.widthHint = DefaultDialog.computeButtonWidth(previewButton);
			this.previewButton.setLayoutData(data);
					
			this.previewButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
			    	String name = ReportingComposite.this.nameText.getText().trim();
			    	String email = ReportingComposite.this.emailText.getText().trim();
			    	ReportingComposite.this.reporter.setUserName(name);
					ReportingComposite.this.reporter.setUserEMail(email);
					ReportingComposite.this.reporter.setUserComment(ReportingComposite.this.commentText.getText().trim());
					ReportingComposite.this.reporter.buildReport();
					
					PreviewReportPanel panel = null;
					if (ReportingComposite.this.isError) {
						panel = new PreviewErrorReportPanel(ReportingComposite.this.reporter.buildReport());
					}
					else {
						String msg = SVNTeamUIPlugin.instance().getResource("ReportingComposite.Preview.Title");
						panel = new PreviewReportPanel(MessageFormat.format(msg, new String[] {ReportingComposite.this.reportType}), ReportingComposite.this.reporter.buildReport());
					}
					DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getDisplay().getActiveShell(), panel);
					dialog.open();
				}
			});
		}
		this.setEnablement();
	}
	
	protected void setReporter(int selectedProviderIdx) {
		if (this.providers.length > selectedProviderIdx) {
			this.reporter = ExtensionsManager.getInstance().getReporter(this.providers[selectedProviderIdx], this.isError ? ReportType.BUG : ReportType.TIP);
			if (this.reporter != null) {
				this.reporter.setProblemStatus(this.status);
			}
		}
		this.setEnablement();
	}
	
	protected void setEnablement() {
		boolean enabled = this.reporter != null && !this.reporter.isCustomEditorSupported();
		if (this.previewButton != null) {
			this.previewButton.setEnabled(enabled);
		}
		if (this.nameText != null) {
			this.nameText.setEnabled(enabled);
		}
		if (this.emailText != null) {
			this.emailText.setEnabled(enabled);
		}
	}
	
}
