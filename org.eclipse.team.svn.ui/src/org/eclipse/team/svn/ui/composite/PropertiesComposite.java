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

import java.io.FileOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.property.RemovePropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.SetKeywordsAction;
import org.eclipse.team.svn.ui.action.local.SetPropertyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.SetPropertyWithOverrideDialog;
import org.eclipse.team.svn.ui.panel.view.property.PropertyApplyPanel;
import org.eclipse.team.svn.ui.properties.ResourcePropertyEditPanel;
import org.eclipse.team.svn.ui.properties.RemovePropertyDialog;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * Property viewer composite 
 * 
 * @author Sergiy Logvin
 */
public class PropertiesComposite extends Composite {
	public static final int APPLY_TO_ALL = 0;
	public static final int APPLY_TO_FILES = 1;
	public static final int APPLY_TO_FOLDERS = 2;
	
	protected static final int COLUMN_NAME = 0;
	protected static final int COLUMN_VALUE = 1;
	
	protected SVNProperty[] properties;
	protected TableViewer propertyViewer;
	protected Text propertyText;
	protected boolean isProcessing;
	
	protected IResourcePropertyProvider provider;
	
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;
	protected IAdaptable resource;
	
	protected ViewPart workbenchPart;
	
	public PropertiesComposite(Composite parent,  ViewPart workbenchPart) {
		super(parent, SWT.NONE);
		this.isProcessing = false;
		this.workbenchPart = workbenchPart; 
		this.createControls(parent);
	}
	
	public PropertiesComposite(Composite parent) {
		this(parent, null);
	}
	
	public synchronized void setResource(IAdaptable resource, IResourcePropertyProvider provider) {
		if (resource instanceof IResource) {
			this.repositoryResource = null;
			this.wcResource = (IResource)resource;
		}
		else if (resource instanceof IRepositoryResource){
			this.repositoryResource = (IRepositoryResource)resource;
		}
		this.provider = provider;
	}
	
	public IActionOperation getRefreshViewOperation() {
		return new AbstractActionOperation("Operation.PShowProperties") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				synchronized (PropertiesComposite.this) {
					if (PropertiesComposite.this.provider != null && PropertiesComposite.this.provider.getExecutionState() == IActionOperation.OK) {
						if (PropertiesComposite.this.resource != null && 
								!(PropertiesComposite.this.resource.equals(PropertiesComposite.this.provider.getLocal())
									|| PropertiesComposite.this.resource.equals(PropertiesComposite.this.provider.getRemote()))) {
							//do nothing if by this moment resource selected in Properties Composite is different 
							//from the resource for which the Get(Remote)PropertiesOperation was executed
							return;
						}
						PropertiesComposite.this.provider.refresh();
						PropertiesComposite.this.properties = PropertiesComposite.this.provider.getProperties();
						if (PropertiesComposite.this.properties == null) {
							PropertiesComposite.this.properties = new SVNProperty[0];
						}
					}
					else {
						PropertiesComposite.this.properties = null;
					}
				}
				if (!PropertiesComposite.this.isDisposed()) {
					PropertiesComposite.this.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (PropertiesComposite.this.properties != null) {
								PropertiesComposite.this.setPending(false);
							}
							PropertiesComposite.this.initializeComposite();
						}
					});
				}
			}
		};
	}
	
	private void createControls(Composite parent) {
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
						PropertiesComposite.this.propertyText.setText(((SVNProperty)selection).value);
					}
				}	
			}
		});
		
		//creating a comparator right now to get column listeners
		PropertiesTableComparator comparator = new PropertiesTableComparator(this.propertyViewer);
		
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
		comparator.setColumnNumber(PropertiesComposite.COLUMN_NAME);
		this.propertyViewer.getTable().setSortColumn(this.propertyViewer.getTable().getColumn(PropertiesComposite.COLUMN_NAME));
		this.propertyViewer.getTable().setSortDirection(SWT.UP);

		this.propertyViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (PropertiesComposite.this.isProcessing) {
					return (Object [])inputElement;
				}
				if (PropertiesComposite.this.wcResource == null && PropertiesComposite.this.repositoryResource == null) {
					return new SVNProperty[0];
				}
				return PropertiesComposite.this.properties;
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
				if (PropertiesComposite.this.isProcessing) {
					if (columnIndex == 0 && PropertiesComposite.this.wcResource != null) {
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
				synchronized (PropertiesComposite.this) {
					manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					final IStructuredSelection tSelection = (IStructuredSelection) PropertiesComposite.this.propertyViewer.getSelection();
					if (tSelection.size() == 1 && tSelection.getFirstElement() instanceof String) {
						return;
					}
					Action tAction = null;
					boolean isEditAllowed = PropertiesComposite.this.provider != null && PropertiesComposite.this.provider.isEditAllowed();
					if (PropertiesComposite.this.wcResource != null && PropertiesComposite.this.repositoryResource == null) {
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Add")) {
							public void run() {
								PropertiesComposite.this.editProperty(null);
							}
						});
						tAction.setEnabled(isEditAllowed);
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Edit")) {
							public void run() {
								SVNProperty data = (SVNProperty) tSelection.getFirstElement();
								PropertiesComposite.this.editProperty(data);
							}
						});
						tAction.setEnabled(isEditAllowed && tSelection.size() == 1);
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Remove")) {
							public void run() {
								SVNProperty[] data = (SVNProperty[]) tSelection.toList().toArray(new SVNProperty[tSelection.size()]);
								PropertiesComposite.this.removeProperty(data);
							}
						});
						tAction.setEnabled(isEditAllowed && tSelection.size() > 0);
						manager.add(new Separator());
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.SetKeywords")) {
							public void run() {
								PropertiesComposite.this.setKeywords();
							}
						});
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.ApplyRecursively")) {
							public void run() {
								SVNProperty[] data = (SVNProperty[]) tSelection.toList().toArray(new SVNProperty[tSelection.size()]);
								PropertiesComposite.this.setPropertyRecursive(data);
							}
						});
						tAction.setEnabled(isEditAllowed && tSelection.size() > 0 && PropertiesComposite.this.wcResource instanceof IContainer);
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.SaveValueToFile")) {
						public void run() {
							SVNProperty data = (SVNProperty) tSelection.getFirstElement();
							PropertiesComposite.this.saveValueToFile(data);
						}
					});
					tAction.setEnabled(PropertiesComposite.this.provider != null && tSelection.size() == 1);
					
					manager.add(new Separator());
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("PropertiesComposite.Refresh")) {
						public void run() {
							synchronized (PropertiesComposite.this) {
								if (PropertiesComposite.this.repositoryResource != null) {
									PropertiesComposite.this.provider = new GetRemotePropertiesOperation(PropertiesComposite.this.repositoryResource);
								}
								CompositeOperation composite = new CompositeOperation("Operation.PRefresh");
								if (PropertiesComposite.this.provider != null && PropertiesComposite.this.provider.getExecutionState() != IStatus.OK) {
									composite.add(PropertiesComposite.this.provider);
									composite.add(PropertiesComposite.this.getRefreshViewOperation(), new IActionOperation[] {PropertiesComposite.this.provider});
								}
								else {
									composite.add(PropertiesComposite.this.getRefreshViewOperation());
								}
								
								UIMonitorUtility.doTaskScheduledActive(composite);
							}
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
					tAction.setEnabled(PropertiesComposite.this.provider != null);
				}				
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		table.setMenu(menu);
		
		this.propertyViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				synchronized (PropertiesComposite.this) {
					if (PropertiesComposite.this.provider != null && PropertiesComposite.this.provider.isEditAllowed()) {
						IStructuredSelection selection = (IStructuredSelection) e.getSelection();
						if (selection.size() == 1) {
							SVNProperty data = (SVNProperty) selection.getFirstElement();
							PropertiesComposite.this.editProperty(data);
						}
					}
				}
			}
		});
	}
	
	protected void removeProperty(SVNProperty[] data) {
		RemovePropertyDialog dialog = new RemovePropertyDialog(this.getShell(), data.length == 1, this.wcResource instanceof IFile);
		if (dialog.open() != 0) {
			return;
		}
		CompositeOperation composite = new CompositeOperation("Operation.PRemoveProperty");

		composite.add(new RemovePropertiesOperation(new IResource[] {this.wcResource}, data, dialog.isRecursive()));
		composite.add(this.getRefreshOperation(dialog.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO));
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void setKeywords() {
		SetKeywordsAction.doSetKeywords(new IResource[] {this.wcResource});
	}

	protected void setPropertyRecursive(SVNProperty []data) {
		PropertyApplyPanel panel = new PropertyApplyPanel(data.length == 1);		
	    DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		
		if (dialog.open() == 0) {
			SetPropertyAction.doSetProperty(new IResource[] {this.wcResource}, data, null, true, panel.getApplyMethod(), panel.useMask(), panel.getFilterMask(), false, this.getRefreshViewOperation());
		}
	}

	protected void editProperty(SVNProperty data) {
		boolean propertyAlreadyExists = false;
		boolean override = true;
		IResource []resources = new IResource[] {this.wcResource};
		final ResourcePropertyEditPanel panel = new ResourcePropertyEditPanel(new SVNProperty[] {data}, resources, false);
	    DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		
		if (dialog.open() != 0) {
			return;
		}
		
		for (int i = 0; i < this.properties.length ; i++) {
		    if (this.properties[i].name.equals(panel.getPropertyName())) {
		        propertyAlreadyExists = true;
		        break;
		    }		   
		}
		if (propertyAlreadyExists && data == null) {
		    SetPropertyWithOverrideDialog overrideDialog = new SetPropertyWithOverrideDialog(this.getShell(), panel.getPropertyName());
		    if (overrideDialog.open() != 0) {
				override = false;
			}
		}
		if (override) {
			SetPropertyAction.doSetProperty(resources, panel, this.getRefreshViewOperation());
		}
	}

	protected void saveValueToFile(final SVNProperty data) {
		FileDialog fileDialog = new FileDialog(this.getShell(), SWT.SAVE);
		fileDialog.setFileName(data.name);
		final String fileName = fileDialog.open();

		AbstractActionOperation saveValue = new AbstractActionOperation("Operation.PSaveValueToFile") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				FileOutputStream output = null;
				if (fileName != null) {
					output = new FileOutputStream(fileName);
					byte[] value = data.value.getBytes();
					output.write(value);
				}
				if (output != null) {
					output.close();
				}
			}
		};
		UIMonitorUtility.doTaskBusyDefault(saveValue);
	}
	
	protected IActionOperation getRefreshOperation(int depth) {
		CompositeOperation composite = new CompositeOperation("Operation.PRefreshView");
		composite.add(this.getRefreshViewOperation());
		if (this.wcResource != null) {
			composite.add(new RefreshResourcesOperation(new IResource[] {this.wcResource}, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
		}
		return composite;
	}
	
	public void initializeComposite() {
		if (this.isProcessing) {
			this.propertyViewer.setInput(new String[] {""});
			this.propertyViewer.getTable().setLinesVisible(false);
		}
		else {
			this.propertyViewer.setInput(this.properties);
			this.propertyViewer.getTable().setLinesVisible(true);
		}
		this.propertyText.setText("");
	}
	
	public void setPending(boolean isProcessing) {
		this.isProcessing = isProcessing;
	}
	
	public synchronized void disconnectComposite() {
		this.wcResource = null;
		this.properties = null;
		this.provider = null;
	}
	
	protected class PropertiesTableComparator extends ColumnedViewerComparator {
		
		public PropertiesTableComparator(Viewer tableViewer) {
			super(tableViewer);
		}
		
		public int compareImpl(Viewer viewer, Object row1, Object row2) {
			if (row1 instanceof SVNProperty) {
				SVNProperty data1 = (SVNProperty) row1;
				SVNProperty data2 = (SVNProperty) row2;
				return
					this.column == PropertiesComposite.COLUMN_NAME ?
					ColumnedViewerComparator.compare(data1.name, data2.name) :
					ColumnedViewerComparator.compare(data1.value, data2.value);
			}
			return 0;
		}

	}
	
}
