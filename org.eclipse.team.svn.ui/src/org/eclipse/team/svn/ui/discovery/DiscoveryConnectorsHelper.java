/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.discovery;

import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.team.svn.core.discovery.util.WebUtil;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.svnstorage.SVNCachedProxyCredentialsManager;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryLocation;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.discovery.wizards.ConnectorDiscoveryWizard;
import org.eclipse.team.svn.ui.panel.callback.PromptCredentialsPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.osgi.framework.Bundle;

/**
 * Operation which checks whether connectors are installed and if they're not,
 * it provides a wizard to installing them.
 * 
 * Users need to restart Eclipse in order to apply changes
 * 
 * @author Igor Burilo
 */
public class DiscoveryConnectorsHelper {
	
	protected static class ProxyAuthenticator extends Authenticator {
		
		protected PasswordAuthentication getPasswordAuthentication() {
			if (this.getRequestorType() == Authenticator.RequestorType.PROXY) {
				SVNCachedProxyCredentialsManager proxyCredentialsManager = SVNRemoteStorage.instance().getProxyCredentialsManager();					
				if (proxyCredentialsManager.getUsername() == null || proxyCredentialsManager.getUsername() == "") { //$NON-NLS-1$
					final boolean[] result = new boolean[1];
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							PromptCredentialsPanel panel = new PromptCredentialsPanel(getRequestingPrompt(), SVNRepositoryLocation.PROXY_CONNECTION);
							DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
							if (dialog.open() == 0) {
								result[0] = true; 
							}
						}						
					});
					if (result[0]) {
						String pswd = proxyCredentialsManager.getPassword();
						return new PasswordAuthentication(proxyCredentialsManager.getUsername(), pswd == null ? "".toCharArray() : pswd.toCharArray()); //$NON-NLS-1$
					}
				} else {							
					String pswd = proxyCredentialsManager.getPassword();
					return new PasswordAuthentication(proxyCredentialsManager.getUsername(), pswd == null ? "".toCharArray() : pswd.toCharArray()); //$NON-NLS-1$
				}																									
			}
			return null;
		}
	}	
	
	public void run(IProgressMonitor monitor) throws Exception {
		//check that connectors exist
		if (CoreExtensionsManager.instance().getAccessibleClients().isEmpty() && Platform.getBundle("org.eclipse.equinox.p2.repository") != null) { //$NON-NLS-1$
			final IConnectorsInstallJob[] installJob = new IConnectorsInstallJob[1];
			
			try {
				installJob[0] = this.getInstallJob();				
			} catch (Exception e) {
				//make more user-friendly error message
				throw new UnreportableException("Errors occured while initializing provisioning framework. This make cause discovery install to fail.", e); //$NON-NLS-1$
			}
			
			if (installJob[0] != null) {
				//set proxy authenticator to WebUtil for accessing Internet files
				WebUtil.setAuthenticator(new ProxyAuthenticator());							
				
				UIMonitorUtility.getDisplay().asyncExec(new Runnable() {
					public void run() {
						ConnectorDiscoveryWizard wizard = new ConnectorDiscoveryWizard(installJob[0]);
						WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
						dialog.open();		
					}
				});	
			}			
		}
	}
	
	/*
	 * If there's a problem during creating job, exception is thrown which indicates the reason
	 * 
	 * Note that job can be null, e.g. for Eclipse 3.4
	 */
	protected IConnectorsInstallJob getInstallJob() throws Exception {
		IConnectorsInstallJob runnable = null;
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.p2.engine"); //$NON-NLS-1$	
		if (bundle != null) {
			if (new VersionRange("[1.0.0,1.1.0)").isIncluded(bundle.getVersion())) { //$NON-NLS-1$
				//for Eclipse 3.5
				runnable = new PrepareInstallProfileJob_3_5();
			} else {
				//for Eclipse 3.6						
				Class<?> clazz = Class.forName("org.eclipse.team.svn.ui.discovery.PrepareInstallProfileJob_3_6"); //$NON-NLS-1$
				Constructor<?> c = clazz.getConstructor();
				runnable = (IConnectorsInstallJob) c.newInstance();			
			}	
		}
		return runnable;
	}
}


