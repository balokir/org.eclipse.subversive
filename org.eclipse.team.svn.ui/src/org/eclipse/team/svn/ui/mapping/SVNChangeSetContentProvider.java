/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffChangeEvent;
import org.eclipse.team.core.diff.IDiffChangeListener;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.BatchingChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener;
import org.eclipse.team.internal.core.subscribers.BatchingChangeSetManager.CollectorChangeEvent;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.mapping.ResourceModelContentProvider;
import org.eclipse.team.internal.ui.mapping.ResourceModelLabelProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.core.mapping.SVNChangeSetModelProvider;
import org.eclipse.team.svn.core.mapping.SVNIncomingChangeSet;
import org.eclipse.team.svn.core.mapping.SVNUnassignedChangeSet;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorSorterService;

public class SVNChangeSetContentProvider extends ResourceModelContentProvider implements ITreePathContentProvider {

	private final class CollectorListener implements IChangeSetChangeListener, BatchingChangeSetManager.IChangeSetCollectorChangeListener {
		
		public void setAdded(final ChangeSet set) {
			if (set instanceof ActiveChangeSet) {
				if (SVNChangeSetContentProvider.this.isVisibleInMode(set)) {
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							AbstractTreeViewer viewer = (AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer();
							viewer.add(viewer.getInput(), set);
						}
					});
				}
				SVNChangeSetContentProvider.this.getUnassignedSet().remove(set.getResources());
			}
		}

		private void handleSetAddition(final ChangeSet set) {
			SVNChangeSetContentProvider.this.getUnassignedSet().remove(set.getResources());
		}

		public void defaultSetChanged(final ChangeSet previousDefault, final ChangeSet set) {
			if (SVNChangeSetContentProvider.this.isVisibleInMode(set) || SVNChangeSetContentProvider.this.isVisibleInMode(previousDefault)) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						((AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer()).update(previousDefault != null ? new Object[] {previousDefault, set} : set, null);
					}
				});
			}
		}

		public void setRemoved(final ChangeSet set) {
			if (set instanceof ActiveChangeSet) {
				if (SVNChangeSetContentProvider.this.isVisibleInMode(set)) {
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							((AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer()).remove(TreePath.EMPTY.createChildPath(set));
						}
					});
				}
				this.handleSetRemoval(set);
			}
		}

		private void handleSetRemoval(final ChangeSet set) {
			IResource[] resources = set.getResources();
			ArrayList<IDiff> toAdd = new ArrayList<IDiff>();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				IDiff diff = getContext().getDiffTree().getDiff(resource);
				//if active change set deleted, then we can freely add its resources to unassigned change set 
				if (diff != null) {
					toAdd.add(diff);
				}
			}
			getUnassignedSet().add(toAdd.toArray(new IDiff[toAdd.size()]));
		}

		public void nameChanged(final ChangeSet set) {
			if (SVNChangeSetContentProvider.this.isVisibleInMode(set)) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						((AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer()).update(set, null);
					}
				});
			}
		}

		public void resourcesChanged(final ChangeSet set, final IPath[] paths) {
			if (set instanceof ActiveChangeSet) {
				if (SVNChangeSetContentProvider.this.isVisibleInMode(set)) {
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							AbstractTreeViewer viewer = (AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer();
							if (SVNChangeSetContentProvider.this.hasChildrenInContext(set)){
								if (SVNChangeSetContentProvider.this.getVisibleSetsInViewer().contains(set)) {
									viewer.refresh(set, true);
								}
								else {
									viewer.add(viewer.getInput(), set);
								}
							}
							else {
								viewer.remove(set);
							}
						}
					});
				}
				this.handleSetChange(set, paths);
			}
		}

		private void handleSetChange(final ChangeSet set, final IPath[] paths) {
			try {
				SVNChangeSetContentProvider.this.getTheRest().beginInput();
			    for (int i = 0; i < paths.length; i++) {
					if (((DiffChangeSet)set).contains(paths[i])) {
						IDiff diff = ((DiffChangeSet)set).getDiffTree().getDiff(paths[i]);
						if (diff != null) {
							SVNChangeSetContentProvider.this.getTheRest().remove(ResourceDiffTree.getResourceFor(diff));
						}
					}
					else {
			            IDiff diff = SVNChangeSetContentProvider.this.getContext().getDiffTree().getDiff(paths[i]);
			            if (diff != null && canAddToUnnassignedChangeSet(diff)) {
			            	SVNChangeSetContentProvider.this.getTheRest().add(diff);
			            }
			        }   
			    }
			}
			finally {
				SVNChangeSetContentProvider.this.getTheRest().endInput(null);
			}
		}

		public void changeSetChanges(final CollectorChangeEvent event, IProgressMonitor monitor) {
			ChangeSet[] addedSets = event.getAddedSets();
			final ChangeSet[] visibleAddedSets = this.getVisibleSets(addedSets);
			ChangeSet[] removedSets = event.getRemovedSets();
			final ChangeSet[] visibleRemovedSets = this.getVisibleSets(removedSets);
			ChangeSet[] changedSets = event.getChangedSets();
			final ChangeSet[] visibleChangedSets = this.getVisibleSets(changedSets);
			if (visibleAddedSets.length > 0 || visibleRemovedSets.length > 0 || visibleChangedSets.length > 0) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						AbstractTreeViewer viewer = (AbstractTreeViewer)SVNChangeSetContentProvider.this.getViewer();
						try {
							viewer.getControl().setRedraw(false);
							if (visibleAddedSets.length > 0) {
								viewer.add(viewer.getInput(), visibleAddedSets);
							}
							if (visibleRemovedSets.length > 0) {
								viewer.remove(visibleRemovedSets);
							}
							for (int i = 0; i < visibleChangedSets.length; i++) {
								viewer.refresh(visibleChangedSets[i], true);		
							}
						}
						finally {
							SVNChangeSetContentProvider.this.getViewer().getControl().setRedraw(true);
						}
					}
				});
			}
			try {
				SVNChangeSetContentProvider.this.getTheRest().beginInput();
				for (int i = 0; i < addedSets.length; i++) {
					this.handleSetAddition(addedSets[i]);
				}
				if (removedSets.length > 0) {
					SVNChangeSetContentProvider.this.addAllUnassignedToUnassignedSet();
				}
				for (int i = 0; i < changedSets.length; i++) {
					ChangeSet set = changedSets[i];
					IPath[] paths = event.getChangesFor(set);
					if (event.getSource().contains(set)) {
						handleSetChange(set, paths);
					}
					else {
						try {
							getTheRest().beginInput();
							for (int j = 0; j < paths.length; j++) {
								IDiff diff = getContext().getDiffTree().getDiff(paths[j]);
								if (diff != null && canAddToUnnassignedChangeSet(diff)) {
									SVNChangeSetContentProvider.this.getTheRest().add(diff);
								}
							}
						}
						finally {
							SVNChangeSetContentProvider.this.getTheRest().endInput(null);
						}
					}
				}
			}
			finally {
				SVNChangeSetContentProvider.this.getTheRest().endInput(monitor);
			}
		}

		private ChangeSet[] getVisibleSets(ChangeSet[] sets) {
			ArrayList<ChangeSet> result = new ArrayList<ChangeSet>();
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				if (SVNChangeSetContentProvider.this.isVisibleInMode(set)) {
					result.add(set);
				}
			}
			return result.toArray(new ChangeSet[result.size()]);
		}
	}
	
	private DiffChangeSet unassignedDiffs;
	private SVNIncomingChangeSetCollector incomingCollector;
	private boolean collectorInitialized;
	private IChangeSetChangeListener collectorListener = new CollectorListener();
	
	private IDiffChangeListener diffTreeListener = new IDiffChangeListener() {
		
		public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		}
	
		boolean isSetVisible(DiffChangeSet set) {
			return SVNChangeSetContentProvider.this.getVisibleSetsInViewer().contains(set);
		}
		
		public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
			Object input = getViewer().getInput();
			if (input instanceof SVNChangeSetModelProvider
					&& SVNChangeSetContentProvider.this.unassignedDiffs != null
					&& event.getTree() == SVNChangeSetContentProvider.this.unassignedDiffs.getDiffTree()) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (SVNChangeSetContentProvider.this.unassignedDiffs.isEmpty()
								|| !hasChildren(TreePath.EMPTY.createChildPath(getUnassignedSet()))) {
							((AbstractTreeViewer)getViewer()).remove(SVNChangeSetContentProvider.this.unassignedDiffs);
						}
						else if (!isSetVisible(SVNChangeSetContentProvider.this.unassignedDiffs)) {
							((AbstractTreeViewer)getViewer()).add(getViewer().getInput(), SVNChangeSetContentProvider.this.unassignedDiffs);
						}
						else {
							((AbstractTreeViewer)getViewer()).refresh(SVNChangeSetContentProvider.this.unassignedDiffs);
						}
					}
				});
			}
		}
	
	};
	
	protected String getModelProviderId() {
		return SVNChangeSetModelProvider.ID;
	}

	protected boolean isVisibleInMode(ChangeSet set) {
		if (this.getViewer().getInput() instanceof SVNChangeSetModelProvider) {
			if (set instanceof ActiveChangeSet) {
				return getConfiguration().getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
			}
			if (set instanceof DiffChangeSet) {
				return getConfiguration().getMode() != ISynchronizePageConfiguration.OUTGOING_MODE;
			}
		}
		return false;
	}
	
	protected boolean isEnabled() {
		final Object input = this.getViewer().getInput();
		return (input instanceof SVNChangeSetModelProvider);
	}
	
	public Object[] getElements(Object parent) {
		if (parent instanceof ISynchronizationContext) {
			//FIXME Does change set model conflict with other models? Remove this override if no.
			return new Object[0];
		}
		if (parent == this.getModelProvider()) {
			return this.getRootElements();
		}
		return super.getElements(parent);
	}
	
	private Object[] getRootElements() {
		if (!this.collectorInitialized) {
			this.initializeCheckedInChangeSetCollector(this.getChangeSetCapability());
			this.collectorInitialized = true;
		}
		ArrayList<ChangeSet> result = new ArrayList<ChangeSet>();
		ChangeSet[] sets = this.getAllSets();
		for (int i = 0; i < sets.length; i++) {
			if (this.hasChildren(TreePath.EMPTY.createChildPath(sets[i])) || sets[i] instanceof ActiveChangeSet && ((ActiveChangeSet)sets[i]).isUserCreated()) {
				result.add(sets[i]);
			}
		}
		if (!this.getUnassignedSet().isEmpty() && this.hasChildren(TreePath.EMPTY.createChildPath(getUnassignedSet()))) {
			result.add(getUnassignedSet());
		}
		return result.toArray();
	}

	private synchronized DiffChangeSet getUnassignedSet() {
		if (this.unassignedDiffs == null) {
			this.unassignedDiffs = new SVNUnassignedChangeSet(SVNUIMessages.ChangeSetModel_UnassignedChangeSetTitle);
			this.unassignedDiffs.getDiffTree().addDiffChangeListener(this.diffTreeListener);
			this.addAllUnassignedToUnassignedSet();
		}
		return this.unassignedDiffs;
	}
	
	private void addAllUnassignedToUnassignedSet() {
		IResourceDiffTree allChanges = getContext().getDiffTree();
		final ArrayList<IDiff> diffs = new ArrayList<IDiff>();
		allChanges.accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDiffVisitor() {
			public boolean visit(IDiff diff) {
				if (canAddToUnnassignedChangeSet(diff))
					diffs.add(diff);
				return true;
			}
		}, IResource.DEPTH_INFINITE);
		this.unassignedDiffs.add(diffs.toArray(new IDiff[diffs.size()]));
	}
	
	private ResourceDiffTree getTheRest() {
		return (ResourceDiffTree)getUnassignedSet().getDiffTree();
	}
	
	protected boolean isContainedInSet(IDiff diff, ChangeSet[] sets) {
		for (int i = 0; i < sets.length; i++) {
			ChangeSet set = sets[i];
			if (set.contains(ResourceDiffTree.getResourceFor(diff))) {
				return true;
			}
		}
		return false;
	}

	protected ResourceTraversal[] getTraversals(ISynchronizationContext context, Object object) {
		if (object instanceof ChangeSet) {
			ChangeSet set = (ChangeSet)object;
			IResource[] resources = set.getResources();
			return new ResourceTraversal[] { new ResourceTraversal(resources, IResource.DEPTH_ZERO, IResource.NONE) };
		}
		return super.getTraversals(context, object);
	}
	
	public Object[] getChildren(TreePath parentPath) {
		if (!this.isEnabled()) {
			return new Object[0];
		}
		if (parentPath.getSegmentCount() == 0) {
			return this.getRootElements();
		}
		Object first = parentPath.getFirstSegment();
		if (!this.isVisibleInMode(first)) {
			return new Object[0];
		}
		IResourceDiffTree diffTree;
		Object parent = parentPath.getLastSegment();
		if (first instanceof DiffChangeSet) {
			DiffChangeSet set = (DiffChangeSet)first;
			diffTree = set.getDiffTree();
			if (parent instanceof DiffChangeSet) {
				parent = this.getModelRoot();
			}
		}
		else {
			return new Object[0];
		}
		Object[] children = this.getChildren(parent);
		HashSet<Object> result = new HashSet<Object>();
		for (int i = 0; i < children.length; i++) {
			Object child = children[i];
			if (this.isVisible(child, diffTree)) {
				result.add(child);
			}
		}
		return result.toArray();
	}
	
	private boolean isVisibleInMode(Object first) {
		if (first instanceof ChangeSet) {
			ChangeSet changeSet = (ChangeSet)first;
			int mode = getConfiguration().getMode();
			switch (mode) {
			case ISynchronizePageConfiguration.BOTH_MODE:
				return true;
			case ISynchronizePageConfiguration.CONFLICTING_MODE:
				return this.containsConflicts(changeSet);
			case ISynchronizePageConfiguration.INCOMING_MODE:
				return changeSet instanceof SVNIncomingChangeSet || (this.isUnassignedSet(changeSet) && this.hasIncomingChanges(changeSet));
			case ISynchronizePageConfiguration.OUTGOING_MODE:
				return changeSet instanceof ActiveChangeSet || this.hasConflicts(changeSet) || (this.isUnassignedSet(changeSet) && this.hasOutgoingChanges(changeSet));
			default:
				break;
			}
		}
		return true;
	}

	private boolean hasIncomingChanges(ChangeSet changeSet) {
		if (changeSet instanceof DiffChangeSet) {
			return ((DiffChangeSet)changeSet).getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK) > 0;
		}
		return false;
	}

	private boolean hasOutgoingChanges(ChangeSet ChangeSet) {
		if (ChangeSet instanceof DiffChangeSet) {
			return ((DiffChangeSet)ChangeSet).getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK) > 0;
		}
		return false;
	}

	private boolean isUnassignedSet(ChangeSet changeSet) {
		return changeSet == this.unassignedDiffs;
	}

	private boolean hasConflicts(ChangeSet changeSet) {
		if (changeSet instanceof DiffChangeSet) {
			return ((DiffChangeSet)changeSet).getDiffTree().countFor(IThreeWayDiff.CONFLICTING, IThreeWayDiff.DIRECTION_MASK) > 0;
		}
		return false;
	}
	
	private boolean containsConflicts(ChangeSet changeSet) {
		if (changeSet instanceof DiffChangeSet) {
			return ((DiffChangeSet)changeSet).getDiffTree().hasMatchingDiffs(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), ResourceModelLabelProvider.CONFLICT_FILTER);
		}
		return false;
	}
	
	private boolean isVisible(Object object, IResourceDiffTree tree) {
		if (object instanceof IResource) {
			IResource resource = (IResource) object;
			IDiff diff = tree.getDiff(resource);
			if (diff != null && this.isVisible(diff)) {
				return true;
			}
			IDiff[] diffs = tree.getDiffs(resource, this.getTraversalCalculator().getLayoutDepth(resource, null));
			for (int i = 0; i < diffs.length; i++) {
				if (isVisible(diffs[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean hasChildrenInContext(ChangeSet set) {
		IResource[] resources = set.getResources();
		for (int i = 0; i < resources.length; i++) {
			if (this.getContext().getDiffTree().getDiff(resources[i]) != null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasChildren(TreePath path) {
		if (path.getSegmentCount() == 1) {
			Object first = path.getFirstSegment();
			if (first instanceof ChangeSet) {
				return isVisibleInMode(first) && hasChildrenInContext((ChangeSet)first);
			}
		}
		return getChildren(path).length > 0;
	}
	
	public TreePath[] getParents(Object element) {
		if (element instanceof ChangeSet) {
			return new TreePath[] { TreePath.EMPTY };
		}
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			DiffChangeSet[] sets = this.getSetsContaining(resource);
			if (sets.length > 0) {
				ArrayList<TreePath> result = new ArrayList<TreePath>();
				for (int i = 0; i < sets.length; i++) {
					DiffChangeSet set = sets[i];
					TreePath path = this.getPathForElement(set, resource.getParent());
					if (path != null) {
						result.add(path);
					}
				}
				return result.toArray(new TreePath[result.size()]);
			}
			TreePath path = this.getPathForElement(getUnassignedSet(), resource.getParent());
			if (path != null) {
				return new TreePath[] { path };
			}
		}
		return new TreePath[0];
	}

	/*
	 * It doesn't include unassigned change set
	 */
	private DiffChangeSet[] getOutgoingSets() {
		ArrayList<DiffChangeSet> result = new ArrayList<DiffChangeSet>();
		ChangeSetCapability changeSetCapability = this.getChangeSetCapability();
		if (changeSetCapability != null && changeSetCapability.supportsActiveChangeSets()) {
			ActiveChangeSetManager collector = changeSetCapability.getActiveChangeSetManager();
			ChangeSet[] sets = collector.getSets();	
			for (int i = 0; i < sets.length; i++) {
				result.add((DiffChangeSet)sets[i]);
			}
		}		
		return result.toArray(new DiffChangeSet[result.size()]);
	} 
	
	private DiffChangeSet[] getAllSets() {
		ArrayList<DiffChangeSet> result = new ArrayList<DiffChangeSet>();
		ChangeSetCapability changeSetCapability = this.getChangeSetCapability();
		if (changeSetCapability != null && changeSetCapability.supportsActiveChangeSets()) {
			ActiveChangeSetManager collector = changeSetCapability.getActiveChangeSetManager();
			ChangeSet[] sets = collector.getSets();	
			for (int i = 0; i < sets.length; i++) {
				result.add((DiffChangeSet)sets[i]);
			}
		}
		if (this.incomingCollector != null) {
			ChangeSet[] sets = this.incomingCollector.getSets();	
			for (int i = 0; i < sets.length; i++) {
				result.add((DiffChangeSet)sets[i]);
			}
		}
		return result.toArray(new DiffChangeSet[result.size()]);
	}
	
	private DiffChangeSet[] getSetsContaining(IResource resource) {
		ArrayList<DiffChangeSet> result = new ArrayList<DiffChangeSet>();
		DiffChangeSet[] allSets = getAllSets();
		for (int i = 0; i < allSets.length; i++) {
			DiffChangeSet set = allSets[i];
			if (isVisible(resource, set.getDiffTree())) {
				result.add(set);
			}
		}
		return result.toArray(new DiffChangeSet[result.size()]);
	}

	private TreePath getPathForElement(DiffChangeSet set, IResource resource) {
		List<Object> pathList = this.getPath(set.getDiffTree(), resource);
		if (pathList != null) {
			pathList.add(0, set);
			TreePath path = new TreePath(pathList.toArray());
			return path;
		}
		return null;
	}
	
	private List<Object> getPath(IResourceDiffTree tree, IResource resource) {
		if (resource == null)
			return null;
		boolean hasDiff = tree.getDiff(resource) == null;
		if (hasDiff && tree.members(resource).length == 0)
			return null;
		if (resource.getType() == IResource.ROOT) {
			return null;
		}
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(resource.getProject());
		if (resource.getType() != IResource.PROJECT) {
			String layout = this.getTraversalCalculator().getLayout();
			if (layout.equals(IPreferenceIds.FLAT_LAYOUT)) {
				result.add(resource);
			}
			else if (layout.equals(IPreferenceIds.COMPRESSED_LAYOUT) && resource.getType() == IResource.FOLDER) {
				result.add(resource);
			}
			else if (layout.equals(IPreferenceIds.COMPRESSED_LAYOUT) && resource.getType() == IResource.FILE) {
				IContainer parent = resource.getParent();
				if (parent.getType() != IResource.PROJECT) {
					result.add(parent);
				}
				result.add(resource);
			}
			else {
				ArrayList<Object> resourcePath = new ArrayList<Object>();
				IResource next = resource;
				while (next.getType() != IResource.PROJECT) {
					resourcePath.add(next);
					next = next.getParent();
				}
				for (int i = resourcePath.size() - 1; i >= 0; i--) {
					result.add(resourcePath.get(i));
				}
			}
		}
		return result;
	}

	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		ChangeSetCapability capability = getChangeSetCapability();
		if (capability.supportsActiveChangeSets()) {
			ActiveChangeSetManager collector = capability.getActiveChangeSetManager();
			collector.addListener(this.collectorListener);
		}
		SVNChangeSetSorter sorter = this.getSorter();
		if (sorter != null) {
			sorter.setConfiguration(getConfiguration());
		}
	}
	
	private SVNChangeSetSorter getSorter() {
		INavigatorContentService contentService = getExtensionSite().getService();
		INavigatorSorterService sortingService = contentService.getSorterService();
		INavigatorContentExtension extension = getExtensionSite().getExtension();
		if (extension != null) {
			ViewerSorter sorter = sortingService.findSorter(extension.getDescriptor(), getModelProvider(), new DiffChangeSet(), new DiffChangeSet());
			if (sorter instanceof SVNChangeSetSorter) {
				return (SVNChangeSetSorter)sorter;
			}
		}
		return null;
	}

	private void initializeCheckedInChangeSetCollector(ChangeSetCapability capability) {
		if (capability.supportsCheckedInChangeSets()) {
			this.incomingCollector = ((SVNModelParticipantChangeSetCapability)capability).createIncomingChangeSetCollector(this.getConfiguration());
			this.incomingCollector.addListener(this.collectorListener);
			this.incomingCollector.add(((ResourceDiffTree)getContext().getDiffTree()).getDiffs());
		}
	}
	
	public void dispose() {
		ChangeSetCapability capability = this.getChangeSetCapability();
		if (capability.supportsActiveChangeSets()) {
			capability.getActiveChangeSetManager().removeListener(this.collectorListener);
		}
		if (this.incomingCollector != null) {
			this.incomingCollector.removeListener(this.collectorListener);
			this.incomingCollector.dispose();
		}
		if (this.unassignedDiffs != null) {
			this.unassignedDiffs.getDiffTree().removeDiffChangeListener(this.diffTreeListener);
		}
		super.dispose();
	}
	
	public IResourceDiffTree getDiffTree(TreePath path) {
		if (path.getSegmentCount() > 0) {
			Object first = path.getFirstSegment();
			if (first instanceof DiffChangeSet) {
				DiffChangeSet set = (DiffChangeSet) first;
				return set.getDiffTree();
			}
		}
		return this.getTheRest();
	}
	
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		IPath[] removed = event.getRemovals();
		IDiff[] added = event.getAdditions();
		IDiff[] changed = event.getChanges();
		try {
			this.getTheRest().beginInput();
			for (int i = 0; i < removed.length; i++) {
				getTheRest().remove(removed[i]);
			}
			this.doDiffsChanged(added);
			this.doDiffsChanged(changed);
		}
		finally {
			this.getTheRest().endInput(monitor);
		}
		if (this.incomingCollector != null) {
			this.incomingCollector.handleChange(event);
		}
		UIMonitorUtility.getDisplay().asyncExec(new Runnable() {
			public void run() {
				SVNChangeSetContentProvider.this.getViewer().refresh();
			}
		});
	}
	
	protected void doDiffsChanged(IDiff[] diff) {
		
		for (int i = 0; i < diff.length; i++) {
			if (!this.isContainedInSet(diff[i], this.getOutgoingSets())) {
				if (this.hasLocalChanges(diff[i])) {
					getTheRest().add(diff[i]);
				} else {
					getTheRest().remove(diff[i].getPath());
				}
			}
		}
	}
	
	protected boolean canAddToUnnassignedChangeSet(IDiff diff) {
		if (!this.isContainedInSet(diff, this.getOutgoingSets())) {
			return this.hasLocalChanges(diff);
		}
		return false;		
	}
	
	protected boolean hasLocalChanges(IDiff diff) {
		try {						
			//TODO correctly get subscriber									 			
			Subscriber subscriber = UpdateSubscriber.instance();									
			AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo) subscriber.getSyncInfo(ResourceDiffTree.getResourceFor(diff));
			if (syncInfo != null && (SyncInfo.getDirection(syncInfo.getKind()) & SyncInfo.OUTGOING) != 0) {
				return true;
			}
		} catch (Exception e) {
			LoggedOperation.reportError(SVNChangeSetContentProvider.class.getName(), e);
		}
		return false;
	}
	
	protected void updateLabels(ISynchronizationContext context, IPath[] paths) {
		super.updateLabels(context, paths);
		ChangeSet[] sets = this.getSetsShowingPropogatedStateFrom(paths);
		if (sets.length > 0) {
			((AbstractTreeViewer)this.getViewer()).update(sets, null);
		}
	}

	private ChangeSet[] getSetsShowingPropogatedStateFrom(IPath[] paths) {
		HashSet<ChangeSet> result = new HashSet<ChangeSet>();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			ChangeSet[] sets = this.getSetsShowingPropogatedStateFrom(path);
			for (int j = 0; j < sets.length; j++) {
				result.add(sets[j]);
			}
		}
		return result.toArray(new ChangeSet[result.size()]);
	}

	protected DiffChangeSet[] getSetsShowingPropogatedStateFrom(IPath path) {
		ArrayList<DiffChangeSet> result = new ArrayList<DiffChangeSet>();
		DiffChangeSet[] allSets = this.getAllSets();
		for (int i = 0; i < allSets.length; i++) {
			DiffChangeSet set = allSets[i];
			if (set.getDiffTree().getDiff(path) != null || set.getDiffTree().getChildren(path).length > 0) {
				result.add(set);
			}
		}
		return result.toArray(new DiffChangeSet[result.size()]);
	}
	
	public ChangeSetCapability getChangeSetCapability() {
        ISynchronizeParticipant participant = this.getConfiguration().getParticipant();
        if (participant instanceof IChangeSetProvider) {
            IChangeSetProvider provider = (IChangeSetProvider)participant;
            return provider.getChangeSetCapability();
        }
        return null;
    }

	private HashSet<ChangeSet> getVisibleSetsInViewer() {
		TreeViewer viewer = (TreeViewer)getViewer();
		Tree tree = viewer.getTree();
		TreeItem[] children = tree.getItems();
		HashSet<ChangeSet> result = new HashSet<ChangeSet>();
		for (int i = 0; i < children.length; i++) {
			TreeItem control = children[i];
			Object data = control.getData();
			if (data instanceof ChangeSet) {
				ChangeSet set = (ChangeSet) data;
				result.add(set);
			}
		}
		return result;
	}
}
