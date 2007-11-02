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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.resource.ProxySettings;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ProxyPortVerifier;

/**
 * Proxy properties editor panel
 *
 * @author Sergiy Logvin
 */
public class ProxyComposite extends AbstractDynamicComposite implements IPropertiesPanel {
	protected static final String USER_HISTORY_NAME = "proxyUser";
	protected static final String HOST_HISTORY_NAME = "proxyHost";
	
	protected boolean proxyEnabled;
	protected String host;
	protected int port;
	protected boolean authenticationEnabled;
	protected String username;
	protected String password;
	protected boolean savePassword;
	
	protected boolean callback;
	
	protected Button enableAuthenticationCheckBox;
	protected Button proxyEnableCheckBox;
	protected Combo hostText;
	protected Text portText;	
	protected Combo usernameText;
	protected Text passwordText;
	protected Button savePasswordCheckBox;
	
	protected IValidationManager validationManager;
	protected UserInputHistory userHistory;
	protected UserInputHistory hostHistory;
	
	protected boolean tempProxyEnabled;
	protected boolean tempAuthEnabled;
	protected boolean tempSavePassEnabled;
	protected boolean tempOptionsSaved;
	
	public ProxyComposite(Composite parent, int style, IValidationManager validationManager) {
		this(parent, style, validationManager, false);
	}
	
	public ProxyComposite(Composite parent, int style, IValidationManager validationManager, boolean callback) {
		super(parent, style);
		this.validationManager = validationManager;
		this.callback = callback;
		this.port = ProxySettings.DEFAULT_PORT;
	}

	public ProxySettings getProxySettingsDirect() {
		ProxySettings settings = new ProxySettings();
		this.getProxySettingsDirectImpl(settings);
		return settings;
	}
	
	public void setProxySettingsDirect(ProxySettings settings) {
		this.proxyEnableCheckBox.setSelection(settings.isEnabled());
		this.portText.setText(String.valueOf(settings.getPort()));
		this.hostText.setText(settings.getHost());
		this.enableAuthenticationCheckBox.setSelection(settings.isAuthenticationEnabled());
		this.usernameText.setText(settings.getUsername());
		this.passwordText.setText(settings.getPassword());
		this.savePasswordCheckBox.setSelection(settings.isPasswordSaved());
		if (this.callback && settings.isAuthenticationEnabled()) {
			if (this.username != null && this.username.trim().length() > 0) {
				this.passwordText.setFocus();
				this.passwordText.selectAll();
			}
			else {
				this.usernameText.setFocus();
			}
		}
		
		this.refreshControlsEnablement();		
	}
	
	public void saveChanges() {
		this.userHistory.addLine(this.usernameText.getText());	
		this.hostHistory.addLine(this.hostText.getText());
		
		if (this.proxyEnabled = this.proxyEnableCheckBox.getSelection()) {
			this.host = this.hostText.getText().trim();
			this.port = Integer.parseInt(this.portText.getText().trim());
			if (this.authenticationEnabled = this.enableAuthenticationCheckBox.getSelection()) {
				this.username = this.usernameText.getText().trim();
				this.savePassword = this.savePasswordCheckBox.getSelection();
				this.password = this.passwordText.getText().trim();
			}
		}
	}

	public void resetChanges() {
		this.proxyEnableCheckBox.setSelection(this.proxyEnabled);
		this.portText.setText(String.valueOf(this.port));
		this.hostText.setText(this.host != null ? this.host : "");
		this.enableAuthenticationCheckBox.setSelection(this.authenticationEnabled);
		this.usernameText.setText(this.username != null ? this.username : "");
		this.passwordText.setText(this.password != null ? this.password : "");
		this.savePasswordCheckBox.setSelection(this.savePassword);
		if (this.callback && this.authenticationEnabled) {
			if (this.username != null && this.username.trim().length() > 0) {
				this.passwordText.setFocus();
				this.passwordText.selectAll();
			}
			else {
				this.usernameText.setFocus();
			}
		}
		
		this.refreshControlsEnablement();		
	}

	public void cancelChanges() {
		
	}

	public void initialize() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.verticalSpacing = 12;
		layout.marginHeight = 7;
		this.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);
		
		Composite wrap = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		wrap.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		wrap.setLayoutData(data);
		
		// Enable Proxy Authentication checkbox
		this.proxyEnableCheckBox = new Button(wrap, SWT.CHECK);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		this.proxyEnableCheckBox.setLayoutData(data);
		this.proxyEnableCheckBox.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.EnableProxy"));
		this.proxyEnableCheckBox.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				ProxyComposite.this.refreshControlsEnablement();
			}
			public void widgetDefaultSelected(SelectionEvent e) {			
			}			
		});
		
		//Host and port composite
		Group group = new Group(wrap, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Proxy"));
		
		Label description = new Label(group, SWT.NULL);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Host"));
		
		this.hostHistory = new UserInputHistory(ProxyComposite.HOST_HISTORY_NAME);
		
		this.hostText = new Combo(group, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.hostText.setLayoutData(data);
		this.hostText.setVisibleItemCount(this.hostHistory.getDepth());
		this.hostText.setItems(this.hostHistory.getHistory());
		this.validationManager.attachTo(this.hostText, new AbstractVerifierProxy(new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Host.Verifier"))) {
			protected boolean isVerificationEnabled(Control input) {
				return ProxyComposite.this.proxyEnableCheckBox.getSelection() && ProxyComposite.this.isVisible();
			}
		});
		
		description = new Label(group, SWT.NULL);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Port"));
		
		this.portText = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.portText.setLayoutData(data);
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNTeamUIPlugin.instance().getResource("ProxyComposite.Port.Verifier");
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ProxyPortVerifier(name));
		this.validationManager.attachTo(this.portText, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return ProxyComposite.this.proxyEnableCheckBox.getSelection() && ProxyComposite.this.isVisible();
			}
		});
		
		wrap = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		data = new GridData(GridData.FILL_HORIZONTAL);
		wrap.setLayout(layout);
		wrap.setLayoutData(data);
		
		this.enableAuthenticationCheckBox = new Button(wrap, SWT.CHECK);
		this.enableAuthenticationCheckBox.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.EnableAuthentication"));
		this.enableAuthenticationCheckBox.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				ProxyComposite.this.refreshControlsEnablement();
			}
			public void widgetDefaultSelected(SelectionEvent e) {			
			}			
		});
		
		//Authentication group
		Group authGroup = new Group(wrap, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		authGroup.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 12;
		authGroup.setLayout(layout);
		authGroup.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Authentication"));
		
		//Username and password inner group
		Composite inner = new Composite(authGroup, SWT.FILL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		inner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		inner.setLayoutData(data);
		
		description = new Label(inner, SWT.NULL);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Username"));
		
		this.userHistory = new UserInputHistory(ProxyComposite.USER_HISTORY_NAME);
		
		this.usernameText = new Combo(inner, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.usernameText.setLayoutData(data);
		this.usernameText.setVisibleItemCount(this.userHistory.getDepth());
		this.usernameText.setItems(this.userHistory.getHistory());
		
		description = new Label(inner, SWT.NULL);
		description.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.Password"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		
		this.passwordText = new Text(inner, SWT.PASSWORD | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		passwordText.setLayoutData(data);
		
		//Save password checkbox and warning label inner group
		inner = new Composite(authGroup, SWT.FILL);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		inner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		inner.setLayoutData(data);
		
		this.savePasswordCheckBox = new Button(inner, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.savePasswordCheckBox.setLayoutData(data);
		this.savePasswordCheckBox.setText(SVNTeamUIPlugin.instance().getResource("ProxyComposite.SavePassword"));
		
		new SecurityWarningComposite(inner);
	}
	
	public boolean isAuthenticationEnabled() {
		return this.authenticationEnabled;
	}
	
	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean isProxyEnabled() {
		return this.proxyEnabled;
	}
	
	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean isSavePassword() {
		return savePassword;
	}
	
	public void setSavePassword(boolean savePassword) {
		this.savePassword = savePassword;
	}
	
	protected void refreshControlsEnablement() {
		boolean enabled = this.proxyEnableCheckBox.getSelection();
		this.hostText.setEnabled(enabled);
		this.portText.setEnabled(enabled);
		this.enableAuthenticationCheckBox.setEnabled(enabled);
		boolean authEnabled = this.enableAuthenticationCheckBox.getSelection();
		this.usernameText.setEnabled(enabled && authEnabled);
		this.passwordText.setEnabled(enabled && authEnabled);
		this.savePasswordCheckBox.setEnabled(enabled && authEnabled);
		
		this.validationManager.validateContent();
	}
	
	public void saveAppearance() {
		this.tempProxyEnabled = this.proxyEnableCheckBox.getSelection();
		this.tempAuthEnabled = this.enableAuthenticationCheckBox.getSelection();
		this.tempSavePassEnabled = this.savePasswordCheckBox.getSelection();
		this.tempOptionsSaved = true;
	}
	
	public void restoreAppearance() {
		if (this.tempOptionsSaved) {
			this.proxyEnableCheckBox.setSelection(this.tempProxyEnabled);
			this.enableAuthenticationCheckBox.setSelection(this.tempAuthEnabled);
			this.savePasswordCheckBox.setSelection(this.tempSavePassEnabled);
			this.tempOptionsSaved = false;
			this.refreshControlsEnablement();
		}
		else {
			this.resetChanges();
		}
	}
	
	public void revalidateContent() {
		this.validationManager.validateContent();
	}

	protected void getProxySettingsDirectImpl(ProxySettings settings) {
		settings.setEnabled(this.proxyEnableCheckBox.getSelection());
		settings.setHost(this.hostText.getText());
		String text = this.portText.getText().trim();
		if (text.length() > 0) {
			settings.setPort(Integer.parseInt(text));
		}
		settings.setAuthenticationEnabled(this.enableAuthenticationCheckBox.getSelection());
		settings.setUsername(this.usernameText.getText());
		settings.setPassword(this.passwordText.getText());
		settings.setPasswordSaved(this.savePasswordCheckBox.getSelection());
	}
	
}
