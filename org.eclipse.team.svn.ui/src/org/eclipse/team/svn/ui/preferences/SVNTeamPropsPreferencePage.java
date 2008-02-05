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

import org.eclipse.compare.internal.TabFolderLayout;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SpellcheckedTextProvider;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.EditAutoPropertiesPanel;
import org.eclipse.team.svn.ui.panel.common.EditCustomPropertiesPanel;
import org.eclipse.ui.PlatformUI;

/**
 * Auto-props preferences page
 *
 * @author Alexey Mikoyan
 * 
 * @since 0.7.0 - Properties configuration preference page
 */
public class SVNTeamPropsPreferencePage extends AbstractSVNTeamPreferencesPage {
	protected CheckboxTableViewer autopropTableViewer;
	protected Button autopropBtnAdd;
	protected Button autopropBtnEdit;
	protected Button autopropBtnRemove;
	protected Button autopropBtnExport;
	protected Button autopropBtnImport;
	
	protected String autoPropsValue;
	protected String customPropsValue;
	
	protected TableViewer custompropTableViewer;
	protected Button custompropBtnAdd;
	protected Button custompropBtnEdit;
	protected Button custompropBtnRemove;
	protected StyledText customPropDescription;
	
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
	
	public static class CustomProperty {
		public String propName;
		public String regExp;
		public String descriprion;
		
		public CustomProperty(String propName, String regExp, String description) {
			this.propName = propName;
			this.regExp = regExp;
			this.descriprion = description;
		}
	}

	protected void saveValues(IPreferenceStore store) {
		int propsCount = this.autopropTableViewer.getTable().getItemCount();
		String []props = new String[3 * propsCount];
		for (int i = 0; i < propsCount; i++) {
			SVNTeamPropsPreferencePage.AutoProperty property = (SVNTeamPropsPreferencePage.AutoProperty)this.autopropTableViewer.getElementAt(i);
			props[3 * i] = property.enabled ? "1" : "0";
			props[3 * i + 1] = property.fileName;
			props[3 * i + 2] = property.properties;
		}
		this.autoPropsValue = FileUtility.encodeArrayToString(props);
		SVNTeamPreferences.setAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME, this.autoPropsValue);
		
		propsCount = this.custompropTableViewer.getTable().getItemCount();
		props = new String[3 * propsCount];
		for (int i = 0; i < propsCount; i++) {
			SVNTeamPropsPreferencePage.CustomProperty property = (SVNTeamPropsPreferencePage.CustomProperty)this.custompropTableViewer.getElementAt(i);
			props[3 * i] = property.propName;
			props[3 * i + 1] = property.regExp;
			props[3 * i + 2] = property.descriprion;
		}
		this.customPropsValue = FileUtility.encodeArrayToString(props);
		SVNTeamPreferences.setCustomPropertiesList(store, SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME, this.customPropsValue);
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.autoPropsValue = SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT;
		this.customPropsValue = SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.autoPropsValue = SVNTeamPreferences.getAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME);
		this.customPropsValue = SVNTeamPreferences.getCustomPropertiesList(store, SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME);
	}
	
	protected void initializeControls() {
		this.removeAllProperties();
		this.populateAutopropTable(SVNTeamPropsPreferencePage.loadAutoProperties(this.autoPropsValue));
		this.populateCustompropTable(SVNTeamPropsPreferencePage.loadCustomProperties(this.customPropsValue));
	}
	
	protected Control createContentsImpl(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData());
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Automatic properties");
		tabItem.setControl(this.createAutopropsComposite(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Custom properties");
		tabItem.setControl(this.createCustompropsComposite(tabFolder));
		
		//Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.autoPropsPreferencesContext");
		
		return tabFolder;
	}
	
	protected Composite createAutopropsComposite(TabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.createAutopropTable(composite);
		this.createAutopropButtons(composite);
		
		this.autopropTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				SVNTeamPropsPreferencePage.this.autopropBtnEdit.setEnabled(selection.size() == 1);
				SVNTeamPropsPreferencePage.this.autopropBtnRemove.setEnabled(selection.size() > 0);
			}
		});
		this.autopropTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				SVNTeamPropsPreferencePage.this.editAutoProperty();
			}
		});
		this.autopropTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				((SVNTeamPropsPreferencePage.AutoProperty)event.getElement()).enabled = event.getChecked();
				SVNTeamPropsPreferencePage.this.autopropBtnExport.setEnabled(SVNTeamPropsPreferencePage.this.autopropTableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.autopropBtnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.addAutoProperty();
				SVNTeamPropsPreferencePage.this.autopropBtnExport.setEnabled(SVNTeamPropsPreferencePage.this.autopropTableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.autopropBtnEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.editAutoProperty();
			}
		});
		
		this.autopropBtnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.removeAutoProperty();
				SVNTeamPropsPreferencePage.this.autopropBtnExport.setEnabled(SVNTeamPropsPreferencePage.this.autopropTableViewer.getCheckedElements().length != 0);
			}
		});
		
		this.autopropBtnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.exportAutoProperties();
			}
		});
		
		this.autopropBtnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.importAutoProperties();
				SVNTeamPropsPreferencePage.this.autopropBtnExport.setEnabled(SVNTeamPropsPreferencePage.this.autopropTableViewer.getCheckedElements().length != 0);
			}
		});
		
		return composite;
	}
	
	protected void createAutopropTable(Composite parent) {
		this.autopropTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnPixelData(20, false));
		layout.addColumnData(new ColumnWeightData(25, true));
		layout.addColumnData(new ColumnWeightData(75, true));
		
		this.autopropTableViewer.getTable().setLayout(layout);
		this.autopropTableViewer.getTable().setLinesVisible(true);
		this.autopropTableViewer.getTable().setHeaderVisible(true);
		this.autopropTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.autopropTableViewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == SVNTeamPropsPreferencePage.COLUMN_INDEX_FILE_NAME) {
					return ((SVNTeamPropsPreferencePage.AutoProperty)element).fileName;
				}
				else if (columnIndex == SVNTeamPropsPreferencePage.COLUMN_INDEX_PROPERTIES) {
					return ((SVNTeamPropsPreferencePage.AutoProperty)element).properties;
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
		
		TableColumn column = new TableColumn(this.autopropTableViewer.getTable(), SWT.NONE);
		column.setResizable(false);
		
		column = new TableColumn(this.autopropTableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.columnHeaderFileName"));
		
		column =  new TableColumn(this.autopropTableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.columnHeaderProperties"));
	}
	
	protected void createAutopropButtons(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttons.setLayout(layout);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
		this.autopropBtnAdd = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextAdd"),
				true);
		this.autopropBtnEdit = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextEdit"),
				false);
		this.autopropBtnRemove = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextRemove"),
				false);
		this.autopropBtnExport = createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextExport"),
				this.autopropTableViewer.getCheckedElements().length != 0);
		this.autopropBtnImport = this.createButton(buttons,
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
	
	public void addAutoProperty() {
		EditAutoPropertiesPanel panel = new EditAutoPropertiesPanel(null);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			SVNTeamPropsPreferencePage.AutoProperty property =
				new SVNTeamPropsPreferencePage.AutoProperty(panel.getFileName(), panel.getProperties(), true);
			this.autopropTableViewer.add(property);
			this.autopropTableViewer.setChecked(property, property.enabled);
		}
	}

	public void editAutoProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.autopropTableViewer.getSelection();
		SVNTeamPropsPreferencePage.AutoProperty property =
			(SVNTeamPropsPreferencePage.AutoProperty)selection.getFirstElement();
		EditAutoPropertiesPanel panel = new EditAutoPropertiesPanel(property);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			property.fileName = panel.getFileName();
			property.properties = panel.getProperties();
			this.autopropTableViewer.update(property, null);
		}
	}
	
	public void removeAutoProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.autopropTableViewer.getSelection();
		this.autopropTableViewer.remove(selection.toArray());
	}
	
	public void removeAllProperties() {
		this.autopropTableViewer.getTable().clearAll();
		this.autopropTableViewer.refresh();
		this.custompropTableViewer.getTable().clearAll();
		this.custompropTableViewer.refresh();
	}
	
	public void exportAutoProperties() {
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
					!line.startsWith(SVNTeamPropsPreferencePage.AUTO_PROPS_SECTION_HEADER)) {
				tmpWriter.println(line);
			}
			// Write [auto-props] section header
			tmpWriter.println(SVNTeamPropsPreferencePage.AUTO_PROPS_SECTION_HEADER);
			// Copy [auto-props] section content
			while ((line = srcReader.readLine()) != null &&
					!line.trim().equals("")) {
				tmpWriter.println(line);
			}
			// Insert auto-properties
			Object[] checkedProps = this.autopropTableViewer.getCheckedElements();
			for (int i = 0; i < checkedProps.length; i++) {
				SVNTeamPropsPreferencePage.AutoProperty property =
					(SVNTeamPropsPreferencePage.AutoProperty)checkedProps[i];
				if (!property.properties.equals("")) {
					tmpWriter.println(property.fileName +
							" " +
							SVNTeamPropsPreferencePage.AUTO_PROPS_PATTERN_SEPARATOR +
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
	
	public void importAutoProperties() {
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
				if (line.startsWith(SVNTeamPropsPreferencePage.AUTO_PROPS_SECTION_HEADER)) {
					break;
				}
			}
			
			// Process [auto-props] section content
			while ((line = cfgReader.readLine()) != null &&
					!line.trim().equals("")) {
				if (line.startsWith(SVNTeamPropsPreferencePage.AUTO_PROPS_COMMENT_START)) {
					continue;
				}
				String fileName = "";
				String properties = "";
				int index = line.indexOf(SVNTeamPropsPreferencePage.AUTO_PROPS_PATTERN_SEPARATOR);
				if (index != -1) {
					fileName = line.substring(0, index).trim();
					if (index < line.length() - 1) {
						properties = line.substring(index + 1).trim();
					}
				}
				else {
					fileName = line.trim();
				}
				autoPropsList.add(new SVNTeamPropsPreferencePage.AutoProperty(fileName, properties, true));
			}
			
			// Set new properties
			this.populateAutopropTable(autoPropsList.toArray());
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
	
	public void populateAutopropTable(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			this.autopropTableViewer.add(items[i]);
			this.autopropTableViewer.setChecked(items[i], ((SVNTeamPropsPreferencePage.AutoProperty)items[i]).enabled);
		}
	}
	
	public void populateCustompropTable(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			this.custompropTableViewer.add(items[i]);
		}
	}
	
	protected Composite createCustompropsComposite(TabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.createCustompropTable(composite);
		this.createCustompropButtons(composite);
        this.customPropDescription = SpellcheckedTextProvider.getTextWidget(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 80;
		data.widthHint = 0;
		this.customPropDescription.setLayoutData(data);
		this.customPropDescription.setText(SVNTeamUIPlugin.instance().getResource("CustomPropsPreferencePage.description"));
		this.customPropDescription.setBackground(this.customPropDescription.getBackground());
		this.customPropDescription.setEditable(false);
		
		this.custompropTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				SVNTeamPropsPreferencePage.this.refreshDescription();
				SVNTeamPropsPreferencePage.this.custompropBtnEdit.setEnabled(selection.size() == 1);
				SVNTeamPropsPreferencePage.this.custompropBtnRemove.setEnabled(selection.size() > 0);
			}
		});
		
		this.custompropTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				SVNTeamPropsPreferencePage.this.editCustomProperty();
			}
		});
		
		this.custompropBtnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.addCustomProperty();
			}
		});
		
		this.custompropBtnEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.editCustomProperty();
			}
		});
		
		this.custompropBtnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPropsPreferencePage.this.removeCustomProperty();
			}
		});
		
		return composite;
	}
	
	protected void createCustompropTable(Composite parent) {
		this.custompropTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);// CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(30, true));
		layout.addColumnData(new ColumnWeightData(70, true));
		
		this.custompropTableViewer.getTable().setLayout(layout);
		this.custompropTableViewer.getTable().setLinesVisible(true);
		this.custompropTableViewer.getTable().setHeaderVisible(true);
		this.custompropTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.custompropTableViewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0) {
					return ((SVNTeamPropsPreferencePage.CustomProperty)element).propName;
				}
				else if (columnIndex == 1) {
					return ((SVNTeamPropsPreferencePage.CustomProperty)element).regExp;
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
		
		TableColumn column = new TableColumn(this.custompropTableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("CustomPropsPreferencePage.columnHeaderPropName"));
		
		column =  new TableColumn(this.custompropTableViewer.getTable(), SWT.NONE);
		column.setText(SVNTeamUIPlugin.instance().getResource("CustomPropsPreferencePage.columnHeaderRegexp"));
		
	}
	
	protected void createCustompropButtons(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttons.setLayout(layout);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
		this.custompropBtnAdd = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextAdd"),
				true);
		this.custompropBtnEdit = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextEdit"),
				false);
		this.custompropBtnRemove = this.createButton(buttons,
				SVNTeamUIPlugin.instance().getResource("AutoPropsPreferencePage.buttonTextRemove"),
				false);
	}
	
	protected void refreshDescription() {
		IStructuredSelection selection = (IStructuredSelection)this.custompropTableViewer.getSelection();
		if (selection.size() == 0) {
			this.customPropDescription.setText(SVNTeamUIPlugin.instance().getResource("CustomPropsPreferencePage.description"));
			return;
		}
		String description = ((SVNTeamPropsPreferencePage.CustomProperty)selection.getFirstElement()).descriprion;
		if (description.equals("")) {
			this.customPropDescription.setText("No description available.");
		}
		else {
			this.customPropDescription.setText(description);
		}
	}
	
	public void addCustomProperty() {
		EditCustomPropertiesPanel panel = new EditCustomPropertiesPanel(null);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			this.custompropTableViewer.add(panel.getProperty());
		}
	}
	
	public void editCustomProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.custompropTableViewer.getSelection();
		SVNTeamPropsPreferencePage.CustomProperty property =
			(SVNTeamPropsPreferencePage.CustomProperty)selection.getFirstElement();
		EditCustomPropertiesPanel panel = new EditCustomPropertiesPanel(property);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			property  = panel.getProperty();
			this.custompropTableViewer.update(property, null);
			this.refreshDescription();
		}
	}
	
	public void removeCustomProperty() {
		IStructuredSelection selection = (IStructuredSelection)this.custompropTableViewer.getSelection();
		this.custompropTableViewer.remove(selection.toArray());
		this.refreshDescription();
	}
	
	public static SVNTeamPropsPreferencePage.CustomProperty [] loadCustomProperties(String encodedProps) {
		ArrayList<SVNTeamPropsPreferencePage.CustomProperty> propsList = new ArrayList();
		String[] props = FileUtility.decodeStringToArray(encodedProps);
		for (int i = 0; i < props.length; i += 3) {
			String propName = props[i];
			String regexp =  (i + 1 == props.length) ? "" : props[i + 1];
			String description =  (i + 2 >= props.length) ? "" :  props[i + 2];
			SVNTeamPropsPreferencePage.CustomProperty property = 
				new SVNTeamPropsPreferencePage.CustomProperty(propName, regexp, description);
			propsList.add(property);
		}
		SVNTeamPropsPreferencePage.CustomProperty [] propArr = {};
		return propsList.toArray(propArr);
	}
	
	public static Object[] loadAutoProperties(String encodedProps) {
		ArrayList propsList = new ArrayList();
		String[] props = FileUtility.decodeStringToArray(encodedProps);
		for (int i = 0; i < props.length; i += 3) {
			boolean enabled = !props[i].equals("0");
			String fileName = props[i + 1];
			String properties = (i + 2 == props.length) ? "" : props[i + 2];
			SVNTeamPropsPreferencePage.AutoProperty property = 
				new SVNTeamPropsPreferencePage.AutoProperty(fileName, properties, enabled);
			propsList.add(property);
		}
		return propsList.toArray();
	}
	
	public String findConfigFile(String dialogTitle) {
		ISVNConnector connector = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		String cfgDir;
		try {
			cfgDir = connector.getConfigDirectory();
		}
		catch (SVNConnectorException cwe) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.FindConfigFile"), cwe);
			return null;
		}
		FileDialog dlg = new FileDialog(this.getShell());
		dlg.setText(dialogTitle);
		dlg.setFilterPath(cfgDir);
		File cfgFile = new File(cfgDir + System.getProperty("file.separator") + SVNTeamPropsPreferencePage.AUTO_PROPS_CONFIG_FILE_NAME);
		if (cfgFile.exists()) {
			dlg.setFileName(SVNTeamPropsPreferencePage.AUTO_PROPS_CONFIG_FILE_NAME);
		}
		return dlg.open();
	}
	
}
