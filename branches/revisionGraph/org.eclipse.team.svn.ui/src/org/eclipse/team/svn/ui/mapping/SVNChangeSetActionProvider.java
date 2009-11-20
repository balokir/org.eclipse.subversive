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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.team.core.diff.FastDiffFilter;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.mapping.ResourceModelActionProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.svn.core.mapping.SVNChangeSetModelProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelParticipantAction;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;

public class SVNChangeSetActionProvider extends ResourceModelActionProvider {
	
	/**
     * Menu group that can be added to the context menu
     */
    public final static String CHANGE_SET_GROUP = "svnChangeSetActions"; //$NON-NLS-1$
	
	private static final String CHANGESET_SORTING_TYPE = TeamUIPlugin.ID + ".SET_SORTING_TYPE"; //$NON-NLS-1$
	
	private MenuManager sortChangeSetsMenu;
	private MenuManager addToChangeSetMenu;
	private CreateChangeSetAction createChangeSetAction;
	private EditChangeSetAction editChangeSetAction;
    private RemoveChangeSetAction removeChangeSetAction;
    private MakeDefaultChangeSetAction makeDefaultAction;
	
	private class CreateChangeSetAction extends ModelParticipantAction {
	    
        public CreateChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(TeamUIMessages.ChangeLogModelProvider_0, configuration); 
        }

        public void run() {
        	final IDiff[] diffs = SVNChangeSetActionProvider.this.getLocalChanges(getStructuredSelection());
        	UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					CreateChangeSetAction.this.createChangeSet(diffs);
				}
			});
        }
        
		protected void createChangeSet(IDiff[] diffs) {
            ActiveChangeSet set = SVNChangeSetActionProvider.this.getChangeSetCapability().createChangeSet(getConfiguration(), diffs);
            if (set != null) {
            	SVNChangeSetActionProvider.this.getActiveChangeSetManager().add(set);
            }
        }
        
		protected boolean isEnabledForSelection(IStructuredSelection selection) {
			return isContentProviderEnabled() && containsLocalChanges(selection);
		}
	}
	
	private class AddToChangeSetAction extends ModelParticipantAction {
		 
        private final ActiveChangeSet set;
	    
        public AddToChangeSetAction(ISynchronizePageConfiguration configuration, ActiveChangeSet set, ISelection selection) {
            super(set == null ? TeamUIMessages.ChangeSetActionGroup_2 : set.getTitle(), configuration); 
            this.set = set;
            selectionChanged(selection);
        }
        
        public void run() {
        	IDiff[] diffArray = getLocalChanges(getStructuredSelection());
            if (this.set != null) {
            	this.set.add(diffArray);
            }
            else {
                ChangeSet[] sets = SVNChangeSetActionProvider.this.getActiveChangeSetManager().getSets();
                IResource[] resources = this.getResources(diffArray);
                for (int i = 0; i < sets.length; i++) {
                    ActiveChangeSet activeSet = (ActiveChangeSet)sets[i];
					activeSet.remove(resources);
                }
            }
        }

		private IResource[] getResources(IDiff[] diffArray) {
			ArrayList<IResource> result = new ArrayList<IResource>();
			for (int i = 0; i < diffArray.length; i++) {
				IDiff diff = diffArray[i];
				IResource resource = ResourceDiffTree.getResourceFor(diff);
				if (resource != null) {
					result.add(resource);
				}
			}
			return result.toArray(new IResource[result.size()]);
		}

		protected boolean isEnabledForSelection(IStructuredSelection selection) {
			return isContentProviderEnabled() && containsLocalChanges(selection);
		}
	}
	
	private abstract class ChangeSetAction extends BaseSelectionListenerAction {

        public ChangeSetAction(String title, ISynchronizePageConfiguration configuration) {
            super(title);
        }
        
        protected boolean updateSelection(IStructuredSelection selection) {
            return this.getSelectedSet() != null;
        }

        protected ActiveChangeSet getSelectedSet() {
            IStructuredSelection selection = getStructuredSelection();
            if (selection.size() == 1) {
                Object first = selection.getFirstElement();
                if (first instanceof ActiveChangeSet) {
					ActiveChangeSet activeChangeSet = (ActiveChangeSet) first;
					if (activeChangeSet.isUserCreated())
						return activeChangeSet;
				}
            }
            return null;
        }
	}
	
	private class EditChangeSetAction extends ChangeSetAction {

        public EditChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(TeamUIMessages.ChangeLogModelProvider_6, configuration); 
        }
        
        public void run() {
            ActiveChangeSet set = getSelectedSet();
            if (set == null) return;
            getChangeSetCapability().editChangeSet(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration(), set);
        }
	}

	private class RemoveChangeSetAction extends ChangeSetAction {

        public RemoveChangeSetAction(ISynchronizePageConfiguration configuration) {
            super("Remove Change Set", configuration); //$NON-NLS-1$
        }
        
        public void run() {
            ActiveChangeSet set = getSelectedSet();
            if (set == null) return;
            if (MessageDialog.openConfirm(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration().getSite().getShell(), TeamUIMessages.ChangeSetActionGroup_0, SVNUIMessages.format(TeamUIMessages.ChangeSetActionGroup_1, new String[] {set.getTitle()}))) { 
                SVNChangeSetActionProvider.this.getActiveChangeSetManager().remove(set);
            }
        }
	}
	
	private class MakeDefaultChangeSetAction extends ChangeSetAction {
        
		public MakeDefaultChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(TeamUIMessages.ChangeLogModelProvider_9, configuration); 
        }
        
        public void run() {
            ActiveChangeSet set = getSelectedSet();
            if (set == null) {
            	return;
            }
    		getActiveChangeSetManager().makeDefault(set);
        }
	}
	
	private class RefreshSortOrderAction extends Action {
		
		private int criteria;
		
		protected RefreshSortOrderAction(String name, int criteria) {
			super(name, IAction.AS_RADIO_BUTTON);
			this.criteria = criteria;
			update();		
		}

		public void run() {
			int sortCriteria = SVNChangeSetActionProvider.getSortCriteria(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration());
			if (isChecked() && sortCriteria != this.criteria) {
				SVNChangeSetActionProvider.setSortCriteria(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration(), this.criteria);
				this.update();
				((SynchronizePageConfiguration)SVNChangeSetActionProvider.this.getSynchronizePageConfiguration()).getPage().getViewer().refresh();
			}
		}
		
		public void update() {
		    setChecked(this.criteria == SVNChangeSetActionProvider.getSortCriteria(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration()));
		}
	}
	
	public SVNChangeSetActionProvider() {
		super();
	}
	
	protected void initialize() {
		super.initialize();
		if (getChangeSetCapability().supportsCheckedInChangeSets()) {
			this.sortChangeSetsMenu = new MenuManager(TeamUIMessages.ChangeLogModelProvider_0a);	 
			this.sortChangeSetsMenu.add(new RefreshSortOrderAction(TeamUIMessages.ChangeLogModelProvider_1a, SVNChangeSetSorter.COMMENT)); 
			this.sortChangeSetsMenu.add(new RefreshSortOrderAction(TeamUIMessages.ChangeLogModelProvider_2a, SVNChangeSetSorter.DATE)); 
			this.sortChangeSetsMenu.add(new RefreshSortOrderAction(TeamUIMessages.ChangeLogModelProvider_3a, SVNChangeSetSorter.USER));
		}
		if (getChangeSetCapability().supportsActiveChangeSets()) {
			this.addToChangeSetMenu = new MenuManager(TeamUIMessages.ChangeLogModelProvider_12); 
			this.addToChangeSetMenu.setRemoveAllWhenShown(true);
			this.addToChangeSetMenu.addMenuListener(new IMenuListener() {
	            public void menuAboutToShow(IMenuManager manager) {
	            	ChangeSet[] sets = getActiveChangeSetManager().getSets();
	                Arrays.sort(sets, new SVNChangeSetComparator());
	                ISelection selection = getContext().getSelection();
	                SVNChangeSetActionProvider.this.createChangeSetAction.selectionChanged(selection);
	                SVNChangeSetActionProvider.this.addToChangeSetMenu.add(SVNChangeSetActionProvider.this.createChangeSetAction);
	        		SVNChangeSetActionProvider.this.addToChangeSetMenu.add(new Separator());
	                for (int i = 0; i < sets.length; i++) {
	                    ActiveChangeSet set = (ActiveChangeSet)sets[i];
	                    AddToChangeSetAction action = new AddToChangeSetAction(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration(), set, selection);
	                    manager.add(action);
	                }
	                SVNChangeSetActionProvider.this.addToChangeSetMenu.add(new Separator());
	                SVNChangeSetActionProvider.this.addToChangeSetMenu.add(new AddToChangeSetAction(SVNChangeSetActionProvider.this.getSynchronizePageConfiguration(), null, selection));
	            }
	        });
			this.createChangeSetAction = new CreateChangeSetAction(this.getSynchronizePageConfiguration());
			this.addToChangeSetMenu.add(this.createChangeSetAction);
			this.addToChangeSetMenu.add(new Separator());
			this.editChangeSetAction = new EditChangeSetAction(this.getSynchronizePageConfiguration());
			this.makeDefaultAction = new MakeDefaultChangeSetAction(this.getSynchronizePageConfiguration());
			this.removeChangeSetAction = new RemoveChangeSetAction(this.getSynchronizePageConfiguration());
		}
	}
	
	public void fillContextMenu(IMenuManager menu) {
		if (isContentProviderEnabled()) {
			super.fillContextMenu(menu);
			if (this.getChangeSetCapability().enableCheckedInChangeSetsFor(this.getSynchronizePageConfiguration())) {
				appendToGroup(menu, ISynchronizePageConfiguration.SORT_GROUP, this.sortChangeSetsMenu);
			}
			if (getChangeSetCapability().enableActiveChangeSetsFor(this.getSynchronizePageConfiguration())) {
				appendToGroup(menu, CHANGE_SET_GROUP, this.addToChangeSetMenu);
				appendToGroup(menu, CHANGE_SET_GROUP, this.editChangeSetAction);
				appendToGroup(menu, CHANGE_SET_GROUP, this.removeChangeSetAction);
				appendToGroup(menu, CHANGE_SET_GROUP, this.makeDefaultAction);
			}
		}
	}
	
	public void dispose() {
	    if (this.addToChangeSetMenu != null) {
	    	this.addToChangeSetMenu.dispose();
	    	this.addToChangeSetMenu.removeAll();
	    }
	    if (this.sortChangeSetsMenu != null) {
	    	this.sortChangeSetsMenu.dispose();
	    	this.sortChangeSetsMenu.removeAll();
	    }
		super.dispose();
	}
	
	private boolean appendToGroup(IContributionManager manager, String groupId, IContributionItem item) {
		if (manager == null || item == null) {
			return false;
		}
		IContributionItem group = manager.find(groupId);
		if (group != null) {
			manager.appendToGroup(group.getId(), item);
			return true;
		}
		return false;
	}
	
	private boolean appendToGroup(IContributionManager manager, String groupId, IAction action) {
		if (manager == null || action == null) {
			return false;
		}
		IContributionItem group = manager.find(groupId);
		if (group != null) {
			manager.appendToGroup(group.getId(), action);
			return true;
		}
		return false;
	}
	
	public ChangeSetCapability getChangeSetCapability() {
        ISynchronizeParticipant participant = getSynchronizePageConfiguration().getParticipant();
        if (participant instanceof IChangeSetProvider) {
            IChangeSetProvider provider = (IChangeSetProvider) participant;
            return provider.getChangeSetCapability();
        }
        return null;
    }
	
	protected boolean isContentProviderEnabled() {
		SVNChangeSetContentProvider provider = getContentProvider();
		if (provider != null) {
			return provider.isEnabled();
		}
		return false;
	}
	
	public static int getSortCriteria(ISynchronizePageConfiguration configuration) {
		int sortCriteria = SVNChangeSetSorter.DATE;
    	if (configuration != null) {
	    	Object o = configuration.getProperty(SVNChangeSetActionProvider.CHANGESET_SORTING_TYPE);
	    	if (o instanceof Integer) {
				sortCriteria = ((Integer)o).intValue();
			}
	    	else {
				try {
					IDialogSettings pageSettings = configuration.getSite().getPageSettings();
					if(pageSettings != null) {
						sortCriteria = pageSettings.getInt(SVNChangeSetActionProvider.CHANGESET_SORTING_TYPE);
					}
				}
				catch(NumberFormatException e) {
					// ignore and use the defaults.
				}
			}
		}
		switch (sortCriteria)
		{
        	case SVNChangeSetSorter.COMMENT:
        	case SVNChangeSetSorter.DATE:
        	case SVNChangeSetSorter.USER:
        		break;
        	default:
        		sortCriteria = SVNChangeSetSorter.DATE;
            break;
        }
		return sortCriteria;
    }
	
	public static void setSortCriteria(ISynchronizePageConfiguration configuration, int criteria) {
		configuration.setProperty(SVNChangeSetActionProvider.CHANGESET_SORTING_TYPE, new Integer(criteria));
		IDialogSettings pageSettings = configuration.getSite().getPageSettings();
		if (pageSettings != null) {
			pageSettings.put(SVNChangeSetActionProvider.CHANGESET_SORTING_TYPE, criteria);
		}
	}
	
	private SVNChangeSetContentProvider getContentProvider() {
		INavigatorContentExtension extension = this.getExtension();
		if (extension != null) {
			ITreeContentProvider provider = extension.getContentProvider();
			if (provider instanceof SVNChangeSetContentProvider) {
				return (SVNChangeSetContentProvider) provider;
			}
		}
		return null;
	}
	
	private INavigatorContentExtension getExtension() {
		INavigatorContentService service = getActionSite().getContentService();
		Set<?> set = service.findContentExtensionsByTriggerPoint(getModelProvider());
		for (Iterator<?> iter = set.iterator(); iter.hasNext();) {
			INavigatorContentExtension extension = (INavigatorContentExtension) iter.next();
			return extension;
		}
		return null;
	}
	
	private Object getModelProvider() {
		return SVNChangeSetModelProvider.getProvider();
	}
	
	public IDiff[] getLocalChanges(IStructuredSelection selection) {
		if (selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection)selection).getPaths();
			ArrayList<IDiff> result = new ArrayList<IDiff>();
			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				IDiff[] diffs = getLocalChanges(path);
				for (int j = 0; j < diffs.length; j++) {
					IDiff diff = diffs[j];
					result.add(diff);
				}
			}
			return result.toArray(new IDiff[result.size()]);
		}
		return new IDiff[0];
	}
	
	private IDiff[] getLocalChanges(TreePath path) {
		IResourceDiffTree tree = this.getDiffTree(path);
		if (path.getSegmentCount() == 1 && path.getLastSegment() instanceof IDiffTree) {
			return ((ResourceDiffTree)tree).getDiffs();
		}
		ResourceTraversal[] traversals = this.getTraversals(path.getLastSegment());
		return tree.getDiffs(traversals);
	}
	
	private IResourceDiffTree getDiffTree(TreePath path) {
		return this.getContentProvider().getDiffTree(path);
	}
	
	private ResourceTraversal[] getTraversals(Object element) {
		if (element instanceof ChangeSet) {
			ChangeSet set = (ChangeSet) element;
			return new ResourceTraversal[] { new ResourceTraversal(set.getResources(), IResource.DEPTH_ZERO, IResource.NONE) };
		}
		if (element instanceof IProject) {
			IProject project = (IProject) element;
			return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { project }, IResource.DEPTH_INFINITE, IResource.NONE) };
		}
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { file }, IResource.DEPTH_ZERO, IResource.NONE) };
		}
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			if (getLayout().equals(IPreferenceIds.COMPRESSED_LAYOUT)) {
				return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { folder }, IResource.DEPTH_ONE, IResource.NONE) };
			} else if (getLayout().equals(IPreferenceIds.TREE_LAYOUT)) {
				return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { folder }, IResource.DEPTH_INFINITE, IResource.NONE) };
			} else if (getLayout().equals(IPreferenceIds.FLAT_LAYOUT)) {
				return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { folder }, IResource.DEPTH_ZERO, IResource.NONE) };
			}
		}
		return new ResourceTraversal[0];
	}
	
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (context != null) {
	        if (this.editChangeSetAction != null)
	        	this.editChangeSetAction.selectionChanged((IStructuredSelection)getContext().getSelection());
	        if (this.removeChangeSetAction != null)
	        	this.removeChangeSetAction.selectionChanged((IStructuredSelection)getContext().getSelection());
	        if (this.makeDefaultAction != null)
	        	this.makeDefaultAction.selectionChanged((IStructuredSelection)getContext().getSelection());
		}
	}
	
	public boolean containsLocalChanges(IStructuredSelection selection) {
		if (selection instanceof ITreeSelection) {
			ITreeSelection ts = (ITreeSelection) selection;
			TreePath[] paths = ts.getPaths();
			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				if (containsLocalChanges(path)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean containsLocalChanges(TreePath path) {
		IResourceDiffTree tree = getDiffTree(path);
		ResourceTraversal[] traversals = getTraversals(path.getLastSegment());
		return tree.hasMatchingDiffs(traversals, getVisibleLocalChangesFilter());
	}
	
	private FastDiffFilter getVisibleLocalChangesFilter() {
	    return new FastDiffFilter() {
			public boolean select(IDiff diff) {
				if (diff instanceof IThreeWayDiff && isVisible(diff)) {
					IThreeWayDiff twd = (IThreeWayDiff) diff;
					if (twd.getDirection() == IThreeWayDiff.OUTGOING || twd.getDirection() == IThreeWayDiff.CONFLICTING) {
						return true;
					}
				}
				return false;
			}
		};
	}
	
	protected boolean isVisible(IDiff diff) {
		return ((SynchronizePageConfiguration)getSynchronizePageConfiguration()).isVisible(diff);
	}
	
	protected ActiveChangeSetManager getActiveChangeSetManager() {
		return SVNTeamUIPlugin.instance().getModelCangeSetManager();
	}
	
	private String getLayout() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCVIEW_DEFAULT_LAYOUT);
	}

}
