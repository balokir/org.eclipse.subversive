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
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.remote.BreakLockOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
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

	public final static int COLUMN_ICON = 0;
	public final static int COLUMN_NAME = 1;
	public final static int COLUMN_PATH = 2;
	public final static int COLUMN_STATE = 3;
	public final static int COLUMN_OWNER = 4;
	public final static int COLUMN_DATE = 5;
			
	protected boolean isProcessing;
	protected LocksView locksView;

	protected IResource resource;
	protected LockResource rootLockResource;
	
	protected TableViewer tableViewer;
	protected TreeViewer treeViewer;
	protected Text commentText;
	
	protected LockResourcesTreeLabelProvider labelProvider;
	
	private static final String PENDING_NAME = "pending";  //$NON-NLS-1$
	private static LockResource FAKE_PENDING = new LockResource(LocksComposite.PENDING_NAME);
	
	private static final String NO_LOCKS_NAME = "nolocks";  //$NON-NLS-1$
	private static LockResource FAKE_NO_LOCKS = new LockResource(LocksComposite.NO_LOCKS_NAME);
	
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
        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
        
        TableLayout layout = new TableLayout();
        table.setLayout(layout);                
        this.tableViewer = new TableViewer(table);
        
        LockResourcesTableComparator tableComparator = new LockResourcesTableComparator(this.tableViewer);
        
        ///0.image        
        TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(""); //$NON-NLS-1$
		col.setResizable(false);
		col.setAlignment(SWT.CENTER);
        layout.addColumnData(new ColumnPixelData(20, false));
        
        //1.name
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNUIMessages.LocksComposite_Name);
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(20, true));
        
        //2.path
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNUIMessages.LocksComposite_Path);
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(30, true));
        
        //3.state
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNUIMessages.LocksComposite_State);
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(20, true));
        
        //4.owner
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNUIMessages.LocksComposite_Owner);
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(20, true));
        
        //5.date
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNUIMessages.LocksComposite_CreationDate);
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(10, true));
        
        tableComparator.setReversed(false);
        tableComparator.setColumnNumber(LocksComposite.COLUMN_PATH);
        this.tableViewer.setComparator(tableComparator);
        this.tableViewer.getTable().setSortColumn(this.tableViewer.getTable().getColumn(LocksComposite.COLUMN_PATH));
        this.tableViewer.getTable().setSortDirection(SWT.UP);
        
        this.tableViewer.setContentProvider(new ArrayStructuredContentProvider());
		this.tableViewer.setLabelProvider(new LockResourcesTableLabelProvider());
		
		this.createResourcesTableMenu(table);
		
		this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {				
				IStructuredSelection tSelection = (IStructuredSelection) event.getSelection();
				if (tSelection.size() > 0) {
					LockResource lockResource = (LockResource) tSelection.getFirstElement();
					if (!LocksComposite.isFakeLockResource(lockResource)) {
						LocksComposite.this.commentText.setText(lockResource.getComment() == null || lockResource.getComment().length() == 0 ? SVNMessages.SVNInfo_NoComment : lockResource.getComment());	
					} else {
						LocksComposite.this.commentText.setText(""); //$NON-NLS-1$
					}					
				}
			}
		});
	}	
	
	protected void createResourcesTableMenu(Table table) {
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(table);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				synchronized (LocksComposite.this) {
					//ignore fake resource
					IStructuredSelection tSelection = (IStructuredSelection) LocksComposite.this.tableViewer.getSelection();
					if (tSelection.size() == 1) {
						LockResource lockResource = (LockResource) tSelection.getFirstElement();
						if (LocksComposite.isFakeLockResource(lockResource)) {
							return;
						}
					}
					
					manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));															
																				
					final Map<LockStatusEnum, List<LockResource>> resourcesMap = new HashMap<LockStatusEnum, List<LockResource>>();
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
					
					//unlock action
					Action tAction = null;
					manager.add(tAction = new Action(SVNUIMessages.UpdateActionGroup_Unlock) {
						public void run() {
							List<LockResource> lockResources = resourcesMap.get(LockStatusEnum.LOCALLY_LOCKED);
							List<IResource> resources = new ArrayList<IResource>();
							for (LockResource lockResource : lockResources) {
								IResource resource = (IResource) lockResource.getAdapter(IResource.class);
								if (resource != null) {
									resources.add(resource);
								} 
							}
							if (!resources.isEmpty()) {
								IResource[] resourcesArray = resources.toArray(new IResource[0]);
								UnlockOperation mainOp = new UnlockOperation(resourcesArray);							    
								CompositeOperation op = new CompositeOperation(mainOp.getId());
								op.add(mainOp);
								op.add(new RefreshResourcesOperation(resourcesArray));
								runScheduled(op);
							}
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));					 //$NON-NLS-1$
					tAction.setEnabled(resourcesMap.containsKey(LockStatusEnum.LOCALLY_LOCKED));					
					
					//break lock action
					manager.add(tAction = new Action(SVNUIMessages.BreakLockAction_label) {
						public void run() {
							List<LockResource> lockResources = new ArrayList<LockResource>();
							if (resourcesMap.containsKey(LockStatusEnum.OTHER_LOCKED)) {
								lockResources.addAll(resourcesMap.get(LockStatusEnum.OTHER_LOCKED));
							}
							if (resourcesMap.containsKey(LockStatusEnum.STOLEN)) {
								lockResources.addAll(resourcesMap.get(LockStatusEnum.STOLEN));
							}																						
							List<IRepositoryResource> resources = new ArrayList<IRepositoryResource>();
							for (LockResource lockResource : lockResources) {
								IRepositoryResource resource = (IRepositoryResource) lockResource.getAdapter(IRepositoryResource.class);
								if (resource != null) {
									resources.add(resource);
								} 
							}
							if (!resources.isEmpty()) {
								IRepositoryResource[] resourcesArray = resources.toArray(new IRepositoryResource[0]);								
								BreakLockOperation mainOp = new BreakLockOperation(resourcesArray);
								CompositeOperation op = new CompositeOperation(mainOp.getId());
								op.add(mainOp);
								op.add(new RefreshRemoteResourcesOperation(resourcesArray));
								op.add(new AbstractActionOperation("") {									 //$NON-NLS-1$
									protected void runImpl(IProgressMonitor monitor) throws Exception {
										LocksComposite.this.locksView.refreshView();
									}
								});								
								runScheduled(op);	
							}
						}
					});
					tAction.setEnabled(resourcesMap.containsKey(LockStatusEnum.OTHER_LOCKED) || resourcesMap.containsKey(LockStatusEnum.STOLEN));
				}
			}
			
			protected ICancellableOperationWrapper runScheduled(IActionOperation operation) {
				return UIMonitorUtility.doTaskScheduled(LocksComposite.this.locksView, operation, new DefaultOperationWrapperFactory());
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		table.setMenu(menu);
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
												
						Table table = LocksComposite.this.tableViewer.getTable();
						TableItem firstItem = table.getItem(0);
						
						//TODO it doesn't work when change selection oustide, e.g. from Package Explorer
						table.setSelection(firstItem);
						((Table)LocksComposite.this.tableViewer.getControl()).showSelection();
						
						this.oldSelection = selection;
					}
				}
				else {
					LocksComposite.this.tableViewer.setInput(null);					
				}
			}
        });
	}
	
	public void initializeComposite() {
		if (this.isProcessing) {
			this.treeViewer.setInput(null);
			this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_PENDING});
			this.tableViewer.getTable().setLinesVisible(false);
			this.commentText.setText(""); //$NON-NLS-1$
		} else {
			((LockResourcesTreeContentProvider) this.treeViewer.getContentProvider()).initialize(this.rootLockResource);
			if (this.rootLockResource != null) {
				this.treeViewer.setInput(SVNUIMessages.LocksComposite_Root);
				this.treeViewer.expandAll();
				this.treeViewer.setSelection(new StructuredSelection(this.rootLockResource));
				((Tree)this.treeViewer.getControl()).showSelection();
				this.tableViewer.getTable().setLinesVisible(true);
			} else {
				this.treeViewer.setInput(null);
				this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_NO_LOCKS});
				this.tableViewer.getTable().setLinesVisible(false);
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
