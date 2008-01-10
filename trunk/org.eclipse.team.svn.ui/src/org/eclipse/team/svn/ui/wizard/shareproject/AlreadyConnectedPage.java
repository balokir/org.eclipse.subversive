/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.shareproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Already connected to SVN wizard page
 * 
 * @author Alexander Gurov
 */
public class AlreadyConnectedPage extends AbstractVerifiedWizardPage {
	protected boolean useProjectSettings;
	protected boolean createUsingProjectSettings;
	protected String url;
	protected Text urlText;
	protected TableViewer repositoryRootsView;
	protected IRepositoryRoot []repositoryRoots;
	protected IRepositoryRoot selectedRoot;
	protected Button useProjectSettingsButton;
	protected Button createProjectLocationButton;
	protected Button reconnectButton;

	public AlreadyConnectedPage() {
		super(
			AlreadyConnectedPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.Description"));
	}
	
	public void setProjects(IProject []projects) {
		this.url = null;
		for (int i = 0; i < projects.length; i++) {
			SVNChangeStatus info = SVNUtility.getSVNInfoForNotConnected(projects[i]);
			String tmp = SVNUtility.decodeURL(info.url);
			if (this.url == null) {
				this.url = tmp;
			}
			else {
				IPath tPath = new Path(this.url);
				IPath tPath2 = new Path(tmp);
				if (tPath2.isPrefixOf(tPath)) {
					this.url = tmp;
				}
				else {
					while (!tPath.isPrefixOf(tPath2)) {
						tPath = tPath.removeLastSegments(1);
					}
					this.url = tPath.toString();
				}
			}
		}
		this.repositoryRoots = SVNUtility.findRoots(this.url, false);
		
		this.initControls();
	}
	
	public IRepositoryRoot getSelectedRoot() {
		return this.selectedRoot;
	}
	
	public String getResourceUrl() {
		return this.url;
	}

	public boolean useProjectSettings() {
		return this.useProjectSettings;
	}

	public boolean createUsingProjectSettings() {
		return this.createUsingProjectSettings;
	}

	public boolean canFlipToNextPage() {
		return !this.useProjectSettings();
	}
	
	public Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		
		this.initializeDialogUnits(parent);
		
		Label description = new Label(composite, SWT.WRAP);
		description.setLayoutData(this.makeGridData());
		description.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.ProjectURL"));
		
		this.urlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.urlText.setLayoutData(data);
		this.urlText.setEditable(false);
		
		description = new Label(composite, SWT.WRAP);
		description.setLayoutData(this.makeGridData());
		description.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.RepositoryLocation"));
		
		Table table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout tLayout = new TableLayout();
		tLayout.addColumnData(new ColumnWeightData(30, true));
		tLayout.addColumnData(new ColumnWeightData(70, true));
		table.setLayout(tLayout);

		this.repositoryRootsView = new TableViewer(table);
		this.repositoryRootsView.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return AlreadyConnectedPage.this.repositoryRoots;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		final ITableLabelProvider labelProvider = new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				IRepositoryRoot root = (IRepositoryRoot)element;
				if (columnIndex == 0) {
					return root.getRepositoryLocation().getLabel();
				}
				else {
					return root.getRepositoryLocation().getUrl();
				}
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
		this.repositoryRootsView.setLabelProvider(labelProvider);
		TableViewerSorter sorter = new TableViewerSorter(this.repositoryRootsView, new TableViewerSorter.IColumnComparator() {
			public int compare(Object row1, Object row2, int column) {
				String val1 = labelProvider.getColumnText(row1, column);
				String val2 = labelProvider.getColumnText(row2, column);
				return TableViewerSorter.compare(val1, val2);
			}
		});
		this.repositoryRootsView.setSorter(sorter);
		this.repositoryRootsView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)AlreadyConnectedPage.this.repositoryRootsView.getSelection();
				AlreadyConnectedPage.this.selectedRoot = (IRepositoryRoot)selection.getFirstElement();
				AlreadyConnectedPage.this.setPageComplete(true);
			}
		});
		
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.LocationLabel"));
		col.addSelectionListener(sorter);
		
		col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.URL"));
		col.addSelectionListener(sorter);

		Composite btnComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		btnComposite.setLayout(layout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		btnComposite.setLayoutData(data);
		
		description = new Label(btnComposite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = this.convertHeightInCharsToPixels(2);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.Hint"));
		
		this.useProjectSettingsButton = new Button(btnComposite, SWT.RADIO);
		this.useProjectSettingsButton.setLayoutData(this.makeGridData());
		this.useProjectSettingsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				boolean selectEnabled = AlreadyConnectedPage.this.useProjectSettings = button.getSelection();
				AlreadyConnectedPage.this.repositoryRootsView.getTable().setEnabled(selectEnabled);
				AlreadyConnectedPage.this.setPageComplete(true);
			}
		});
		this.useProjectSettingsButton.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.UseProjectSettings")); 
		
		this.createProjectLocationButton = new Button(btnComposite, SWT.RADIO);
		this.createProjectLocationButton.setLayoutData(this.makeGridData());
		this.createProjectLocationButton.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.CreateLocation"));
		this.createProjectLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				AlreadyConnectedPage.this.createUsingProjectSettings = button.getSelection();
				AlreadyConnectedPage.this.repositoryRootsView.getTable().setEnabled(false);
				AlreadyConnectedPage.this.setPageComplete(true);
			}
		});
		
		this.reconnectButton = new Button(btnComposite, SWT.RADIO);
		this.reconnectButton.setLayoutData(this.makeGridData());
		this.reconnectButton.setText(SVNTeamUIPlugin.instance().getResource("AlreadyConnectedPage.ReconnectToAnother")); 
		
		this.initControls();
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.alreadyConnectedContext");
		
		return composite;
	}
	
	protected void initControls() {
		if (this.urlText != null && this.url != null) {
			this.urlText.setText(this.url);
			this.repositoryRootsView.setInput(this.repositoryRoots);
			if (this.repositoryRoots.length > 0) {
				this.repositoryRootsView.getTable().select(0);
				this.selectedRoot = this.repositoryRoots[0];
				this.repositoryRootsView.getTable().setEnabled(true);
				this.useProjectSettings = true;
				this.createUsingProjectSettings = false;
				this.useProjectSettingsButton.setSelection(true);
				this.reconnectButton.setSelection(false);
				//this.createProjectLocationButton.setEnabled(false);
			}
			else {
				this.repositoryRootsView.getTable().setEnabled(false);
				this.useProjectSettingsButton.setSelection(false);
				this.useProjectSettingsButton.setEnabled(false);
				this.useProjectSettings = false;
				this.createUsingProjectSettings = true;
				this.createProjectLocationButton.setSelection(false);
				this.createProjectLocationButton.setSelection(true);
			}
		}
	}
	
	protected GridData makeGridData() {
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		return data;
	}

}
