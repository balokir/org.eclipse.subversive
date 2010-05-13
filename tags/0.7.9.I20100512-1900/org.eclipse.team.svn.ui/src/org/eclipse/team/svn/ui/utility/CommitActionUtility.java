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
import org.eclipse.team.svn.core.operation.IActionOperation;
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
	
	protected HashSet<IResource> newNonRecursive;
	protected HashSet<IResource> newRecursive;
	protected HashSet<IResource> parents;
	
	protected IResource[] allResources;
	protected HashSet<IResource> allResourcesSet;
	
	/*
	 * Determines whether we can call commit recursively.
	 * We can't call recursively in following cases:
	 * 	1. if there are externals among resources which we'll be shown in commit dialog
	 *  (if call commit on not external directory, externals will not be committed, 
	 *  because commit doesn't handle externals(in contrast e.g. to 'update'))
	 *  2. if selected resource and its parent don't exist in repository
	 *  (if selected resource is new(or added) we show also its parent in commit dialog, 
	 *  because we can't directly commit resource which parent doesn't exist in repository and
	 *  so we can't commit recursively) 
	 */
	protected boolean canBeRecursiveCommit = true;
	
	public CommitActionUtility(IResourceSelector selector) {
		this.initialize(selector);
	}
	
	public void initialize(IResourceSelector selector) {
		this.selector = selector;
		
		this.allResourcesSet = new HashSet<IResource>();
		this.allResourcesSet.addAll(Arrays.asList(this.selector.getSelectedResourcesRecursive(new IStateFilter.OrStateFilter(new IStateFilter[] {IStateFilter.SF_COMMITABLE, IStateFilter.SF_CONFLICTING, IStateFilter.SF_TREE_CONFLICTING, IStateFilter.SF_NEW}))));
		
		this.newNonRecursive = new HashSet<IResource>(Arrays.asList(this.selector.getSelectedResources(IStateFilter.SF_IGNORED_BUT_NOT_EXTERNAL)));
		this.newRecursive = new HashSet<IResource>(Arrays.asList(FileUtility.getResourcesRecursive((IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]), IStateFilter.SF_NEW, IResource.DEPTH_ZERO)));
		
		HashSet<IResource> fullSet = new HashSet<IResource>(this.newNonRecursive);
		fullSet.addAll(this.newRecursive);
		this.parents = new HashSet<IResource>(Arrays.asList(FileUtility.getOperableParents((IResource [])fullSet.toArray(new IResource[fullSet.size()]), IStateFilter.SF_UNVERSIONED)));
		this.newNonRecursive.addAll(this.parents);
		fullSet.addAll(this.parents);
		
		this.allResourcesSet.addAll(fullSet);
		
		this.allResources = (IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]);
		this.allResourcesSet.addAll(Arrays.asList(FileUtility.addOperableParents(this.allResources, IStateFilter.SF_ADDED, true)));
		this.allResources = (IResource [])this.allResourcesSet.toArray(new IResource[this.allResourcesSet.size()]);
					
		this.canBeRecursiveCommit = FileUtility.getOperableParents(this.selector.getSelectedResources(), IStateFilter.SF_ADDED, false).length == 0;				
		if (this.canBeRecursiveCommit && FileUtility.checkForResourcesPresence(this.allResources, IStateFilter.SF_SWITCHED, IResource.DEPTH_ZERO)) {
			this.canBeRecursiveCommit = false;
		}
	}
	
	public HashSet<IResource> getAllResourcesSet() {
		return this.allResourcesSet;
	}

	public IResource[] getAllResources() {
		return this.allResources;
	}
	
	public CompositeOperation getCompositeCommitOperation(IResource []selectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part) {
		return this.getNonRecursiveImpl(selectedResources, message, keepLocks, shell, part);
	}
	
	/*
	 * We separate commit on recursive and not recursive because of performance reasons, i.e. recursive
	 * commit works significantly faster.
	 */
	public CompositeOperation getCompositeCommitOperation(IResource []selectedResources, IResource []notSelectedResources, String message, boolean keepLocks, Shell shell, IWorkbenchPart part, boolean tryRecursive) {
		return 
			this.canBeRecursiveCommit && tryRecursive ? 
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
		
		CommitOperation mainOp = new CommitOperation(notSelectedResources.length == 0 ? this.selector.getSelectedResources() : selectedResources, message, allowsRecursiveAdd && notSelectedNew.length == notSelectedResources.length, keepLocks);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		
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
		
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		
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
		op.add(new ClearUpdateStatusesOperation(selectedResources), new IActionOperation[]{mainOp});
		op.add(new RefreshResourcesOperation(selectedResources));
		op.add(new NotifyUnresolvedConflictOperation(mainOp));
		
		ExtensionsManager.getInstance().getCurrentCommitFactory().performAfterCommitTasks(op, mainOp, null, part);
	}
	
}
