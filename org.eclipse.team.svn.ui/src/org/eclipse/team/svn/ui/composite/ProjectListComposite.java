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

package org.eclipse.team.svn.ui.composite;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Projects list composite
 *
 * @author Sergiy Logvin
 */
public class ProjectListComposite extends Composite {
	
	protected IProject []resources;
	protected TableViewer tableViewer;
	protected Composite parent;
	protected boolean remoteMode;
	protected Map<ImageDescriptor, Image> images;
	
	public ProjectListComposite(Composite parent, int style, IProject []resources, boolean remoteMode) {
		super(parent, style);
		this.resources = resources;
		this.remoteMode = remoteMode;
		this.images = new HashMap<ImageDescriptor, Image>();
	}
	
	public void dispose() {
    	for (Image img : this.images.values()) {
    		img.dispose();
    	}
		super.dispose();
	}
	
	public void initialize() {
		GridLayout gLayout = new GridLayout();
		gLayout.marginHeight = gLayout.marginWidth = 0;
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 150;
		this.setLayout(gLayout);
		this.setLayoutData(data);
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		this.tableViewer = new TableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		this.tableViewer.getTable().setLayoutData(data);
		this.tableViewer.getTable().setHeaderVisible(true);
		this.tableViewer.getTable().setLinesVisible(true);

		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		layout.addColumnData(new ColumnPixelData(20, false));

		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ProjectListComposite_ProjectName);
		layout.addColumnData(new ColumnWeightData(30, true));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(this.remoteMode ? SVNUIMessages.ProjectListComposite_RepositoryLabel : SVNUIMessages.ProjectListComposite_RepositoryURL);
		layout.addColumnData(new ColumnWeightData(70, true));
		
		this.tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
					if (adapter == null) {
						return null;
					}
					ImageDescriptor descriptor = adapter.getImageDescriptor(element);
					if (descriptor == null) {
						return null;
					}
					Image image = ProjectListComposite.this.images.get(descriptor);
					if (image == null) {
						image = descriptor.createImage();
						ProjectListComposite.this.images.put(descriptor, image);
					}
					return image;
				}
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				IResource resource = (IResource)element;
				if (columnIndex == 1) {
					return resource.getName();
				}
				if (columnIndex == 2) {
	                IRemoteStorage storage = SVNRemoteStorage.instance();
	                if (ProjectListComposite.this.remoteMode) {
						return storage.getRepositoryLocation(resource).getLabel();
	                }
					return storage.asRepositoryResource(resource).getUrl();
				}
				return "";
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
		
		this.tableViewer.setContentProvider(new ArrayStructuredContentProvider());
		
		this.tableViewer.setInput(this.resources);

	}


}
