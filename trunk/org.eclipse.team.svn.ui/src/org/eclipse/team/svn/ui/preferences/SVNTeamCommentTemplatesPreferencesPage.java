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

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SpellcheckedTextProvider;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.EditCommentTemplatePanel;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IntegerFieldVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.ui.PlatformUI;

/**
 * Comments templates preferences page implementation
 *
 * @author Sergiy Logvin
 */
public class SVNTeamCommentTemplatesPreferencesPage extends AbstractSVNTeamPreferencesPage implements ISelectionChangedListener {
	protected StyledText previewText;
	protected ListViewer listViewer;
	protected Button newButton;
	protected Button editButton;
	protected Button removeButton;
	protected Button useLogTemplatesButton;
	protected Button useTemplatesButton;
	protected int savedCommentsCount;
	protected Text savedCommentsCountField;
	protected boolean logTemplatesEnabled;
	protected boolean userTemplatesEnabled;
	
	public SVNTeamCommentTemplatesPreferencesPage() {
		super();
	}
	
	protected void saveValues(IPreferenceStore store) {
		SVNTeamPreferences.setCommentTemplatesInt(store, SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME, this.savedCommentsCount);
		SVNTeamPreferences.setCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME, this.logTemplatesEnabled);
		SVNTeamPreferences.setCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME, this.userTemplatesEnabled);
		
		int numTemplates = this.listViewer.getList().getItemCount();
		String[] templates = new String[numTemplates];
		for (int i = 0; i < numTemplates; i++) {
			templates[i] = (String)this.listViewer.getElementAt(i);
		}
		SVNTeamPreferences.setCommentTemplatesString(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME, FileUtility.encodeArrayToString(templates));
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.savedCommentsCount = SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.savedCommentsCount = SVNTeamPreferences.getCommentTemplatesInt(store, SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME);
		this.logTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME);
		this.userTemplatesEnabled = SVNTeamPreferences.getCommentTemplatesBoolean(store, SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME);
	}
	
	protected void initializeControls() {
		this.savedCommentsCountField.setText(String.valueOf(this.savedCommentsCount));
		this.useLogTemplatesButton.setSelection(this.logTemplatesEnabled);
		this.useTemplatesButton.setSelection(this.userTemplatesEnabled);
		
		this.listViewer.getControl().setEnabled(this.userTemplatesEnabled);
		this.newButton.setEnabled(this.userTemplatesEnabled);
		this.previewText.setEnabled(this.userTemplatesEnabled);
		
		IStructuredSelection selection = (IStructuredSelection) this.listViewer.getSelection();
		this.editButton.setEnabled(this.userTemplatesEnabled && selection.size() == 1);
		this.removeButton.setEnabled(this.userTemplatesEnabled && selection.size() > 0);
		this.previewText.setText(selection.size() == 1 ? (String)selection.getFirstElement() : "");
	}
	
	protected Control createContentsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite checkBoxComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		checkBoxComposite.setLayout(layout);
		checkBoxComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(checkBoxComposite, SWT.NONE);
		GridData data = new GridData();
		label.setLayoutData(data);
		String labelText = SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.historySavedCommentsCount");
		label.setText(labelText);
		
		this.savedCommentsCountField = new Text(checkBoxComposite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.savedCommentsCountField.setLayoutData(data);
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new IntegerFieldVerifier(labelText, true));
		this.attachTo(this.savedCommentsCountField, verifier);
		this.savedCommentsCountField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					SVNTeamCommentTemplatesPreferencesPage.this.savedCommentsCount = Integer.parseInt(SVNTeamCommentTemplatesPreferencesPage.this.savedCommentsCountField.getText());
				}
				catch (Exception ex) {
				}
			}
		});
		
		this.useLogTemplatesButton = new Button(checkBoxComposite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		this.useLogTemplatesButton.setLayoutData(data);
		this.useLogTemplatesButton.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.LogTemplates"));
		this.useLogTemplatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamCommentTemplatesPreferencesPage.this.logTemplatesEnabled = ((Button)e.widget).getSelection();
			}
		});
		
		this.useTemplatesButton = new Button(checkBoxComposite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		this.useTemplatesButton.setLayoutData(data);
		this.useTemplatesButton.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.UserTemplates"));
		this.useTemplatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamCommentTemplatesPreferencesPage.this.selectionChanged(null);
			}
		});

		Label separator = new Label(checkBoxComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		separator.setLayoutData(data);
		
		Label templatesLabel = new Label(composite, SWT.NONE);
		templatesLabel.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.EditHint"));

		this.createTemplatesList(composite);
		
		Dialog.applyDialogFont(parent);

//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.commentTemplatesPreferencesContext");
		
		return composite;
	}

	protected Composite createTemplatesList(Composite parent) {
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

		Label previewLabel = new Label(listAndButtons, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		previewLabel.setLayoutData(data);
		previewLabel.setText(SVNTeamUIPlugin.instance().getResource("CommentTemplatesPreferencePage.ViewHint"));
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = this.convertHeightInCharsToPixels(5);
		this.previewText = SpellcheckedTextProvider.getTextWidget(listAndButtons, data, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.previewText.setEditable(false);
		
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

	public void selectionChanged(SelectionChangedEvent event) {
		this.userTemplatesEnabled = this.useTemplatesButton.getSelection();
		this.listViewer.getControl().setEnabled(this.userTemplatesEnabled);
		this.newButton.setEnabled(this.userTemplatesEnabled);
		this.previewText.setEnabled(this.userTemplatesEnabled);
		
		IStructuredSelection selection = (IStructuredSelection) this.listViewer.getSelection();
		this.editButton.setEnabled(this.userTemplatesEnabled && selection.size() == 1);
		this.removeButton.setEnabled(this.userTemplatesEnabled && selection.size() > 0);
		this.previewText.setText(selection.size() == 1 ? (String)selection.getFirstElement() : "");
	}
	
}
