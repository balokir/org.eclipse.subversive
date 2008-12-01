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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Proxy properties editor panel
 *
 * @author Sergiy Logvin
 */
public class ProxyComposite extends AbstractDynamicComposite implements IPropertiesPanel {
	protected static final String USER_HISTORY_NAME = "proxyUser";
	
	protected String username;
	protected String password;
	protected String host;
	
	protected boolean callback;
		
	protected Combo usernameText;
	protected Text passwordText;
	
	protected IValidationManager validationManager;
	protected UserInputHistory userHistory;
	
	public ProxyComposite(Composite parent, int style, IValidationManager validationManager) {
		this(parent, style, validationManager, false);
	}
	
	public ProxyComposite(Composite parent, int style, IValidationManager validationManager, boolean callback) {
		super(parent, style);
		this.validationManager = validationManager;
		this.callback = callback;
	}
		
	public void setProxySettingsDirect(String username, String password) {
		this.usernameText.setText(username);
		this.passwordText.setText(password);
		if (this.callback) {
			if (this.username != null && this.username.trim().length() > 0) {
				this.passwordText.setFocus();
				this.passwordText.selectAll();
			}
			else {
				this.usernameText.setFocus();
			}
		}	
	}
	
	public void saveChanges() {
		this.userHistory.addLine(this.usernameText.getText());
		this.username = this.usernameText.getText();
		this.password = this.passwordText.getText();
	}

	public void resetChanges() {
		this.usernameText.setText(this.username != null ? this.username : "");
		this.passwordText.setText(this.password != null ? this.password : "");
		if (this.callback) {
			if (this.username != null && this.username.trim().length() > 0) {
				this.passwordText.setFocus();
				this.passwordText.selectAll();
			}
			else {
				this.usernameText.setFocus();
			}
		}
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
		
		Label description = new Label(this, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		description.setLayoutData(data);
		description.setText(SVNUIMessages.format(SVNUIMessages.ProxyComposite_Description, new String [] {this.host}));
						
		//Authentication group
		Group authGroup = new Group(this, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		authGroup.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 12;
		authGroup.setLayout(layout);
		authGroup.setText(SVNUIMessages.ProxyComposite_Authentication);
		
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
		description.setText(SVNUIMessages.ProxyComposite_Username);
		
		this.userHistory = new UserInputHistory(ProxyComposite.USER_HISTORY_NAME);
		
		this.usernameText = new Combo(inner, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.usernameText.setLayoutData(data);
		this.usernameText.setVisibleItemCount(this.userHistory.getDepth());
		this.usernameText.setItems(this.userHistory.getHistory());
		this.usernameText.setText(this.getUsername());
		
		description = new Label(inner, SWT.NULL);
		description.setText(SVNUIMessages.ProxyComposite_Password);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		
		this.passwordText = new Text(inner, SWT.PASSWORD | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.passwordText.setLayoutData(data);
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void revalidateContent() {
		this.validationManager.validateContent();
	}

	public void restoreAppearance() {
	}

	public void saveAppearance() {
	}
	
}
