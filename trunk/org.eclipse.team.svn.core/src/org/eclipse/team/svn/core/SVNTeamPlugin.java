/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Zend Technologies - Workaround for 'thread "Worker-3" timed out waiting (5000ms) for thread...' problem 
 *******************************************************************************/

package org.eclipse.team.svn.core;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.crashrecovery.DefaultErrorHandlingFacility;
import org.eclipse.team.svn.core.extension.crashrecovery.IErrorHandlingFacility;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.osgi.framework.BundleContext;

/**
 * Team provider plugin implementation
 * 
 * @author Alexander Gurov
 */
public class SVNTeamPlugin extends Plugin {
	// all projects shared with subversion will have this nature
	public static final String NATURE_ID = "org.eclipse.team.svn.core.svnnature";
	
	public static final String CORE_SVNCLIENT_NAME = "svnclient";
	
	private static SVNTeamPlugin instance = null;
	
	private ProjectCloseListener pcListener;
	private ResourceChangeListener rcListener;
	private SVNFolderListener svnListener;
	
	private IErrorHandlingFacility errorHandlingFacility;
	
	public SVNTeamPlugin() {
		super();
		
        SVNTeamPlugin.instance = this;
        
        this.pcListener = new ProjectCloseListener();
        this.rcListener = new ResourceChangeListener();
        this.svnListener = new SVNFolderListener();
        
		this.errorHandlingFacility = new DefaultErrorHandlingFacility();
	}

    public static SVNTeamPlugin instance() {
    	return SVNTeamPlugin.instance;
    }
    
    public String getResource(String key) {
        return FileUtility.getResource(Platform.getResourceBundle(this.getBundle()), key);
    }
    
	public IOptionProvider getOptionProvider() {
		return CoreExtensionsManager.instance().getOptionProvider();
	}
	
	public IErrorHandlingFacility getErrorHandlingFacility() {
		return this.errorHandlingFacility;
	}
	
	//FIXME remove later after Integration API is changed
    public void setOptionProvider(IOptionProvider optionProvider) {
    	CoreExtensionsManager.instance().setOptionProvider(optionProvider == null ? IOptionProvider.DEFAULT : optionProvider);
    	
    	SVNRemoteStorage.instance().reconfigureLocations();
		
		// remove temporary files if IDE is crached time ago...
		ProgressMonitorUtility.doTaskScheduledDefault(new AbstractNonLockingOperation("Remove Temporary Files") {
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
    
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		IPath stateLocation = this.getStateLocation();
		SVNFileStorage.instance().initialize(stateLocation);
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		storage.initialize(stateLocation);
		
		// check for previous plugin versions repository info storage 
		if (storage.getRepositoryLocations().length == 0) {
			String coreFolderName = stateLocation.lastSegment();
			IPath uiLocation = stateLocation.removeLastSegments(1).append(coreFolderName.substring(0, coreFolderName.length() - 4) + "ui");
			File previousStore = new File(uiLocation + "/" + SVNRemoteStorage.STATE_INFO_FILE_NAME);
			if (previousStore.exists()) {
				FileUtility.copyFile(new File(stateLocation.toString() + "/" + SVNRemoteStorage.STATE_INFO_FILE_NAME), previousStore, new NullProgressMonitor());
				previousStore.delete();
				storage.initialize(stateLocation);
			}
		}
		
		// In some very complex cases Eclipse Platform throws following exceptions:
		//	While loading class "org.eclipse.team.svn.ui.SVNFolderListener$1", thread "Worker-5" timed out waiting (5000ms) for thread "Worker-2" to finish starting bundle "org.eclipse.team.svn.ui". To avoid deadlock, thread "Worker-5" is proceeding but
		//
		// The problem can be reproduced by changing resource tree and delaying start() method execution after listeners is installed.
		// NOTE: I think the problem is related to incorrect check at org.eclipse.core.runtime.internal.adaptor.EclipseLazyStarter.preFindLocalClass(EclipseLazyStarter.java:75).
		// But I'm not sure that check is completelly incorrect. 
//		new Thread() {
//			public void run() {
//				try {
		WorkspaceJob job = new WorkspaceJob("") {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				
				workspace.addResourceChangeListener(SVNTeamPlugin.this.rcListener, IResourceChangeEvent.POST_CHANGE);
				workspace.addResourceChangeListener(SVNTeamPlugin.this.svnListener, IResourceChangeEvent.PRE_BUILD);
				workspace.addResourceChangeListener(SVNTeamPlugin.this.pcListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);

				// shouldn't prevent plugin start 
				try {
					SVNTeamPlugin.this.rcListener.handleInitialWorkspaceDelta();
				}
				catch (Throwable ex) {
					LoggedOperation.reportError("Handle Initial Workspace Delta", ex);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setUser(false);
		job.schedule();
//				}
//				catch (CoreException ex) {
//					throw new RuntimeException(ex);
//				}
//			}
//		}.start();
	}
	
	public void stop(BundleContext context) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		workspace.removeResourceChangeListener(this.svnListener);
		workspace.removeResourceChangeListener(this.rcListener);
		workspace.removeResourceChangeListener(this.pcListener);
		
		try {
			SVNRemoteStorage.instance().saveConfiguration();
		} 
		catch (Exception ex) {
			LoggedOperation.reportError("Save Repository Locations [Resource]", ex);
		}
		
		try {
			SVNFileStorage.instance().saveConfiguration();
		} 
		catch (Exception ex) {
			LoggedOperation.reportError("Save Repository Locations [File]", ex);
		}
		
		SVNRemoteStorage.instance().dispose();
		SVNFileStorage.instance().dispose();
		
		// cleanup temporary files if any
		File temporaryFilesStorage = SVNTeamPlugin.instance().getStateLocation().toFile();
		File []files = temporaryFilesStorage.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("tmp");
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
