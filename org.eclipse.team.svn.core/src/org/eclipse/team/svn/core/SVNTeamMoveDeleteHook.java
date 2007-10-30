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

package org.eclipse.team.svn.core;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNWithPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.refactor.CopyResourceOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.operation.local.refactor.MoveResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Hook for handle resources move(refactor) and deletion
 * 
 * @author Alexander Gurov
 */
public class SVNTeamMoveDeleteHook implements IMoveDeleteHook {

	public SVNTeamMoveDeleteHook() {

	}

	public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor) {
		return this.doDelete(tree, file, monitor);
	}

	public boolean deleteFolder(IResourceTree tree, final IFolder folder, int updateFlags, IProgressMonitor monitor) {
		return this.doDelete(tree, folder, monitor);
	}
	
	public boolean moveFile(final IResourceTree tree, final IFile source, final IFile destination, int updateFlags, IProgressMonitor monitor) {
		return this.doMove(tree, source, destination, monitor);
	}

	public boolean moveFolder(final IResourceTree tree, final IFolder source, final IFolder destination, int updateFlags, IProgressMonitor monitor) {
		return this.doMove(tree, source, destination, monitor);
	}

	public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {
		//NOTE Eclipse bug ? Project should be disconnected first but it is not possible due to different resource locking rules used by project deletion and project unmpaping code.
		return false;
	}

	public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
		return false;
	}

	protected boolean doMove(IResourceTree tree, IResource source, IResource destination, IProgressMonitor monitor) {
	    IRemoteStorage storage = SVNRemoteStorage.instance();
		ILocalResource local = storage.asLocalResource(source);
		if (local == null || 
		    IStateFilter.SF_NOTEXISTS.accept(source, local.getStatus(), local.getChangeMask()) ||
		    IStateFilter.SF_NONVERSIONED.accept(source, local.getStatus(), local.getChangeMask()) ||
		    IStateFilter.SF_OBSTRUCTED.accept(source, local.getStatus(), local.getChangeMask())) {
			return FileUtility.isSVNInternals(source);
		}
		local = storage.asLocalResource(destination.getParent());
		if (local == null || 
		    IStateFilter.SF_LINKED.accept(destination, local.getStatus(), local.getChangeMask()) ||
		    IStateFilter.SF_OBSTRUCTED.accept(destination, local.getStatus(), local.getChangeMask())) {
		    return false;
		}
		
		MoveResourceOperation moveOp = new MoveResourceOperation(source, destination);
		CompositeOperation op = new CompositeOperation(moveOp.getId());
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(new IResource[] {source, destination});
		op.add(saveOp);
		if (!moveOp.isAllowed()) {
			//target was placed on different repository -- do <copy + delete>
			AbstractActionOperation copyLocalResourceOp = new CopyResourceOperation(source, destination);
			op.add(copyLocalResourceOp);
			op.add(new DeleteResourceOperation(source), new IActionOperation[] {copyLocalResourceOp});
			op.add(new RestoreProjectMetaOperation(saveOp));
		    op.add(new RefreshResourcesOperation(new IResource[] {source, destination}, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
		}
    	else if (IStateFilter.SF_NONVERSIONED.accept(destination, local.getStatus(), local.getChangeMask())) {
	        IResource []scheduledForAddition = FileUtility.getOperableParents(new IResource[] {destination}, IStateFilter.SF_NONVERSIONED, true);
	        AbstractActionOperation addToSVNOp = new AddToSVNWithPropertiesOperation(scheduledForAddition, false); 
	        op.add(addToSVNOp);
	       	op.add(moveOp, new IActionOperation[] {addToSVNOp});
			op.add(new RestoreProjectMetaOperation(saveOp));
	       	ArrayList fullSet = new ArrayList(Arrays.asList(scheduledForAddition));
	       	fullSet.addAll(Arrays.asList(new IResource[] {source, destination}));
		    op.add(new RefreshResourcesOperation((IResource [])fullSet.toArray(new IResource[fullSet.size()]), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
	    }
	    else {
	    	op.add(moveOp);
			op.add(new RestoreProjectMetaOperation(saveOp));
		    op.add(new RefreshResourcesOperation(new IResource[] {source, destination}, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
		}

	    // already in WorkspaceModifyOperation context
	    ProgressMonitorUtility.doTaskExternal(op, monitor);
		
		return true;
	}
	
	protected boolean doDelete(IResourceTree tree, IResource resource, IProgressMonitor monitor) {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (local == null || 
		    IStateFilter.SF_NOTEXISTS.accept(resource, local.getStatus(), local.getChangeMask()) ||
		    IStateFilter.SF_NONVERSIONED.accept(resource, local.getStatus(), local.getChangeMask())) {
			return FileUtility.isSVNInternals(resource);
		}
		
	    DeleteResourceOperation mainOp = new DeleteResourceOperation(resource);
	    CompositeOperation op = new CompositeOperation(mainOp.getId());
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(new IResource[] {resource});
		op.add(saveOp);
	    op.add(mainOp);
		op.add(new RestoreProjectMetaOperation(saveOp));
	    op.add(new RefreshResourcesOperation(new IResource[] {resource}, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
	    // already in WorkspaceModifyOperation context
	    ProgressMonitorUtility.doTaskExternal(op, monitor);
		
		return true;
	}

}
