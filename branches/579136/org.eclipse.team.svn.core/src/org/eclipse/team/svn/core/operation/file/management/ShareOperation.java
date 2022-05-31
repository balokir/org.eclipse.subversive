/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file.management;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Share selected folders with the specified repository
 * 
 * @author Alexander Gurov
 */
public class ShareOperation extends AbstractFileOperation {
	public static final int LAYOUT_DEFAULT = 0;
	public static final int LAYOUT_SINGLE = 1;
	public static final int LAYOUT_MULTIPLE = 2;
	
	public interface IFolderNameMapper {
		public String getRepositoryFolderName(File folder);
	}
	
	protected IRepositoryLocation location;
	protected IFolderNameMapper mapper;
	protected String rootName;
	protected int shareLayout;
	protected boolean managementFoldersEnabled;
	protected String commitComment;
	protected boolean ignoreExternals;
	
	public ShareOperation(File []files, IRepositoryLocation location, IFolderNameMapper mapper, String rootName, int shareLayout, boolean managementFoldersEnabled, String commitComment, boolean ignoreExternals) {
		super("Operation_ShareFile", SVNMessages.class, files); //$NON-NLS-1$
		this.location = location;
		this.mapper = mapper;
		this.rootName = rootName;
		this.shareLayout = shareLayout;
		this.managementFoldersEnabled = managementFoldersEnabled;
		this.commitComment = commitComment;
		this.ignoreExternals = ignoreExternals;
	}

	public ShareOperation(IFileProvider provider, IRepositoryLocation location, IFolderNameMapper mapper, String rootName, int shareLayout, boolean managementFoldersEnabled, String commitComment, boolean ignoreExternals) {
		super("Operation_ShareFile", SVNMessages.class, provider); //$NON-NLS-1$
		this.location = location;
		this.mapper = mapper;
		this.rootName = rootName;
		this.shareLayout = shareLayout;
		this.managementFoldersEnabled = managementFoldersEnabled;
		this.commitComment = commitComment;
		this.ignoreExternals = ignoreExternals;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []folders = FileUtility.shrinkChildNodes(this.operableData(), true);
		HashMap<File, IRepositoryContainer> local2remote = new HashMap<File, IRepositoryContainer>();
		for (int i = 0; i < folders.length; i++) {
			String url = this.getTargetUrl(this.mapper == null ? folders[i].getName() : this.mapper.getRepositoryFolderName(folders[i]), this.managementFoldersEnabled);
			IRepositoryContainer remote = this.location.asRepositoryContainer(url, false);
			local2remote.put(folders[i], remote);
		}
		if (this.commitComment == null) {
			this.commitComment = ""; //$NON-NLS-1$
			for (Iterator<?> it = local2remote.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				String commentPart = ShareOperation.getDefaultComment((File)entry.getKey(), (IRepositoryContainer)entry.getValue());
				this.commitComment += this.commitComment.length() == 0 ? commentPart : ("\n" + commentPart); //$NON-NLS-1$
			}
		}
		
		final ISVNConnector proxy = this.location.acquireSVNProxy();
		try {
			IRepositoryResource []resourceSet = null;
			switch (this.shareLayout) {
				case ShareProjectOperation.LAYOUT_DEFAULT: {
					resourceSet = this.doDefaultLayout(local2remote);
					break;
				}
				case ShareProjectOperation.LAYOUT_SINGLE: {
					resourceSet = this.doSingleLayout(local2remote);
					break;
				}
				case ShareProjectOperation.LAYOUT_MULTIPLE: {
					resourceSet = this.doMultipleLayout(local2remote);
					break;
				}
				default: {
					String message = this.getNationalizedString("Error_UnknownProjectLayoutType"); //$NON-NLS-1$
					throw new Exception(BaseMessages.format(message, new Object[] {String.valueOf(this.shareLayout)}));
				}
			}
			
			this.mkdir(proxy, resourceSet, monitor);
			
			for (Iterator<?> it = local2remote.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				final Map.Entry entry = (Map.Entry)it.next();
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						IRepositoryContainer remote = (IRepositoryContainer)entry.getValue();
						File local = (File)entry.getKey();
						long options = ShareOperation.this.ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE; 
						proxy.checkout(SVNUtility.getEntryRevisionReference(remote), local.getAbsolutePath(), SVNDepth.EMPTY, options, new SVNProgressMonitor(ShareOperation.this, monitor, null));
					}
				}, monitor, local2remote.size());
			}
		}
		finally {
			this.location.releaseSVNProxy(proxy);
		}
	}

	public static String getDefaultComment(File folder, IRepositoryResource remote) {
		return SVNMessages.format(SVNMessages.Operation_ShareFile_DefaultComment, new String[] {folder.getName(), remote.getUrl()});
	}
	
	public String getTargetUrl(String projectName, boolean managementFoldersEnabled) {
		return SVNUtility.normalizeURL(this.getTargetUrlImpl(projectName, managementFoldersEnabled));
	}
	
	protected String getTargetUrlImpl(String projectName, boolean managementFoldersEnabled) {
		String trunkName = managementFoldersEnabled ? ("/" + this.getTrunkName()) : ""; //$NON-NLS-1$ //$NON-NLS-2$
		switch (this.shareLayout) {
			case ShareProjectOperation.LAYOUT_DEFAULT: {
				return this.location.getUrl() + trunkName + "/" + projectName; //$NON-NLS-1$
			}
			case ShareProjectOperation.LAYOUT_SINGLE: {
				return this.location.getUrl() + "/" + projectName + trunkName; //$NON-NLS-1$
			}
			case ShareProjectOperation.LAYOUT_MULTIPLE: {
				return this.location.getUrl() + "/" + this.rootName + trunkName + "/" + projectName; //$NON-NLS-1$ //$NON-NLS-2$
			}
			default: {
				String message = this.getNationalizedString("Error_UnknownProjectLayoutType"); //$NON-NLS-1$
				throw new RuntimeException(BaseMessages.format(message, new Object[] {String.valueOf(this.shareLayout)}));
			}
		}
	}
	
	protected void mkdir(ISVNConnector proxy, IRepositoryResource []resourceSet, IProgressMonitor monitor) throws Exception {
		ArrayList<String> urlsList = new ArrayList<String>();
		for (int i = 0; i < resourceSet.length && !monitor.isCanceled(); i++) {
			ProgressMonitorUtility.setTaskInfo(monitor, this, resourceSet[i].getUrl());
			if (!resourceSet[i].exists()) {
				urlsList.add(SVNUtility.encodeURL(resourceSet[i].getUrl()));
			}
			ProgressMonitorUtility.progress(monitor, IProgressMonitor.UNKNOWN, resourceSet.length);
		}
		String []urls = urlsList.toArray(new String[urlsList.size()]);
		proxy.mkdir(urls, this.commitComment, ISVNConnector.Options.INCLUDE_PARENTS, null, new SVNProgressMonitor(this, monitor, null));
	}
	
	protected IRepositoryResource []doDefaultLayout(Map<File, IRepositoryContainer> local2remote) {
		HashSet<IRepositoryResource> fullSet = new HashSet<IRepositoryResource>();
		for (Iterator<IRepositoryContainer> it = local2remote.values().iterator(); it.hasNext(); ) {
			IRepositoryContainer remote = it.next();
			IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
			fullSet.addAll(Arrays.asList(resources));
		}
		return this.getOrderedSet(fullSet);
	}
	
	protected IRepositoryResource []doSingleLayout(Map<File, IRepositoryContainer> local2remote) {
		if (this.managementFoldersEnabled) {
			HashSet<IRepositoryResource> fullSet = new HashSet<IRepositoryResource>();
			for (Iterator<IRepositoryContainer> it = local2remote.values().iterator(); it.hasNext(); ) {
				IRepositoryContainer remote = it.next();
				IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
				fullSet.addAll(Arrays.asList(resources));
				IRepositoryContainer parent = (IRepositoryContainer)remote.getParent();
				fullSet.add(this.makeChild(parent, this.getTagsName()));
				fullSet.add(this.makeChild(parent, this.getBranchesName()));
			}
			return this.getOrderedSet(fullSet);
		}
		return this.doDefaultLayout(local2remote);
	}
	
	protected IRepositoryResource []doMultipleLayout(Map<File, IRepositoryContainer> local2remote) {
		if (this.managementFoldersEnabled) {
			HashSet<IRepositoryResource> fullSet = new HashSet<IRepositoryResource>();
			for (Iterator<IRepositoryContainer> it = local2remote.values().iterator(); it.hasNext(); ) {
				IRepositoryContainer remote = it.next();
				IRepositoryResource []resources = SVNUtility.makeResourceSet(remote.getRepositoryLocation().getRoot(), remote);
				fullSet.addAll(Arrays.asList(resources));
				
				String targetUrl = this.getTargetUrl("", false); //$NON-NLS-1$
				IRepositoryContainer parent = this.location.asRepositoryContainer(targetUrl, false);
				fullSet.add(this.makeChild(parent, this.getTagsName()));
				fullSet.add(this.makeChild(parent, this.getBranchesName()));
			}
			return this.getOrderedSet(fullSet);
		}
		return this.doDefaultLayout(local2remote);
	}
	
	protected IRepositoryContainer makeChild(IRepositoryContainer parent, String name) {
		return this.location.asRepositoryContainer(parent.getUrl() + "/" + name, false); //$NON-NLS-1$
	}
	
	protected IRepositoryResource []getOrderedSet(Set<IRepositoryResource> fullSet) {
		IRepositoryResource [] resources = fullSet.toArray(new IRepositoryResource[fullSet.size()]);
		Arrays.sort(resources, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				IRepositoryResource first = (IRepositoryResource)arg0;
				IRepositoryResource second = (IRepositoryResource)arg1;
				return first.getUrl().compareTo(second.getUrl());
			}
		});
		return resources;
	}
	
	protected String getTrunkName() {
		if (this.location.isStructureEnabled()) {
			return this.location.getTrunkLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TRUNK_NAME);
	}

	protected String getBranchesName() {
		if (this.location.isStructureEnabled()) {
			return this.location.getBranchesLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_BRANCHES_NAME);
	}

	protected String getTagsName() {
		if (this.location.isStructureEnabled()) {
			return this.location.getTagsLocation();
		}
		return SVNTeamPlugin.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TAGS_NAME);
	}

}
