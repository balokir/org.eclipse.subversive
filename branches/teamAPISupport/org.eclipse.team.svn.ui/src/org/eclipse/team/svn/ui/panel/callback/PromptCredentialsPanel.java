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

package org.eclipse.team.svn.ui.panel.callback;

import java.net.MalformedURLException;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;
import org.eclipse.team.svn.core.svnstorage.SVNCachedProxyCredentialsManager;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryLocation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CredentialsComposite;
import org.eclipse.team.svn.ui.composite.ProxyComposite;
import org.eclipse.team.svn.ui.composite.SSHComposite;
import org.eclipse.team.svn.ui.composite.SSLComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Prompt user credentials panel
 * 
 * @author Alexander Gurov
 */
public class PromptCredentialsPanel extends AbstractDialogPanel {
    public static ISVNCredentialsPrompt DEFAULT_PROMPT = new DefaultPrompt();

    protected String selectedRealm;
    
    protected CredentialsComposite credentialsComposite;
	protected String username;
	protected String password;
	protected String host;
	protected boolean savePassword;
	
	protected SSHComposite compositeSSH;
	protected SSLComposite compositeSSL;
	protected ProxyComposite proxyComposite;
	
	protected SSHSettings sshSettings;
	protected SSLSettings sslSettings;

	protected String rootLocationName;
	
	protected int connectionType;
	
	public PromptCredentialsPanel() {
        this("", SVNRepositoryLocation.DEFAULT_CONNECTION);
    }
    
    public PromptCredentialsPanel(String forWhat, int connectionType) {
        super();
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Description");
        this.rootLocationName = SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.LocationRealm");
        this.defaultMessage = forWhat;
        this.host = SVNTeamPlugin.instance().getProxyService().getProxyData(
        		forWhat.split(":")[0].equals("https") ? IProxyData.HTTPS_PROXY_TYPE : IProxyData.HTTP_PROXY_TYPE).getHost();
        this.connectionType = connectionType;
        this.selectedRealm = this.connectionType != SVNRepositoryLocation.PROXY_CONNECTION ? forWhat : this.rootLocationName;

        this.sslSettings = new SSLSettings();
        this.sslSettings.setAuthenticationEnabled(true);
        this.sshSettings = new SSHSettings();
        this.sshSettings.setUseKeyFile(true);
        this.savePassword = false;
		this.username = null;
		this.password = null;
    }

	public String getRealmToSave() {
		return this.selectedRealm == this.rootLocationName ? ISVNCredentialsPrompt.ROOT_LOCATION : this.selectedRealm;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setSavePasswordEnabled(boolean savePassword) {
		this.savePassword = savePassword;
	}
	
	public boolean isSavePasswordEnabled() {
		return this.savePassword;
	}
	
	public String getSSHPassphrase() {
		return this.sshSettings.getPassPhrase();
	}
	
	public void setSSHPassphrase(String passphrase) {
		this.sshSettings.setPassPhrase(passphrase);
	}
	
	public boolean isSSHPassphraseSaved() {
		return this.sshSettings.isPassPhraseSaved();
	}
	
	public void setSSHPassphraseSaved(boolean passphraseSaved) {
		this.sshSettings.setPassPhraseSaved(passphraseSaved);
	}
	
	public String getSSHPrivateKeyFile() {
		return this.sshSettings.getPrivateKeyPath();
	}
	
	public void setSSHPrivateKeyFile(String privateKeyFile) {
		this.sshSettings.setPrivateKeyPath(privateKeyFile);
	}
	
	public int getSSHPort() {
		return this.sshSettings.getPort();
	}
	
	public void setSSHPort(int sshPort) {
		this.sshSettings.setPort(sshPort);
	} 
	
	public boolean isSSHPublicKeySelected() {
		return this.sshSettings.isUseKeyFile();
	}
	
	public void setSSHPublicKeySelected(boolean publicKeySelected) {
		this.sshSettings.setUseKeyFile(publicKeySelected);
	};
	
	public boolean isSSLAuthenticationEnabled() {
		return this.sslSettings.isAuthenticationEnabled();
	}

	public void setSSLAuthenticationEnabled(boolean sslAuthenticationEnabled) {
		this.sslSettings.setAuthenticationEnabled(sslAuthenticationEnabled);
	}
	
	public String getSSLCertificatePath() {
		return this.sslSettings.getCertificatePath();
	}

	public void setSSLCertificatePath(String sslCertificatePath) {
		this.sslSettings.setCertificatePath(sslCertificatePath);
	}

	public String getSSLPassphrase() {
		return this.sslSettings.getPassPhrase();
	}

	public void setSSLPassphrase(String sslPassphrase) {
		this.sslSettings.setPassPhrase(sslPassphrase);
	}

	public boolean isSSLPassphraseSaved() {
		return this.sslSettings.isPassPhraseSaved();
	}

	public void setSSLPassphraseSaved(boolean sslPassphraseSaved) {
		this.sslSettings.setPassPhraseSaved(sslPassphraseSaved);
	}
    
    public void createControlsImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 3;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		composite.setVisible(this.connectionType != SVNRepositoryLocation.PROXY_CONNECTION && this.connectionType != SVNRepositoryLocation.SSL_CONNECTION);
		
		this.createGeneral(composite);
		
		if (this.connectionType != SVNRepositoryLocation.PROXY_CONNECTION) {
			if (this.connectionType != SVNRepositoryLocation.SSL_CONNECTION) {
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Tab.General"));
				tabItem.setControl(composite);
				
				if (this.connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
					tabItem = new TabItem(tabFolder, SWT.NONE);
					tabItem.setText(SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Tab.SSHSettings"));
					this.compositeSSH = new SSHComposite(tabFolder, SWT.NONE, this, true);
					this.compositeSSH.setCredentialsInput(this.sshSettings);
					this.compositeSSH.initialize();
					tabItem.setControl(this.compositeSSH);
					if (this.isSSHPublicKeySelected()) {
						tabFolder.setSelection(new TabItem[] {tabItem});
					}
				}
			}
			else {
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Tab.SSLSettings"));
				this.compositeSSL = new SSLComposite(tabFolder, SWT.NONE, this, true);
				this.compositeSSL.setCredentialsInput(this.sslSettings);
				this.compositeSSL.initialize();
				tabItem.setControl(this.compositeSSL);
			}
		}
		else {
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.Tab.ProxySettings"));
			this.proxyComposite = new ProxyComposite(tabFolder, SWT.NONE, this, true);
			SVNCachedProxyCredentialsManager proxyCredentialsManager = SVNRemoteStorage.instance().getProxyCredentialsManager();
   			this.proxyComposite.setUsername(proxyCredentialsManager.getUsername());
   			this.proxyComposite.setPassword(proxyCredentialsManager.getPassword());
   			this.proxyComposite.setHost(this.host);
			this.proxyComposite.initialize();
			tabItem.setControl(this.proxyComposite);
		}
		
		if (this.connectionType != SVNRepositoryLocation.PROXY_CONNECTION) {
			composite = new Composite(parent, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 1;
			layout.numColumns = 2;
			composite.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			
			Label label = new Label(composite, SWT.NONE);
			data = new GridData();
			label.setLayoutData(data);
			label.setText(SVNTeamUIPlugin.instance().getResource("PromptCredentialsPanel.ApplyTo"));
			
			final Combo combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			data = new GridData(GridData.FILL_HORIZONTAL);
			combo.setLayoutData(data);
			combo.setItems(new String[] {this.selectedRealm, this.rootLocationName});
			combo.select(0);
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PromptCredentialsPanel.this.selectedRealm = combo.getItem(combo.getSelectionIndex());
				}
			});
		}
    }

    public void postInit() {
		if (this.connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
			this.compositeSSH.resetChanges();
		}
		if (this.connectionType == SVNRepositoryLocation.SSL_CONNECTION) {
			this.compositeSSL.resetChanges();
		}
    	super.postInit();
    }
    
    protected void createGeneral(Composite parent) {
		this.credentialsComposite = new CredentialsComposite(parent, SWT.NONE);
		
		this.credentialsComposite.setUsernameInput(this.username);
		this.credentialsComposite.setPasswordInput(this.password);
		this.credentialsComposite.setPasswordSaved(this.savePassword);
		
		this.credentialsComposite.initialize();
    }
    
    protected void saveChangesImpl() {
    	if (this.connectionType != SVNRepositoryLocation.PROXY_CONNECTION) {
    		this.username = this.credentialsComposite.getUsername().getText().trim();
    		this.password = this.credentialsComposite.getPassword().getText().trim();
    		this.savePassword = this.credentialsComposite.getSavePassword().getSelection();
    	
    		if (this.connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
    			this.compositeSSH.saveChanges();
    		}
		
			if (this.connectionType == SVNRepositoryLocation.SSL_CONNECTION) {
				this.compositeSSL.saveChanges();
			}
    	} 
    	else {
    		this.proxyComposite.saveChanges();
    		SVNCachedProxyCredentialsManager proxyCredentialsManager = SVNRemoteStorage.instance().getProxyCredentialsManager();
			proxyCredentialsManager.setUsername(this.proxyComposite.getUsername());
			proxyCredentialsManager.setPassword(this.proxyComposite.getPassword());
		}
    }

    protected void cancelChangesImpl() {
    }

    protected static class DefaultPrompt implements ISVNCredentialsPrompt {
        protected String realmToSave;
    	protected String username;
    	protected String password;
		protected boolean saveCredentials;
		protected boolean sslAuthenticationEnabled;
		protected boolean sslSavePassphrase;
    	protected String sslClientCertPassword;
		protected String sslClientCertPath;
		protected boolean sshPublicKeySelected;
		protected String sshPrivateKeyPath;
		protected String sshPrivateKeyPassphrase;
		protected boolean sshPrivateKeyPassphraseSaved;
		protected int sshPort;
		protected boolean proxyEnabled;
		protected String proxyHost;
		protected int proxyPort;
		protected boolean proxyAuthenticationEnabled;
		protected String proxyUsername;
		protected String proxyPassword;
		protected boolean proxySavePassword;
		
		public String getRealmToSave() {
			return this.realmToSave;
		}
		
        public boolean prompt(Object context, String realm) {
            return this.showPanel((IRepositoryLocation)context, SVNRepositoryLocation.DEFAULT_CONNECTION, realm);
        }
        
		public boolean promptSSL(Object context, String realm) {
			return this.showPanel((IRepositoryLocation)context, SVNRepositoryLocation.SSL_CONNECTION, realm);
		}

	    public boolean promptSSH(Object context, String realm) {
	    	return this.showPanel((IRepositoryLocation)context, SVNRepositoryLocation.SSH_CONNECTION, realm);
	    }
	    
		public boolean promptProxy(Object context) {
			return this.showPanel((IRepositoryLocation)context, SVNRepositoryLocation.PROXY_CONNECTION, ((IRepositoryLocation)context).getUrlAsIs());
		}	
	    
		public int askTrustSSLServer(final Object context, final String info, final boolean allowPermanently) {
            final int []retVal = new int[1];
            UIMonitorUtility.getDisplay().syncExec(new Runnable() {
                public void run() {
                    AskTrustSSLServerPanel panel = new AskTrustSSLServerPanel(((IRepositoryLocation)context).getUrlAsIs(), info, allowPermanently);
                    DefaultDialog dlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
                	retVal[0] = dlg.open();
	            }
	        });
			return retVal[0] == 0 ? ISVNCredentialsPrompt.ACCEPT_TEMPORARY : (retVal[0] == 2 ? ISVNCredentialsPrompt.REJECT : (retVal[0] == 1 ? (allowPermanently ? ISVNCredentialsPrompt.ACCEPT_PERMANENTLY : ISVNCredentialsPrompt.REJECT) : ISVNCredentialsPrompt.REJECT));
		}
        
        public String getUsername() {
            return this.username;
        }
        
        public String getPassword() {
            return this.password;
        }
        
        public boolean isSaveCredentialsEnabled() {
            return this.saveCredentials;
        }
        
        public boolean isSSLAuthenticationEnabled() {
			return this.sslAuthenticationEnabled;
		}

		public boolean isSSLSavePassphrase() {
			return this.sslSavePassphrase;
		}

		public String getSSLClientCertPath() {
			return this.sslClientCertPath;
		}

		public String getSSLClientCertPassword() {
			return this.sslClientCertPassword;
		}

	    public String getSSHPrivateKeyPath() {
	    	return this.sshPublicKeySelected ? this.sshPrivateKeyPath : null;
	    }
	    
	    public String getSSHPrivateKeyPassphrase() {
	    	return this.sshPublicKeySelected ? this.sshPrivateKeyPassphrase : null;
	    }
	    
	    public int getSSHPort() {
	    	return this.sshPort;
	    }
	    
		public boolean isSSHPublicKeySelected() {
			return this.sshPublicKeySelected;
		}
		
		public boolean isSSHPrivateKeyPassphraseSaved() {
			return this.sshPrivateKeyPassphraseSaved;
		}
		
		public String getProxyHost() {
			return this.proxyHost;
		}

		public int getProxyPort() {
			return this.proxyPort;
		}

		public String getProxyUserName() {
			return this.proxyUsername;
		}

		public String getProxyPassword() {
			return this.proxyPassword;
		}
		
		public boolean isProxyEnabled() {
			return this.proxyEnabled;
		}
		
		public boolean isProxyAuthenticationEnabled() {
			return this.proxyAuthenticationEnabled;
		}

		public boolean isSaveProxyPassword() {
			return this.proxySavePassword;
		}
	    
	    protected boolean showPanel(IRepositoryLocation inputLocation, final int connectionType, final String realm) {
	    	final IRepositoryLocation location = inputLocation.getLocationForRealm(realm) != null ? inputLocation.getLocationForRealm(realm) : inputLocation;
            final int []retVal = new int[1];
            final SSLSettings settings = location.getSSLSettings();
            
            UIMonitorUtility.getDisplay().syncExec(new Runnable() {
                public void run() {
                    PromptCredentialsPanel panel = new PromptCredentialsPanel(realm, connectionType);
                    DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
                    if (connectionType != SVNRepositoryLocation.PROXY_CONNECTION) {
                    	 panel.setUsername(location.getUsername());
                         panel.setPassword(location.getPassword());
                         panel.setSavePasswordEnabled(location.isPasswordSaved());
                         if (connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
                         	SSHSettings settings = location.getSSHSettings();
                         	panel.setSSHPublicKeySelected(settings.isUseKeyFile());
                         	panel.setSSHPrivateKeyFile(settings.getPrivateKeyPath());
                         	panel.setSSHPassphrase(settings.getPassPhrase());
                         	panel.setSSHPassphraseSaved(settings.isPassPhraseSaved());
                         	panel.setSSHPort(settings.getPort());
                         } else if (connectionType == SVNRepositoryLocation.SSL_CONNECTION) {
                         	panel.setSSLAuthenticationEnabled(settings.isAuthenticationEnabled());
                         	panel.setSSLCertificatePath(settings.getCertificatePath());
                         	panel.setSSLPassphrase(settings.getPassPhrase());
                         	panel.setSSLPassphraseSaved(settings.isPassPhraseSaved());
                         }
                    } 
                    if ((retVal[0] = dialog.open()) == 0) {
                    	DefaultPrompt.this.realmToSave = panel.getRealmToSave();
                    	if (connectionType != SVNRepositoryLocation.PROXY_CONNECTION) {
                    		DefaultPrompt.this.username = panel.getUsername();
                    		DefaultPrompt.this.password = panel.getPassword();
                    		DefaultPrompt.this.saveCredentials = panel.isSavePasswordEnabled();
                    		
                    		if (connectionType == SVNRepositoryLocation.SSH_CONNECTION) {
                            	DefaultPrompt.this.sshPublicKeySelected = panel.isSSHPublicKeySelected();
                            	DefaultPrompt.this.sshPrivateKeyPath = panel.getSSHPrivateKeyFile();
                            	DefaultPrompt.this.sshPrivateKeyPassphrase = panel.getSSHPassphrase();
                            	DefaultPrompt.this.sshPort = panel.getSSHPort();
                            	DefaultPrompt.this.sshPrivateKeyPassphraseSaved = panel.isSSHPassphraseSaved();
                            }
                            if (connectionType == SVNRepositoryLocation.SSL_CONNECTION) {
                            	DefaultPrompt.this.sslAuthenticationEnabled = panel.isSSLAuthenticationEnabled();
                            	DefaultPrompt.this.sslClientCertPath = panel.getSSLCertificatePath();
                            	DefaultPrompt.this.sslClientCertPassword = panel.getSSLPassphrase();
                            	DefaultPrompt.this.sslSavePassphrase = panel.isSSLPassphraseSaved();
                            }
                    	} 
                    	else {
                    		IProxyService proxyService = SVNTeamPlugin.instance().getProxyService();
                    		String proxyType;
                    		SVNCachedProxyCredentialsManager proxyCredentialsManager = SVNRemoteStorage.instance().getProxyCredentialsManager();
                    		String protocol = "http";
                    		try {
                    			protocol = SVNUtility.getSVNUrl(location.getUrlAsIs()).getProtocol();
                    		}
                    		catch (MalformedURLException ex) {
                    			//ignore
                    		}
                    		if (protocol != null && protocol.equals("https")) {
                    			proxyType = IProxyData.HTTPS_PROXY_TYPE;
                    		}
                    		else {
                    			proxyType = IProxyData.HTTP_PROXY_TYPE;
                    		}
                    		IProxyData proxyData = proxyService.getProxyData(proxyType);
                            DefaultPrompt.this.proxyHost = proxyData.getHost();
                            DefaultPrompt.this.proxyPort = proxyData.getPort();
                            DefaultPrompt.this.proxyAuthenticationEnabled = proxyData.isRequiresAuthentication();
                            DefaultPrompt.this.proxyUsername = proxyCredentialsManager.getUsername();
                            DefaultPrompt.this.proxyPassword = proxyCredentialsManager.getPassword();
                            DefaultPrompt.this.proxySavePassword = true;
                        }
                    }
                    else {
                        DefaultPrompt.this.username = null;
                        DefaultPrompt.this.password = null;
                        DefaultPrompt.this.saveCredentials = false;
                    }
                }
            });
            return retVal[0] == 0;
	    }
	    
    }

}
