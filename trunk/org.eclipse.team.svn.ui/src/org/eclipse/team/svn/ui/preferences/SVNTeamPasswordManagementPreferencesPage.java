/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryLocation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Password management preferences page
 * 
 * @author Sergiy Logvin
 */
public class SVNTeamPasswordManagementPreferencesPage extends AbstractSVNTeamPreferencesPage {
	protected TableViewer viewer;
	protected Button removeButton;
	protected Button removeAllButton;
	protected List forRemove = new ArrayList();
	
	public SVNTeamPasswordManagementPreferencesPage() {
		super();
	}
	
	public void init(IWorkbench workbench) {
		setDescription(SVNTeamUIPlugin.instance().getResource("PasswordManagementPreferencePage.Hint"));
	}
	
	protected Control createContentsImpl(Composite parent) {
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.viewer = new TableViewer(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = this.viewer.getTable();
		new TableEditor(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 290;
		data.widthHint = 370;
		table.setLayoutData(data);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamPasswordManagementPreferencesPage.this.handleSelection();
			}
		});
		
		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(SVNTeamUIPlugin.instance().getResource("PasswordManagementPreferencePage.Location"));
		column = new TableColumn(table, SWT.NULL);
		column.setText(SVNTeamUIPlugin.instance().getResource("PasswordManagementPreferencePage.Username"));
		this.viewer.setLabelProvider(new TableLabelProvider());
		this.viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				if (inputElement == null) {
					return null;
				}
				IRepositoryLocation[] locations = ((SVNRemoteStorage)inputElement).getRepositoryLocations();
				List repositories = new ArrayList();
				for (int i = 0; i < locations.length; i++) {
					if (SVNTeamPasswordManagementPreferencesPage.this.checkIfAnyPasswordSaved(locations[i]) &&
						!SVNTeamPasswordManagementPreferencesPage.this.forRemove.contains(locations[i])) {
							repositories.add(locations[i]);
					}
				}
				return repositories.toArray(new SVNRepositoryLocation[repositories.size()]);
			}
		});
		TableLayout tLayout = new TableLayout();
        tLayout.addColumnData(new ColumnWeightData(70));
        tLayout.addColumnData(new ColumnWeightData(30));
		table.setLayout(tLayout);
		
		Composite buttons = new Composite(composite, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL));
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		buttons.setLayout(layout);
		
		this.removeButton = new Button(buttons, SWT.PUSH);
		this.removeButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Remove"));  
		this.removeButton.setEnabled(false);
		this.removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});
		this.removeAllButton = new Button(buttons, SWT.PUSH);
		this.removeAllButton.setText(SVNTeamUIPlugin.instance().getResource("Button.RemoveAll"));  
		this.removeAllButton.setEnabled(true);
		this.removeAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeAll();
			}
		});
		
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.removeAllButton);
		this.removeButton.setLayoutData(data);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.removeAllButton);
		this.removeAllButton.setLayoutData(data);
		
		this.viewer.setInput(SVNRemoteStorage.instance());
		this.handleSelection();
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.passwordPreferencesContext");
		
		return composite;
	}
	
	protected void remove() {
		IStructuredSelection selection = (IStructuredSelection)this.viewer.getSelection();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			this.forRemove.add(it.next());
		}
		this.viewer.refresh();
		this.handleSelection();
	}
	
	protected void removeAll() {
		IRepositoryLocation[] locations = SVNRemoteStorage.instance().getRepositoryLocations();
		for (int i = 0; i < locations.length; i++) {
			if (this.checkIfAnyPasswordSaved(locations[i])) {
				this.forRemove.add(locations[i]);
			}
		}
		this.viewer.refresh();
		this.handleSelection();
	}
	
	protected boolean checkIfAnyPasswordSaved(IRepositoryLocation location) {		
		return location.isPasswordSaved() ||
			location.getSSHSettings().isPassPhraseSaved() ||
			location.getSSLSettings().isPassPhraseSaved();
	}
	
	protected void resetPasswordsSaving(IRepositoryLocation location) {
		location.setPasswordSaved(false);
		location.getSSHSettings().setPassPhraseSaved(false);
		location.getSSLSettings().setPassPhraseSaved(false);
		location.reconfigure();
	}
	
	protected void handleSelection() {
		if (this.viewer.getTable().getSelectionCount() > 0) {
			this.removeButton.setEnabled(true);
		} 
		else {
			this.removeButton.setEnabled(false);
		}
		this.removeAllButton.setEnabled(this.viewer.getTable().getItemCount() > 0);
	}
	
	protected void saveValues(IPreferenceStore store) {
		for (Iterator iter = this.forRemove.iterator(); iter.hasNext();) {
			this.resetPasswordsSaving((IRepositoryLocation)iter.next());
		}
		this.forRemove = new ArrayList();
		UIMonitorUtility.doTaskBusyDefault(new SaveRepositoryLocationsOperation());
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		
	}

	protected void loadValues(IPreferenceStore store) {
		
	}
	
	protected void initializeControls() {
		
	}
	
	protected class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			SVNRepositoryLocation location = (SVNRepositoryLocation)element;
			switch (columnIndex) {
				case 0:
					return location.getUrl();
				case 1:
					return location.getUsername();
				default:
					return null;
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	};
	
}
