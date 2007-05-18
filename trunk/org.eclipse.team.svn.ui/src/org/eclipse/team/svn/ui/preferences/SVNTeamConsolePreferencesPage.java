/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Console preferences page
 * 
 * @author Alexander Gurov
 */
public class SVNTeamConsolePreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	protected ColorFieldEditor cmdEditor;
	protected ColorFieldEditor okEditor;
	protected ColorFieldEditor wrnEditor;
	protected ColorFieldEditor errEditor;
	
	protected boolean enabled;
	protected int autoshow;
	protected boolean wrapEnabled;
	protected boolean limitEnabled;
	protected int wrapWidth;
	protected int limitValue;
	
	protected Button enabledButton;
	protected Button showNeverButton;
	protected Button showAlwaysButton;
	protected Button showErrorButton;
	protected Button showWarningErrorButton;
	protected Button wrapEnabledButton;
	protected Button limitEnabledButton;
	protected Text wrapWidthText;
	protected Text limitValueText;

	public SVNTeamConsolePreferencesPage() {
		super();
		this.setDescription(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.desc"));
	}
	
	public void init(IWorkbench workbench) {

	}

	public boolean performOk() {
		this.cmdEditor.store();
		this.okEditor.store();
		this.wrnEditor.store();
		this.errEditor.store();

		IPreferenceStore store = this.getPreferenceStore();
		
		SVNTeamPreferences.setConsoleInt(store, SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NAME, this.autoshow);
		
		SVNTeamPreferences.setConsoleBoolean(store, SVNTeamPreferences.CONSOLE_ENABLED_NAME, this.enabled);
		
		SVNTeamPreferences.setConsoleBoolean(store, SVNTeamPreferences.CONSOLE_WRAP_ENABLED_NAME, this.wrapEnabled);
		SVNTeamPreferences.setConsoleInt(store, SVNTeamPreferences.CONSOLE_WRAP_WIDTH_NAME, this.wrapWidth);
		
		SVNTeamPreferences.setConsoleBoolean(store, SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_NAME, this.limitEnabled);
		SVNTeamPreferences.setConsoleInt(store, SVNTeamPreferences.CONSOLE_LIMIT_VALUE_NAME, this.limitValue);
		
		SVNTeamUIPlugin.instance().savePluginPreferences();
		
		return true;
	}
	
	protected void performDefaults() {
		super.performDefaults();
		
		IPreferenceStore store = this.getPreferenceStore();
		
		SVNTeamPreferences.resetToDefaultConsoleValues(store);
		
		this.loadDefaultValues();
		this.initializeControls();
	}
	
	protected void loadDefaultValues() {
		this.autoshow = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_DEFAULT;
		this.enabled = SVNTeamPreferences.CONSOLE_ENABLED_DEFAULT;
		this.wrapEnabled = SVNTeamPreferences.CONSOLE_WRAP_ENABLED_DEFAULT;
		this.wrapWidth = SVNTeamPreferences.CONSOLE_WRAP_WIDTH_DEFAULT;
		this.limitEnabled = SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_DEFAULT;
		this.limitValue = SVNTeamPreferences.CONSOLE_LIMIT_VALUE_DEFAULT;
	}
	
	protected void loadValues() {
		IPreferenceStore store = this.getPreferenceStore();
		
		this.autoshow = SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NAME);
		this.enabled = SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_ENABLED_NAME);
		this.wrapEnabled = SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_WRAP_ENABLED_NAME);
		this.wrapWidth = SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_WRAP_WIDTH_NAME);
		this.limitEnabled = SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_NAME);
		this.limitValue = SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_LIMIT_VALUE_NAME);
	}
	
	protected void initializeControls() {
		this.cmdEditor.load();
		this.okEditor.load();
		this.wrnEditor.load();
		this.errEditor.load();
		
		this.showNeverButton.setSelection(false);
		this.showAlwaysButton.setSelection(false);
		this.showErrorButton.setSelection(false);
		if (this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NEVER) {
			this.showNeverButton.setSelection(true);
		}
		else if (this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ALWAYS) {
			this.showAlwaysButton.setSelection(true);
		}
		else if (this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ERROR) {
			this.showErrorButton.setSelection(true);
		}
		else {
			this.showWarningErrorButton.setSelection(true);
		}
		
		this.enabledButton.setSelection(this.enabled);
		
		this.wrapEnabledButton.setSelection(this.wrapEnabled);
		this.wrapWidthText.setEnabled(this.wrapEnabled);
		this.wrapWidthText.setText(String.valueOf(this.wrapWidth));
		
		this.limitEnabledButton.setSelection(this.limitEnabled);
		this.limitValueText.setEnabled(this.limitEnabled);
		this.limitValueText.setText(String.valueOf(this.limitValue));
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return SVNTeamUIPlugin.instance().getPreferenceStore();
	}
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = false;
		composite.setLayoutData(data);
		
		this.enabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.enabledButton.setLayoutData(data);
		this.enabledButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textIsEnabled"));
		this.enabledButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				SVNTeamConsolePreferencesPage.this.enabled = SVNTeamConsolePreferencesPage.this.enabledButton.getSelection();
			}
		});
		
		Group showType = new Group(composite, SWT.FILL);
		showType.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textShowOnGroup"));
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing = 10;
		showType.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.grabExcessVerticalSpace = false;
		showType.setLayoutData(data);
		
		this.showNeverButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.showNeverButton.setLayoutData(data);
		this.showNeverButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textShowNever"));
		this.showNeverButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamConsolePreferencesPage.this.showNeverButton.getSelection()) {
					SVNTeamConsolePreferencesPage.this.autoshow = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NEVER;
				}
			}
		});
		
		this.showAlwaysButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.showAlwaysButton.setLayoutData(data);
		this.showAlwaysButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textShowAlways"));
		this.showAlwaysButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamConsolePreferencesPage.this.showAlwaysButton.getSelection()) {
					SVNTeamConsolePreferencesPage.this.autoshow = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ALWAYS;
				}
			}
		});
		
		this.showErrorButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.showErrorButton.setLayoutData(data);
		this.showErrorButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textShowError"));
		this.showErrorButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamConsolePreferencesPage.this.showErrorButton.getSelection()) {
					SVNTeamConsolePreferencesPage.this.autoshow = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ERROR;
				}
			}
		});
		
		this.showWarningErrorButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.showWarningErrorButton.setLayoutData(data);
		this.showWarningErrorButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textShowWarningError"));
		this.showWarningErrorButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamConsolePreferencesPage.this.showWarningErrorButton.getSelection()) {
					SVNTeamConsolePreferencesPage.this.autoshow = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_WARNING_ERROR;
				}
			}
		});
		
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		separator.setLayoutData(data);
		separator.setVisible(false);
		
		this.wrapEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.wrapEnabledButton.setLayoutData(data);
		this.wrapEnabledButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textWrapEnabled"));
		this.wrapEnabledButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				SVNTeamConsolePreferencesPage.this.wrapEnabled = SVNTeamConsolePreferencesPage.this.wrapEnabledButton.getSelection();
				SVNTeamConsolePreferencesPage.this.wrapWidthText.setEnabled(SVNTeamConsolePreferencesPage.this.wrapEnabled);
			}
		});
		
		Label label = new Label(composite, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textWrapWidth"));
		
		this.wrapWidthText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.wrapWidthText.setLayoutData(data);
		this.wrapWidthText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					SVNTeamConsolePreferencesPage.this.wrapWidth = Integer.parseInt(SVNTeamConsolePreferencesPage.this.wrapWidthText.getText());
				}
				catch (Exception ex) {
					SVNTeamConsolePreferencesPage.this.wrapWidthText.setText(String.valueOf(SVNTeamConsolePreferencesPage.this.wrapWidth));
				}
			}
		});
		
		this.limitEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.limitEnabledButton.setLayoutData(data);
		this.limitEnabledButton.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textLimitEnabled"));
		this.limitEnabledButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				SVNTeamConsolePreferencesPage.this.limitEnabled = SVNTeamConsolePreferencesPage.this.limitEnabledButton.getSelection();
				SVNTeamConsolePreferencesPage.this.limitValueText.setEnabled(SVNTeamConsolePreferencesPage.this.limitEnabled);
			}
		});
		
		label = new Label(composite, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textLimitValue"));
		
		this.limitValueText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.limitValueText.setLayoutData(data);
		this.limitValueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					SVNTeamConsolePreferencesPage.this.limitValue = Integer.parseInt(SVNTeamConsolePreferencesPage.this.limitValueText.getText());
				}
				catch (Exception ex) {
					SVNTeamConsolePreferencesPage.this.limitValueText.setText(String.valueOf(SVNTeamConsolePreferencesPage.this.limitValue));
				}
			}
		});
		
		separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		separator.setLayoutData(data);
		separator.setVisible(false);
		
		label = new Label(composite, SWT.NULL);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textColorsGroup"));
		
		this.cmdEditor = new ColorFieldEditor(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_CMD_COLOR_NAME), SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textCmdMessage"), composite);
		this.cmdEditor.setPreferencePage(this);
		this.cmdEditor.setPreferenceStore(this.getPreferenceStore());
		
		this.okEditor = new ColorFieldEditor(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_OK_COLOR_NAME), SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textOkMessage"), composite);
		this.okEditor.setPreferencePage(this);
		this.okEditor.setPreferenceStore(this.getPreferenceStore());

		this.wrnEditor = new ColorFieldEditor(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRN_COLOR_NAME), SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textWrnMessage"), composite);
		this.wrnEditor.setPreferencePage(this);
		this.wrnEditor.setPreferenceStore(this.getPreferenceStore());

		this.errEditor = new ColorFieldEditor(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_ERR_COLOR_NAME), SVNTeamUIPlugin.instance().getResource("ConsolePreferencePage.textErrMessage"), composite);
		this.errEditor.setPreferencePage(this);
		this.errEditor.setPreferenceStore(this.getPreferenceStore());
		
		this.loadValues();
		this.initializeControls();
		
		return composite;
	}
	
}
