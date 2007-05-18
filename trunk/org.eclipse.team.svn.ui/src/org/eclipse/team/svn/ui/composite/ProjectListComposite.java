/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnPixelData;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Projects list composite
 *
 * @author Elena Matokhina
 */
public class ProjectListComposite extends Composite {
	
	protected IProject []resources;
	protected TableViewer tableViewer;
	protected Composite parent;
	protected boolean remoteMode;
	
	public ProjectListComposite(Composite parent, int style, IProject []resources, boolean remoteMode) {
		super(parent, style);
		this.resources = resources;
		this.remoteMode = remoteMode;
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
		col.setText(SVNTeamUIPlugin.instance().getResource("ProjectListComposite.ProjectName"));
		layout.addColumnData(new ColumnWeightData(30, true));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource(this.remoteMode ? "ProjectListComposite.RepositoryLabel" : "ProjectListComposite.RepositoryURL"));
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
					Image image = (Image) SWTResourceUtil.getImageTable().get(descriptor);
					if (image == null) {
						image = descriptor.createImage();
						SWTResourceUtil.getImageTable().put(descriptor, image);
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
		
		this.tableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public Object[] getElements(Object inputElement) {
				return ProjectListComposite.this.resources;
			}
		});
		
		this.tableViewer.setInput(this.resources);

	}


}
