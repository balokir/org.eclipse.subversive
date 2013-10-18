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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Resource selection composite
 * 
 * @author Sergiy Logvin
 */
public class ResourceSelectionComposite extends Composite {
	protected static final ImageDescriptor ERROR_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif") //$NON-NLS-1$
			.createImage(), TeamImages.getImageDescriptor(ISharedImages.IMG_ERROR_OVR), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);

	protected static final ImageDescriptor WARNING_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif") //$NON-NLS-1$
			.createImage(), TeamImages.getImageDescriptor(ISharedImages.IMG_WARNING_OVR), new Point(9, 9), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.TOP);

	protected static final ImageDescriptor EMPTY_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif") //$NON-NLS-1$
			.createImage(), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/empty_error.gif"), new Point(9, 9), OverlayedImageDescriptor.RIGHT //$NON-NLS-1$
			| OverlayedImageDescriptor.TOP);

	protected static final ImageDescriptor SWITCHED_IMAGE_DESC = new OverlayedImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/problem_underlay.gif") //$NON-NLS-1$
			.createImage(), SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/switched.gif"), new Point(9, 9), OverlayedImageDescriptor.RIGHT //$NON-NLS-1$
			| OverlayedImageDescriptor.TOP);

	protected static final int COLUMN_CHECKBOX = 0;

	protected static final int COLUMN_NAME = 1;

	protected static final int COLUMN_STATUS = 2;

	protected static final int COLUMN_PROPSTATUS = 3;

	protected static final int COLUMN_TREAT_AS_EDIT = 4;

	protected CheckboxTableViewer tableViewer;

	protected ISelectionChangedListener selectionListener;

	protected IResource[] resources;

	protected IResource[] selectedResources;

	protected IResource[] notSelectedResources;
	
	protected HashMap<IResource, Button> treatAsEditButtons = new HashMap();
	protected HashSet<IResource> treatAsEdit = new HashSet();

	protected List selectionChangedListeners;

	protected boolean deselectNewl;

	protected boolean cacheEnabled;

	protected HashSet externalResources = new HashSet();

	protected HashSet userSelectedResources = new HashSet();
	protected Map<ImageDescriptor, Image> images;

	protected Label lblSelectedResourcesNumber;
	
	protected boolean showCheckBoxesAndButtons;
	protected boolean allowTreatAsEditsColumn;

	public ResourceSelectionComposite(Composite parent, int style, IResource[] resources, boolean selectAll, boolean allowTreatAsEditColumn) {
		this(parent, style, resources, selectAll, null, allowTreatAsEditColumn);
	}

	public ResourceSelectionComposite(Composite parent, int style, IResource[] resources, boolean selectAll, boolean allowTreatAsEditColumn, boolean showCheckBoxesAndButtons) {
		this(parent, style, resources, selectAll, null, allowTreatAsEditColumn , showCheckBoxesAndButtons);
	}
		 
	public ResourceSelectionComposite(Composite parent, int style, IResource[] resources, boolean selectAll, IResource[] userSelectedResources, boolean allowTreatAsEditColumn) {
		this(parent, style, resources, selectAll, userSelectedResources, allowTreatAsEditColumn, true);
	}
		 	
	public ResourceSelectionComposite(Composite parent, int style, IResource[] resources, boolean selectAll, IResource[] userSelectedResources, boolean allowTreatAsEditColumn, boolean showCheckBoxesAndButtons) {
		super(parent, style);
		this.allowTreatAsEditsColumn = allowTreatAsEditColumn;
		this.showCheckBoxesAndButtons = showCheckBoxesAndButtons;
		this.selectedResources = this.resources = resources;
		if (SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_TREAT_REPLACEMENT_AS_EDIT_NAME)) {
			for (IResource resource : resources) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
					this.treatAsEdit.add(resource);
				}				
			}
		}
		this.notSelectedResources = new IResource[0];
		this.selectionChangedListeners = new ArrayList();
		this.deselectNewl = selectAll;
		this.images = new HashMap<ImageDescriptor, Image>();
		if (userSelectedResources != null) {
			this.userSelectedResources.addAll(Arrays.asList(userSelectedResources));
		}
		IResource[] externals = FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_SWITCHED, IResource.DEPTH_ZERO);
		for (int i = 0; i < externals.length; i++) {
			this.externalResources.add(externals[i]);
		}
		this.cacheEnabled = CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled();
		this.createControls();
		this.refreshSelection();
	}

	public void dispose() {
		for (Button button : this.treatAsEditButtons.values()) {
			button.dispose();
		}
    	for (Image img : this.images.values()) {
    		img.dispose();
    	}
		super.dispose();
	}
	
	public IResource[] getSelectedResources() {
		return this.selectedResources;
	}

	public IResource[] getNotSelectedResources() {
		return this.notSelectedResources;
	}
	
	public IResource[] getTreatAsEdits() {
		return (IResource [])this.treatAsEdit.toArray(new IResource[this.treatAsEdit.size()]);
	}

	public List getCurrentSelection() {
		StructuredSelection selection = (StructuredSelection) this.tableViewer.getSelection();
		return selection.toList();
	}

	public TableViewer getTableViewer() {
		return this.tableViewer;
	}

	public void setResources(IResource[] resources) {
		this.resources = resources;
		this.tableViewer.setInput(this.resources);
		this.updateSelectedResources();
		this.tableViewer.refresh();
	}

	public void createControls() {
		GridLayout gridLayout = null;
		GridData data = null;

		gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);

		int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI  | SWT.BORDER;
		Table table = new Table(this, this.showCheckBoxesAndButtons ? style | SWT.CHECK : style);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		this.tableViewer = new CheckboxTableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		this.tableViewer.getTable().setLayoutData(data);

		// creating a comparator right now to get column listeners
		ResourcesTableComparator comparator = new ResourcesTableComparator(this.tableViewer);

		// checkbox
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		layout.addColumnData(new ColumnPixelData(20, false));

		// resource name
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ResourceSelectionComposite_Resource);
		layout.addColumnData(new ColumnWeightData(44, true));
		col.addSelectionListener(comparator);

		// status
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ResourceSelectionComposite_Content);
		layout.addColumnData(new ColumnWeightData(12, true));
		if (this.cacheEnabled) {
			col.addSelectionListener(comparator);
		}

		// propstatus
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ResourceSelectionComposite_Properties);
		layout.addColumnData(new ColumnWeightData(12, true));
		if (this.cacheEnabled) {
			col.addSelectionListener(comparator);
		}

		TableColumn treatAsEdit = null;
		if (this.allowTreatAsEditsColumn) {
			treatAsEdit = col = new TableColumn(table, SWT.NONE);
			col.setResizable(false);
			col.setText(SVNUIMessages.ResourceSelectionComposite_TreatAsEdit);
			layout.addColumnData(new ColumnWeightData(12, true));
		}

		// adding comparator and selection default sorting column and direction
		this.tableViewer.setComparator(comparator);
		comparator.setColumnNumber(ResourceSelectionComposite.COLUMN_STATUS);
		this.tableViewer.getTable().setSortColumn(this.tableViewer.getTable().getColumn(ResourceSelectionComposite.COLUMN_STATUS));
		this.tableViewer.getTable().setSortDirection(SWT.UP);

		this.tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == ResourceSelectionComposite.COLUMN_NAME && element instanceof IAdaptable) {
					IWorkbenchAdapter adapter = (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
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
						IResource currentResource = (IResource) element;
						IMarker[] markers = currentResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
						// Errors always with highest priority. So, other severities should be ignored.
						for (int i = 0; i < markers.length && !hasError; i++) {
							Integer severity = markers[i] != null ? (Integer) markers[i].getAttribute(IMarker.SEVERITY) : null;
							if (severity != null) {
								hasWarning |= severity.intValue() == IMarker.SEVERITY_WARNING;
								hasError |= severity.intValue() == IMarker.SEVERITY_ERROR;
							}
						}
					}
					catch (CoreException e) {
						// Markers are inaccessible: do not decorate resource icon
					}

					Image image = ResourceSelectionComposite.this.images.get(descriptor);
					if (image == null) {
						ResourceSelectionComposite.this.images.put(descriptor, image = descriptor.createImage());
					}
					OverlayedImageDescriptor desc = null;
					if (hasError) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.ERROR_IMAGE_DESC, new Point(16, 16), OverlayedImageDescriptor.BOTTOM
								| OverlayedImageDescriptor.LEFT);
					}
					else if (hasWarning) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.WARNING_IMAGE_DESC, new Point(16, 16), OverlayedImageDescriptor.BOTTOM
								| OverlayedImageDescriptor.LEFT);
					}
					else {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.EMPTY_IMAGE_DESC, new Point(16, 16), OverlayedImageDescriptor.BOTTOM
								| OverlayedImageDescriptor.LEFT);
					}
					image = this.createImage(desc);

					if (ResourceSelectionComposite.this.externalResources.contains(element)) {
						desc = new OverlayedImageDescriptor(image, ResourceSelectionComposite.SWITCHED_IMAGE_DESC, new Point(16, 16), OverlayedImageDescriptor.BOTTOM
								| OverlayedImageDescriptor.RIGHT);
					}
					image = this.createImage(desc);

					return image;
				}
				return null;
			}

			protected Image createImage(OverlayedImageDescriptor descriptor) {
				Image image = ResourceSelectionComposite.this.images.get(descriptor);
				if (image == null) {
					ResourceSelectionComposite.this.images.put(descriptor, image = descriptor.createImage());
				}
				return image;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == ResourceSelectionComposite.COLUMN_CHECKBOX) {
					return ""; //$NON-NLS-1$
				}
				IResource resource = (IResource) element;
				if (columnIndex == ResourceSelectionComposite.COLUMN_NAME) {
					String path = resource.getFullPath().toString();
					return path.startsWith("/") ? path.substring(1) : path; //$NON-NLS-1$
				}
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);				
				if (columnIndex == ResourceSelectionComposite.COLUMN_STATUS) {
					return ResourceSelectionComposite.this.contentStatusAsString(local);
				} else if (columnIndex == ResourceSelectionComposite.COLUMN_PROPSTATUS) {
					return ResourceSelectionComposite.this.propertiesStatusAsString(local);
				}
				return ""; //$NON-NLS-1$
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
		
		if (this.allowTreatAsEditsColumn) {
			// the order is important otherwise the common label provider overrides this one
			TableViewerColumn cbColumn = new TableViewerColumn(this.tableViewer, treatAsEdit);
			cbColumn.setLabelProvider(new ColumnLabelProvider() {
				public void update(ViewerCell cell) {
					IResource resource = (IResource)cell.getElement();
					ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
					if (IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
						TableItem item = (TableItem)cell.getItem();
						Button button;
						if (ResourceSelectionComposite.this.treatAsEditButtons.containsKey(cell.getElement())) {
							button = ResourceSelectionComposite.this.treatAsEditButtons.get(cell.getElement());
						}
						else {
							button = new Button((Composite)cell.getViewerRow().getControl(), SWT.CHECK);
							button.setData(resource);
							button.setSelection(ResourceSelectionComposite.this.treatAsEdit.contains(resource));
							button.setBackground(cell.getBackground());
							button.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent e) {
									if (((Button)e.getSource()).getSelection()) {
										ResourceSelectionComposite.this.treatAsEdit.add((IResource)((Button)e.getSource()).getData());
									}
									else {
										ResourceSelectionComposite.this.treatAsEdit.remove((IResource)((Button)e.getSource()).getData());
									}
									ResourceSelectionComposite.this.getTableViewer().refresh(((Button)e.getSource()).getData());
								}
							});
							ResourceSelectionComposite.this.treatAsEditButtons.put(resource, button);
						}
						TableEditor editor = new TableEditor(item.getParent());
						editor.grabHorizontal = true;
						editor.grabVertical = true;
						editor.setEditor(button, item, cell.getColumnIndex());
						editor.layout();
					}
				}
			});
		}

		this.tableViewer.setContentProvider(new ArrayStructuredContentProvider());

		this.tableViewer.setInput(this.resources);
		for (int i = 0; i < this.resources.length; i++) {
			this.tableViewer.setChecked(this.resources[i], this.isSelectableResource(this.resources[i]));
		}
		this.updateSelectedResources();

		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				final IResource resource = (IResource) selection.getFirstElement();
				IResource[] resources = { resource };
				if (selection.size() == 1 && !FileUtility.checkForResourcesPresence(resources, IStateFilter.SF_NOTONREPOSITORY, IResource.DEPTH_ZERO)) {
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
							if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
								IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
								remote.setSelectedRevision(CompareResourcesOperation.getRemoteResourceRevisionForCompare(resource));
								UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(local, remote, true, true));
							}
						}
					});
				}
			}
		});
					
		if (!this.showCheckBoxesAndButtons) {
			return;
		}	
		
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
		gLayout.numColumns = 4;
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
				ResourceSelectionComposite.this.tableViewer.setAllChecked(true);
				Object[] elements = ResourceSelectionComposite.this.tableViewer.getCheckedElements();
				ResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				ResourceSelectionComposite.this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent(Arrays.asList(elements).toArray(new IResource[elements.length])));
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
				TableItem []items = ResourceSelectionComposite.this.tableViewer.getTable().getSelection();
				for (TableItem item : items)
				{
					ResourceSelectionComposite.this.tableViewer.setChecked(item.getData(), false);
				}
				ResourceSelectionComposite.this.selectionListener.selectionChanged(null);
				ResourceSelectionComposite.this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent(new IResource[0]));
			}
		};
		deselectButton.addSelectionListener(listener);

		deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNUIMessages.Button_ClearAll);
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
	}

	protected String resourceNumberToString(int value) {
		return SVNUIMessages.format(SVNUIMessages.ResourceSelectionComposite_Info, new String[] { String.valueOf(value), String.valueOf(this.resources.length) });
	}

	protected String contentStatusAsString(ILocalResource local) {				
		String status = ""; //$NON-NLS-1$
		if (!IStateFilter.ST_NORMAL.equals(local.getTextStatus())) {
			status = SVNUtility.getStatusText(this.treatAsEdit.contains(local.getResource()) ? IStateFilter.ST_MODIFIED : local.getTextStatus());			
			if (local.isCopied()) {
				status += " (+)"; //$NON-NLS-1$
			}
		}
		if (local.hasTreeConflict() && local.getTreeConflictDescriptor().conflictKind == SVNConflictDescriptor.Kind.CONTENT) {
			if (!"".equals(status)) { //$NON-NLS-1$
				status += ", "; //$NON-NLS-1$
			}
			status += SVNMessages.TreeConflicting;			
		}
		return status;
	}
	
	protected String propertiesStatusAsString(ILocalResource local) {				
		String status = "";	 //$NON-NLS-1$
		if (IStateFilter.SF_HAS_PROPERTIES_CHANGES.accept(local)) {
			status = SVNUtility.getStatusText(local.getPropStatus());
		}
		if (local.hasTreeConflict() && local.getTreeConflictDescriptor().conflictKind == SVNConflictDescriptor.Kind.PROPERTIES) {
			if (!"".equals(status)) { //$NON-NLS-1$
				status += ", "; //$NON-NLS-1$
			}
			status += SVNMessages.TreeConflicting;			
		}
		return status;
	}

	public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.selectionChangedListeners.remove(listener);
	}

	public void fireResourcesSelectionChanged(ResourceSelectionChangedEvent event) {
		IResourceSelectionChangeListener[] listeners = (IResourceSelectionChangeListener[]) this.selectionChangedListeners
				.toArray(new IResourceSelectionChangeListener[this.selectionChangedListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resourcesSelectionChanged(event);
		}
	}

	public void refreshSelection() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean isSelectNewResources = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_COMMIT_SELECT_NEW_RESOURCES_NAME);
		boolean isSelectMissingResources = !SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_DO_NOT_SELECT_MISSING_RESOURCES_NAME);
		if (this.deselectNewl && (!isSelectNewResources || !isSelectMissingResources)) {
			Object[] elements = this.tableViewer.getCheckedElements();
			for (int i = 0; i < elements.length; i++) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource((IResource) elements[i]);
				if (!isSelectNewResources && local.getStatus() == IStateFilter.ST_NEW || 
					!isSelectMissingResources && local.getStatus() == IStateFilter.ST_MISSING) {
					this.tableViewer.setChecked(elements[i], false);
				}
			}
			elements = this.tableViewer.getCheckedElements();
			this.fireResourcesSelectionChanged(new ResourceSelectionChangedEvent(Arrays.asList(elements).toArray(new IResource[elements.length])));
			this.selectionListener.selectionChanged(null);
		}
	}

	public void fireSelectionChanged() {
		this.selectionListener.selectionChanged(null);
	}

	protected void updateSelectedResources() {
		TableItem[] items = this.tableViewer.getTable().getItems();
		ArrayList checked = new ArrayList(items.length);
		ArrayList unchecked = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			(items[i].getChecked() ? checked : unchecked).add(items[i].getData());
		}
		this.selectedResources = (IResource[]) checked.toArray(new IResource[checked.size()]);
		this.notSelectedResources = (IResource[]) unchecked.toArray(new IResource[unchecked.size()]);
	}

	protected boolean isSelectableResource(IResource resource) {
		if (!this.externalResources.contains(resource)) {
			return true;
		}

		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		if (!SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_DO_NOT_SELECT_EXTERNALS_NAME)) {
			return true;
		}

		if (this.userSelectedResources.contains(resource)) {
			return true;
		}

		while ((resource = resource.getParent()) != null) {
			ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(resource);
			if (IStateFilter.SF_INTERNAL_INVALID.accept(localResource) || (localResource.getChangeMask() & ILocalResource.IS_SWITCHED) == 0) {
				break;
			}
			if (this.userSelectedResources.contains(resource)) {
				return true;
			}
		}

		return false;
	}

	protected class ResourcesTableComparator extends ColumnedViewerComparator {

		public ResourcesTableComparator(Viewer tableViewer) {
			super(tableViewer);
		}

		public int compareImpl(Viewer viewer, Object row1, Object row2) {
			if (this.column == ResourceSelectionComposite.COLUMN_CHECKBOX) {
				return 0;
			}
			IResource rowData1 = (IResource) row1;
			IResource rowData2 = (IResource) row2;
			if (this.column == ResourceSelectionComposite.COLUMN_NAME) {
				return this.compareNames(rowData1, rowData2);
			}
			if (!ResourceSelectionComposite.this.cacheEnabled) {
				return 0;
			}
			ILocalResource local1 = SVNRemoteStorage.instance().asLocalResource(rowData1);
			ILocalResource local2 = SVNRemoteStorage.instance().asLocalResource(rowData2);
			if (this.column == ResourceSelectionComposite.COLUMN_STATUS) {
				String status1 = ResourceSelectionComposite.this.contentStatusAsString(local1);
				String status2 = ResourceSelectionComposite.this.contentStatusAsString(local2);
				int retVal = this.compareStatuses(status1, status2);
				return retVal != 0 ? retVal : this.compareNames(rowData1, rowData2);
			}
			if (this.column == ResourceSelectionComposite.COLUMN_PROPSTATUS) {
				String propStatus1 = ResourceSelectionComposite.this.propertiesStatusAsString(local1);
				String propStatus2 = ResourceSelectionComposite.this.propertiesStatusAsString(local2);
				return ColumnedViewerComparator.compare(propStatus1, propStatus2);
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
			return ColumnedViewerComparator.compare(status1, status2);
		}

		protected int compareNames(IResource rowData1, IResource rowData2) {
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
			return ColumnedViewerComparator.compare(path1, path2);
		}

	}

}
