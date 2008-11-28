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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.change.FolderChange;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.operation.local.change.visitors.CompositeVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.RemoveNonVersionedVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.RestoreContentVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.RestorePropertiesVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.SaveContentVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.SavePropertiesVisitor;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Mark as merged operation implementation
 * 
 * @author Alexander Gurov
 */
public class MarkAsMergedOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor, IResourceProvider {
    protected boolean override;
    protected boolean keepLocks;
    protected String overrideMessage;
    protected IResource []committables;
    protected IResource []withDifferentNodeKind;

	public MarkAsMergedOperation(IResource[] resources, boolean override, String overrideMessage) {
		this(resources, override, overrideMessage, false);
	}

	public MarkAsMergedOperation(IResource[] resources, boolean override, String overrideMessage, boolean keepLocks) {
		super("Operation_MarkAsMerged", resources); //$NON-NLS-1$
		this.override = override;
		this.overrideMessage = overrideMessage;
		this.keepLocks = keepLocks;
	}

	public MarkAsMergedOperation(IResourceProvider provider, boolean override, String overrideMessage, boolean keepLocks) {
		super("Operation_MarkAsMerged", provider); //$NON-NLS-1$
		this.override = override;
		this.overrideMessage = overrideMessage;
		this.keepLocks = keepLocks;
	}
	
	public MarkAsMergedOperation(IResourceProvider provider, boolean override, String overrideMessage) {
		this(provider, override, overrideMessage, false);
	}

	public IResource []getResources() {
	    return this.committables;
	}
	
	public IResource []getHavingDifferentNodeKind() {
	    return this.withDifferentNodeKind;
	}
	
	public void doOperation(IActionOperation op, IProgressMonitor monitor) {
	    this.reportStatus(op.run(monitor).getStatus());
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = FileUtility.shrinkChildNodes(this.operableData());
		final ArrayList<IResource> committables = new ArrayList<IResource>();
		final ArrayList<IResource> withDifferentNodeKind = new ArrayList<IResource>();
		this.committables = new IResource[0];
		this.withDifferentNodeKind = new IResource[0];

		for (int i = 0; i < resources.length; i++) {
			final IResource current = resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(current);
					if (IStateFilter.SF_DELETED.accept(local) && !IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
						MarkAsMergedOperation.this.markDeleted(local, new NullProgressMonitor());
						committables.add(local.getResource());
					}
					else if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
						boolean nodeKindChanged = MarkAsMergedOperation.this.markExisting(local, monitor);
						if (!nodeKindChanged) {
							committables.add(local.getResource());
						}
						else {
						    withDifferentNodeKind.add(local.getResource());
						}
					}
				}
			}, monitor, resources.length);
		}
		
		this.committables = committables.toArray(new IResource[committables.size()]);
		this.withDifferentNodeKind = withDifferentNodeKind.toArray(new IResource[withDifferentNodeKind.size()]);
	}

	protected void markDeleted(ILocalResource local, IProgressMonitor monitor) {
		this.doOperation(new RevertOperation(new IResource[] {local.getResource()}, true), monitor);
		this.doOperation(new UpdateOperation(new IResource[] {local.getResource()}, true), monitor);
		this.doOperation(new DeleteResourceOperation(local.getResource()), monitor);
	}
	
	protected boolean markExisting(ILocalResource local, IProgressMonitor monitor) throws Exception {
	    boolean nodeKindChanged = false;
	    ResourceChange change = ResourceChange.wrapLocalResource(null, local, true);
	    
	    try {
	    	CompositeVisitor visitor = new CompositeVisitor();
	    	visitor.add(new SavePropertiesVisitor());
	    	visitor.add(new SaveContentVisitor());
	    	change.traverse(visitor, IResource.DEPTH_INFINITE, this, monitor);

		    if (FileUtility.checkForResourcesPresenceRecursive(new IResource[] {local.getResource()}, IStateFilter.SF_REVERTABLE)) {
				this.doOperation(new RevertOperation(new IResource[] {local.getResource()}, true), monitor);
			}
		    
		    change.traverse(new RemoveNonVersionedVisitor(true), IResource.DEPTH_INFINITE, this, monitor);
		    
			this.doOperation(new UpdateOperation(new IResource[] {local.getResource()}, true), monitor);
			String wcPath = FileUtility.getWorkingCopyPath(local.getResource());
			boolean isLocalExists = new File(wcPath).exists();
			if (this.override && isLocalExists) {
			    nodeKindChanged = this.prepareToOverride(change, monitor);
			    //do additional check for exists because prepareToOverride can delete resources
			    if (new File(wcPath).exists()) {
			    	this.doOperation(new CommitOperation(new IResource[] {local.getResource()}, this.overrideMessage, true, this.keepLocks), monitor);
			    	// update child records for node kind replacement
			    	this.doOperation(new UpdateOperation(new IResource[] {local.getResource()}, true), monitor);
			    }
			}
			
			visitor = new CompositeVisitor();
	    	visitor.add(new RestoreContentVisitor(nodeKindChanged));
	    	visitor.add(new RestorePropertiesVisitor());
	    	change.traverse(visitor, IResource.DEPTH_INFINITE, this, monitor);
	    }
	    finally {
		    change.disposeChangeModel(monitor);
	    }
	    
	    return nodeKindChanged;
	}

	protected boolean prepareToOverride(ResourceChange change, IProgressMonitor monitor) {
	    boolean nodeKindBeforeUpdate = change instanceof FolderChange;
	    if (nodeKindBeforeUpdate) {
	        ResourceChange []children = ((FolderChange)change).getChildren();
	        for (int i = 0; i < children.length; i++) {
	            this.prepareToOverride(children[i], monitor);
	        }
	    }
	    ILocalResource local = change.getLocal();
	    File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
	    boolean nodeKindChanged = false;
	    if (real.exists()) {
		    nodeKindChanged = nodeKindBeforeUpdate != real.isDirectory();
		    if (IStateFilter.SF_NOTONREPOSITORY.accept(local) || nodeKindChanged) {
		        this.doOperation(new DeleteResourceOperation(local.getResource()), monitor);
		    }
	    }
	    return nodeKindChanged;
	}
	
}
