/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;

/**
 * Composite operation which prepares destination folder (if it doesn't exist 
 * on the repository) and then makes branch or tag for the selected resources
 *
 * @author Elena Matokhina
 */
public class PreparedBranchTagOperation extends CompositeOperation implements IRepositoryResourceProvider {
	protected IRepositoryResource destination;
	protected IRepositoryResource []resources;
	protected String message;
	protected String operationName;
	protected boolean createLastSegment;
	protected boolean forceCreate;
	protected IRepositoryResource []targets;
	
	protected IResource []wcResources;

	public PreparedBranchTagOperation(String operationName, IRepositoryResource []resources, IRepositoryResource destination, String message, boolean multipleProjectLayout) {
		this(operationName, resources, destination, message);
		this.forceCreate = multipleProjectLayout;
	}
	
	public PreparedBranchTagOperation(String operationName, IRepositoryResource []resources, IRepositoryResource destination, String message) {
		super("Operation.Prepared" + operationName);
		this.operationName = operationName;
		this.resources = resources;
		this.destination = destination;
		this.message = message;
	}
	
	public PreparedBranchTagOperation(String operationName, IResource []wcResources, IRepositoryResource []resources, IRepositoryResource destination, String message, boolean multipleProjectLayout) {
		this(operationName, resources, destination, message, multipleProjectLayout);
		this.wcResources = wcResources;
	}
	
	public IRepositoryResource[] getRepositoryResources() {
		return this.targets;
	}
	
	public boolean isCreateLastSegmrent() {
		return this.createLastSegment;
	}
	
	public IRepositoryResource getDestination() {
		return this.destination;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource parent = PreparedBranchTagOperation.getExistentParent(this.destination);
		if (parent == null) {
			throw new UnreportableException(SVNTeamPlugin.instance().getResource("Error.RepositoryInaccessible"));
		}
		this.targets = new IRepositoryResource[this.resources.length];
		CreateFolderOperation op = null;
		if (parent != this.destination) {
			IPath newFolderPath = new Path(this.destination.getUrl().substring(parent.getUrl().length() + 1));
			IPath cutPath = newFolderPath.removeLastSegments(1);
			this.createLastSegment = this.resources.length == 1 && !newFolderPath.isEmpty() && !this.forceCreate;
			if (this.resources.length != 1 || !cutPath.isEmpty() || this.forceCreate) {
				String folderName = this.resources.length == 1 && !cutPath.isEmpty() && !this.forceCreate ? cutPath.toString() : newFolderPath.toString();
				this.add(op = new CreateFolderOperation(parent, folderName, this.message));
			}
		}
		for (int i = 0; i < this.targets.length; i++) {
			String targetUrl = this.destination.getUrl();
			if (!this.createLastSegment) {
				targetUrl += "/" + this.resources[i].getName();
			}
			this.targets[i] = this.resources[i] instanceof IRepositoryFile ? (IRepositoryResource)this.destination.asRepositoryFile(targetUrl, false) : this.destination.asRepositoryContainer(targetUrl, false);
		}
		
		if (this.wcResources == null) {
			this.add(new BranchTagOperation(this.operationName, this.resources, this.destination, this.message), op == null ? null : new IActionOperation[] {op});
		}
		else {
			this.add(new org.eclipse.team.svn.core.operation.local.BranchTagOperation(this.operationName, this.wcResources, this.destination, this.message), op == null ? null : new IActionOperation[] {op});
		}
		super.runImpl(monitor);
	}
	
	protected static IRepositoryResource getExistentParent(IRepositoryResource notExistentResource) throws Exception {
		if (notExistentResource == null) {
			return null;
		}
		return notExistentResource.exists() ? notExistentResource : PreparedBranchTagOperation.getExistentParent(notExistentResource.getParent());
	}

}
