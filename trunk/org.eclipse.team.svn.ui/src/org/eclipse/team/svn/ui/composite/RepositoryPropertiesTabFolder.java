/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Sergey Vasilchenko - [patch] bug fix: Trunk, branches and tags default values are not stored if they are disabled while creating new location
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Repository properties tab folder
 *
 * @author Sergiy Logvin
 */
public class RepositoryPropertiesTabFolder extends Composite implements IPropertiesPanel, ISecurityInfoProvider {
	
	protected static final String AUTHOR_HISTORY_NAME = "authorName";
	
	protected RepositoryPropertiesComposite repositoryPropertiesPanel;
	protected SSHComposite sshComposite;
	protected SSLComposite sslComposite;
	protected RepositoryRootsComposite rootsComposite;
	protected Composite parent;
	protected IRepositoryLocation repositoryLocation;
	protected int style;
	protected IValidationManager validationManager;
	protected Button validateButton;
	protected Button resetChangesButton;
	protected boolean validateOnFinish;
	protected boolean forceDisableRoots;
	protected boolean createNew;
	protected Combo cachedRealms;
	protected boolean isAuthorNameEnabled;
	protected Combo authorInput;
	protected Button authorEnabled;
	protected UserInputHistory authorNameHistory;
	
	protected TabItem sshTab;
	protected TabItem sslTab;
	protected Composite unavailableSSHComposite;
	protected Composite unavailableProxyComposite;
	
	protected IRepositoryLocation backup;
	
	public RepositoryPropertiesTabFolder(Composite parent, int style, IValidationManager validationManager, IRepositoryLocation repositoryLocation) {
		super(parent, style);
		this.parent = parent;
		this.style = style;
		this.validationManager = validationManager;	
		this.repositoryLocation = repositoryLocation;
		this.createNew = repositoryLocation == null;
		if (this.createNew) {
			this.repositoryLocation = SVNRemoteStorage.instance().newRepositoryLocation();
		}
		else {
			this.backup = SVNRemoteStorage.instance().newRepositoryLocation();
			SVNRemoteStorage.instance().copyRepositoryLocation(this.backup, this.repositoryLocation);
		}
	}
	
	protected String sipUsername;
	protected String sipPassword;
	protected boolean sipIsPasswordSaved;
	protected SSHSettings sipSSHSettings;
	protected SSLSettings sipSSLSettings;
	
	public String getUsername() {
		return this.sipUsername = this.repositoryPropertiesPanel.getUsernameDirect();
	}
	
	public void setUsername(String username) {
		this.sipUsername = username;
	}
	
	public String getPassword() {
		return this.sipPassword = this.repositoryPropertiesPanel.getPasswordDirect();
	}
	
	public void setPassword(String password) {
		this.sipPassword = password;
	}
	
	public boolean isPasswordSaved() {
		return this.sipIsPasswordSaved = this.repositoryPropertiesPanel.getPasswordSavedDirect();
	}
	
	public void setPasswordSaved(boolean saved) {
		this.sipIsPasswordSaved = saved;
	}
	
	public SSLSettings getSSLSettings() {
		return this.sipSSLSettings = this.sslComposite.getSSLSettingsDirect();
	}
	
	public SSHSettings getSSHSettings() {
		return this.sipSSHSettings = this.sshComposite.getSSHSettingsDirect();
	}
	
	public void commit() {
		this.repositoryPropertiesPanel.setUsernameDirect(this.sipUsername);
		this.repositoryPropertiesPanel.setPasswordDirect(this.sipPassword);
		this.repositoryPropertiesPanel.setPasswordSavedDirect(this.sipIsPasswordSaved);
		this.sshComposite.setSSHSettingsDirect(this.sipSSHSettings);
		this.sslComposite.setSSLSettingsDirect(this.sipSSLSettings);
	}
	
	public void initialize() {
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 7;
		this.setLayout(layout);
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.RepositoryPropertiesTabFolder_General);
		tabItem.setControl(this.createRepositoryPropertiesPanel(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.RepositoryPropertiesTabFolder_Advanced);
		Composite rootsComposite = this.createRepositoryRootsComposite(tabFolder);
		GridData data = new GridData();
		data.verticalIndent = 10;	
		this.authorEnabled = new Button(rootsComposite, SWT.CHECK);
		this.authorEnabled.setText(SVNUIMessages.NewRepositoryLocationWizard_OverrideAuthor); 
		this.authorEnabled.setSelection(this.repositoryLocation.isAuthorNameEnabled());
		this.authorEnabled.setLayoutData(data);
		String name = (this.repositoryLocation.getAuthorName() == null) ? "" : this.repositoryLocation.getAuthorName();
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.authorInput = new Combo(rootsComposite, SWT.BORDER);
		this.authorNameHistory = new UserInputHistory(RepositoryPropertiesTabFolder.AUTHOR_HISTORY_NAME);
		this.authorInput.setVisibleItemCount(this.authorNameHistory.getDepth());
		this.authorInput.setItems(this.authorNameHistory.getHistory());
		this.authorInput.setText(name);
		this.authorInput.setEnabled(this.repositoryLocation.isAuthorNameEnabled());
		this.authorInput.setLayoutData(data);
		AbstractFormattedVerifier verifier = new AbstractFormattedVerifier("AuthorNameVerifier") {
		    protected String getErrorMessageImpl(Control input) {
		    	if (this.getText(input).equals("")) {
		    		return SVNUIMessages.NewRepositoryLocationWizard_AuthorName_Verifier;
		    	}
		    	return null;
		    }
		    protected String getWarningMessageImpl(Control input)		    {
		    	return null;
		    }
		}; 
		this.validationManager.attachTo(this.authorInput, new AbstractVerifierProxy(verifier){
			protected boolean isVerificationEnabled(Control input) {
				return RepositoryPropertiesTabFolder.this.authorEnabled.getSelection();
			}
		});
		this.authorEnabled.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				RepositoryPropertiesTabFolder.this.authorInput.setEnabled(((Button)event.widget).getSelection());
				RepositoryPropertiesTabFolder.this.validationManager.validateContent();
			}
			public void widgetDefaultSelected(SelectionEvent e) {			
			}	
		});
		
		Group proxyGroup = new Group(rootsComposite, SWT.NONE);
		GridLayout proxyGroupLayout = new GridLayout(1, true);
		proxyGroupLayout.marginHeight = 5;
		proxyGroupLayout.marginWidth = 5;
		proxyGroup.setLayout(proxyGroupLayout);
		proxyGroup.setText(SVNUIMessages.RepositoryPropertiesTabFolder_Proxy);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalIndent = 10;
		proxyGroup.setLayoutData(data);
		
		Link proxyLink = new Link(proxyGroup, SWT.WRAP);
		proxyLink.setText(SVNUIMessages.RepositoryPropertiesTabFolder_Proxy_Link);
		proxyLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String pageId = "org.eclipse.ui.net.NetPreferences";
				PreferencesUtil.createPreferenceDialogOn(null, pageId, new String[] {pageId}, null).open();
			}
		});
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 200;
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this, 3);
		proxyLink.setLayoutData(data);
		
		tabItem.setControl(rootsComposite);
		this.unavailableSSHComposite = this.createUnavailableComposite(tabFolder);
		this.unavailableSSHComposite.setVisible(false);
		this.createSSHHostComposite(tabFolder);
		this.sshComposite.setVisible(false);
		this.sshTab = new TabItem(tabFolder, SWT.NONE);
		this.sshTab.setText(SVNUIMessages.RepositoryPropertiesTabFolder_SSHSettings);
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0) {
			this.sshTab.setControl(this.sshComposite);
		}
		else {
			this.sshTab.setControl(this.unavailableSSHComposite);
		}

		this.createSSLHostComposite(tabFolder);
		this.sslComposite.setVisible(false);
		this.sslTab = new TabItem(tabFolder, SWT.NONE);
		this.sslTab.setText(SVNUIMessages.RepositoryPropertiesTabFolder_SSLSettings);
		this.sslTab.setControl(this.sslComposite);	

		this.unavailableProxyComposite = this.createUnavailableComposite(tabFolder);
		this.unavailableProxyComposite.setVisible(false);

		Composite bottomPart = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0; 
		layout.marginWidth = 0; 
		layout.numColumns = 3;
		bottomPart.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		bottomPart.setLayoutData(data);

		Composite realmsComposite = new Composite(bottomPart, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		realmsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		realmsComposite.setLayoutData(data);
		
		Label label = new Label(realmsComposite, SWT.NONE);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNUIMessages.RepositoryPropertiesTabFolder_ShowFor);
		
		this.cachedRealms = new Combo(realmsComposite, SWT.BORDER | SWT.READ_ONLY);
		final Button deleteRealm = new Button(realmsComposite, SWT.PUSH);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.cachedRealms.setLayoutData(data);
		final ArrayList itemSet = new ArrayList();
		itemSet.add(ISVNCredentialsPrompt.ROOT_LOCATION);
		itemSet.addAll(this.repositoryLocation.getRealms());
		this.cachedRealms.setItems((String [])itemSet.toArray(new String[itemSet.size()]));
		this.cachedRealms.select(0);
		this.cachedRealms.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteRealm.setEnabled(RepositoryPropertiesTabFolder.this.cachedRealms.getSelectionIndex() != 0);
				RepositoryPropertiesTabFolder.this.realmSelectionChanged();
			}
		});
		
		ImageDescriptor imgDescr = SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/delete_realm.gif");
		deleteRealm.setImage(imgDescr.createImage());
		data = new GridData();
		data.heightHint = this.cachedRealms.getTextHeight() + 2;
		deleteRealm.setLayoutData(data);
		deleteRealm.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = RepositoryPropertiesTabFolder.this.cachedRealms.getSelectionIndex();
				if (idx != 0) {
					String item = RepositoryPropertiesTabFolder.this.cachedRealms.getItem(idx);
					itemSet.remove(item);
					RepositoryPropertiesTabFolder.this.cachedRealms.setItems((String [])itemSet.toArray(new String[itemSet.size()]));
					RepositoryPropertiesTabFolder.this.cachedRealms.select(idx - 1);
					RepositoryPropertiesTabFolder.this.realmSelectionChanged();
				}
				boolean enabled = RepositoryPropertiesTabFolder.this.cachedRealms.getItems().length > 1;
				((Button)e.widget).setEnabled(enabled);
				RepositoryPropertiesTabFolder.this.cachedRealms.setEnabled(enabled);
				idx = RepositoryPropertiesTabFolder.this.cachedRealms.getSelectionIndex();
				if (idx == 0) {
					((Button)e.widget).setEnabled(false);
				}
			}
		});
		deleteRealm.setEnabled(false);
		RepositoryPropertiesTabFolder.this.cachedRealms.setEnabled(itemSet.size() > 1);
		
		this.validateButton = new Button(bottomPart, SWT.CHECK);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		this.validateButton.setLayoutData(data);
		this.validateButton.setText(SVNUIMessages.RepositoryPropertiesTabFolder_ValidateOnFinish);
		this.validateButton.setSelection(true);
		
		Text empty = new Text(bottomPart, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		empty.setLayoutData(data);
		empty.setVisible(false);
		
		this.resetChangesButton = new Button(bottomPart, SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		this.resetChangesButton.setText(SVNUIMessages.RepositoryPropertiesTabFolder_ResetChanges);
		int widthHint = DefaultDialog.computeButtonWidth(this.resetChangesButton);
		data.widthHint = widthHint;
		this.resetChangesButton.setLayoutData(data);
		this.resetChangesButton.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
		    	RepositoryPropertiesTabFolder.this.resetChanges();
		    	RepositoryPropertiesTabFolder.this.validationManager.validateContent();
		    }
		});
		
		if ((this.repositoryLocation.getUsername() == null || this.repositoryLocation.getUsername().length() == 0) && 
			this.repositoryLocation.getRealms().size() > 0) {
			this.cachedRealms.select(1);
			deleteRealm.setEnabled(true);
			this.realmSelectionChanged();
		}
	}
	
	protected void realmSelectionChanged() {
		IRepositoryLocation location = this.repositoryLocation;
		int idx = this.cachedRealms.getSelectionIndex();
		if (idx != 0) {
			location = location.getLocationForRealm(this.cachedRealms.getItem(idx));
		}
		
		this.repositoryPropertiesPanel.saveChanges();
		this.repositoryPropertiesPanel.setCredentialsInput(location, this);
		this.repositoryPropertiesPanel.resetChanges();
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0) {
			this.sshComposite.saveChanges();
			this.sshComposite.setCredentialsInput(location.getSSHSettings());
			this.sshComposite.resetChanges();
		}
		this.sslComposite.saveChanges();
		this.sslComposite.setCredentialsInput(location.getSSLSettings());
		this.sslComposite.resetChanges();
	}
	
	protected Composite createRepositoryPropertiesPanel(Composite tabFolder) {
		this.repositoryPropertiesPanel = new RepositoryPropertiesComposite(tabFolder, this.style, this.validationManager);
		this.repositoryPropertiesPanel.setRepositoryLocation(this.repositoryLocation, this.createNew ? null : this.repositoryLocation.getRepositoryRootUrl(), this);
		this.repositoryPropertiesPanel.initialize();
		
		return this.repositoryPropertiesPanel;
	}
	
	protected Composite createSSHHostComposite(Composite tabFolder) {
		this.sshComposite = new SSHComposite(tabFolder, this.style, this.validationManager);
		this.sshComposite.setCredentialsInput(this.repositoryLocation.getSSHSettings());
		this.sshComposite.initialize();
		
		return this.sshComposite;
	}
	
	protected Composite createSSLHostComposite(Composite tabFolder) {
		this.sslComposite = new SSLComposite(tabFolder, this.style, this.validationManager);
		this.sslComposite.setCredentialsInput(this.repositoryLocation.getSSLSettings());
		this.sslComposite.initialize();
		
		return this.sslComposite;
	}
	
	protected Composite createRepositoryRootsComposite(Composite tabFolder) {
		this.rootsComposite = new RepositoryRootsComposite(tabFolder, this.style, this.validationManager);
		this.rootsComposite.setStructureEnabled(this.repositoryLocation.isStructureEnabled());
		
		this.rootsComposite.setTrunkLocation(this.repositoryLocation.getUserInputTrunk());
		this.rootsComposite.setBranchesLocation(this.repositoryLocation.getUserInputBranches());
		this.rootsComposite.setTagsLocation(this.repositoryLocation.getUserInputTags());
		
		this.rootsComposite.setCreateLocation(this.createNew);
		this.rootsComposite.initialize();
		
		return this.rootsComposite;
	}
	
	protected Composite createUnavailableComposite(Composite tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(SVNUIMessages.RepositoryPropertiesTabFolder_UnavailableMessage);
		
		Link link = new Link(composite, SWT.NONE);
		link.setText(SVNUIMessages.RepositoryPropertiesTabFolder_LinkToPreferences);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RepositoryPropertiesTabFolder.this.handleLinkSelection();
			}
		});
		
		return composite;
	}
	
	public void handleLinkSelection() {
		boolean sshWasAllowed = (CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0;

		String pageId = "org.eclipse.team.svn.ui.SVNTeamPreferences";
		PreferencesUtil.createPreferenceDialogOn(null, pageId, new String[] {pageId}, null).open();

		boolean sshAllowed = (CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0;
		
		this.updateTabContent(sshWasAllowed, sshAllowed, this.sshTab, this.sshComposite, this.unavailableSSHComposite);
	}
	
	public void updateTabContent(boolean wasAvailable, boolean isAvailable, TabItem tab, AbstractDynamicComposite availableComposite, Composite unavailableComposite) {
		if (isAvailable) {
			if (!wasAvailable) {
				availableComposite.restoreAppearance();
				tab.setControl(availableComposite);
			}
		}
		else {
			if (wasAvailable) {
				availableComposite.saveAppearance();
				tab.setControl(unavailableComposite);
				availableComposite.revalidateContent();
			}
		}
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.repositoryLocation;
	}
	
	public String getLocationUrl() {
		return this.repositoryPropertiesPanel.getLocationUrl();
	}
	
	public boolean isStructureEnabled() {
		return this.rootsComposite.isStructureEnabled();
	}
	
	public boolean isValidateOnFinishRequested() {
		return this.validateOnFinish;
	}
	
	public void saveChanges() {
		
		this.repositoryPropertiesPanel.saveChanges();
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0) {
			this.sshComposite.saveChanges();
		}
		this.sslComposite.saveChanges();
		this.rootsComposite.saveChanges();
		
		this.repositoryLocation.setAuthorName(this.authorInput.getText());
		this.repositoryLocation.setAuthorNameEnabled(this.authorEnabled.getSelection());
		this.authorNameHistory.addLine(this.authorInput.getText());
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean enabled = this.rootsComposite.isStructureEnabled();
		if (enabled) {
			this.repositoryLocation.setTrunkLocation(this.rootsComposite.getTrunkLocation());			
			this.repositoryLocation.setBranchesLocation(this.rootsComposite.getBranchesLocation());
			this.repositoryLocation.setTagsLocation(this.rootsComposite.getTagsLocation());
		}
		else if (this.createNew) {
			this.repositoryLocation.setTrunkLocation(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME));
			this.repositoryLocation.setBranchesLocation(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME));
			this.repositoryLocation.setTagsLocation(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME));
		}
		this.repositoryLocation.setStructureEnabled(enabled);
		
		HashSet realms = new HashSet(Arrays.asList(this.cachedRealms.getItems()));
		for (Iterator<String> it = this.repositoryLocation.getRealms().iterator(); it.hasNext(); ) {
			String current = it.next();
			if (!realms.contains(current)) {
				it.remove();
				SVNRemoteStorage.instance().removeAuthInfoForLocation(this.repositoryLocation, current);
			}
		}
		this.validateOnFinish = this.validateButton.getSelection();
	}

	public void resetChanges() {
		this.repositoryPropertiesPanel.resetChanges();
		if ((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.SSH_SETTINGS) != 0) {
			this.sshComposite.resetChanges();
		}
		this.sslComposite.resetChanges();
		this.rootsComposite.resetChanges();
	}

	public void cancelChanges() {
		if (!this.createNew) {
			SVNRemoteStorage.instance().copyRepositoryLocation(this.repositoryLocation, this.backup);
		}
	}

	public void setForceDisableRoots(boolean forceDisableRoots, AbstractVerifier verifier) {
		if (this.rootsComposite != null) {
			this.rootsComposite.setForceDisableRoots(forceDisableRoots);
		}
		if (this.repositoryPropertiesPanel != null) {
			this.repositoryPropertiesPanel.defineUrlVerifier(verifier);
		}
	}
	
}
