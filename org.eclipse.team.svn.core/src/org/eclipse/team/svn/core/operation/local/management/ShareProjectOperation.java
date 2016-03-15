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

package org.eclipse.team.svn.core.operation.local.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProjectMapper;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Share project operation implementation
 * 
 * @author Alexander Gurov
 */
public class ShareProjectOperation extends AbstractWorkingCopyOperation {
	public static final int LAYOUT_DEFAULT = 0;
	public static final int LAYOUT_SINGLE = 1;
	public static final int LAYOUT_MULTIPLE = 2;
	
	public interface IFolderNameMapper {
		public String getRepositoryFolderName(IProject project);
	}
	
	protected IRepositoryLocation location;
	protected IFolderNameMapper mapper;
	protected int shareLayout;
	protected String rootName;
	protected boolean managementFoldersEnabled;
	protected String commitComment;
	protected IShareProjectPrompt shareProjectPrompt;
	protected boolean ignoreExternals;
	
	public ShareProjectOperation(IProject []projects, IRepositoryLocation location, IFolderNameMapper mapper) {
		this(projects, location, mapper, null);
	}
	
	public ShareProjectOperation(IProject []projects, IRepositoryLocation location, IFolderNameMapper mapper, String commitComment) {
		this(projects, location, mapper, null, ShareProjectOperation.LAYOUT_DEFAULT, true, commitComment);
	}

	public ShareProjectOperation(IProject []projects, IRepositoryLocation location, IFolderNameMapper mapper, String rootName, int shareLayout, boolean managementFoldersEnabled) {
		this(projects, location, mapper, rootName, shareLayout, managementFoldersEnabled, null);
	}
	
	public ShareProjectOperation(IProject []projects, IRepositoryLocation location, IFolderNameMapper mapper, String rootName, int shareLayout, boolean managementFoldersEnabled, String commitComment) {
		super("Operation_ShareProject", SVNMessages.class, projects); //$NON-NLS-1$
		this.mapper = mapper;
		this.location = location;
		this.shareLayout = shareLayout;
		this.rootName = rootName;
		this.managementFoldersEnabled = managementFoldersEnabled;
		this.commitComment = commitComment;
	}
	
	public void setIngoreExternals(boolean ignoreExternals) {
		this.ignoreExternals = ignoreExternals;
	}
	
	public void setSharePrompt(IShareProjectPrompt prompt) {
		this.shareProjectPrompt = prompt;
	}
	
	public static String getTargetUrl(IRepositoryLocation location, int shareLayout, String projectName, String rootName, boolean managementFoldersEnabled) {
		return SVNUtility.normalizeURL(ShareProjectOperation.getTargetUrlImpl(location, shareLayout, projectName, rootName, managementFoldersEnabled));
	}
	
	public static String getDefaultComment(IProject project, IRepositoryResource remote) {
		return SVNMessages.format(SVNMessages.Operation_ShareProject_DefaultComment, new String[] {project.getName(), SVNUtility.encodeURL(remote.getUrl())});
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		HashMap local2remote = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			String url = ShareProjectOperation.getTargetUrl(this.location, this.shareLayout, this.mapper == null ? resources[i].getName() : this.mapper.getRepositoryFolderName((IProject)resources[i]), this.rootName, this.managementFoldersEnabled);
			IRepositoryContainer remote = this.location.asRepositoryContainer(url, false);
			local2remote.put(resources[i], remote);
		}
		
		if (this.commitComment == null) {
			this.commitComment = ""; //$NON-NLS-1$
			for (Iterator it = local2remote.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				String commentPart = ShareProjectOperation.getDefaultComment((IProject)entry.getKey(), (IRepositoryContainer)entry.getValue());
				this.commitComment += this.commitComment.length() == 0 ? commentPart : ("\n" + commentPart); //$NON-NLS-1$
			}
		}
		
		Set fullSet = null;
		switch (this.shareLayout) {
			case ShareProjectOperation.LAYOUT_DEFAULT: {
				fullSet = this.doDefaultLayout(local2remote);
				break;
			}
			case ShareProjectOperation.LAYOUT_SINGLE: {
				fullSet = this.doSingleLayout(local2remote);
				break;
			}
			case ShareProjectOperation.LAYOUT_MULTIPLE: {
				fullSet = this.doMultipleLayout(local2remote);
				break;
			}
			default: {
				String message = this.getNationalizedString("Error_UnknownProjectLayoutType"); //$NON-NLS-1$
				throw new Exception(BaseMessages.format(message, new Object[] {String.valueOf(this.shareLayout)}));
			}
		}
		
		final HashSet existingProjects = new HashSet();
		for (Iterator it = local2remote.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			final Map.Entry entry = (Map.Entry)it.next();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					try {
						IRepositoryContainer remote = (IRepositoryContainer)entry.getValue();
						if (remote.getChildren().length > 0) {
							existingProjects.add(entry.getKey());
						}
					}
					catch (SVNConnectorException ex) {
						// do nothing
					}
				}
			}, monitor, local2remote.size() * 2);
		}			
		
		if (existingProjects.size() > 0 && this.shareProjectPrompt != null && !this.shareProjectPrompt.prompt((IProject [])existingProjects.toArray(new IProject[existingProjects.size()]))) {
			throw new SVNConnectorCancelException(this.getNationalizedString("Error_ShareCanceled")); //$NON-NLS-1$
		}
		
		final ISVNConnector proxy = this.location.acquireSVNProxy();
		try {
			IRepositoryResource []resourceSet = this.getOrderedSet(fullSet);
			this.mkdir(proxy, resourceSet, monitor);
			
			for (Iterator it = local2remote.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				final Map.Entry entry = (Map.Entry)it.next();
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						IRepositoryContainer remote = (IRepositoryContainer)entry.getValue();
						IProject project = (IProject)entry.getKey();
						
						File tempDir = existingProjects.contains(project) ? ShareProjectOperation.this.createTempDirectory(project) : null;
						String checkoutTo = tempDir != null ? tempDir.toString() : FileUtility.getWorkingCopyPath(project);
						long options = ShareProjectOperation.this.ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE;
						proxy.checkout(SVNUtility.getEntryRevisionReference(remote), checkoutTo, SVNDepth.INFINITY, options, new SVNProgressMonitor(ShareProjectOperation.this, monitor, null));
						
						if (tempDir != null) {
							ShareProjectOperation.this.copySVNMeta(tempDir, FileUtility.getResourcePath(project).toFile());
							FileUtility.deleteRecursive(tempDir, monitor);
						}
						
						SVNTeamProjectMapper.map(project, remote);
					}
				}, monitor, local2remote.size() * 2);
			}
		}
		finally {
			this.location.releaseSVNProxy(proxy);
		}
	}
	
	protected Set doDefaultLayout(Map local2remote) throws Exception {
		HashSet fullSet = new HashSet();
		for (Iterator it = local2remote.values().iterator(); it.hasNext(); ) {
			IRepositoryContainer remote = (IRepositoryContainer)it.next();
			IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
			fullSet.addAll(Arrays.asList(resources));
		}
		return fullSet;
	}
	
	protected Set doSingleLayout(Map local2remote) throws Exception {
		if (this.managementFoldersEnabled) {
			HashSet fullSet = new HashSet();
			for (Iterator it = local2remote.values().iterator(); it.hasNext(); ) {
				IRepositoryContainer remote = (IRepositoryContainer)it.next();
				IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
				fullSet.addAll(Arrays.asList(resources));
				IRepositoryContainer parent = (IRepositoryContainer)remote.getParent();
				fullSet.add(this.makeChild(parent, ShareProjectOperation.getTagsName(this.location)));
				fullSet.add(this.makeChild(parent, ShareProjectOperation.getBranchesName(this.location)));
			}
			return fullSet;
		}
		return this.doDefaultLayout(local2remote);
	}
	
	protected Set doMultipleLayout(Map local2remote) throws Exception {
		if (this.managementFoldersEnabled) {
			HashSet fullSet = new HashSet();
			for (Iterator it = local2remote.values().iterator(); it.hasNext(); ) {
				IRepositoryContainer remote = (IRepositoryContainer)it.next();
				IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
				fullSet.addAll(Arrays.asList(resources));
				
				String targetUrl = ShareProjectOperation.getTargetUrl(this.location, this.shareLayout, "", this.rootName, false); //$NON-NLS-1$
				IRepositoryContainer parent = this.location.asRepositoryContainer(targetUrl, false);
				fullSet.add(this.makeChild(parent, ShareProjectOperation.getTagsName(this.location)));
				fullSet.add(this.makeChild(parent, ShareProjectOperation.getBranchesName(this.location)));
			}
			return fullSet;
		}
		return this.doDefaultLayout(local2remote);
	}
	
	protected IRepositoryResource []getOrderedSet(Set fullSet) {
		IRepositoryResource [] resources = (IRepositoryResource [])fullSet.toArray(new IRepositoryResource[fullSet.size()]);
		Arrays.sort(resources, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				IRepositoryResource first = (IRepositoryResource)arg0;
				IRepositoryResource second = (IRepositoryResource)arg1;
				return first.getUrl().compareTo(second.getUrl());
			}
		});
		return resources;
	}
	
	protected void mkdir(ISVNConnector proxy, IRepositoryResource []resourceSet, IProgressMonitor monitor) throws Exception {
		ArrayList urlsList = new ArrayList();
		for (int i = 0; i < resourceSet.length && !monitor.isCanceled(); i++) {
			ProgressMonitorUtility.setTaskInfo(monitor, this, resourceSet[i].getUrl());
			if (!resourceSet[i].exists()) {
				urlsList.add(SVNUtility.encodeURL(resourceSet[i].getUrl()));
			}
			ProgressMonitorUtility.progress(monitor, IProgressMonitor.UNKNOWN, resourceSet.length);
		}
		String []urls = (String [])urlsList.toArray(new String[urlsList.size()]);
		proxy.mkdir(urls, this.commitComment, ISVNConnector.Options.INCLUDE_PARENTS, null, new SVNProgressMonitor(this, monitor, null));
	}
	
	protected IRepositoryContainer makeChild(IRepositoryContainer parent, String name) {
		return this.location.asRepositoryContainer(parent.getUrl() + "/" + name, false); //$NON-NLS-1$
	}
	
	protected File createTempDirectory(IProject project) {
		try {
			File tempDirectory = File.createTempFile("save_" + project.getName(), ".tmp", FileUtility.getResourcePath(project).toFile().getParentFile()); //$NON-NLS-1$ //$NON-NLS-2$
			tempDirectory.deleteOnExit();
			tempDirectory.delete();
			return tempDirectory;
		}
		catch (IOException ex) {
			String message = SVNMessages.formatErrorString("Error_CannotCheckOutMeta", new String[] {String.valueOf(project.getName())}); //$NON-NLS-1$
			throw new UnreportableException(message, ex);
		}
	}
	
	protected void copySVNMeta(File fromFolder, File toFolder) {
		if (!toFolder.isDirectory()) {
			return;
		}
		File []tempFiles = fromFolder.listFiles();
		if (tempFiles != null) {
			for (int i = 0; i < tempFiles.length; i++) {
				File renameToFile = new File(toFolder + "/" + tempFiles[i].getName()); //$NON-NLS-1$
				if (!renameToFile.exists()) {
					tempFiles[i].renameTo(renameToFile);
				}
				else if (tempFiles[i].isDirectory()) {
					this.copySVNMeta(tempFiles[i], renameToFile);
				}
			}
		}
	}
	
	protected static String getTargetUrlImpl(IRepositoryLocation location, int shareLayout, String projectName, String rootName, boolean managementFoldersEnabled) {
		String trunkName = managementFoldersEnabled ? ("/" + ShareProjectOperation.getTrunkName(location)) : ""; //$NON-NLS-1$ //$NON-NLS-2$
		switch (shareLayout) {
			case ShareProjectOperation.LAYOUT_DEFAULT: {
				return location.getUrl() + trunkName + "/" + projectName; //$NON-NLS-1$
			}
			case ShareProjectOperation.LAYOUT_SINGLE: {
				return location.getUrl() + "/" + projectName + trunkName; //$NON-NLS-1$
			}
			case ShareProjectOperation.LAYOUT_MULTIPLE: {
				return location.getUrl() + "/" + rootName + trunkName + "/" + projectName; //$NON-NLS-1$ //$NON-NLS-2$
			}
			default: {
				String message = SVNMessages.formatErrorString("Error_UnknownProjectLayoutType", new String[] {String.valueOf(shareLayout)}); //$NON-NLS-1$
				throw new RuntimeException(message);
			}
		}
	}
	
	public static String getTrunkName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getTrunkLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TRUNK_NAME);
	}

	public static String getBranchesName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getBranchesLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_BRANCHES_NAME);
	}

	public static String getTagsName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getTagsLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TAGS_NAME);
	}

}
