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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.local.management.FindRelatedProjectsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.ProxySettings;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;

/**
 * Repository properties editor panel
 * 
 * @author Alexander Gurov
 */
public class RepositoryPropertiesComposite extends Composite implements IPropertiesPanel {
    protected static final String URL_HISTORY_NAME = "repositoryURL";
    
	protected Text repositoryLabel;
	protected Combo url;
	protected CompositeVerifier urlVerifier;
	protected UserInputHistory urlHistory;
	protected Button browse;
	protected Button useLocationButton;
	protected Button newLabelButton;
	protected CredentialsComposite credentialsComposite;
	
	protected IRepositoryLocation repositoryLocation;
	protected String rootUrl;
	
	protected IValidationManager validationManager;
	
	protected IRepositoryLocation credentialsInput;
	protected ISecurityInfoProvider provider;

	public RepositoryPropertiesComposite(Composite parent, int style, IValidationManager validationManager) {
		super(parent, style);

		this.validationManager = validationManager;
	}
	
	public String getPasswordDirect() {
		return this.credentialsComposite.getPassword().getText();
	}
	
	public void setPasswordDirect(String password) {
		this.credentialsComposite.getPassword().setText(password);
	}
	
	public String getUsernameDirect() {
		return this.credentialsComposite.getUsername().getText();
	}
	
	public void setUsernameDirect(String username) {
		this.credentialsComposite.getUsername().setText(username);
	}
	
	public boolean getPasswordSavedDirect() {
		return this.credentialsComposite.getSavePassword().getSelection();
	}
	
	public void setPasswordSavedDirect(boolean saved) {
		this.credentialsComposite.getSavePassword().setSelection(saved);
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
		
		Composite rootURLGroup = new Composite(this, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		rootURLGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		rootURLGroup.setLayoutData(data);
		
		Label description = new Label(rootURLGroup, SWT.NULL);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.URL"));
		
		this.urlHistory = new UserInputHistory(RepositoryPropertiesComposite.URL_HISTORY_NAME);
		
		this.url = new Combo(rootURLGroup, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.url.setLayoutData(data);
		this.url.setVisibleItemCount(this.urlHistory.getDepth());
		this.url.setItems(this.urlHistory.getHistory());
		this.urlVerifier = new CompositeVerifier() {
			public boolean verify(Control input) {
				boolean retVal = super.verify(input);
				RepositoryPropertiesComposite.this.browse.setEnabled(retVal);
				return retVal;
			}
		};
		this.defineUrlVerifier(null);
		this.validationManager.attachTo(this.url, this.urlVerifier);
		
		this.browse = new Button (rootURLGroup, SWT.PUSH);
		this.browse.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);	
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    SVNRemoteStorage storage = SVNRemoteStorage.instance();
				IRepositoryLocation location = storage.newRepositoryLocation();
				location.setUrl(RepositoryPropertiesComposite.this.url.getText());
				location.setLabel(RepositoryPropertiesComposite.this.url.getText());
				
				location.setPassword(RepositoryPropertiesComposite.this.provider.getPassword());
				location.setUsername(RepositoryPropertiesComposite.this.provider.getUsername());
				location.setPasswordSaved(RepositoryPropertiesComposite.this.provider.isPasswordSaved());
				
				SSHSettings sshNew = location.getSSHSettings();
				SSHSettings sshOriginal = RepositoryPropertiesComposite.this.provider.getSSHSettings();
				sshNew.setPassPhrase(sshOriginal.getPassPhrase());
				sshNew.setPassPhraseSaved(sshOriginal.isPassPhraseSaved());
				sshNew.setPort(sshOriginal.getPort());
				sshNew.setPrivateKeyPath(sshOriginal.getPrivateKeyPath());
				sshNew.setUseKeyFile(sshOriginal.isUseKeyFile());
				
				SSLSettings sslOriginal = location.getSSLSettings();
				SSLSettings sslNew = RepositoryPropertiesComposite.this.provider.getSSLSettings();
				sslNew.setAuthenticationEnabled(sslOriginal.isAuthenticationEnabled());
				sslNew.setCertificatePath(sslOriginal.getCertificatePath());
				sslNew.setPassPhrase(sslOriginal.getPassPhrase());
				sslNew.setPassPhraseSaved(sslOriginal.isPassPhraseSaved());
				
				ProxySettings proxyOriginal = location.getProxySettings();
				ProxySettings proxyNew = RepositoryPropertiesComposite.this.provider.getProxySettings();
				proxyNew.setAuthenticationEnabled(proxyOriginal.isAuthenticationEnabled());
				proxyNew.setEnabled(proxyOriginal.isEnabled());
				proxyNew.setHost(proxyOriginal.getHost());
				proxyNew.setPassword(proxyOriginal.getPassword());
				proxyNew.setPasswordSaved(proxyOriginal.isPasswordSaved());
				proxyNew.setPort(proxyOriginal.getPort());
				proxyNew.setUsername(proxyOriginal.getUsername());
				
				RepositoryTreePanel panel = new RepositoryTreePanel(
						SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.SelectNewURL"),
						SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Description"),
						SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Message"),
						null,
						true,
						location);
				panel.setAutoExpandFirstLevel(true);
				DefaultDialog browser = new DefaultDialog(RepositoryPropertiesComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					if (panel.getSelectedResource() != null) {
						String newUrl = panel.getSelectedResource().getUrl();
						RepositoryPropertiesComposite.this.url.setText(newUrl);
					}
					RepositoryPropertiesComposite.this.provider.setUsername(location.getUsername());
					RepositoryPropertiesComposite.this.provider.setPassword(location.getPassword());
					RepositoryPropertiesComposite.this.provider.setPasswordSaved(location.isPasswordSaved());
					
					sshNew = RepositoryPropertiesComposite.this.provider.getSSHSettings();
					sshOriginal = location.getSSHSettings();
					sshNew.setPassPhrase(sshOriginal.getPassPhrase());
					sshNew.setPassPhraseSaved(sshOriginal.isPassPhraseSaved());
					sshNew.setPort(sshOriginal.getPort());
					sshNew.setPrivateKeyPath(sshOriginal.getPrivateKeyPath());
					sshNew.setUseKeyFile(sshOriginal.isUseKeyFile());
					
					sslOriginal = RepositoryPropertiesComposite.this.provider.getSSLSettings();
					sslNew = location.getSSLSettings();
					sslNew.setAuthenticationEnabled(sslOriginal.isAuthenticationEnabled());
					sslNew.setCertificatePath(sslOriginal.getCertificatePath());
					sslNew.setPassPhrase(sslOriginal.getPassPhrase());
					sslNew.setPassPhraseSaved(sslOriginal.isPassPhraseSaved());
					
					proxyOriginal = RepositoryPropertiesComposite.this.provider.getProxySettings();
					proxyNew = location.getProxySettings();
					proxyNew.setAuthenticationEnabled(proxyOriginal.isAuthenticationEnabled());
					proxyNew.setEnabled(proxyOriginal.isEnabled());
					proxyNew.setHost(proxyOriginal.getHost());
					proxyNew.setPassword(proxyOriginal.getPassword());
					proxyNew.setPasswordSaved(proxyOriginal.isPasswordSaved());
					proxyNew.setPort(proxyOriginal.getPort());
					proxyNew.setUsername(proxyOriginal.getUsername());
					
					RepositoryPropertiesComposite.this.provider.commit();
				}
			}
		});
		
		Group labelGroup = new Group(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout = new GridLayout();
		labelGroup.setLayout(layout);
		labelGroup.setLayoutData(data);
		labelGroup.setText(SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.Label"));
		
		this.useLocationButton = new Button(labelGroup, SWT.RADIO);
		this.useLocationButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.useLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    RepositoryPropertiesComposite.this.validationManager.validateContent();
				Button button = (Button)e.widget;
				RepositoryPropertiesComposite.this.repositoryLabel.setEnabled(!button.getSelection());
				if (!button.getSelection()) {
					RepositoryPropertiesComposite.this.repositoryLabel.selectAll();
				}
				else {
					RepositoryPropertiesComposite.this.repositoryLabel.setText("");
				}
			}
		});
		this.useLocationButton.setText(SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.UseURL"));
		
		this.newLabelButton = new Button(labelGroup, SWT.RADIO);
		this.newLabelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.newLabelButton.setText(SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.UseCustom")); 
		
		this.repositoryLabel = new Text(labelGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.repositoryLabel.setLayoutData(data);
		this.validationManager.attachTo(this.repositoryLabel, new AbstractVerifierProxy(new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.UseCustom.Verifier"))) {
			protected boolean isVerificationEnabled(Control input) {
				return RepositoryPropertiesComposite.this.newLabelButton.getSelection();
			}			
		});
		this.repositoryLabel.setEnabled(false);
		
		this.credentialsComposite = new CredentialsComposite(this, SWT.NONE);
		this.credentialsComposite.initialize();
		
		this.url.setFocus();
		
		this.resetChanges();
	}
	
	public void setRepositoryLocation(IRepositoryLocation location, String rootUrl, ISecurityInfoProvider provider) {
		this.credentialsInput = this.repositoryLocation = location;
		this.rootUrl = rootUrl;
		this.provider = provider;
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.repositoryLocation;
	}
	
	public String getLocationUrl() {
		return this.url.getText();
	}
	
	public void setCredentialsInput(IRepositoryLocation location, ISecurityInfoProvider provider) {
		this.credentialsInput = location;
		this.provider = provider;
	}
	
	public void defineUrlVerifier(AbstractVerifier verifier) {
		String name = SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.URL.Verifier");
		this.urlVerifier.removeAll();
		this.urlVerifier.add(new URLVerifier(name));
		this.urlVerifier.add(new AbsolutePathVerifier(name));
		if (this.rootUrl != null && SVNRemoteStorage.instance().getRepositoryLocation(this.repositoryLocation.getId()) != null) {
			this.urlVerifier.add(new AbstractFormattedVerifier(name) {
				protected Boolean relatedProjects;
				
				protected String getErrorMessageImpl(Control input) {
					return null;
				}
				protected String getWarningMessageImpl(Control input) {
					if (this.relatedProjects == null) {
						FindRelatedProjectsOperation op = new FindRelatedProjectsOperation(RepositoryPropertiesComposite.this.repositoryLocation);
						UIMonitorUtility.doTaskBusyDefault(op);
						this.relatedProjects = op.getResources() == null || op.getResources().length == 0 ? Boolean.FALSE : Boolean.TRUE;
					}
					if (this.relatedProjects == Boolean.FALSE) {
						return null;
					}
					String newUrl = this.getText(input);
					newUrl = SVNUtility.normalizeURL(newUrl);
					try {
						newUrl = SVNUtility.decodeURL(newUrl);
					}
					catch (Exception ex) {
						// is not encoded URL
					}
					if (!new Path(RepositoryPropertiesComposite.this.rootUrl).isPrefixOf(new Path(newUrl))) {
						return SVNTeamUIPlugin.instance().getResource("RepositoryPropertiesComposite.URL.Verifier.Warning");
					}
					return null;
				}
			});
		}

		if (verifier != null) {
			this.urlVerifier.add(verifier);
		}
	}
	
	public void saveChanges() {
		if (this.useLocationButton.getSelection()) {
			this.repositoryLocation.setLabel(this.url.getText());
		}
		else {
			this.repositoryLocation.setLabel(this.repositoryLabel.getText());
		}
		String newUrl = this.url.getText();
		this.urlHistory.addLine(newUrl);
		this.repositoryLocation.setUrl(newUrl);

		this.credentialsComposite.getUserHistory().addLine(this.credentialsComposite.userName.getText());
		
		this.credentialsInput.setUsername(this.credentialsComposite.getUsername().getText());
		this.credentialsInput.setPassword(this.credentialsComposite.getPassword().getText());
		this.credentialsInput.setPasswordSaved(this.credentialsComposite.getSavePassword().getSelection());
	}
	
	public void resetChanges() {
		String url = this.repositoryLocation.getUrlAsIs();
		url = url == null ? "" : url;
		if (this.repositoryLocation.getLabel() == null || 
			this.repositoryLocation.getLabel().equalsIgnoreCase(this.repositoryLocation.getUrlAsIs()) ||
			this.repositoryLocation.getLabel().equalsIgnoreCase(this.repositoryLocation.getUrl())) {
			this.repositoryLabel.setText(url);
			this.useLocationButton.setSelection(true);
			this.newLabelButton.setSelection(false);
		}
		else {
			this.repositoryLabel.setText(this.repositoryLocation.getLabel());
			this.useLocationButton.setSelection(false);
			this.newLabelButton.setSelection(true);
		}
		RepositoryPropertiesComposite.this.repositoryLabel.setEnabled(!this.useLocationButton.getSelection());
		this.url.setText(url);

		String username = this.credentialsInput.getUsername();
		this.credentialsComposite.getUsername().setText(username == null ? "" : username);
		String password = this.credentialsInput.getPassword();
		this.credentialsComposite.getPassword().setText(password == null ? "" : password);
		
		this.credentialsComposite.getSavePassword().setSelection(this.credentialsInput.isPasswordSaved());
	}
	
	public void cancelChanges() {
		
	}
	
}
