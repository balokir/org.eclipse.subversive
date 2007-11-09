/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Resource List Panel implementation
 * 
 * @author Vladimir Bykov
 */
public class ResourceListPanel extends AbstractDialogPanel {
	protected IResource []resources;
	protected TableViewer tableViewer;
	protected boolean showLocalNames;
	
	public ResourceListPanel(IResource []resources, String dialogTitle, String dialogDescription, String defaultMessage, String[] buttons) {
		super(buttons);
		this.dialogTitle = dialogTitle;
		this.dialogDescription = dialogDescription;
		this.defaultMessage = defaultMessage;
		this.resources = resources;
	}
    
	public boolean isShowLocalNames() {
		return this.showLocalNames;
	}

	public void setShowLocalNames(boolean showLocalNames) {
		this.showLocalNames = showLocalNames;
	}
	
    public void createControls(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		this.tableViewer = new TableViewer(table);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 120;
		this.tableViewer.getTable().setLayoutData(data);

		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		layout.addColumnData(new ColumnWeightData(90, true));
		this.tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
				if (adapter == null) {
					return null;
				}
				ImageDescriptor descriptor = adapter.getImageDescriptor(element);
				if (descriptor == null) {
					return null;
				}
				Image image = (Image) SWTResourceUtil.getImageTable().get(descriptor);
				if (image == null) {
					image = descriptor.createImage();
					SWTResourceUtil.getImageTable().put(descriptor, image);
				}
				return image;
			}

			public String getColumnText(Object element, int columnIndex) {
				IResource resource = (IResource)element;
				if (ResourceListPanel.this.showLocalNames) {
					return resource.getFullPath().toString().substring(1);
				}
				else {
					IRepositoryResource node = SVNRemoteStorage.instance().asRepositoryResource(resource);
					return node.getUrl();
				}
			}

			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		this.tableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public Object[] getElements(Object inputElement) {
				return ResourceListPanel.this.resources;
			}
		});
		
		this.tableViewer.setInput(this.resources);
    }
    
    protected void saveChanges() {

    }

    protected void cancelChanges() {

    }

}
