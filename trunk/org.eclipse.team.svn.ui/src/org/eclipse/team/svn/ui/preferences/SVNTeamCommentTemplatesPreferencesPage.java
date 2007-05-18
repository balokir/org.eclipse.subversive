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

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.EditCommentTemplatePanel;

/**
 * Comments templates preferences page implementation
 *
 * @author Elena Matokhina
 */
public class SVNTeamCommentTemplatesPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, ISelectionChangedListener {
	
	protected Text previewText;
	protected ListViewer listViewer;
	protected Button newButton;
	protected Button editButton;
	protected Button removeButton;
	protected Button useLogTemplatesButton;
	protected Button useTemplatesButton;
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite checkBoxComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		checkBoxComposite.setLayout(layout);
		checkBoxComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.useLogTemplatesButton = new Button(checkBoxComposite, SWT.CHECK);
		this.useLogTemplatesButton.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.LogTemplates"));
		
		this.useTemplatesButton = new Button(checkBoxComposite, SWT.CHECK);
		this.useTemplatesButton.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.UserTemplates"));
		this.useTemplatesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamCommentTemplatesPreferencesPage.this.selectionChanged(null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite separatorComposite = new Composite(checkBoxComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 1;
		separatorComposite.setLayout(layout);
		separatorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label separator = new Label(separatorComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label templatesLabel = new Label(composite, SWT.NONE);
		templatesLabel.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.EditHint"));

		this.createListAndButtons(composite);

		Label previewLabel = new Label(composite, SWT.NONE);
		previewLabel.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.ViewHint"));
		
		this.previewText = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = this.convertHeightInCharsToPixels(5);
		this.previewText.setLayoutData(data);
		
		Dialog.applyDialogFont(parent);
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		boolean logTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME);
		boolean userTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME);
		this.useLogTemplatesButton.setSelection(logTemplatesEnabled);
		this.useTemplatesButton.setSelection(userTemplatesEnabled);
		this.selectionChanged(null);
		
		return composite;
	}

	protected Composite createListAndButtons(Composite parent) {
		Composite listAndButtons = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		listAndButtons.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint= 430;
		listAndButtons.setLayoutData(data);
		
		this.listViewer = new ListViewer(listAndButtons);
		this.listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				String template = (String) element;
				return FileUtility.flattenText(template);
			}
		});
		this.listViewer.addSelectionChangedListener(this);
		this.listViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				String template1 = FileUtility.flattenText((String) e1);
				String template2 = FileUtility.flattenText((String) e2);
				return template1.compareToIgnoreCase(template2);
			}
		});
		this.listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				SVNTeamCommentTemplatesPreferencesPage.this.editTemplate();
			}
		});
		List list = this.listViewer.getList();
		list.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		String []templates = FileUtility.decodeStringToArray(SVNTeamPreferences.getCommentTemplatesString(store, 
				SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME));
		
		// populate list
		for (int i = 0; i < templates.length; i++) {
			this.listViewer.add(templates[i]);
		}

		this.createButtons(listAndButtons);
		return listAndButtons;
	}

	protected void createButtons(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		buttons.setLayout(layout);

		this.newButton = new Button(buttons, SWT.PUSH);
		this.newButton.setText(SVNTeamUIPlugin.instance().getResource("Button.New"));
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(newButton);
		this.newButton.setLayoutData(data);
		this.newButton.setEnabled(true);
		this.newButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				SVNTeamCommentTemplatesPreferencesPage.this.newTemplate();
			}
		});

		this.editButton = new Button(buttons, SWT.PUSH);
		this.editButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Edit"));
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(this.editButton);
		this.editButton.setLayoutData(data);
		this.editButton.setEnabled(false);
		this.editButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamCommentTemplatesPreferencesPage.this.editTemplate();
			}
		});

		this.removeButton = new Button(buttons, SWT.PUSH);
		this.removeButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Remove"));
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(this.removeButton);
		this.removeButton.setLayoutData(data);
		this.removeButton.setEnabled(false);
		this.removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamCommentTemplatesPreferencesPage.this.remove();
			}
		});
	}
	
	protected void newTemplate() {
		EditCommentTemplatePanel panel = new EditCommentTemplatePanel(null);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			this.listViewer.add(panel.getTemplate());
		}
	}

	protected void editTemplate() {
		IStructuredSelection selection = (IStructuredSelection) this.listViewer.getSelection();
		if (selection.size() == 1) {
			String oldTemplate = (String)selection.getFirstElement();
			EditCommentTemplatePanel panel = new EditCommentTemplatePanel(oldTemplate);
			DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
			if (dialog.open() == 0) {
				this.listViewer.remove(oldTemplate);
				this.listViewer.add(panel.getTemplate());
			}	
		}
	}
	
	protected void remove() {
		IStructuredSelection selection = (IStructuredSelection)this.listViewer.getSelection();
		this.listViewer.remove(selection.toArray());
	}

	public void init(IWorkbench workbench) {
		
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = this.useTemplatesButton.getSelection();
		this.listViewer.getControl().setEnabled(enabled);
		this.newButton.setEnabled(enabled);
		this.previewText.setEnabled(enabled);
		
		IStructuredSelection selection = (IStructuredSelection) this.listViewer.getSelection();
		this.editButton.setEnabled(enabled && selection.size() == 1);
		this.removeButton.setEnabled(enabled && selection.size() > 0);
		this.previewText.setText(selection.size() == 1 ? (String)selection.getFirstElement() : "");
	}
	
	public boolean performOk() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		SVNTeamPreferences.setCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME, this.useLogTemplatesButton.getSelection());
		SVNTeamPreferences.setCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME, this.useTemplatesButton.getSelection());
		
		int numTemplates = this.listViewer.getList().getItemCount();
		String[] templates = new String[numTemplates];
		for (int i = 0; i < numTemplates; i++) {
			templates[i] = (String)this.listViewer.getElementAt(i);
		}
		SVNTeamPreferences.setCommentTemplatesString(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME, FileUtility.encodeArrayToString(templates));
		SVNTeamUIPlugin.instance().savePluginPreferences();
		return super.performOk();
	}
	
}
