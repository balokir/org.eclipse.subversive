/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.mapping.SVNActiveChangeSetCollector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.discovery.ui.wizards.ConnectorDiscoveryWizard;
import org.eclipse.team.svn.ui.console.SVNConsole;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin entry point. Implements "system facade" pattern
 * 
 * @author Alexander Gurov
 */
public class SVNTeamUIPlugin extends AbstractUIPlugin {
	private volatile static SVNTeamUIPlugin instance = null;
	
	private ProjectCloseListener pcListener;
	private URL baseUrl;
//	private ProblemListener problemListener;
	
	private SVNConsole console;
	private ActiveChangeSetManager activeChangeSetManager;

    public SVNTeamUIPlugin() {
        super();
        
        this.pcListener = new ProjectCloseListener();
//        this.problemListener = new ProblemListener();
        
        SVNTeamUIPlugin.instance = this;
    }
    
    public static SVNTeamUIPlugin instance() {
    	return SVNTeamUIPlugin.instance;
    }
    
    public IConsoleStream getConsoleStream() {
    	return this.console == null ? null : this.console.getConsoleStream();
    }
    
    public ImageDescriptor getImageDescriptor(String path) {
    	try {
			return ImageDescriptor.createFromURL(new URL(this.baseUrl, path));
		} 
    	catch (MalformedURLException e) {
			LoggedOperation.reportError(SVNUIMessages.getErrorString("Error_GetImageDescriptor"), e); //$NON-NLS-1$
			return null;
		}
    }
    
    public String getVersionString() {
        return (String)this.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
    }
    
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
//		Platform.addLogListener(this.problemListener);
		
		this.getModelCangeSetManager();
		
		SVNTeamPreferences.setDefaultValues(this.getPreferenceStore());
		
		Preferences corePreferences = SVNTeamPlugin.instance().getPluginPreferences();
		
		// Earlier Subversive releases save connector id in SVNTeamPlugin store
		// To be compatible with earlier releases copy saved preferences to
		// SVNTeamUIPlugin store and clear SVNTeamPlugin store
		String connector = corePreferences.getString(SVNTeamPlugin.CORE_SVNCLIENT_NAME).trim();
		if (connector.length() != 0) {
			SVNTeamPreferences.setCoreString(this.getPreferenceStore(), SVNTeamPreferences.CORE_SVNCONNECTOR_NAME, connector);
			corePreferences.setValue(SVNTeamPlugin.CORE_SVNCLIENT_NAME, ""); //$NON-NLS-1$
			SVNTeamPlugin.instance().savePluginPreferences();
		}

        this.baseUrl = context.getBundle().getEntry("/"); //$NON-NLS-1$
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(SVNTeamUIPlugin.this.pcListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
		
		this.console = new SVNConsole();
		
		//check that connectors exist
		if (CoreExtensionsManager.instance().getAccessibleClients().isEmpty()) {
			UIMonitorUtility.getDisplay().asyncExec(new Runnable() {
				public void run() {
					ConnectorDiscoveryWizard wizard = new ConnectorDiscoveryWizard();
					WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
					dialog.open();		
				}
			});	
		}					
	}
	
	public void stop(BundleContext context) throws Exception {
		this.console.shutdown();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		workspace.removeResourceChangeListener(this.pcListener);
		
		if (this.activeChangeSetManager != null) {
			this.activeChangeSetManager.dispose();
		}

//		Platform.removeLogListener(this.problemListener);
		super.stop(context);
	}
	
	public synchronized ActiveChangeSetManager getModelCangeSetManager() {
		if (this.activeChangeSetManager == null) {
			this.activeChangeSetManager = new SVNActiveChangeSetCollector(UpdateSubscriber.instance());
		}
		return this.activeChangeSetManager;
	}
	
}
