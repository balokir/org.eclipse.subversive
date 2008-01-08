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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

/**
 * Annotation preferences page 
 * 
 * @author Alexander Gurov
 */
public class SVNTeamAnnotatePreferencesPage extends AbstractSVNTeamPreferencesPage {
	protected ColorFieldEditor rgbEditor;
	protected Composite rgbParent;

	protected int viewType;
	protected String perspective;
	protected IPerspectiveDescriptor []perspectives;
	protected int perspectiveType;
	protected boolean useOneRGB;
	
	protected Button useOneRGBButton;
	protected Button defaultViewButton;
	protected Button quickDiffViewButton;
	protected Button promptViewButton;
	protected Button defaultPerspectiveButton;
	protected Button currentPerspectiveButton;
	protected Button promptPerspectiveButton;
	protected Combo perspectiveField;
	
	public SVNTeamAnnotatePreferencesPage() {
		super();
		this.setDescription(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.desc"));
	}
	
	protected void saveValues(IPreferenceStore store) {
		this.rgbEditor.store();

		SVNTeamPreferences.setAnnotateBoolean(store, SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_NAME, this.useOneRGB);
		SVNTeamPreferences.setAnnotateInt(store, SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_NAME, this.viewType);
		SVNTeamPreferences.setAnnotateInt(store, SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME, this.perspectiveType);
		SVNTeamPreferences.setAnnotateString(store, SVNTeamPreferences.ANNOTATE_PERSPECTIVE_NAME, this.perspective);
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.rgbEditor.loadDefault();
		
		this.viewType = SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_DEFAULT;
		this.useOneRGB = SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_DEFAULT;
		this.perspectiveType = SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_DEFAULT;
		this.perspective = SVNTeamPreferences.ANNOTATE_PERSPECTIVE_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.rgbEditor.load();
		
		this.viewType = SVNTeamPreferences.getAnnotateInt(store, SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_NAME);
		this.useOneRGB = SVNTeamPreferences.getAnnotateBoolean(store, SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_NAME);
		this.perspectiveType = SVNTeamPreferences.getAnnotateInt(store, SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME);
		this.perspective = SVNTeamPreferences.getAnnotateString(store, SVNTeamPreferences.ANNOTATE_PERSPECTIVE_NAME);
	}
	
	protected void initializeControls() {
		this.defaultViewButton.setSelection(false);
		this.quickDiffViewButton.setSelection(false);
		this.promptViewButton.setSelection(false);
		if (this.viewType == SVNTeamPreferences.ANNOTATE_DEFAULT_VIEW) {
			this.defaultViewButton.setSelection(true);
		}
		else if (this.viewType == SVNTeamPreferences.ANNOTATE_QUICK_DIFF_VIEW) {
			this.quickDiffViewButton.setSelection(true);
		}
		else {
			this.promptViewButton.setSelection(true);
		}
		
		this.useOneRGBButton.setSelection(this.useOneRGB);
		this.rgbEditor.setEnabled(this.useOneRGB, this.rgbParent);
		
		this.defaultPerspectiveButton.setSelection(false);
		this.currentPerspectiveButton.setSelection(false);
		this.promptPerspectiveButton.setSelection(false);
		if (this.perspectiveType == SVNTeamPreferences.ANNOTATE_DEFAULT_PERSPECTIVE) {
			this.defaultPerspectiveButton.setSelection(true);
		}
		else if (this.perspectiveType == SVNTeamPreferences.ANNOTATE_CURRENT_PERSPECTIVE) {
			this.currentPerspectiveButton.setSelection(true);
		}
		else {
			this.promptPerspectiveButton.setSelection(true);
		}
		
		int idx = -1;
		for (int i = 0; i < this.perspectives.length; i++) {
			if (this.perspectives[i].getId().equals(this.perspective)) {
				idx = i;
				break;
			}
		}
		this.perspectiveField.select(idx);
	}
	
	protected Control createContentsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = false;
		composite.setLayoutData(data);
		
		Group perspectiveType = new Group(composite, SWT.NONE);
		perspectiveType.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textPerspectiveGroup"));
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.horizontalSpacing = 10;
		perspectiveType.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.grabExcessVerticalSpace = false;
		perspectiveType.setLayoutData(data);
		
		this.defaultPerspectiveButton = new Button(perspectiveType, SWT.RADIO);
		data = new GridData();
		this.defaultPerspectiveButton.setLayoutData(data);
		this.defaultPerspectiveButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textDefaultPerspective"));
		this.defaultPerspectiveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.defaultPerspectiveButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.perspectiveType = SVNTeamPreferences.ANNOTATE_DEFAULT_PERSPECTIVE;
				}
			}
		});
		
		this.currentPerspectiveButton = new Button(perspectiveType, SWT.RADIO);
		data = new GridData();
		this.currentPerspectiveButton.setLayoutData(data);
		this.currentPerspectiveButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textCurrentPerspective"));
		this.currentPerspectiveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.currentPerspectiveButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.perspectiveType = SVNTeamPreferences.ANNOTATE_CURRENT_PERSPECTIVE;
				}
			}
		});
		
		this.promptPerspectiveButton = new Button(perspectiveType, SWT.RADIO);
		data = new GridData();
		this.promptPerspectiveButton.setLayoutData(data);
		this.promptPerspectiveButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textPromptPerspective"));
		this.promptPerspectiveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.promptPerspectiveButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.perspectiveType = SVNTeamPreferences.ANNOTATE_PROMPT_PERSPECTIVE;
				}
			}
		});

		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		separator.setLayoutData(data);
		separator.setVisible(false);
		
		Label label = new Label(composite, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textSelectPerspective"));
		
		this.perspectiveField = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.perspectiveField.setLayoutData(data);
		this.perspectiveField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = SVNTeamAnnotatePreferencesPage.this.perspectiveField.getSelectionIndex();
				SVNTeamAnnotatePreferencesPage.this.perspective = SVNTeamAnnotatePreferencesPage.this.perspectives[idx].getId();
			}
		});
		
		this.perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives(); 
		Arrays.sort(this.perspectives, new Comparator() {
			public int compare(Object o1, Object o2) {
				IPerspectiveDescriptor first = (IPerspectiveDescriptor)o1;
				IPerspectiveDescriptor second = (IPerspectiveDescriptor)o2;
				return first.getLabel().compareTo(second.getLabel());
			}
		});
		String []labels = new String[this.perspectives.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = this.perspectives[i].getLabel();
		}
		this.perspectiveField.setItems(labels);
		
		Group showType = new Group(composite, SWT.NONE);
		showType.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textViewTypeGroup"));
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.horizontalSpacing = 10;
		showType.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.grabExcessVerticalSpace = false;
		showType.setLayoutData(data);
		
		this.quickDiffViewButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.quickDiffViewButton.setLayoutData(data);
		this.quickDiffViewButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textQuickDiffView"));
		this.quickDiffViewButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.quickDiffViewButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.viewType = SVNTeamPreferences.ANNOTATE_QUICK_DIFF_VIEW;
				}
			}
		});
		
		this.defaultViewButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.defaultViewButton.setLayoutData(data);
		this.defaultViewButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textDefaultView"));
		this.defaultViewButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.defaultViewButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.viewType = SVNTeamPreferences.ANNOTATE_DEFAULT_VIEW;
				}
			}
		});
		
		this.promptViewButton = new Button(showType, SWT.RADIO);
		data = new GridData();
		this.promptViewButton.setLayoutData(data);
		this.promptViewButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textPromptView"));
		this.promptViewButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamAnnotatePreferencesPage.this.promptViewButton.getSelection()) {
					SVNTeamAnnotatePreferencesPage.this.viewType = SVNTeamPreferences.ANNOTATE_PROMPT_VIEW;
				}
			}
		});

		separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		separator.setLayoutData(data);
		separator.setVisible(false);
		
		this.useOneRGBButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.useOneRGBButton.setLayoutData(data);
		this.useOneRGBButton.setText(SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textUseOneRGB"));
		this.useOneRGBButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				SVNTeamAnnotatePreferencesPage.this.useOneRGB = SVNTeamAnnotatePreferencesPage.this.useOneRGBButton.getSelection();
				SVNTeamAnnotatePreferencesPage.this.rgbEditor.setEnabled(SVNTeamAnnotatePreferencesPage.this.useOneRGB, SVNTeamAnnotatePreferencesPage.this.rgbParent);
			}
		});
		
		this.rgbEditor = new ColorFieldEditor(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_RGB_BASE_NAME), SVNTeamUIPlugin.instance().getResource("AnnotatePreferencePage.textRGB"), this.rgbParent = composite);
		this.rgbEditor.setPage(this);
		this.rgbEditor.setPreferenceStore(this.getPreferenceStore());
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.annotatePreferencesContext");
		
		return composite;
	}
	
}
