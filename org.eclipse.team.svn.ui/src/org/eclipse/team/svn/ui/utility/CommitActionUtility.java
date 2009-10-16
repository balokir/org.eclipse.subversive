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

package org.eclipse.team.svn.ui.utility;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNWithPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.SetRevisionAuthorNameOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Utility class for package explorer and synchronize view commit actions
 * 
 * @author Sergiy Logvin
 */
public class CommitActionUtility {
	protected IResourceSelector selector;
	
	protected HashSet newNonRecursive;
	protected HashSet newRecursive;
	protected HashSet parents;
	
	protected IResource[] allResources;
	protected HashSet allResourcesSet;
	
	public CommitActionUtility(IResourceSelector selector) {
		this.initialize(selector);
	}
	
	public void initialize(IResourceSelector selector) {
		this.selector = selector;
		
		this.allResourcesSet = new HashSet();
		this.allResourcesSet.addAll(Arrays.asList(this.selector.getSelectedResourcesRecursive(new IStateFilter.OrStateFilter(new IStateFilter[] {IStateFilter.SF_COMMITABLE, IStateFilter.SF_CONFLICTING, IStateFilter.SF_TREE_CONFLICTING, IStateFilter.SF_NEW}))));
		
		this.newNonRecursive = new HashSet(Arrays.asList(this.selector.getSelectedResources(IStateFilter.SF_IGNORED)));
		this.newRecursive = new HashSet(Arrays.asList(FileUtility.getResourcesRecursive((IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]), IStateFilter.SF_NEW, IResource.DEPTH_ZERO)));
		
		HashSet fullSet = new HashSet(this.newNonRecursive);
		fullSet.addAll(this.newRecursive);
		this.parents = new HashSet(Arrays.asList(FileUtility.getOperableParents((IResource [])fullSet.toArray(new IResource[fullSet.size()]), IStateFilter.SF_UNVERSIONED)));
		this.newNonRecursive.addAll(this.parents);
		fullSet.addAll(this.parents);
		
		this.allResourcesSet.addAll(fullSet);
		
		this.allResources = (IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]);
		this.allResourcesSet.addAll(Arrays.asList(FileUtility.addOperableParents(this.allResources, IStateFilter.SF_ADDED, true)));
		this.allResources = (IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]);
	}
	
	public HashSet getAllResourcesSet() {
		return this.allResourcesSet;
	}

	public IResource[] getAllResources() {
		return this.allResources;
	}
	
	public CompositeOperation getCompositeCommitOperation(IResource []selectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part) {
		return this.getNonRecursiveImpl(selectedResources, message, keepLocks, shell, part);
	}
	
	public CompositeOperation getCompositeCommitOperation(IResource []selectedResources, IResource []notSelectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part, boolean tryRecursive) {
		return 
			tryRecursive ? 
			this.getRecursiveImpl(selectedResources, notSelectedResources, message, keepLocks, shell, part) :
			this.getNonRecursiveImpl(selectedResources, message, keepLocks, shell, part);
	}

	protected CompositeOperation getRecursiveImpl(IResource []selectedResources, IResource []notSelectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part) {
		IResource []notSelectedNew = FileUtility.getResourcesRecursive(notSelectedResources, IStateFilter.SF_UNVERSIONED, IResource.DEPTH_ZERO);
		boolean allowsRecursiveAdd = true;
		for (int i = 0; i < notSelectedNew.length; i++) {
			if (this.newRecursive.remove(notSelectedNew[i])) {
				allowsRecursiveAdd = false;
			}
			else if (!this.parents.contains(notSelectedNew[i])) {
				this.newNonRecursive.remove(notSelectedNew[i]);
			}
		}
		
		IResource[] resourcesToCommit = new IResource[0]; 
		boolean isRecursiveCommit = true;				
		if (FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_EXTERNAL, IResource.DEPTH_ZERO)) {
			//if 'selectedResources' has externals -> commit resources selected in commit dialog not recursively			
			resourcesToCommit = selectedResources;
			isRecursiveCommit = false;
		}						
		if (isRecursiveCommit) {
			resourcesToCommit = notSelectedResources.length == 0 ? this.selector.getSelectedResources() : selectedResources;
			isRecursiveCommit = allowsRecursiveAdd && notSelectedNew.length == notSelectedResources.length; 
		}					
		
		CommitOperation mainOp = new CommitOperation(resourcesToCommit, message, isRecursiveCommit, keepLocks);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		if (allowsRecursiveAdd) {
			if (this.newNonRecursive.size() > 0) {
				IResource []newNonRecursive = (IResource [])this.newNonRecursive.toArray(new IResource[this.newNonRecursive.size()]);
				op.add(new AddToSVNWithPropertiesOperation(newNonRecursive, false));
				op.add(new ClearLocalStatusesOperation(newNonRecursive));
			}
			if (this.newRecursive.size() > 0) {
				IResource []newRecursive = (IResource [])this.newRecursive.toArray(new IResource[this.newRecursive.size()]);
				op.add(new AddToSVNWithPropertiesOperation(newRecursive, true));
				op.add(new ClearLocalStatusesOperation(newRecursive));
			}
		}
		else {
			this.newNonRecursive.addAll(this.newRecursive);
			this.newRecursive.clear();
			IResource []newNonRecursive = (IResource [])this.newNonRecursive.toArray(new IResource[this.newNonRecursive.size()]);
			op.add(new AddToSVNWithPropertiesOperation(newNonRecursive, false));
			op.add(new ClearLocalStatusesOperation(newNonRecursive));
		}
		
		this.addCommonPart(selectedResources, op, mainOp, shell, part);
		op.add(new SetRevisionAuthorNameOperation(mainOp, Options.FORCE));
		
		return op;
	}
	
	protected CompositeOperation getNonRecursiveImpl(IResource []selectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part) {
		CommitOperation mainOp = new CommitOperation(selectedResources, message, false, keepLocks);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		
		IResource []newResources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_UNVERSIONED, IResource.DEPTH_ZERO);
		if (newResources.length > 0) {
			op.add(new AddToSVNWithPropertiesOperation(newResources, false));
			op.add(new ClearLocalStatusesOperation(newResources));
		}
		
		this.addCommonPart(selectedResources, op, mainOp, shell, part);
		op.add(new SetRevisionAuthorNameOperation(mainOp, Options.FORCE));
		
		return op;
	}
	
	protected void addCommonPart(IResource []selectedResources, CompositeOperation op, CommitOperation mainOp, Shell shell, IWorkbenchPart part) {
		op.add(mainOp);
		op.add(new ClearUpdateStatusesOperation(selectedResources));
		op.add(new RefreshResourcesOperation(selectedResources));
		op.add(new NotifyUnresolvedConflictOperation(mainOp));
		
		ExtensionsManager.getInstance().getCurrentCommitFactory().performAfterCommitTasks(op, mainOp, null, part);
	}
	
}
