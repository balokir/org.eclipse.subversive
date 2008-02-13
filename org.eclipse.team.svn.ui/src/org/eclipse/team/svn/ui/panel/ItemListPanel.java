/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Item List Panel implementation
 * 
 * @author Sergiy Logvin
 */
public class ItemListPanel extends AbstractDialogPanel {
	protected String []items; 
	protected Image itemIcon;
	protected Table table;

	public ItemListPanel(String []items, ImageDescriptor imageDescriptor, String dialogTitle, String dialogDescription, String defaultMessage) {
		this(items, imageDescriptor, dialogTitle, dialogDescription, defaultMessage, new String[] {IDialogConstants.OK_LABEL});
	}	
	
	public ItemListPanel(String []items, ImageDescriptor imageDescriptor, String dialogTitle, String dialogDescription, String defaultMessage, String[] buttons) {
		super(buttons);
		this.items = items;
		this.dialogTitle = dialogTitle;
		this.dialogDescription = dialogDescription;
		this.defaultMessage = defaultMessage;
		this.itemIcon = imageDescriptor.createImage();
	}	
	
	protected void saveChangesImpl() {
	}

    protected void cancelChangesImpl() {
    }
    
	public void createControlsImpl(Composite parent) {
		this.table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		this.table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		this.table.setLayout(layout);
	    TableViewer viewer = new TableViewer(this.table);
	    
	   	TableColumn col = new TableColumn(this.table, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.CENTER);
        layout.addColumnData(new ColumnWeightData(0, 60, true));        
               
        viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ItemListPanel.this.items;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		ITableLabelProvider labelProvider = new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return ItemListPanel.this.itemIcon;
			}
			public String getColumnText(Object element, int columnIndex) {
				return element.toString();
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return true;
			}

			public void removeListener(ILabelProviderListener listener) {
			}		
		};
		viewer.setLabelProvider(labelProvider);	
		viewer.setInput("");
	}

}
