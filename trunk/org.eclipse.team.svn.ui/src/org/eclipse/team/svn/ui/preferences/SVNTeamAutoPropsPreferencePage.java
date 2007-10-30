/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.EditAutoPropertiesPanel;

/**
 * Auto-props preferences page
 *
 * @author Alexey Mikoyan
 *
 */
public class SVNTeamAutoPropsPreferencePage extends AbstractSVNTeamPreferencesPage {
	protected CheckboxTableViewer tableViewer;
	protected Button btnAdd;
	protected Button btnEdit;
	protected Button btnRemove;
	protected Button btnExport;
	protected Button btnImport;
	
	protected String autoPropsValue;
	
	public static final int COLUMN_INDEX_FILE_NAME = 1;
	public static final int COLUMN_INDEX_PROPERTIES = 2;
	
	public static final String AUTO_PROPS_CONFIG_FILE_NAME = "config";
	public static final String AUTO_PROPS_SECTION_HEADER = "[auto-props]";
	public static final String AUTO_PROPS_COMMENT_START = "#";
	public static final String AUTO_PROPS_PATTERN_SEPARATOR = "=";
	public static final String AUTO_PROPS_PROPS_SEPARATOR = ";";
	
	public static class AutoProperty {
		public String fileName;
		public String properties;
		public boolean enabled;
		
		public AutoProperty(String fileName, String properties, boolean enabled) {
			this.fileName = fileName;
			this.properties = properties;
			this.enabled = enabled;
		}

	}

	protected void saveValues(IPreferenceStore store) {
		int propsCount = this.tableViewer.getTable().getItemCount();
		String []props = new String[3 * propsCount];
		for (int i = 0; i < propsCount; i++) {
			SVNTeamAutoPropsPreferencePage.AutoProperty property = (SVNTeamAutoPropsPreferencePage.AutoProperty)this.tableViewer.getElementAt(i);
			props[3 * i] = property.enabled ? "1" : "0";
			props[3 * i + 1] = property.fileName;
			props[3 * i + 2] = property.properties;
		}
		this.autoPropsValue = FileUtility.encodeArrayToString(props);
		SVNTeamPreferences.setAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME, this.autoPropsValue);
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.autoPropsValue = SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.autoPropsValue = SVNTeamPreferences.getAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME);
	}
	
	protected void initializeControls() {
		this.removeAllProperties();
		this.populateTable(SVNTeamAutoPropsPreferencePage.loadProperties(this.autoPropsValue));
	}
	
	protected Control createContentsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.createTable(composite);
		this.createButtons(composite);
		
		this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				SVNTeamAutoPropsPreferencePage.this.btnEdit.setEnabled(selection.size() == 1);
				SVNTeamAutoPropsPreferencePage.this.btnRemove.setEnabled(selection.size() > 0);
			}
		});
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				SVNTeamAutoPropsPreferencePage.this.editProperty();
			}
		});
		this.tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				((SVNTeamAutoPropsPreferencePage.AutoProperty)event.getElement()).enabled = event.getChecked();
				SVNTeamAutoPropsPreferencePage.this.btnExport.setEnabled(SVNTeamAutoPropsPreferencePage.this.tableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.btnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamAutoPropsPreferencePage.this.addProperty();
				SVNTeamAutoPropsPreferencePage.this.btnExport.setEnabled(SVNTeamAutoPropsPreferencePage.this.tableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.btnEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamAutoPropsPreferencePage.this.editProperty();
			}
		});
		
		this.btnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamAutoPropsPreferencePage.this.removeProperty();
				SVNTeamAutoPropsPreferencePage.this.btnExport.setEnabled(SVNTeamAutoPropsPreferencePage.this.tableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.btnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamAutoPropsPreferencePage.this.exportProperties();
			}
		});
		
		this.btnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamAutoPropsPreferencePage.this.importProperties();
				SVNTeamAutoPropsPreferencePage.this.btnExport.setEnabled(SVNTeamAutoPropsPreferencePage.this.tableViewer.getCheckedElements().length != 0);
			}
		});
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.autoPropsPreferencesContext");
		
		return composite;
	}
	
	protected void createTable(Composite parent) {
		this.tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnPixelData(20, false));
		layout.addColumnData(new ColumnWeightData(25, true));
		layout.addColumnData(new ColumnWeightData(75, true));
		
		this.tableViewer.getTable().setLayout(layout);
		this.tableViewer.getTable().setLinesVisible(true);
		this.tableViewer.getTable().setHeaderVisible(true);
		this.tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.tableViewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == SVNTeamAutoPropsPreferencePage.COLUMN_INDEX_FILE_NAME) {
					return ((SVNTeamAutoPropsPreferencePage.AutoProperty)element).fileName;
				}
				else if (columnIndex == SVNTeamAutoPropsPreferencePage.COLUMN_INDEX_PROPERTIES) {
					return ((SVNTeamAutoPropsPreferencePage.AutoProperty)element).properties;
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
		
		TableColumn column = new TableColumn(this.tableViewer.getTable(), SWT.NONE);
		column.setResizable(false);
		
		column = new TableColumn(this.tableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.columnHeaderFileName"));
		
		column =  new TableColumn(this.tableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.columnHeaderProperties"));
	}
	
	protected void createButtons(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttons.setLayout(layout);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
		this.btnAdd = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextAdd"),
				true);
		this.btnEdit = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextEdit"),
				false);
		this.btnRemove = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextRemove"),
				false);
		this.btnExport = createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextExport"),
				this.tableViewer.getCheckedElements().length != 0);
		this.btnImport = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextImport"),
				true);
	}
	
	protected Button createButton(Composite parent, String text, boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(button);
		button.setLayoutData(data);
		button.setEnabled(enabled);
		return button;
	}
	
	public void addProperty() {
		EditAutoPropertiesPanel panel = new EditAutoPropertiesPanel(null);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			SVNTeamAutoPropsPreferencePage.AutoProperty property =
				new SVNTeamAutoPropsPreferencePage.AutoProperty(panel.getFileName(), panel.getProperties(), true);
			this.tableViewer.add(property);
			this.tableViewer.setChecked(property, property.enabled);
		}
	}

	public void editProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.tableViewer.getSelection();
		SVNTeamAutoPropsPreferencePage.AutoProperty property =
			(SVNTeamAutoPropsPreferencePage.AutoProperty)selection.getFirstElement();
		EditAutoPropertiesPanel panel = new EditAutoPropertiesPanel(property);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			property.fileName = panel.getFileName();
			property.properties = panel.getProperties();
			this.tableViewer.update(property, null);
		}
	}
	
	public void removeProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.tableViewer.getSelection();
		this.tableViewer.remove(selection.toArray());
	}
	
	public void removeAllProperties() {
		this.tableViewer.getTable().clearAll();
		this.tableViewer.refresh();
	}
	
	public void exportProperties() {
		String filePath = this.findConfigFile(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.dialogTitleExport"));
		if (filePath == null) {
			return;
		}
		
		File srcCfg = new File(filePath);
		File tmpCfg = null;
		BufferedReader srcReader = null;
		PrintWriter tmpWriter = null;
		try {
			tmpCfg = File.createTempFile("cfg_", srcCfg.getName() + "tmp", SVNTeamPlugin.instance().getStateLocation().toFile());
			tmpCfg.deleteOnExit();
			if (!srcCfg.exists()) {
				srcCfg.createNewFile();
			}
			srcReader = new BufferedReader(new FileReader(srcCfg));
			tmpWriter = new PrintWriter(new FileOutputStream(tmpCfg));
			String line;
			// Copy cfg content till [auto-props] section
			while ((line = srcReader.readLine()) != null &&
					!line.startsWith(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_SECTION_HEADER)) {
				tmpWriter.println(line);
			}
			// Write [auto-props] section header
			tmpWriter.println(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_SECTION_HEADER);
			// Copy [auto-props] section content
			while ((line = srcReader.readLine()) != null &&
					!line.trim().equals("")) {
				tmpWriter.println(line);
			}
			// Insert auto-properties
			Object[] checkedProps = this.tableViewer.getCheckedElements();
			for (int i = 0; i < checkedProps.length; i++) {
				SVNTeamAutoPropsPreferencePage.AutoProperty property =
					(SVNTeamAutoPropsPreferencePage.AutoProperty)checkedProps[i];
				if (!property.properties.equals("")) {
					tmpWriter.println(property.fileName +
							" " +
							SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PATTERN_SEPARATOR +
							" " +
							property.properties);
				}
			}
			// Copy the rest of cfg
			while ((line = srcReader.readLine()) != null) {
				tmpWriter.println(line);
			}
			// Close streams
			srcReader.close();
			tmpWriter.close();
			// Copy tmpCfg file to srcCfg file
			srcCfg.delete();
			tmpCfg.renameTo(srcCfg);
		}
		catch (IOException ioe) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.ExportProperties"), ioe);
			return;
		}
		finally {
			try {
				if (srcReader != null) {
					srcReader.close();
				}
			}
			catch (IOException ioe) {
				// nothing to do
			}
			if (tmpWriter != null) {
				tmpWriter.close();
			}
			if (tmpCfg != null) {
				tmpCfg.delete();
			}
		}
	}
	
	public void importProperties() {
		String filePath = this.findConfigFile(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.dialogTitleImport"));
		if (filePath == null) {
			return;
		}
		
		BufferedReader cfgReader = null;
		ArrayList autoPropsList = new ArrayList();
		try {
			cfgReader = new BufferedReader(new FileReader(filePath));
			String line;
			// Skip cfg content till [auto-props] section header inclusive
			while ((line = cfgReader.readLine()) != null) {
				if (line.startsWith(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_SECTION_HEADER)) {
					break;
				}
			}
			
			// Process [auto-props] section content
			while ((line = cfgReader.readLine()) != null &&
					!line.trim().equals("")) {
				if (line.startsWith(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_COMMENT_START)) {
					continue;
				}
				String fileName = "";
				String properties = "";
				int index = line.indexOf(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PATTERN_SEPARATOR);
				if (index != -1) {
					fileName = line.substring(0, index).trim();
					if (index < line.length() - 1) {
						properties = line.substring(index + 1).trim();
					}
				}
				else {
					fileName = line.trim();
				}
				autoPropsList.add(new SVNTeamAutoPropsPreferencePage.AutoProperty(fileName, properties, true));
			}
			
			// Set new properties
			this.populateTable(autoPropsList.toArray());
		}
		catch (IOException ioe) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.ImportProperties"), ioe);
			return;
		}
		finally {
			try {
				if (cfgReader != null) {
					cfgReader.close();
				}
			}
			catch (IOException ioe) {
				// nothing to do
			}
		}
	}
	
	public void populateTable(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			this.tableViewer.add(items[i]);
			this.tableViewer.setChecked(items[i], ((SVNTeamAutoPropsPreferencePage.AutoProperty)items[i]).enabled);
		}
	}
	
	public static Object[] loadProperties(String encodedProps) {
		ArrayList propsList = new ArrayList();
		String[] props = FileUtility.decodeStringToArray(encodedProps);
		for (int i = 0; i < props.length; i += 3) {
			boolean enabled = !props[i].equals("0");
			String fileName = props[i + 1];
			String properties = (i + 2 == props.length) ? "" : props[i + 2];
			SVNTeamAutoPropsPreferencePage.AutoProperty property = 
				new SVNTeamAutoPropsPreferencePage.AutoProperty(fileName, properties, enabled);
			propsList.add(property);
		}
		return propsList.toArray();
	}
	
	public String findConfigFile(String dialogTitle) {
		ISVNClientWrapper client = CoreExtensionsManager.instance().getSVNClientWrapperFactory().newInstance();
		String cfgDir;
		try {
			cfgDir = client.getConfigDirectory();
		}
		catch (ClientWrapperException cwe) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.FindConfigFile"), cwe);
			return null;
		}
		FileDialog dlg = new FileDialog(this.getShell());
		dlg.setText(dialogTitle);
		dlg.setFilterPath(cfgDir);
		File cfgFile = new File(cfgDir + System.getProperty("file.separator") + SVNTeamAutoPropsPreferencePage.AUTO_PROPS_CONFIG_FILE_NAME);
		if (cfgFile.exists()) {
			dlg.setFileName(SVNTeamAutoPropsPreferencePage.AUTO_PROPS_CONFIG_FILE_NAME);
		}
		return dlg.open();
	}
	
}
