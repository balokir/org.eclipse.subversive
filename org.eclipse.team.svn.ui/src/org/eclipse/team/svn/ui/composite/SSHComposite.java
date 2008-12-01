/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ProxyPortVerifier;

/**
 * SSH properties editor composite
 *
 * @author Sergiy Logvin
 */
public class SSHComposite extends AbstractDynamicComposite implements IPropertiesPanel {
	protected boolean callback;
	
	protected Button passwordRadioButton;
	protected Button privateKeyRadioButton;
	protected Text privateKeyFileText;
	protected Button browseButton;
	protected Text passphraseText;
	protected Button savePassphraseCheckBox;
	protected Text portText;
	
	protected IValidationManager validationManager;
	protected UserInputHistory userHistory;
	
	protected SSHSettings credentialsInput;
	
	protected boolean tempOptionsSaved;
	protected boolean tempPassBtnEnabled;
	protected boolean tempKeyBtnEnabled;
	protected boolean tempSavePassChecked;

	public SSHComposite(Composite parent, int style, IValidationManager validationManager) {
		this(parent, style, validationManager, false);
	}
	
	public SSHComposite(Composite parent, int style, IValidationManager validationManager, boolean callback) {
		super(parent, style);
		this.validationManager = validationManager;
		this.callback = callback;
		this.credentialsInput = new SSHSettings();
	}
	
	public SSHSettings getSSHSettingsDirect() {
		SSHSettings settings = new SSHSettings();
		this.getSSHSettingsDirectImpl(settings);
		return settings;
	}
	
	public void setSSHSettingsDirect(SSHSettings settings) {
		this.savePassphraseCheckBox.setSelection(settings.isPassPhraseSaved());
		this.privateKeyRadioButton.setSelection(settings.isUseKeyFile());
		this.passwordRadioButton.setSelection(!settings.isUseKeyFile());
		String text = settings.getPassPhrase();
		this.passphraseText.setText(text == null ? "" : text); //$NON-NLS-1$
		text = settings.getPrivateKeyPath();
		this.privateKeyFileText.setText(text == null ? "" : text); //$NON-NLS-1$
		this.portText.setText(String.valueOf(settings.getPort()));
		
		if (this.callback && text != null && text.length() > 0) {
			this.passphraseText.setFocus();
			this.passphraseText.selectAll();
		}
		
		this.refreshControlsEnablement();
	}
	
	public void initialize() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.verticalSpacing = 3;
		this.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);

		Composite sshGroup = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		sshGroup.setLayout(layout);
		sshGroup.setLayoutData(data);
		
		Label lblPort = new Label(sshGroup, SWT.NONE);
		lblPort.setText(SVNUIMessages.SSHComposite_Port);
		
		this.portText = new Text(sshGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.portText.setLayoutData(data);
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNUIMessages.SSHComposite_Port_Verifier;
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ProxyPortVerifier(name));
		this.portText.setText(String.valueOf(SSHSettings.SSH_PORT_DEFAULT));
		this.validationManager.attachTo(this.portText, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return SSHComposite.this.isVisible();
			}
		});
		
		Group passGroup = new Group(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout = new GridLayout();
		layout.verticalSpacing = 12;
		passGroup.setLayout(layout);
		passGroup.setLayoutData(data);
		passGroup.setText(SVNUIMessages.SSHComposite_Authentication);
		
		Composite inner = new Composite(passGroup, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		inner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;		
		inner.setLayoutData(data);
		
		this.passwordRadioButton = new Button(inner, SWT.RADIO);
		data = new GridData(GridData.BEGINNING);
		this.passwordRadioButton.setLayoutData(data);
		this.passwordRadioButton.setText(SVNUIMessages.SSHComposite_Password);
		
		this.passwordRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				SSHComposite.this.refreshControlsEnablement();
			}
			public void widgetDefaultSelected(SelectionEvent e) {				
			}			
		});
		
		this.privateKeyRadioButton = new Button(inner, SWT.RADIO);
		data = new GridData();
		this.privateKeyRadioButton.setLayoutData(data);
		this.privateKeyRadioButton.setText(SVNUIMessages.SSHComposite_PrivateKey);
		this.privateKeyRadioButton.setSelection(false);
		
		Composite groupInner = new Composite(inner, SWT.FILL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		groupInner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		groupInner.setLayoutData(data);
		
		Label description = new Label(groupInner, SWT.NULL);
		description.setText(SVNUIMessages.SSHComposite_File);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		
		Composite privateKeyFileComposite = new Composite(groupInner, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		privateKeyFileComposite.setLayout(layout);
		privateKeyFileComposite.setLayoutData(data);
		
		this.privateKeyFileText = new Text(privateKeyFileComposite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.privateKeyFileText.setLayoutData(data);
		this.validationManager.attachTo(this.privateKeyFileText, new AbstractVerifierProxy(new ExistingResourceVerifier(SVNUIMessages.SSHComposite_File_Verifier, true)) {
			protected boolean isVerificationEnabled(Control input) {
				return SSHComposite.this.privateKeyRadioButton.getSelection() && SSHComposite.this.isVisible();
			}
		});
		
		this.browseButton = new Button (privateKeyFileComposite, SWT.PUSH);
		this.browseButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);	
		data.widthHint = DefaultDialog.computeButtonWidth(this.browseButton);
		this.browseButton.setLayoutData(data);
		
		this.browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(SSHComposite.this.getShell(), SWT.OPEN);
				String res = fileDialog.open();
				if (res != null) {
					SSHComposite.this.privateKeyFileText.setText(res);
					SSHComposite.this.validationManager.validateContent();
				}
			}
		});
				
		description = new Label(groupInner, SWT.NULL);
		description.setText(SVNUIMessages.SSHComposite_Passphrase);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		
		this.passphraseText = new Text(groupInner, SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.passphraseText.setLayoutData(data);
		
		Composite savePassphrase = new Composite(passGroup, SWT.FILL);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		savePassphrase.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		savePassphrase.setLayoutData(data);
		
		this.savePassphraseCheckBox = new Button(savePassphrase, SWT.CHECK);
		this.savePassphraseCheckBox.setText(SVNUIMessages.SSHComposite_SavePassphrase);
		
		new SecurityWarningComposite(savePassphrase);
	}

	public void setCredentialsInput(SSHSettings input) {
		this.credentialsInput = input;
	}
	
	public void saveChanges() {
		this.getSSHSettingsDirectImpl(this.credentialsInput);
	}

	public void resetChanges() {
		this.setSSHSettingsDirect(this.credentialsInput);
	}

	public void cancelChanges() {
		
	}

	protected void getSSHSettingsDirectImpl(SSHSettings settings) {
		settings.setPassPhraseSaved(this.savePassphraseCheckBox.getSelection());
		settings.setUseKeyFile(this.privateKeyRadioButton.getSelection());
		settings.setPassPhrase(this.passphraseText.getText());
		settings.setPrivateKeyPath(this.privateKeyFileText.getText());
		String port = this.portText.getText().trim();
		if (port.length() > 0) {
			settings.setPort(Integer.parseInt(port));
		}
	}
	
	protected void refreshControlsEnablement() {
		boolean buttonSelected = this.passwordRadioButton.getSelection();
		
		buttonSelected = this.privateKeyRadioButton.getSelection();
		this.privateKeyFileText.setEnabled(buttonSelected);
		this.browseButton.setEnabled(buttonSelected);
		this.passphraseText.setEnabled(buttonSelected);
		this.savePassphraseCheckBox.setEnabled(buttonSelected);
		
		this.validationManager.validateContent();
	}
	
	public void saveAppearance() {
		this.tempPassBtnEnabled = this.passwordRadioButton.getSelection();
		this.tempKeyBtnEnabled = this.privateKeyRadioButton.getSelection();
		this.tempSavePassChecked = this.savePassphraseCheckBox.getSelection();
		this.tempOptionsSaved = true;
	}
	
	public void restoreAppearance() {
		if (this.tempOptionsSaved) {
			this.passwordRadioButton.setSelection(this.tempPassBtnEnabled);
			this.privateKeyRadioButton.setSelection(this.tempKeyBtnEnabled);
			this.savePassphraseCheckBox.setSelection(this.tempSavePassChecked);
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

}
