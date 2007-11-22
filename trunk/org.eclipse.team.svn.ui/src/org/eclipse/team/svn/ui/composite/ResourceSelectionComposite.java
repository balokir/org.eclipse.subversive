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

package org.eclipse.team.svn.ui.composite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Resource selection composite
 *
 * @author Sergiy Logvin
 */
public class ResourceSelectionComposite extends Composite {
	protected static final ImageDescriptor ERROR_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif").createImage(), TeamImages.getImageDescriptor(ISharedImages.IMG_ERROR_OVR), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);
	protected static final ImageDescriptor WARNING_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif").createImage(), TeamImages.getImageDescriptor(ISharedImages.IMG_WARNING_OVR), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);
	protected static final ImageDescriptor EMPTY_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif").createImage(), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/empty_error.gif"), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);
	protected static final ImageDescriptor SWITCHED_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif").createImage(), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/switched.gif"), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);
	
	protected CheckboxTableViewer tableViewer;
	protected ISelectionChangedListener selectionListener;
	protected IResource []resources;
	protected IResource []selectedResources;
	protected IResource []notSelectedResources;
	protected List selectionChangedListeners;
	protected boolean deselectNewl;
	protected boolean noCache;
	protected HashSet externalResources = new HashSet();
	protected HashSet userSelectedResources = new HashSet();
	
	protected Label lblSelectedResourcesNumber;

	public ResourceSelectionComposite(Composite parent, int style, IResource []resources, boolean selectAll) {
		this(parent, style, resources, selectAll, null);
	}
	
	public ResourceSelectionComposite(Composite parent, int style, IResource []resources, boolean selectAll, IResource[] userSelectedResources) {
		super(parent, style);
		this.selectedResources = this.resources = resources;
		this.notSelectedResources = new IResource[0];
		this.selectionChangedListeners = new ArrayList();
		this.deselectNewl = selectAll;
		if (userSelectedResources != null) {
			this.userSelectedResources.addAll(Arrays.asList(userSelectedResources));
		}
		IResource[] externals = FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_EXTERNAL, IResource.DEPTH_ZERO);
		for (int i = 0; i < externals.length; i++) {
			this.externalResources.add(externals[i]);
		}
		this.createControls();
		this.refreshSelection();
		this.noCache = !CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled();
	}
	
	public IResource []getSelectedResources() {
		return this.selectedResources;
	}
	
	public IResource []getNotSelectedResources() {
		return this.notSelectedResources;
	}
	
	public List getCurrentSelection() {
		StructuredSelection selection = (StructuredSelection)this.tableViewer.getSelection();
		return selection.toList();
	}
	
	public TableViewer getTableViewer() {
		return this.tableViewer;
	}
	
	public void createControls() {
		GridLayout gridLayout = null;
		GridData data = null;
		
		gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);
        
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		this.tableViewer = new CheckboxTableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		this.tableViewer.getTable().setLayoutData(data);

		TableViewerSorter sorter = new TableViewerSorter(this.tableViewer, new TableViewerSorter.IColumnComparator() {
            public int compare(Object row1, Object row2, int column) {
            	if (column == 0) {
            		return 0;
            	}
                IResource rowData1 = (IResource)row1;
                IResource rowData2 = (IResource)row2;
                if (column == 1) {
                    return this.compareNames(rowData1, rowData2);
                }
                if (ResourceSelectionComposite.this.noCache) {
                	return 0;
                }
                IRemoteStorage storage = SVNRemoteStorage.instance();
                ILocalResource local1 = storage.asLocalResource(rowData1);
                ILocalResource local2 = storage.asLocalResource(rowData2);
                if (local1 == null || local2 == null) {
                	return 0;
                }
                int changeMask1 = local1.getChangeMask();
                int changeMask2 = local2.getChangeMask();
                if (column == 2) {
                    String status1 = ResourceSelectionComposite.this.statusAsString(local1.getStatus(), changeMask1);
                    String status2 = ResourceSelectionComposite.this.statusAsString(local2.getStatus(), changeMask2);
                    int retVal = this.compareStatuses(status1, status2);
                    return retVal != 0 ? retVal : this.compareNames(rowData1, rowData2);
                }
                if (column == 3) {
                	String propStatus1 = changeMaskAsString(changeMask1);
                	String propStatus2 = changeMaskAsString(changeMask2);
                	return TableViewerSorter.compare(propStatus1, propStatus2);
                }
                return 0;
            }
            
            protected int compareStatuses(String status1, String status2) {
            	if (status1 == status2) {
            		return 0;
            	}
            	if (status1 == IStateFilter.ST_NEW || status1 == IStateFilter.ST_IGNORED) {
            		return 1;
            	}
            	if (status2 == IStateFilter.ST_NEW || status2 == IStateFilter.ST_IGNORED) {
            		return -1;
            	}
            	return TableViewerSorter.compare(status1, status2);
            }
            
            protected int compareNames(IResource rowData1 , IResource rowData2) {
                boolean cnd1 = rowData1 instanceof IContainer;
                boolean cnd2 = rowData2 instanceof IContainer;
                if (cnd1 && !cnd2) {
                    return -1;
                }
                else if (cnd2 && !cnd1) {
                    return 1;
                }
				String path1 = rowData1.getFullPath().toString();
				String path2 = rowData2.getFullPath().toString();
                return TableViewerSorter.compare(path1, path2);
            }
        });
        if (this.noCache) {
        	// bit mask, really one column enabled with index 1
        	sorter.setEnabledColumns(2);
        }
		this.tableViewer.setSorter(sorter);
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		layout.addColumnData(new ColumnPixelData(20, false));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Resource"));
		layout.addColumnData(new ColumnWeightData(56, true));
		col.addSelectionListener(sorter);
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Content"));
		layout.addColumnData(new ColumnWeightData(12, true));
		col.addSelectionListener(sorter);
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Properties"));
		layout.addColumnData(new ColumnWeightData(12, true));
		col.addSelectionListener(sorter);
		
		sorter.setDefaultColumn(2);
		
		this.tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 1 && element instanceof IAdaptable) {
					IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
					if (adapter == null) {
						return null;
					}
					ImageDescriptor descriptor = adapter.getImageDescriptor(element);
					if (descriptor == null) {
						return null;
					}
					
					boolean hasWarning = false;
					boolean hasError = false;
					try {
						IResource currentResource = (IResource)element;
						IMarker []markers = currentResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
						// Errors always with highest priority. So, other severities should be ignored.
						for (int i = 0; i < markers.length && !hasError; i++) {
							Integer severity = markers[i] != null ? (Integer)markers[i].getAttribute(IMarker.SEVERITY) : null;
							if (severity != null) {
								hasWarning |= severity.intValue() == IMarker.SEVERITY_WARNING;
								hasError |= severity.intValue() == IMarker.SEVERITY_ERROR;
							}
						}
					}
					catch (CoreException e) {
						//Markers are inaccessible: do not decorate resource icon
					}
		
					Image image = (Image)SWTResourceUtil.getImageTable().get(descriptor);
					if (image == null) {
						SWTResourceUtil.getImageTable().put(descriptor, image = descriptor.createImage());
					}
					OverlayedImageDescriptor desc = null;
					if (hasError) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.ERROR_IMAGE_DESC,  new Point(16,16), OverlayedImageDescriptor.BOTTOM | OverlayedImageDescriptor.LEFT);
					}
					else if (hasWarning) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.WARNING_IMAGE_DESC,  new Point(16,16), OverlayedImageDescriptor.BOTTOM | OverlayedImageDescriptor.LEFT);
					}
					else {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.EMPTY_IMAGE_DESC,  new Point(16,16), OverlayedImageDescriptor.BOTTOM | OverlayedImageDescriptor.LEFT);
					}
					image = this.createImage(desc);
					
					if (ResourceSelectionComposite.this.externalResources.contains(element)) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.SWITCHED_IMAGE_DESC, new Point(16, 16), OverlayedImageDescriptor.BOTTOM | OverlayedImageDescriptor.RIGHT);
					}
					image = this.createImage(desc);
					
					return image;
				}
				return null;
			}
			
			protected Image createImage(OverlayedImageDescriptor descriptor) {
				Image image = (Image)SWTResourceUtil.getImageTable().get(descriptor);
				if (image == null) {
					SWTResourceUtil.getImageTable().put(descriptor, image = descriptor.createImage());
				}
				return image;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0) {
					return "";
				}
				IResource resource = (IResource)element;
				if (columnIndex == 1) {
					String path = resource.getFullPath().toString();
					return path.startsWith("/") ? path.substring(1) : path;
				}
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (local == null) {
					return SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.InvalidResource");
				}
				int changeMask = local.getChangeMask();
				if (columnIndex == 2) {
					return ResourceSelectionComposite.this.statusAsString(local.getStatus(), changeMask);
				}
				return ResourceSelectionComposite.this.changeMaskAsString(changeMask);
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
				return ResourceSelectionComposite.this.resources;
			}
		});
		
		this.tableViewer.setInput(this.resources);
		for (int i = 0; i < this.resources.length; i++) {
			this.tableViewer.setChecked(this.resources[i], this.isSelectableResource(this.resources[i]));
		}
		this.updateSelectedResources();
		
		this.tableViewer.addSelectionChangedListener(this.selectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ResourceSelectionComposite.this.updateSelectedResources();
		        
				ResourceSelectionComposite.this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent(ResourceSelectionComposite.this.selectedResources));
				
				int selectedNumber = ResourceSelectionComposite.this.selectedResources.length;
				ResourceSelectionComposite.this.lblSelectedResourcesNumber.setText(ResourceSelectionComposite.this.resourceNumberToString(selectedNumber));
			}
		});
		
		Composite tComposite = new Composite(this, SWT.RIGHT);
		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 3;
		gLayout.marginWidth = 0;
		tComposite.setLayout(gLayout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tComposite.setLayoutData(data);
	
		Button selectButton = new Button(tComposite, SWT.PUSH);
		selectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.SelectAll"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(selectButton);
		selectButton.setLayoutData(data);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionComposite.this.tableViewer.setAllChecked(true);
				Object []elements = ResourceSelectionComposite.this.tableViewer.getCheckedElements();
				ResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				ResourceSelectionComposite.this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent((IResource [])Arrays.asList(elements).toArray(new IResource[elements.length])));
			}
		};
		selectButton.addSelectionListener(listener);
	
		Button deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.ClearSelection"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(deselectButton);
		deselectButton.setLayoutData(data);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionComposite.this.tableViewer.setAllChecked(false);
				ResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				ResourceSelectionComposite.this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent(new IResource[0]));
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
		
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					final IResource resource = (IResource) selection.getFirstElement();
					UIMonitorUtility.getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(resource, SVNRevision.BASE, null, true));
						}
					});
				}
			}
		});
	}
	
	protected String resourceNumberToString(int value) {
		String msg = SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Info");
		return MessageFormat.format(msg, new String[] {String.valueOf(value), String.valueOf(this.resources.length)});
	}
	
	protected String statusAsString(String status, int changeMask) {
		if ((changeMask & ILocalResource.TEXT_MODIFIED) == 0) {
			return "";
		}
		return SVNUtility.getStatusText(status);
	}
	
	protected String changeMaskAsString(int changeMask) {
		if ((changeMask & ILocalResource.PROP_MODIFIED) != 0) {
			return IStateFilter.ST_MODIFIED;
		}
		return "";
	}
	
	public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.add(listener);
	}
	
	public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.remove(listener);
	}
	
	public void fireResourcesSelectionChanged(ResourceSelectionChangedEvent event) {
		IResourceSelectionChangeListener []listeners = (IResourceSelectionChangeListener [])this.selectionChangedListeners.toArray(new IResourceSelectionChangeListener[this.selectionChangedListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resourcesSelectionChanged(event);
		}
	}
	
	public void refreshSelection() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean isSelectNewResources = SVNTeamPreferences.getResourceSelectionBoolean(store, SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME);
		if (this.deselectNewl && !isSelectNewResources) {
			Object []elements = this.tableViewer.getCheckedElements();
			for (int i = 0; i < elements.length; i++) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource((IResource)elements[i]);
				if (local == null || local.getStatus() == IStateFilter.ST_NEW) {
					this.tableViewer.setChecked(elements[i], false);
				}
			}
			elements = this.tableViewer.getCheckedElements();
			this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent((IResource [])Arrays.asList(elements).toArray(new IResource[elements.length])));
			this.selectionListener.selectionChanged(null);
		}
	}
	
	public void setResources(IResource[] resources) {
		this.resources = resources;
	}
	
	public void fireSelectionChanged() {
		this.selectionListener.selectionChanged(null);
	}
	
	protected void updateSelectedResources() {
		TableItem []items = this.tableViewer.getTable().getItems();
        ArrayList checked = new ArrayList(items.length);
        ArrayList unchecked = new ArrayList();
        for (int i = 0; i < items.length; i++) {
			(items[i].getChecked() ? checked : unchecked).add(items[i].getData());
        }
        this.selectedResources = (IResource [])checked.toArray(new IResource[checked.size()]);
        this.notSelectedResources = (IResource [])unchecked.toArray(new IResource[unchecked.size()]);
	}
	
	protected boolean isSelectableResource(IResource resource) {
		if (!this.externalResources.contains(resource)) {
			return true;
		}
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		if (!SVNTeamPreferences.getResourceSelectionBoolean(store, SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME)) {
			return true;
		}

		if (this.userSelectedResources.contains(resource)) {
			return true;
		}
		
		while ((resource = resource.getParent()) != null) {
			ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(resource);
			if (localResource == null ||
				(localResource.getChangeMask() & ILocalResource.IS_EXTERNAL) == 0) {
				break;
			}
			if (this.userSelectedResources.contains(resource)) {
				return true;
			}
		}
		
		return false;
	}
	
}
