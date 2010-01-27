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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.mapping.SVNActiveChangeSetCollector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.ui.console.SVNConsole;
import org.eclipse.team.svn.ui.decorator.SVNLightweightDecorator;
import org.eclipse.team.svn.ui.discovery.DiscoveryConnectorsHelper;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.ui.PlatformUI;
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
    
    public SVNConsole getConsole() {
    	return this.console;
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
    
    /*
     * Important: Don't call any CoreExtensionsManager's methods here,
     * because CoreExtensionsManager instantiates some UI classes through extension
     * points and there could be problems in following case:
     * Subversive core calls CoreExtensionsManager, where CoreExtensionsManager calls SVNTeamUIPlugin.start
     * but SVNTeamUIPlugin.start calls CoreExtensionsManager
     */
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
				
		IPreferenceStore store = this.getPreferenceStore();
		if (store.getBoolean(SVNTeamPreferences.FIRST_STARTUP)) {
			// If we enable the decorator in the XML, the SVN plugin will be loaded
			// on startup even if the user never uses SVN. Therefore, we enable the 
			// decorator on the first start of the SVN plugin since this indicates that 
			// the user has done something with SVN. Subsequent startups will load
			// the SVN plugin unless the user disables the decorator. In this case,
			// we will not re-enable since we only enable automatically on the first startup.
			PlatformUI.getWorkbench().getDecoratorManager().setEnabled(SVNLightweightDecorator.ID, true);
			store.setValue(SVNTeamPreferences.FIRST_STARTUP, false);
		}
		
		//run discovery connectors
		this.discoveryConnectors();	
	}
	
	protected void discoveryConnectors() {
		/*
		 * We can't run Discovery Connectors through IActionOperation, because
		 * it uses CoreExtensionsManager (for getting nationalized operation name),
		 * which isn't allowed here, see bug 300592; so instead we use Job. 
		 */
		Job job = new Job(SVNUIMessages.Operation_DiscoveryConnectors) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					DiscoveryConnectorsHelper discovery = new DiscoveryConnectorsHelper();														
					discovery.run(monitor);																
				} catch (Throwable t) {
					//shouldn't prevent plug-in start 
					LoggedOperation.reportError(SVNUIMessages.Operation_DiscoveryConnectors_Error, t); 
				}
				return Status.OK_STATUS;
			}
		};				
		job.setUser(false);
		job.schedule();
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
