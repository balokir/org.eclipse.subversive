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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.mapping.SVNActiveChangeSetCollector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.console.SVNConsole;
import org.eclipse.team.svn.ui.console.SVNConsoleFactory;
import org.eclipse.team.svn.ui.decorator.SVNLightweightDecorator;
import org.eclipse.team.svn.ui.discovery.DiscoveryConnectorsHelper;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

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
	
	private ActiveChangeSetManager activeChangeSetManager;

    public SVNTeamUIPlugin() {
        super();

        SVNTeamUIPlugin.instance = this;
        
        this.pcListener = new ProjectCloseListener();
//        this.problemListener = new ProblemListener();
    }
    
    public static SVNTeamUIPlugin instance() {
    	return SVNTeamUIPlugin.instance;
    }
    
    /**
     * @deprecated
     */
    public SVNConsole getConsole() {
    	return SVNConsoleFactory.getConsole();
    }
    
    /**
     * @deprecated
     */
    public IConsoleStream getConsoleStream() {
    	return SVNConsoleFactory.getConsole().getConsoleStream();
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
    
	public IEclipsePreferences getPreferences() {
		return new InstanceScope().getNode(this.getBundle().getSymbolicName());
	}
	
	public void savePreferences() {
		try {
			SVNTeamUIPlugin.instance().getPreferences().flush();
		} 
		catch (BackingStoreException ex) {
			UILoggedOperation.reportError(SVNUIMessages.getErrorString("Error_SavePreferences"), ex); //$NON-NLS-1$
		}
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
		
		this.getModelChangeSetManager();
		
        this.baseUrl = context.getBundle().getEntry("/"); //$NON-NLS-1$
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(SVNTeamUIPlugin.this.pcListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
		
		IPreferenceStore store = this.getPreferenceStore();
		if (store.getBoolean(SVNTeamPreferences.FIRST_STARTUP) || this.isConnectorsRequired()) {
			store.setValue(SVNTeamPreferences.FIRST_STARTUP, false);
			this.savePreferences();
			// If we enable the decorator in the XML, the SVN plugin will be loaded
			// on startup even if the user never uses SVN. Therefore, we enable the 
			// decorator on the first start of the SVN plugin since this indicates that 
			// the user has done something with SVN. Subsequent startups will load
			// the SVN plugin unless the user disables the decorator. In this case,
			// we will not re-enable since we only enable automatically on the first startup.
			PlatformUI.getWorkbench().getDecoratorManager().setEnabled(SVNLightweightDecorator.ID, true);
			
			//run discovery connectors
			this.discoveryConnectors();	
		}
	}
	
	protected boolean isConnectorsRequired() {
		boolean svnProjectFound = false;
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			svnProjectFound |= projects[i].findMember(SVNUtility.getSVNFolderName()) != null || FileUtility.isConnected(projects[i]);
			if (svnProjectFound) {
				break;
			}
		}
		return svnProjectFound && !CoreExtensionsManager.isExtensionsRegistered(CoreExtensionsManager.SVN_CONNECTOR);
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
		job.setSystem(true);
		job.setUser(false);
		job.schedule();
	}
	
	public void stop(BundleContext context) throws Exception {
		SVNConsoleFactory.destroyConsole();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		workspace.removeResourceChangeListener(this.pcListener);
		
		if (this.activeChangeSetManager != null) {
			this.activeChangeSetManager.dispose();
		}

//		Platform.removeLogListener(this.problemListener);
		super.stop(context);
	}
	
	public synchronized ActiveChangeSetManager getModelChangeSetManager() {
		if (this.activeChangeSetManager == null) {
			this.activeChangeSetManager = new SVNActiveChangeSetCollector(UpdateSubscriber.instance());
		}
		return this.activeChangeSetManager;
	}
	
}
