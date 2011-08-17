/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
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
 * @author Sergiy Logvin
 */
public class PreparedBranchTagOperation extends CompositeOperation implements IRepositoryResourceProvider {
	protected IRepositoryResource destination;
	protected IRepositoryResource []resources;
	protected String message;
	protected String operationName;
	protected boolean forceCreate;
	protected IRepositoryResource []targets;
	
	protected IResource []wcResources;

	public PreparedBranchTagOperation(String operationName, IResource []wcResources, IRepositoryResource []resources, IRepositoryResource destination, String message, boolean forceCreate) {
		this(operationName, resources, destination, message, forceCreate);
		this.wcResources = wcResources;
	}
	
	public PreparedBranchTagOperation(String operationName, IRepositoryResource []resources, IRepositoryResource destination, String message, boolean forceCreate) {
		super("Operation_Prepared" + operationName, SVNMessages.class); //$NON-NLS-1$
		this.operationName = operationName;
		this.resources = resources;
		this.destination = destination;
		this.message = message;
		this.forceCreate = forceCreate;
	}
	
	public IRepositoryResource[] getRepositoryResources() {
		return this.targets;
	}
	
	public IRepositoryResource getDestination() {
		return this.destination;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() < ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x || this.wcResources != null) {
			IRepositoryResource parent = PreparedBranchTagOperation.getExistentParent(this.destination);
			if (parent == null) {
				throw new UnreportableException(SVNMessages.getErrorString("Error_RepositoryInaccessible")); //$NON-NLS-1$
			}
			this.targets = new IRepositoryResource[this.resources.length];
			CreateFolderOperation op = null;
			boolean createLastSegment = false;
			if (parent != this.destination) {
				IPath newFolderPath = new Path(this.destination.getUrl().substring(parent.getUrl().length() + 1));
				IPath cutPath = newFolderPath.removeLastSegments(1);
				createLastSegment = this.resources.length == 1 && !newFolderPath.isEmpty() && !this.forceCreate;
				if (this.resources.length != 1 || !cutPath.isEmpty() || this.forceCreate) {
					String folderName = this.resources.length == 1 && !cutPath.isEmpty() && !this.forceCreate ? cutPath.toString() : newFolderPath.toString();
					this.add(op = new CreateFolderOperation(parent, folderName, this.message));
					this.add(new SetRevisionAuthorNameOperation(op, Options.FORCE), new IActionOperation[] {op});
				}
			}
			for (int i = 0; i < this.targets.length; i++) {
				String targetUrl = this.destination.getUrl();
				if (!createLastSegment) {
					targetUrl += "/" + this.resources[i].getName(); //$NON-NLS-1$
				}
				this.targets[i] = this.resources[i] instanceof IRepositoryFile ? (IRepositoryResource)this.destination.asRepositoryFile(targetUrl, false) : this.destination.asRepositoryContainer(targetUrl, false);
				if (this.wcResources != null) {
					this.add(new org.eclipse.team.svn.core.operation.local.BranchTagOperation(this.operationName, new IResource[] {this.wcResources[i]}, this.targets[i], this.message), op == null ? null : new IActionOperation[] {op});
				}
			}
			
			if (this.wcResources == null) {
				BranchTagOperation branchtagOp = new BranchTagOperation(this.operationName, SVNMessages.class, this.resources, this.destination, this.message);
				this.add(branchtagOp, op == null ? null : new IActionOperation[] {op});
				this.add(new SetRevisionAuthorNameOperation(branchtagOp, Options.FORCE));
			}
		}
		else {
			// SVN 1.5 allows to create branch/tag for repository resources at once
			CopyResourcesOperation branchtagOp = this.forceCreate ? 
					new CopyResourcesOperation(this.destination, this.resources, this.message, this.resources[0].getName()) :
					new CopyResourcesOperation(this.destination.getParent(), this.resources, this.message, this.destination.getName());
			this.add(branchtagOp);
			this.add(new SetRevisionAuthorNameOperation(branchtagOp, Options.FORCE));
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
