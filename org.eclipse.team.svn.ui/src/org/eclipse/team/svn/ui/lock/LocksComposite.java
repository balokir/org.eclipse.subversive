/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.lock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.remote.BreakLockOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;
import org.eclipse.team.svn.ui.lock.LockResourceSelectionComposite.ILockResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.lock.LockResourceSelectionComposite.LockResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.local.LockResourcesPanel;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.ICancellableOperationWrapper;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * SVN Locks composite
 * 
 * @author Igor Burilo
 */
public class LocksComposite extends Composite {
			
	protected boolean isProcessing;
	protected LocksView locksView;

	protected IResource resource;
	protected LockResource rootLockResource;
	
	protected LockResourceSelectionComposite tableViewer;
	protected TreeViewer treeViewer;
	protected Text commentText;
	
	protected LockResourcesTreeLabelProvider labelProvider;
	
	private static final String PENDING_NAME = "pending";  //$NON-NLS-1$
	private static LockResource FAKE_PENDING = LockResource.createDirectory(LocksComposite.PENDING_NAME);
	
	private static final String NO_LOCKS_NAME = "nolocks";  //$NON-NLS-1$
	private static LockResource FAKE_NO_LOCKS = LockResource.createDirectory(LocksComposite.NO_LOCKS_NAME);
	
	public LocksComposite(Composite parent, LocksView locksView) {
		super(parent, SWT.NONE);
		this.locksView = locksView;
		this.isProcessing = false;
		
		this.createControls(parent);
	}
	
	private void createControls(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);
		
		SashForm outerSashForm = new SashForm(this, SWT.VERTICAL);
		outerSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		SashForm innerSashForm = new SashForm(outerSashForm, SWT.HORIZONTAL);
		innerSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
    	
    	this.createResourcesTree(innerSashForm);
    	this.createResourcesTable(innerSashForm);
    	innerSashForm.setWeights(new int[] {25, 75});
    	
    	this.createCommentComposite(outerSashForm);
    	outerSashForm.setWeights(new int[] {70, 30});
	}
		
	protected void createCommentComposite(Composite parent) {
		this.commentText = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.commentText.setBackground(this.commentText.getBackground());
		this.commentText.setEditable(false);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.commentText.setLayoutData(data);
	}
	
	protected void createResourcesTable(Composite parent) {
		this.tableViewer = new LockResourceSelectionComposite(parent, SWT.NONE, false, false);
		this.tableViewer.addResourcesSelectionChangedListener(new ILockResourceSelectionChangeListener() {			
			public void resourcesSelectionChanged(LockResourceSelectionChangedEvent event) {
				if (event.selection != null && !event.selection.isEmpty()) {
					LockResource lockResource = (LockResource) event.selection.getFirstElement();
					if (!LocksComposite.isFakeLockResource(lockResource)) {
						LocksComposite.this.commentText.setText(lockResource.getComment() == null || lockResource.getComment().length() == 0 ? SVNMessages.SVNInfo_NoComment : lockResource.getComment());	
					} else {
						LocksComposite.this.commentText.setText(""); //$NON-NLS-1$
					}					
				}
			}
		});
		
		this.tableViewer.setMenuManager(this.createResourcesTableMenu());
	}	
	
	protected MenuManager createResourcesTreeMenu() {
		MenuManager menuMgr = new MenuManager();		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {				
				//ignore fake resource
				IStructuredSelection tSelection = (IStructuredSelection) LocksComposite.this.treeViewer.getSelection();
				if (tSelection.size() == 1) {
					LockResource lockResource = (LockResource) tSelection.getFirstElement();
					if (LocksComposite.isFakeLockResource(lockResource)) {
						return;
					}
				}
				
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));															
																			
				Map<LockStatusEnum, List<LockResource>> resourcesMap = new HashMap<LockStatusEnum, List<LockResource>>();
				LockResource lockResource = (LockResource) tSelection.getFirstElement();
				LockResource[] children = lockResource.getAllChildFiles();
				for (LockResource child : children) {
					List<LockResource> resourcesList = resourcesMap.get(child.getLockStatus());
					if (resourcesList == null) {
						resourcesList = new ArrayList<LockResource>();
						resourcesMap.put(child.getLockStatus(), resourcesList);
					}
					resourcesList.add(child);
				}												
				
				manager.add(LocksComposite.this.createLockAction(resourcesMap));								
				manager.add(LocksComposite.this.createUnlockAction(resourcesMap));				
				manager.add(LocksComposite.this.createBreakLockAction(resourcesMap));											
			}			
		});
		menuMgr.setRemoveAllWhenShown(true);	
		return menuMgr;
	}
	
	protected MenuManager createResourcesTableMenu() {
		MenuManager menuMgr = new MenuManager();		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {				
				//ignore fake resource
				IStructuredSelection tSelection = (IStructuredSelection) LocksComposite.this.tableViewer.getTableViewer().getSelection();
				if (tSelection.size() == 1) {
					LockResource lockResource = (LockResource) tSelection.getFirstElement();
					if (LocksComposite.isFakeLockResource(lockResource)) {
						return;
					}
				}
				
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));															
																			
				Map<LockStatusEnum, List<LockResource>> resourcesMap = new HashMap<LockStatusEnum, List<LockResource>>();
				Iterator iter = tSelection.iterator();
				while (iter.hasNext()) {
					LockResource lockResource = (LockResource) iter.next();
					List<LockResource> resourcesList = resourcesMap.get(lockResource.getLockStatus());
					if (resourcesList == null) {
						resourcesList = new ArrayList<LockResource>();
						resourcesMap.put(lockResource.getLockStatus(), resourcesList);
					}
					resourcesList.add(lockResource);
				}
				
				manager.add(LocksComposite.this.createLockAction(resourcesMap));								
				manager.add(LocksComposite.this.createUnlockAction(resourcesMap));				
				manager.add(LocksComposite.this.createBreakLockAction(resourcesMap));											
			}			
		});
		menuMgr.setRemoveAllWhenShown(true);	
		return menuMgr;
	}
	
	protected ICancellableOperationWrapper runScheduled(IActionOperation operation) {
		return UIMonitorUtility.doTaskScheduled(LocksComposite.this.locksView, operation, new DefaultOperationWrapperFactory());
	}
	
	protected Action createLockAction(final Map<LockStatusEnum, List<LockResource>> resourcesMap) {		
		final List<LockResource> lockResources = new ArrayList<LockResource>();
		if (resourcesMap.containsKey(LockStatusEnum.BROKEN)) {
			lockResources.addAll(resourcesMap.get(LockStatusEnum.BROKEN));
		}
		if (resourcesMap.containsKey(LockStatusEnum.STOLEN)) {
			lockResources.addAll(resourcesMap.get(LockStatusEnum.STOLEN));
		}
		if (resourcesMap.containsKey(LockStatusEnum.OTHER_LOCKED)) {
			lockResources.addAll(resourcesMap.get(LockStatusEnum.OTHER_LOCKED));
		}
		if (lockResources != null && !lockResources.isEmpty()) {
			//handle if resource doesn't exist locally	
			Iterator<LockResource> iter = lockResources.iterator();
			while (iter.hasNext()) {
				LockResource lockResource = iter.next();
				IResource resource = (IResource) lockResource.getAdapter(IResource.class);
				if (!IStateFilter.SF_VERSIONED.accept(SVNRemoteStorage.instance().asLocalResource(resource))) {
					iter.remove();
				}				
			}
		}
		
		Action action = new Action(SVNUIMessages.LockAction_label) {
			public void run() {
				IActionOperation op = LocksComposite.performLockAction(lockResources.toArray(new LockResource[0]), true, LocksComposite.this.getShell());
				if (op != null) {
					LocksComposite.this.runScheduled(op);
				}
			}
		};
		action.setEnabled(!lockResources.isEmpty());
		action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif")); //$NON-NLS-1$
		return action;			
	}
	
	public static IActionOperation performBreakLockAction(LockResource[] lockResources, Shell shell, final LocksView locksView) {
		LockResourcesPanel panel = new LockResourcesPanel(lockResources, SVNUIMessages.LocksComposite_BreakLockTitle, SVNUIMessages.LocksComposite_BreakLockDescription, SVNUIMessages.LocksComposite_BreakLockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, panel);
		if (dlg.open() == 0) {			
			LockResource[] selectedResources = panel.getSelectedResources();
			IRepositoryResource[] reposResources = LocksView.convertToRepositoryResources(selectedResources);
			BreakLockOperation mainOp = new BreakLockOperation(reposResources);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(reposResources));
			op.add(new AbstractActionOperation("") { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					LocksView lv = locksView;
					if (lv == null) {
						lv = LocksView.instance();
					}
					if (lv != null) {
						lv.refreshView();
					}
				}
			});		
			return op;			
		}
		return null;
	}
	
	public static IActionOperation performUnlockAction(LockResource[] lockResources, Shell shell) {
		LockResourcesPanel unlockPanel = new LockResourcesPanel(lockResources, SVNUIMessages.LocksComposite_UnlockTitle, SVNUIMessages.LocksComposite_UnlockDescription, SVNUIMessages.LocksComposite_UnlockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, unlockPanel);
		if (dlg.open() == 0) {								
			LockResource[] selectedResources = unlockPanel.getSelectedResources();
			IResource[] resources = LocksView.convertToResources(selectedResources);
			UnlockOperation mainOp = new UnlockOperation(resources);							    
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			return op;							
		}		
		return null;
	}
	
	public static IActionOperation performLockAction(LockResource[] lockResources, boolean forceLock, Shell shell) {
		LockResourcesPanel panel = new LockResourcesPanel(lockResources, true, forceLock, SVNUIMessages.LocksComposite_LockTitle, SVNUIMessages.LocksComposite_LockDescription, SVNUIMessages.LocksComposite_LockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, panel);
		if (dlg.open() == 0) {			
			LockResource[] selectedResources = panel.getSelectedResources();
			IResource[] resources = LocksView.convertToResources(selectedResources);
							
			LockOperation mainOp = new LockOperation(resources, panel.getMessage(), panel.getForce());			    
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			return op;
		}
		return null;
	}
	
	protected Action createBreakLockAction(final Map<LockStatusEnum, List<LockResource>> resourcesMap) {
		Action action = new Action(SVNUIMessages.BreakLockAction_label2) {
			public void run() {
				List<LockResource> lockResources = new ArrayList<LockResource>();
				if (resourcesMap.containsKey(LockStatusEnum.OTHER_LOCKED)) {
					lockResources.addAll(resourcesMap.get(LockStatusEnum.OTHER_LOCKED));
				}
				if (resourcesMap.containsKey(LockStatusEnum.STOLEN)) {
					lockResources.addAll(resourcesMap.get(LockStatusEnum.STOLEN));
				}							
				if (lockResources != null && !lockResources.isEmpty()) {
					IActionOperation op = LocksComposite.performBreakLockAction(lockResources.toArray(new LockResource[0]), LocksComposite.this.getShell(), LocksComposite.this.locksView);
					if (op != null) {
						LocksComposite.this.runScheduled(op);
					}
				}
			}
		};
		action.setEnabled(resourcesMap.containsKey(LockStatusEnum.OTHER_LOCKED) || resourcesMap.containsKey(LockStatusEnum.STOLEN));
		return action;	
	}
	
	protected Action createUnlockAction(final Map<LockStatusEnum, List<LockResource>> resourcesMap) {
		Action action = new Action(SVNUIMessages.UnlockAction_label) {
			public void run() {
				List<LockResource> lockResources = resourcesMap.get(LockStatusEnum.LOCALLY_LOCKED);		
				if (lockResources != null && !lockResources.isEmpty()) {
					IActionOperation op = LocksComposite.performUnlockAction(lockResources.toArray(new LockResource[0]), LocksComposite.this.getShell());
					if (op != null) {
						LocksComposite.this.runScheduled(op);
					}																	
				}
			}
		};
		action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif")); //$NON-NLS-1$
		action.setEnabled(resourcesMap.containsKey(LockStatusEnum.LOCALLY_LOCKED));					
		return action;
	}
		
	protected void createResourcesTree(Composite parent) {
        this.treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        this.treeViewer.setContentProvider(new LockResourcesTreeContentProvider());
        this.treeViewer.setLabelProvider(this.labelProvider = new LockResourcesTreeLabelProvider());
        this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        	protected LockResource oldSelection;
        	
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection tSelection = (IStructuredSelection)event.getSelection();
				if (tSelection.size() > 0) {
					LockResource selection = (LockResource)tSelection.getFirstElement();
					if (this.oldSelection != selection) {						
						LocksComposite.this.tableViewer.setInput(selection.getAllChildFiles());
						this.oldSelection = selection;
					}
				}
				else {
					LocksComposite.this.tableViewer.setInput(null);					
				}
			}
        });
                
        MenuManager menuManager = this.createResourcesTreeMenu();
        Menu menu = menuManager.createContextMenu(this.treeViewer.getTree());
        this.treeViewer.getTree().setMenu(menu);
	}
	
	public void initializeComposite() {
		if (this.isProcessing) {
			this.treeViewer.setInput(null);
			this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_PENDING});
			this.tableViewer.getTableViewer().getTable().setLinesVisible(false);
			this.commentText.setText(""); //$NON-NLS-1$
		} else {
			((LockResourcesTreeContentProvider) this.treeViewer.getContentProvider()).initialize(this.rootLockResource);
			if (this.rootLockResource != null) {
				this.treeViewer.setInput(SVNUIMessages.LocksComposite_Root);
				this.treeViewer.expandAll();
				this.treeViewer.setSelection(new StructuredSelection(this.rootLockResource));
				((Tree)this.treeViewer.getControl()).showSelection();
				this.tableViewer.getTableViewer().getTable().setLinesVisible(true);
			} else {
				this.treeViewer.setInput(null);
				this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_NO_LOCKS});
				this.tableViewer.getTableViewer().getTable().setLinesVisible(false);
				this.commentText.setText(""); //$NON-NLS-1$
			}	
		}		
	}
	
	public void setPending(boolean isProcessing) {
		this.isProcessing = isProcessing;
	}
	
	public boolean isPending() {
		return this.isProcessing;
	}
	
	public synchronized void setResource(IResource resource) {
		this.resource = resource;
	}	
	
	public void setRootLockResource(LockResource rootLockResource) {
		this.rootLockResource = rootLockResource;		
	}
	
	public synchronized void disconnectComposite() {
		this.resource = null;
		this.rootLockResource = null;
	}
	
	public static boolean isFakeLockResource(LockResource lockResource) {
		return LocksComposite.isFakePending(lockResource) || LocksComposite.isFakeNoLocks(lockResource);
	}
	
	public static boolean isFakeNoLocks(LockResource lockResource) {
		return lockResource.getName().equals(LocksComposite.NO_LOCKS_NAME);
	}
	
	public static boolean isFakePending(LockResource lockResource) {
		return lockResource.getName().equals(LocksComposite.PENDING_NAME);
	}
	
}
