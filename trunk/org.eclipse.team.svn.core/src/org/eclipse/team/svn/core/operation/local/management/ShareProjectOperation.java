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

package org.eclipse.team.svn.core.operation.local.management;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
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
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProjectMapper;
import org.eclipse.team.svn.core.client.ClientWrapperCancelException;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
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
		super("Operation.ShareProject", projects);
		this.mapper = mapper;
		this.location = location;
		this.shareLayout = shareLayout;
		this.rootName = rootName;
		this.managementFoldersEnabled = managementFoldersEnabled;
		this.commitComment = commitComment;
	}
	
	public void setSharePrompt(IShareProjectPrompt prompt) {
		this.shareProjectPrompt = prompt;
	}
	
	public static String getTargetUrl(IRepositoryLocation location, int shareLayout, String projectName, String rootName, boolean managementFoldersEnabled) {
		return SVNUtility.normalizeURL(ShareProjectOperation.getTargetUrlImpl(location, shareLayout, projectName, rootName, managementFoldersEnabled));
	}
	
	public static String getDefaultComment(IProject project, IRepositoryResource remote) {
		String message = SVNTeamPlugin.instance().getResource("Operation.ShareProject.DefaultComment");
		return MessageFormat.format(message, new String[] {project.getName(), remote.getUrl()});
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
			this.commitComment = "";
			for (Iterator it = local2remote.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				String commentPart = ShareProjectOperation.getDefaultComment((IProject)entry.getKey(), (IRepositoryContainer)entry.getValue());
				this.commitComment += this.commitComment.length() == 0 ? commentPart : ("\n" + commentPart);
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
				String message = this.getNationalizedString("Error.UnknownProjectLayoutType");
				throw new Exception(MessageFormat.format(message, new String[] {String.valueOf(this.shareLayout)}));
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
					catch (ClientWrapperException ex) {
						// do nothing
					}
				}
			}, monitor, local2remote.size() * 2);
		}			
		
		if (existingProjects.size() > 0 && this.shareProjectPrompt != null && !this.shareProjectPrompt.prompt((IProject [])existingProjects.toArray(new IProject[existingProjects.size()]))) {
			throw new ClientWrapperCancelException(this.getNationalizedString("Error.ShareCancelled"));
		}
		
		final ISVNClientWrapper proxy = this.location.acquireSVNProxy();
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
						String checkoutTo = tempDir != null ? tempDir.toString() : project.getLocation().toString();

						proxy.checkout(SVNUtility.encodeURL(remote.getUrl()), checkoutTo, Revision.HEAD, Revision.HEAD, true, false, new SVNProgressMonitor(ShareProjectOperation.this, monitor, null));
						
						if (tempDir != null) {
							ShareProjectOperation.this.copySVNMeta(tempDir, project.getLocation().toFile());
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
				
				String targetUrl = ShareProjectOperation.getTargetUrl(this.location, this.shareLayout, "", this.rootName, false);
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
		FileUtility.sort(resources, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				IRepositoryResource first = (IRepositoryResource)arg0;
				IRepositoryResource second = (IRepositoryResource)arg1;
				return first.getUrl().compareTo(second.getUrl());
			}
		});
		return resources;
	}
	
	protected void mkdir(ISVNClientWrapper proxy, IRepositoryResource []resourceSet, IProgressMonitor monitor) throws Exception {
		ArrayList urlsList = new ArrayList();
		for (int i = 0; i < resourceSet.length && !monitor.isCanceled(); i++) {
			ProgressMonitorUtility.setTaskInfo(monitor, this, resourceSet[i].getUrl());
			if (!resourceSet[i].exists()) {
				urlsList.add(SVNUtility.encodeURL(resourceSet[i].getUrl()));
			}
			ProgressMonitorUtility.progress(monitor, IProgressMonitor.UNKNOWN, resourceSet.length);
		}
		String []urls = (String [])urlsList.toArray(new String[urlsList.size()]);
		proxy.mkdir(urls, this.commitComment, new SVNProgressMonitor(this, monitor, null));
	}
	
	protected IRepositoryContainer makeChild(IRepositoryContainer parent, String name) {
		return this.location.asRepositoryContainer(parent.getUrl() + "/" + name, false);
	}
	
	protected File createTempDirectory(IProject project) {
		try {
			File tempDirectory = File.createTempFile("save_" + project.getName(), ".tmp", project.getLocation().toFile().getParentFile());
			tempDirectory.deleteOnExit();
			tempDirectory.delete();
			return tempDirectory;
		}
		catch (IOException ex) {
			String message = SVNTeamPlugin.instance().getResource("Error.CannotCheckOutMeta");
			throw new UnreportableException(MessageFormat.format(message, new String[] {String.valueOf(project.getName())}), ex);
		}
	}
	
	protected void copySVNMeta(File fromFolder, File toFolder) {
		if (!toFolder.isDirectory()) {
			return;
		}
		File []tempFiles = fromFolder.listFiles();
		if (tempFiles != null) {
			for (int i = 0; i < tempFiles.length; i++) {
				File renameToFile = new File(toFolder + "/" + tempFiles[i].getName());
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
		String trunkName = managementFoldersEnabled ? ("/" + ShareProjectOperation.getTrunkName(location)) : "";
		switch (shareLayout) {
			case ShareProjectOperation.LAYOUT_DEFAULT: {
				return location.getUrl() + trunkName + "/" + projectName;
			}
			case ShareProjectOperation.LAYOUT_SINGLE: {
				return location.getUrl() + "/" + projectName + trunkName;
			}
			case ShareProjectOperation.LAYOUT_MULTIPLE: {
				return location.getUrl() + "/" + rootName + trunkName + "/" + projectName;
			}
			default: {
				String message = SVNTeamPlugin.instance().getResource("Error.UnknownProjectLayoutType");
				throw new RuntimeException(MessageFormat.format(message, new String[] {String.valueOf(shareLayout)}));
			}
		}
	}
	
	public static String getTrunkName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getTrunkLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getDefaultTrunkName();
	}

	public static String getBranchesName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getBranchesLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getDefaultBranchesName();
	}

	public static String getTagsName(IRepositoryLocation location) {
		if (location.isStructureEnabled()) {
			return location.getTagsLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getDefaultTagsName();
	}

}
