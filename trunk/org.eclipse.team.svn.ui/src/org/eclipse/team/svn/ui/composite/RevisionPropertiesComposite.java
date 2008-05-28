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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.properties.RevPropertiesEditPanel;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Composite for viewing revision properties
 * 
 * @author Alexei Goncharov
 */
public class RevisionPropertiesComposite extends Composite {

	protected static final int COLUMN_NAME = 0;
	protected static final int COLUMN_VALUE = 1;
	
	protected SVNProperty[] properties;
	protected IRepositoryLocation location;
	protected SVNRevision revision; 
	protected TableViewer propertyViewer;
	protected Text propertyText;
	
	protected boolean isPending;
	
	public RevisionPropertiesComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.properties = new SVNProperty[0];
		this.createControls(parent);
	}
	
	public void setPending(final boolean isPending) {
		this.isPending = isPending;
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (isPending) {
					RevisionPropertiesComposite.this.propertyViewer.getTable().setLinesVisible(false);
				}
				else {
					RevisionPropertiesComposite.this.propertyViewer.getTable().setLinesVisible(true);
				}
				RevisionPropertiesComposite.this.rereshTableData();
			}
		});
	}
	
	public synchronized void disconnectComposite(){
		this.properties = new SVNProperty [0];
	}
	
	public void setInput(SVNProperty [] revProperties) {
		this.properties = revProperties;
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				RevisionPropertiesComposite.this.rereshTableData();
			}
		});
	}
	
	public SVNProperty [] getSetProps() {
		return this.properties;
	}
	
	public void setLocationAndRevision (IRepositoryLocation location, SVNRevision revision) {
		this.location = location;
		this.revision = revision;
	}
	
	protected void rereshTableData() {
		if (this.isPending) {
			this.propertyViewer.setInput(new String [] {""});
			return;
		}
		this.propertyViewer.setInput(RevisionPropertiesComposite.this.properties);
		this.propertyText.setText("");
	}
	
	protected void createControls(Composite parent) {
		GridLayout layout = null;
	    GridData data = null;
	        
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
		SashForm innerSashForm = new SashForm(this, SWT.VERTICAL);
		
		data = new GridData(GridData.FILL_BOTH);
		innerSashForm.setLayoutData(data);
		
		Table table = new Table(innerSashForm, SWT.H_SCROLL | SWT.V_SCROLL	| SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		table.setLayoutData(data);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		this.propertyText = new Text(innerSashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.propertyText.setBackground(this.propertyText.getBackground());
		this.propertyText.setEditable(false);
		data = new GridData(GridData.FILL_BOTH);
		this.propertyText.setLayoutData(data);

		innerSashForm.setWeights(new int[] {70, 30});
		
		this.propertyViewer = new TableViewer(table);
		this.propertyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (selection != null && selection instanceof SVNProperty) {
						RevisionPropertiesComposite.this.propertyText.setText(((SVNProperty)selection).value);
					}
				}	
			}
		});
		
		//creating a comparator right now to get column listeners
		ColumnedViewerComparator comparator = new ColumnedViewerComparator(this.propertyViewer) {

			public int compareImpl(Viewer viewer, Object row1, Object row2) {
				if (row1 instanceof SVNProperty) {
					SVNProperty data1 = (SVNProperty) row1;
					SVNProperty data2 = (SVNProperty) row2;
					return
						this.column == RevisionPropertiesComposite.COLUMN_NAME ?
						ColumnedViewerComparator.compare(data1.name, data2.name) :
						ColumnedViewerComparator.compare(data1.value, data2.value);
				}
				return 0;
			}
			
		};
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Name"));
		col.addSelectionListener(comparator);
		tableLayout.addColumnData(new ColumnWeightData(30, true));
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Value"));
		col.addSelectionListener(comparator);
		tableLayout.addColumnData(new ColumnWeightData(70, true));
		
		//adding a comparator and selecting a default sort column
		this.propertyViewer.setComparator(comparator);
		comparator.setColumnNumber(RevisionPropertiesComposite.COLUMN_NAME);
		this.propertyViewer.getTable().setSortColumn(this.propertyViewer.getTable().getColumn(RevisionPropertiesComposite.COLUMN_NAME));
		this.propertyViewer.getTable().setSortDirection(SWT.UP);

		this.propertyViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (RevisionPropertiesComposite.this.isPending) {
					return (Object [])inputElement;
				}
				return RevisionPropertiesComposite.this.properties;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		this.propertyViewer.setLabelProvider(new ITableLabelProvider() {
			
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				if (RevisionPropertiesComposite.this.isPending) {
					if (columnIndex == RevisionPropertiesComposite.COLUMN_NAME) {
						return SVNTeamUIPlugin.instance().getResource(RepositoryPending.PENDING);
					}
					return "";
				}
				SVNProperty data = (SVNProperty) element;
				if (columnIndex == 0) {
					return data.name;
				}
				return FileUtility.formatMultilineText(data.value);
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
		});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(table);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection tSelection = (IStructuredSelection) RevisionPropertiesComposite.this.propertyViewer.getSelection();
				if (tSelection.size() == 1 && tSelection.getFirstElement() instanceof String) {
					return;
				}
				Action tAction = null;
				boolean isItitialized = RevisionPropertiesComposite.this.properties != null;
				if (isItitialized) {
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Add")) {
						public void run() {
							RevisionPropertiesComposite.this.editProperty(null);
						}
					});
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Edit")) {
						public void run() {
							SVNProperty data = (SVNProperty) tSelection.getFirstElement();
							RevisionPropertiesComposite.this.editProperty(data);
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		table.setMenu(menu);
		
		this.propertyViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection) e.getSelection();
				if (selection.size() == 1) {
					SVNProperty data = (SVNProperty) selection.getFirstElement();
					RevisionPropertiesComposite.this.editProperty(data);
				}
			}
		});
		
		
	}
	
	protected void editProperty(SVNProperty data) {
		RevPropertiesEditPanel panel = new RevPropertiesEditPanel(this.properties, this.revision);
		panel.setPropertyToEdit(data);
		if (new DefaultDialog(UIMonitorUtility.getShell(), panel).open() == 0) {
			RevPropertiesEditPanel.doSetRevisionProperty(panel, this.location, this.revision);
		}
	}

}
