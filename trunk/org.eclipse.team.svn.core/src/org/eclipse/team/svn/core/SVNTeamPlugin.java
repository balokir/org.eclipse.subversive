/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Zend Technologies - [patch] Workaround for 'thread "Worker-3" timed out waiting (5000ms) for thread...' problem 
 *******************************************************************************/

package org.eclipse.team.svn.core;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.crashrecovery.DefaultErrorHandlingFacility;
import org.eclipse.team.svn.core.extension.crashrecovery.IErrorHandlingFacility;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Team provider plug-in implementation
 * 
 * @author Alexander Gurov
 */
public class SVNTeamPlugin extends Plugin {
	// all projects shared with subversion will have this nature
	public static final String NATURE_ID = "org.eclipse.team.svn.core.svnnature"; //$NON-NLS-1$

	public static final String CORE_SVNCLIENT_NAME = "svnconnector"; //$NON-NLS-1$

	private volatile static SVNTeamPlugin instance = null;

	private ProjectCloseListener pcListener;
	private ResourceChangeListener rcListener;
	private SVNFolderListener svnListener;
	private ServiceTracker tracker;
	//FIXME see bug #276018 private FileReplaceListener fileReplaceListener;

	private IErrorHandlingFacility errorHandlingFacility;

	/**
	 * Allows to save changes in repository root URL and UUID
	 */
	private boolean isLocationsDirty;

	public SVNTeamPlugin() {
		super();

		SVNTeamPlugin.instance = this;

		this.pcListener = new ProjectCloseListener();
		this.rcListener = new ResourceChangeListener();
		this.svnListener = new SVNFolderListener();
		//FIXME see bug #276018 this.fileReplaceListener = new FileReplaceListener();

		this.errorHandlingFacility = new DefaultErrorHandlingFacility();
	}

	public void setLocationsDirty(boolean isLocationsDirty) {
		this.isLocationsDirty = isLocationsDirty;
	}

	public boolean isLocationsDirty() {
		return this.isLocationsDirty;
	}

	public static SVNTeamPlugin instance() {
		return SVNTeamPlugin.instance;
	}

	public IOptionProvider getOptionProvider() {
		return CoreExtensionsManager.instance().getOptionProvider();
	}

	public IErrorHandlingFacility getErrorHandlingFacility() {
		return this.errorHandlingFacility;
	}

	// FIXME remove later after Integration API is changed
	public void setOptionProvider(IOptionProvider optionProvider) {
		CoreExtensionsManager.instance().setOptionProvider(optionProvider == null ? IOptionProvider.DEFAULT : optionProvider);

		SVNRemoteStorage.instance().reconfigureLocations();

		// remove temporary files if IDE is crached time ago...
		ProgressMonitorUtility.doTaskScheduledDefault(new AbstractActionOperation("Remove Temporary Files", SVNMessages.class) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNTeamPlugin.instance().getStateLocation().toFile().listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						String name = pathname.getName();
						if (!name.equals(SVNRemoteStorage.STATE_INFO_FILE_NAME) && !name.equals(SVNFileStorage.STATE_INFO_FILE_NAME)) {
							FileUtility.deleteRecursive(pathname);
						}
						return false;
					}
				});
			}
		});
	}

	/**
	 * Return the Subversive Core preferences node in the instance scope
	 */
	public IEclipsePreferences getSVNCorePreferences() {
		return new InstanceScope().getNode(this.getBundle().getSymbolicName());
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		this.tracker = new ServiceTracker(context, IProxyService.class.getName(), null);
		this.tracker.open();

		HashMap preferences = new HashMap();
		preferences.put(ISVNStorage.PREF_STATE_INFO_LOCATION, this.getStateLocation());
		SVNFileStorage.instance().initialize(preferences);
		SVNRemoteStorage.instance().initialize(preferences);

		WorkspaceJob job = new WorkspaceJob("") { //$NON-NLS-1$
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();

				workspace.addResourceChangeListener(SVNTeamPlugin.this.rcListener, IResourceChangeEvent.POST_CHANGE);
				workspace.addResourceChangeListener(SVNTeamPlugin.this.svnListener, IResourceChangeEvent.PRE_BUILD);
				workspace.addResourceChangeListener(SVNTeamPlugin.this.pcListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
				//FIXME see bug #276018 workspace.addResourceChangeListener(SVNTeamPlugin.this.fileReplaceListener, IResourceChangeEvent.PRE_BUILD);

				// shouldn't prevent plugin start
				try {
					SVNTeamPlugin.this.rcListener.handleInitialWorkspaceDelta();
				}
				catch (Throwable ex) {
					LoggedOperation.reportError("Handle Initial Workspace Delta", ex);
				}

				// if some team provider is missing the code below "enables" team menu "Share Project" action...
				IProject []projects = workspace.getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					RepositoryProvider.getProvider(projects[i]);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setUser(false);
		job.schedule();
	}

	public IProxyService getProxyService() {
		return (IProxyService)this.tracker.getService();
	}

	public void stop(BundleContext context) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		workspace.removeResourceChangeListener(this.svnListener);
		workspace.removeResourceChangeListener(this.rcListener);
		workspace.removeResourceChangeListener(this.pcListener);
		//FIXME see bug #276018 workspace.removeResourceChangeListener(this.fileReplaceListener);

		if (this.isLocationsDirty) {
			SVNRemoteStorage.instance().saveConfiguration();
			SVNFileStorage.instance().saveConfiguration();
		}
		SVNRemoteStorage.instance().dispose();
		SVNFileStorage.instance().dispose();

		this.tracker.close();

		// cleanup temporary files if any
		File temporaryFilesStorage = SVNTeamPlugin.instance().getStateLocation().toFile();
		File []files = temporaryFilesStorage.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().indexOf(".tmp") != -1; //$NON-NLS-1$
			}
		});
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				FileUtility.deleteRecursive(files[i]);
			}
		}

		super.stop(context);
	}

}
