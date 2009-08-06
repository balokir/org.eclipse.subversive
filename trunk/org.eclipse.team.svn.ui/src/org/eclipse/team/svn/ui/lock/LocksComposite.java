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

import org.eclipse.core.resources.IResource;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;

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
	
	protected SashForm sashForm;
	protected TableViewer tableViewer;
	protected TreeViewer treeViewer;
	protected LockResourcesTreeLabelProvider labelProvider;
	
	public static final String PENDING_NAME = "pending";  //$NON-NLS-1$
	public static LockResource FAKE_PENDING = new LockResource(LocksComposite.PENDING_NAME);
	
	public static final String NO_LOCKS_NAME = "nolocks";  //$NON-NLS-1$
	public static LockResource FAKE_NO_LOCKS = new LockResource(LocksComposite.NO_LOCKS_NAME);
	
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
		
		this.sashForm = new SashForm(this, SWT.HORIZONTAL);
    	this.sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.treeViewer = new TreeViewer(this.sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
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
        
        Table table = new Table(this.sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
        
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
                
        this.tableViewer = new TableViewer(table);
        this.sashForm.setWeights(new int[] {25, 75});
        
        LockResourcesTableComparator tableComparator = new LockResourcesTableComparator(this.tableViewer);
        
        ///0.image        
        TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(""); //$NON-NLS-1$
		col.setResizable(false);
		col.setAlignment(SWT.CENTER);
        layout.addColumnData(new ColumnPixelData(26, false));
        
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
	}

	public void initializeComposite() {
		if (this.isProcessing) {
			this.treeViewer.setInput(null);
			this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_PENDING});
		} else {
			((LockResourcesTreeContentProvider) this.treeViewer.getContentProvider()).initialize(this.rootLockResource);
			if (this.rootLockResource != null) {
				this.treeViewer.setInput(SVNUIMessages.LocksComposite_Root);
				this.treeViewer.expandAll();
				this.treeViewer.setSelection(new StructuredSelection(this.rootLockResource));
				((Tree)this.treeViewer.getControl()).showSelection();	
			} else {
				this.treeViewer.setInput(null);
				this.tableViewer.setInput(new LockResource[]{LocksComposite.FAKE_NO_LOCKS});
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
	
}
