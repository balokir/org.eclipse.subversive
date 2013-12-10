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

package org.eclipse.team.svn.core.operation.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamProjectMapper;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Checkout content into the project which has the specified name
 * 
 * @author Alexander Gurov
 */
public class CheckoutAsOperation extends AbstractActionOperation {
	protected IProject project;
	protected IRepositoryResource resource;
	protected String projectLocation;
	protected List<IProject> overlappingProjects;
	protected int depth;
	protected long options;
	protected RestoreProjectMetaOperation restoreOp;
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, int depth, boolean ignoreExternals) {
		this(projectName, resource, Platform.getLocation().toString(), depth, ignoreExternals);
	}
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, boolean respectHierarchy, String location, int depth, boolean ignoreExternals) {
		this(projectName, resource, location == null ? Platform.getLocation().toString() : location + (respectHierarchy ? SVNUtility.getResourceParent(resource) : ""), depth, ignoreExternals); //$NON-NLS-1$
	}
	
	public int getOperationWeight() {
		return 19;
	}

	public CheckoutAsOperation(String projectName, IRepositoryResource resource, String projectLocation, int depth, boolean ignoreExternals) {
		this(projectName, resource, projectLocation, depth, ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE);
	}
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, String projectLocation, int depth, long options) {
		super("Operation_CheckOutAs", SVNMessages.class); //$NON-NLS-1$
		projectName = FileUtility.formatResourceName(projectName);
		if (FileUtility.isCaseInsensitiveOS()) {
			IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			IProject projectToCheckOut = null;
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].getName().equalsIgnoreCase(projectName)) {
					projectToCheckOut = projects[i];
				}
			}
			this.project = projectToCheckOut != null ? projectToCheckOut : ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);			
		}
		else {
			this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);	
		}
		this.resource = resource;
		this.projectLocation = projectLocation;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.CHECKOUT;
		this.overlappingProjects = new ArrayList<IProject>();
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {			
			if (!FileUtility.isRemoteProject(projects[i]) && new Path(this.projectLocation).append(this.project.getName()).isPrefixOf(projects[i].getLocation())) {
				this.overlappingProjects.add(projects[i]);
			}
		}
		this.overlappingProjects.add(this.project);
	}
	
	public ISchedulingRule getSchedulingRule() {
		IProject []projects = this.overlappingProjects.toArray(new IProject[this.overlappingProjects.size()]);
		ISchedulingRule []rules = new ISchedulingRule[this.overlappingProjects.size()];
		for (int i = 0; i < projects.length; i++) {
			rules[i] = SVNResourceRuleFactory.INSTANCE.modifyRule(projects[i]);
		}
		return new MultiRule(rules);
	}
	
	public IProject getProject() {
		return this.project;
	}
	
	public void setRestoreOperation(RestoreProjectMetaOperation restoreOp) {
		this.restoreOp = restoreOp;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		String projectName = this.project.isAccessible() && !FileUtility.isRemoteProject(this.project) ?
				this.project.getLocation().toString().substring(this.project.getLocation().toString().lastIndexOf("/") + 1) //$NON-NLS-1$
				: this.project.getName();
		final IPath destination = new Path(this.projectLocation).append(projectName);

		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				CheckoutAsOperation.this.doCheckout(monitor, destination);
			}
		}, monitor, 20, 19);
		
		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				CheckoutAsOperation.this.doOpen(monitor, destination);
			}
		}, monitor, 20, 1);
	}
	
	protected void doOpen(IProgressMonitor monitor, IPath destination) throws Exception {
		if (this.restoreOp != null) {
			this.reportStatus(this.restoreOp.run(monitor).getStatus());
		}
		
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(this.project.getName());
		if (!Platform.getLocation().equals(new Path(this.projectLocation))) {
			description.setLocation(destination);
		}
		this.project.create(description, monitor);
		this.project.open(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
		SVNTeamProjectMapper.map(this.project, this.resource);
	}
	
	protected void doCheckout(IProgressMonitor monitor, IPath destination) throws Exception {
		// prepare workspace...
		ProgressMonitorUtility.setTaskInfo(monitor, this, this.getOperationResource("PrepareFS")); //$NON-NLS-1$
		
		for (Iterator<IProject> it = this.overlappingProjects.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			IProject overlappingProject = it.next();
			this.deleteProject(overlappingProject, monitor);
		}	
		this.deleteFolderContent(destination.toString(), monitor);
		
		// check out files from the repository
		IRepositoryLocation location = this.resource.getRepositoryLocation();
		// using parent because deleted project does not have any location (null value returned)
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String path = destination.toString();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn checkout \"" + this.resource.getUrl() + "@" + this.resource.getPegRevision() + "\" -r " + this.resource.getSelectedRevision() + SVNUtility.getIgnoreExternalsArg(this.options) + SVNUtility.getDepthArg(this.depth, ISVNConnector.Options.NONE) + " \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			proxy.checkout(
					SVNUtility.getEntryRevisionReference(this.resource), 
					path, 
					this.depth, 
					this.options, 
					new SVNProgressMonitor(this, monitor, this.project.getFullPath()));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected void deleteProject(IProject project, IProgressMonitor monitor) throws Exception {
		IPath location = project.getLocation();
		project.delete(false, true, null);
		if (location != null) {
			this.deleteFolderContent(location.toString(), monitor);
		}
	}
	
	protected void deleteFolderContent(String targetPath, IProgressMonitor monitor) throws Exception {
		File target = new File(targetPath);
		File []children = target.listFiles();
		if (children != null && children.length > 0) {
			for (File child : children) {
				FileUtility.deleteRecursive(child, monitor);
			}
			children = target.listFiles();
			if (children != null && children.length > 0) {
				String message = this.getNationalizedString("Error_LockedExternally"); //$NON-NLS-1$
				throw new UnreportableException(BaseMessages.format(message, new Object[] {children[0].getAbsolutePath()}));
			}
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.resource.getUrl()});
	}
	
}
