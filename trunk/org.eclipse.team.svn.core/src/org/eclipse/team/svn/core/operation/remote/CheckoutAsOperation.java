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

package org.eclipse.team.svn.core.operation.remote;

import java.io.File;
import java.text.MessageFormat;
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
import org.eclipse.team.svn.core.SVNTeamProjectMapper;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
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
	protected List overlappingProjects;
	protected boolean recursive;
	protected boolean ignoreExternals;
	protected RestoreProjectMetaOperation restoreOp;

	public CheckoutAsOperation(String projectName, IRepositoryResource resource, boolean recursive) {
		this(projectName, resource, Platform.getLocation().toString(), recursive, false);
	}
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, boolean recursive, boolean ignoreExternals) {
		this(projectName, resource, Platform.getLocation().toString(), recursive, ignoreExternals);
	}
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, boolean respectHierarchy, String location, boolean recursive, boolean ignoreExternals) {
		this(projectName, resource, location == null ? Platform.getLocation().toString() : location + (respectHierarchy ? SVNUtility.getResourceParent(resource) : ""), recursive, ignoreExternals);
	}
	
	public CheckoutAsOperation(String projectName, IRepositoryResource resource, String projectLocation, boolean recursive, boolean ignoreExternals) {
		super("Operation.CheckOutAs");
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
		this.recursive = recursive;
		this.ignoreExternals = ignoreExternals;
		this.overlappingProjects = new ArrayList();
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (new Path(this.projectLocation).append(this.project.getName()).isPrefixOf(projects[i].getLocation())) {
				this.overlappingProjects.add(projects[i]);
			}
		}
		this.overlappingProjects.add(this.project);
	}
	
	public ISchedulingRule getSchedulingRule() {
		IProject []projects = (IProject[])this.overlappingProjects.toArray(new IProject[this.overlappingProjects.size()]);
		ISchedulingRule []rules = new ISchedulingRule[overlappingProjects.size()];
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
		final IPath destination = new Path(this.projectLocation).append(this.project.getName());

		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				CheckoutAsOperation.this.doCheckout(monitor, destination);
			}
		}, monitor, 2);
		
		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				CheckoutAsOperation.this.doOpen(monitor, destination);
			}
		}, monitor, 2);
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
		ProgressMonitorUtility.setTaskInfo(monitor, this, this.getOperationResource("PrepareFS"));
		
		for (Iterator it = this.overlappingProjects.iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			IProject overlappingProject = (IProject)it.next();
			this.deleteProject(overlappingProject, monitor);
		}	
		this.deleteFolderContent(destination.toString(), monitor);
		
		// check out files from the repository
		IRepositoryLocation location = (IRepositoryLocation)this.resource.getRepositoryLocation();
		// using parent because deleted project does not have any location (null value returned)
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String path = destination.toString();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn checkout \"" + this.resource.getUrl() + "@" + this.resource.getPegRevision() + "\" -r " + this.resource.getSelectedRevision() + (this.recursive ? "" : " -N") + " \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.checkout(
					SVNUtility.getEntryRevisionReference(this.resource), 
					path, 
					Depth.infinityOrFiles(this.recursive), 
					this.ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE, 
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
		if (!FileUtility.deleteRecursive(target, monitor)) {
			File []children = target.listFiles();
			if (children != null && children.length > 0) {
				String message = this.getNationalizedString("Error.LockedExternally");
				throw new UnreportableException(MessageFormat.format(message, new String[] {children[0].getAbsolutePath()}));
			}
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.resource.getUrl()});
	}
	
}
