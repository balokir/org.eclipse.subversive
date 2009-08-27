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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;

/**
 * Lock resource selection composite
 * 
 * Shows resources for lock operations: break lock, unlock, lock/steal lock
 * 
 * @author Igor Burilo
 */
public class LockResourceSelectionComposite extends Composite {
	
	public final static int COLUMN_NAME = 1;
	public final static int COLUMN_PATH = 2;
	public final static int COLUMN_STATE = 3;
	public final static int COLUMN_OWNER = 4;
	public final static int COLUMN_DATE = 5;
	
	protected LockResource[] resources;
	protected boolean showCheckBoxesAndButtons;
	protected boolean hasBorder;
	
	protected CheckboxTableViewer tableViewer;	
	protected Label lblSelectedResourcesNumber;

	protected ISelectionChangedListener selectionListener;
	protected List<ILockResourceSelectionChangeListener> selectionChangedListeners;
	
	protected LockResource[] selectedResources;
	protected LockResource[] notSelectedResources;

	public static interface ILockResourceSelectionChangeListener {
		public void resourcesSelectionChanged(LockResourceSelectionChangedEvent event);
	}
	
	public static class LockResourceSelectionChangedEvent {
		public final LockResource[] checkedResources;
		public final IStructuredSelection selection;
		
		public LockResourceSelectionChangedEvent(LockResource []resources, ISelection selection) {
			this.checkedResources = resources;
			this.selection = (IStructuredSelection) selection;
		}
	}
		
	public LockResourceSelectionComposite(Composite parent, int style, boolean hasBorder, boolean showCheckBoxesAndButtons) {
		super(parent, style);
		this.showCheckBoxesAndButtons = showCheckBoxesAndButtons;
		this.hasBorder = hasBorder;
		
		this.resources = new LockResource[0];
		this.selectedResources = new LockResource[0];
		this.notSelectedResources = new LockResource[0];
		
		this.selectionChangedListeners = new ArrayList<ILockResourceSelectionChangeListener>();
		
		this.createControls();
	}
	
	protected void createControls() {
		GridLayout gridLayout = null;
		GridData data = null;

		gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);

		int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI;
		if (this.hasBorder) {
			style |= SWT.BORDER;
		}
		Table table = new Table(this, this.showCheckBoxesAndButtons ? style | SWT.CHECK : style);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		this.tableViewer = new CheckboxTableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		this.tableViewer.getTable().setLayoutData(data);
		
		LockResourcesTableComparator tableComparator = new LockResourcesTableComparator(this.tableViewer);		
		
		if (this.showCheckBoxesAndButtons) {
			//0.checkbox
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setResizable(false);
			layout.addColumnData(new ColumnPixelData(20, false));	
		} else {
			//0.image
	        TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(""); //$NON-NLS-1$
			col.setResizable(false);
			col.setAlignment(SWT.CENTER);
	        layout.addColumnData(new ColumnPixelData(20, false));
		}		
        
        //1.name
		TableColumn col = new TableColumn(table, SWT.NONE);
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
        tableComparator.setColumnNumber(LockResourceSelectionComposite.COLUMN_PATH);
        this.tableViewer.setComparator(tableComparator);
        this.tableViewer.getTable().setSortColumn(this.tableViewer.getTable().getColumn(LockResourceSelectionComposite.COLUMN_PATH));
        this.tableViewer.getTable().setSortDirection(SWT.UP);
        
        this.tableViewer.setContentProvider(new ArrayStructuredContentProvider());
		this.tableViewer.setLabelProvider(new LockResourcesTableLabelProvider(this.showCheckBoxesAndButtons));		
		
		this.tableViewer.addSelectionChangedListener(this.selectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {											
				if (LockResourceSelectionComposite.this.showCheckBoxesAndButtons) {
					LockResourceSelectionComposite.this.updateSelectedResources();												
					int selectedNumber = LockResourceSelectionComposite.this.selectedResources.length;
					LockResourceSelectionComposite.this.lblSelectedResourcesNumber.setText(LockResourceSelectionComposite.this.resourceNumberToString(selectedNumber));
				}
				LockResourceSelectionComposite.this.fireResourcesSelectionChanged(new LockResourceSelectionChangedEvent(LockResourceSelectionComposite.this.selectedResources, event != null ? event.getSelection() : null));
			}
		});
		
		if (!this.showCheckBoxesAndButtons) {
			return;
		}					
						
		Composite tComposite = new Composite(this, SWT.RIGHT);
		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 3;
		gLayout.marginWidth = 0;
		tComposite.setLayout(gLayout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tComposite.setLayoutData(data);

		Button selectButton = new Button(tComposite, SWT.PUSH);
		selectButton.setText(SVNUIMessages.Button_SelectAll);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(selectButton);
		selectButton.setLayoutData(data);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				LockResourceSelectionComposite.this.tableViewer.setAllChecked(true);
				Object[] elements = LockResourceSelectionComposite.this.tableViewer.getCheckedElements();
				LockResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				LockResourceSelectionComposite.this.fireResourcesSelectionChanged(new LockResourceSelectionChangedEvent(Arrays.asList(elements).toArray(new LockResource[elements.length]), null));
			}
		};
		selectButton.addSelectionListener(listener);

		Button deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNUIMessages.Button_ClearSelection);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(deselectButton);
		deselectButton.setLayoutData(data);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				LockResourceSelectionComposite.this.tableViewer.setAllChecked(false);
				LockResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				LockResourceSelectionComposite.this.fireResourcesSelectionChanged(new LockResourceSelectionChangedEvent(new LockResource[0], null));
			}
		};
		deselectButton.addSelectionListener(listener);

		Composite lComposite = new Composite(tComposite, SWT.NONE);
		GridLayout lLayout = new GridLayout();
		lLayout.horizontalSpacing = 0;
		lLayout.marginWidth = 0;
		lComposite.setLayout(lLayout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		lComposite.setLayoutData(data);

		this.lblSelectedResourcesNumber = new Label(lComposite, SWT.RIGHT);
		this.lblSelectedResourcesNumber.setText(this.resourceNumberToString(this.selectedResources.length));
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		this.lblSelectedResourcesNumber.setLayoutData(data);
	}
	
	protected String resourceNumberToString(int value) {
		return SVNUIMessages.format(SVNUIMessages.ResourceSelectionComposite_Info, new String[] { String.valueOf(value), String.valueOf(this.resources.length) });
	}
	
	public void setInput(LockResource[] resources) {		
		this.selectedResources = this.resources = resources;		
		this.tableViewer.setInput(resources);
		if (this.resources != null) {
			for (int i = 0; i < this.resources.length; i++) {
				this.tableViewer.setChecked(this.resources[i], true);
			}
			
			if (this.showCheckBoxesAndButtons) {
				this.lblSelectedResourcesNumber.setText(this.resourceNumberToString(this.selectedResources.length));	
			}	
		}
		
		//TODO select first row in View
//		if (!this.showCheckBoxesAndButtons) {
//			Table table = this.tableViewer.getTable();
//			TableItem firstItem = table.getItem(0);
//			
//			//TODO it doesn't work when change selection outside, e.g. from Package Explorer
//			table.setSelection(firstItem);
//			((Table)this.tableViewer.getControl()).showSelection();	
//		}
		
		if (this.showCheckBoxesAndButtons) {
			this.updateSelectedResources();
			this.selectionListener.selectionChanged(null);	
		}		
		this.tableViewer.refresh();
	}
	
	protected void updateSelectedResources() {
		TableItem[] items = this.tableViewer.getTable().getItems();
		List<LockResource> checked = new ArrayList<LockResource>(items.length);
		List<LockResource> unchecked = new ArrayList<LockResource>();
		for (int i = 0; i < items.length; i++) {
			(items[i].getChecked() ? checked : unchecked).add((LockResource)items[i].getData());
		}
		this.selectedResources = checked.toArray(new LockResource[checked.size()]);
		this.notSelectedResources = unchecked.toArray(new LockResource[unchecked.size()]);
	}
	
	public TableViewer getTableViewer() {
		return this.tableViewer;
	}
	
	public LockResource[] getSelectedResources() {
		return this.selectedResources;
	}

	public LockResource[] getNotSelectedResources() {
		return this.notSelectedResources;
	}

	public void addResourcesSelectionChangedListener(ILockResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	public void removeResourcesSelectionChangedListener(ILockResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.remove(listener);
	}

	public void fireResourcesSelectionChanged(LockResourceSelectionChangedEvent event) {
		ILockResourceSelectionChangeListener[] listeners = (ILockResourceSelectionChangeListener[]) this.selectionChangedListeners
				.toArray(new ILockResourceSelectionChangeListener[this.selectionChangedListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resourcesSelectionChanged(event);
		}
	}
	
	public void setMenuManager(MenuManager menuMgr) {
		Menu menu = menuMgr.createContextMenu(this.tableViewer.getTable());		
		this.tableViewer.getTable().setMenu(menu);
	} 	
}
