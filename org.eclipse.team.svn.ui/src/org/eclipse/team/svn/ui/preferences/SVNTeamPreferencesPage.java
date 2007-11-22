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

package org.eclipse.team.svn.ui.preferences;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IntegerFieldVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.ui.PlatformUI;

/**
 * Main SVN Team preferences page
 * 
 * @author Alexander Gurov
 */
public class SVNTeamPreferencesPage extends AbstractSVNTeamPreferencesPage {
	protected String head;
	protected String branches;
	protected String tags;
	protected boolean showExternals;
	protected boolean reportRevisionChange;
	protected boolean fastReport;
	protected boolean showMultilineComment;
	protected boolean showAffectedPaths;
	protected boolean pagingEnable;
	protected int pageSize;
	protected boolean usePropertiesView;
	protected boolean mailReporterEnabled;
	protected boolean mailReporterErrorsEnabled;
	protected boolean commitSelectNewResources;
	protected boolean detectDeletedProjects;
	protected boolean useSubversionExternalsBehaviour;
	protected String svnConnector;
	protected ISVNConnectorFactory []factories;
	protected boolean useJavaHLMerge;
	protected boolean checkoutUsingDotProjectName;
	protected boolean branchTagConsiderStructure;
	protected boolean forceExternalsFreeze;
	protected boolean computeKeywordsValues;
	protected boolean enableAutoShare;
	protected boolean caseInsensitiveSorting;
	
	protected Text headField;
	protected Text branchesField;
	protected Text tagsField;
	protected Button showExternalsButton;
	protected Combo svnConnectorField;
	protected Button useInteractiveMergeButton;
	protected Button reportRevisionChangeButton;
	protected Button fastReportButton;
	protected Button showMultilineCommentButton;
	protected Button showAffectedPathsButton;
	protected Button enablePagingButton;
	protected Text pageSizeField;
	protected Button usePropertiesViewButton;
	protected Button usePropertiesEditorButton;
	protected Button mailReporterEnabledButton;
	protected Button mailReporterErrorsEnabledButton;
	protected Button btnResourceSelectionNew;
	protected Button btnDetectDeletedProjects;
	protected Button btnResourceSelectionExternal;
	protected Button checkoutUsingDotProjectNameButton;
	protected Button branchTagConsiderStructureButton;
	protected Button branchTagManualUrlEditButton;
	protected Button computeKeywordsValuesButton;
	protected Button enableAutoShareButton;
	protected Button caseInsensitiveSortingButton;
	protected Button forceExternalsFreezeButton;

	public SVNTeamPreferencesPage() {
		super();
	}

	protected void saveValues(IPreferenceStore store) {
		SVNTeamPreferences.setRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME, this.head);
		SVNTeamPreferences.setRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME, this.branches);
		SVNTeamPreferences.setRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME, this.tags);
		SVNTeamPreferences.setRepositoryBoolean(store, SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME, this.branchTagConsiderStructure);
		SVNTeamPreferences.setRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME, this.forceExternalsFreeze);
		SVNTeamPreferences.setRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME, this.showExternals);
		
		SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME, this.reportRevisionChange);
		SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME, this.fastReport);

		SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME, this.showMultilineComment);
		SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME, this.showAffectedPaths);
		SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME, this.pageSize);
		SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME, this.pagingEnable);
		
		SVNTeamPreferences.setMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ENABLED_NAME, this.mailReporterEnabled);
		SVNTeamPreferences.setMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME, this.mailReporterErrorsEnabled);
		
		SVNTeamPreferences.setResourceSelectionBoolean(store, SVNTeamPreferences.DETECT_DELETED_PROJECTS_NAME, this.detectDeletedProjects);
		SVNTeamPreferences.setResourceSelectionBoolean(store, SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME, this.commitSelectNewResources);
		SVNTeamPreferences.setResourceSelectionBoolean(store, SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME, this.useSubversionExternalsBehaviour);
		
		String oldId = CoreExtensionsManager.instance().getSVNConnectorFactory().getId();
		if (!oldId.equals(this.svnConnector)) {
			SVNTeamPreferences.setCoreString(store, SVNTeamPlugin.CORE_SVNCLIENT_NAME, this.svnConnector);
			// destroy all cached proxies
			SVNRemoteStorage.instance().dispose();
		}
		
		SVNTeamPreferences.setMergeBoolean(store, SVNTeamPreferences.MERGE_USE_JAVAHL_NAME, this.useJavaHLMerge);
		
		SVNTeamPreferences.setCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME, this.checkoutUsingDotProjectName);
		
		SVNTeamPreferences.setKeywordsBoolean(store, SVNTeamPreferences.COMPUTE_KEYWORDS_NAME, this.computeKeywordsValues);
		
		SVNTeamPreferences.setShareBoolean(store, SVNTeamPreferences.SHARE_ENABLE_AUTO_NAME, this.enableAutoShare);
		
		SVNTeamPreferences.setPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_USE_VIEW_NAME, this.usePropertiesView);
		
		SVNTeamPreferences.setTableSortingBoolean(store, SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME, this.caseInsensitiveSorting);
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.head = SVNTeamPreferences.REPOSITORY_HEAD_DEFAULT;
		this.branches = SVNTeamPreferences.REPOSITORY_BRANCHES_DEFAULT;
		this.tags = SVNTeamPreferences.REPOSITORY_TAGS_DEFAULT;
		this.showExternals = SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT;
		
		this.reportRevisionChange = SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_DEFAULT;
		this.fastReport = SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT;
		
		this.showMultilineComment = SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT;
		this.showAffectedPaths = SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_DEFAULT;
		this.pagingEnable = SVNTeamPreferences.HISTORY_PAGING_ENABLE_DEFAULT;
		this.pageSize = SVNTeamPreferences.HISTORY_PAGE_SIZE_DEFAULT;
		
		this.usePropertiesView = SVNTeamPreferences.PROPERTY_USE_VIEW_DEFAULT;
		this.mailReporterEnabled = SVNTeamPreferences.MAILREPORTER_ENABLED_DEFAULT;
		this.mailReporterErrorsEnabled = SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT;
		
		this.commitSelectNewResources = SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_DEFAULT;
		this.detectDeletedProjects = SVNTeamPreferences.DETECT_DELETED_PROJECTS_DEFAULT;
		this.useSubversionExternalsBehaviour = SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT;
		
		this.useJavaHLMerge = SVNTeamPreferences.MERGE_USE_JAVAHL_DEFAULT;
		
		this.checkoutUsingDotProjectName = SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT;
		
		this.branchTagConsiderStructure = SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT;
		this.forceExternalsFreeze = SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT;
		
		this.computeKeywordsValues = SVNTeamPreferences.COMPUTE_KEYWORDS_DEFAULT;
		
		this.enableAutoShare = SVNTeamPreferences.SHARE_ENABLE_AUTO_DEFAULT;
		
		this.caseInsensitiveSorting = SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_DEFAULT;
		
		this.svnConnector = SVNTeamPreferences.CORE_SVNCONNECTOR_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.head = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME);
		this.branches = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME);
		this.tags = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME);
		this.showExternals = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME);
		
		this.reportRevisionChange = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME);
		this.fastReport = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME);
		
		this.showMultilineComment = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
		this.showAffectedPaths = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
		this.pagingEnable = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME);
		this.pageSize = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
		
		this.usePropertiesView = SVNTeamPreferences.getPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_USE_VIEW_NAME);
		this.mailReporterEnabled = SVNTeamPreferences.getMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ENABLED_NAME);
		this.mailReporterErrorsEnabled = SVNTeamPreferences.getMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME);
		
		this.detectDeletedProjects = SVNTeamPreferences.getResourceSelectionBoolean(store, SVNTeamPreferences.DETECT_DELETED_PROJECTS_NAME);
		this.commitSelectNewResources = SVNTeamPreferences.getResourceSelectionBoolean(store, SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME);
		this.useSubversionExternalsBehaviour = SVNTeamPreferences.getResourceSelectionBoolean(store, SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME);
		
		this.useJavaHLMerge = SVNTeamPreferences.getMergeBoolean(store, SVNTeamPreferences.MERGE_USE_JAVAHL_NAME);
		
		this.checkoutUsingDotProjectName = SVNTeamPreferences.getCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME);
		
		this.branchTagConsiderStructure = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		this.forceExternalsFreeze = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME);
		
		this.computeKeywordsValues = SVNTeamPreferences.getKeywordsBoolean(store, SVNTeamPreferences.COMPUTE_KEYWORDS_NAME);
		
		this.enableAutoShare = SVNTeamPreferences.getShareBoolean(store, SVNTeamPreferences.SHARE_ENABLE_AUTO_NAME);
		
		this.caseInsensitiveSorting = SVNTeamPreferences.getTableSortingBoolean(store, SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME);
		
		//Client specified in preferences currently may be uninstalled. So, request real used connector instead of saved.
		this.svnConnector = CoreExtensionsManager.instance().getSVNConnectorFactory().getId();
	}
	
	protected void initializeControls() {
		this.headField.setText(this.head);
		this.branchesField.setText(this.branches);
		this.tagsField.setText(this.tags);
		this.showExternalsButton.setSelection(this.showExternals);
		
		this.reportRevisionChangeButton.setEnabled((CoreExtensionsManager.instance().getSVNConnectorFactory(this.svnConnector).getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.REPORT_REVISION_CHANGE) != 0);
		this.reportRevisionChangeButton.setSelection(this.reportRevisionChange);
		this.fastReportButton.setSelection(this.fastReport);
		
		this.showMultilineCommentButton.setSelection(this.showMultilineComment);
		this.showAffectedPathsButton.setSelection(this.showAffectedPaths);
		this.pageSizeField.setText(String.valueOf(this.pageSize));
		this.enablePagingButton.setSelection(this.pagingEnable);
		this.pageSizeField.setEnabled(this.pagingEnable);
		
		this.usePropertiesViewButton.setSelection(this.usePropertiesView);
		this.usePropertiesEditorButton.setSelection(!this.usePropertiesViewButton.getSelection());
		
		this.mailReporterEnabledButton.setSelection(this.mailReporterEnabled);
		this.mailReporterErrorsEnabledButton.setSelection(this.mailReporterErrorsEnabled);
		
		this.btnDetectDeletedProjects.setSelection(this.detectDeletedProjects);
		this.btnResourceSelectionNew.setSelection(this.commitSelectNewResources);
		this.btnResourceSelectionExternal.setSelection(this.useSubversionExternalsBehaviour);
		
		List factoriesList = Arrays.asList(this.factories);
		this.svnConnectorField.select(factoriesList.indexOf(CoreExtensionsManager.instance().getSVNConnectorFactory(this.svnConnector)));
		
		this.initializeClientSettings();
		
		this.checkoutUsingDotProjectNameButton.setSelection(this.checkoutUsingDotProjectName);
		
		this.branchTagConsiderStructureButton.setSelection(this.branchTagConsiderStructure);
		this.forceExternalsFreezeButton.setSelection(this.forceExternalsFreeze);
		
		this.branchTagManualUrlEditButton.setSelection(!this.branchTagConsiderStructure);
		
		this.computeKeywordsValuesButton.setSelection(this.computeKeywordsValues);
		
		this.enableAutoShareButton.setSelection(this.enableAutoShare);
		
		this.caseInsensitiveSortingButton.setSelection(this.caseInsensitiveSorting);
	}
	
	protected void initializeClientSettings() {
		this.useInteractiveMergeButton.setSelection(!this.useJavaHLMerge);
	}
	
	protected Control createContentsImpl(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.generalTabName"));
		tabItem.setControl(this.createGeneralPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.svnConnectorTabName"));
		tabItem.setControl(this.createSVNClientPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.repositoryTabName"));
		tabItem.setControl(this.createRepositorySettingsPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.viewSettingsTabName"));
		tabItem.setControl(this.createViewSettingsPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mailReporterGroupName"));
		tabItem.setControl(this.createErrorReportingSettingsPage(tabFolder));

//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.SVNPreferencesContext");
	    
		return tabFolder;
	}
	
	protected Control createSVNClientPage(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.svnConnectorPrompt"));
		
		label = new Label(composite, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.svnConnector"));
		
		this.svnConnectorField = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.svnConnectorField.setLayoutData(data);
		Collection fullSet = CoreExtensionsManager.instance().getAccessibleClients();
		this.factories = (ISVNConnectorFactory [])fullSet.toArray(new ISVNConnectorFactory[fullSet.size()]);
		FileUtility.sort(this.factories, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((ISVNConnectorFactory)o1).getName().compareTo(((ISVNConnectorFactory)o2).getName());
			}
		});
		String []items = new String[fullSet.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = this.factories[i].getName() + " (" + this.factories[i].getClientVersion().replace('\n', ' ') + ")";
		}
		this.svnConnectorField.setItems(items);
		this.svnConnectorField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.svnConnector = SVNTeamPreferencesPage.this.factories[SVNTeamPreferencesPage.this.svnConnectorField.getSelectionIndex()].getId();
				SVNTeamPreferencesPage.this.initializeClientSettings();
				SVNTeamPreferencesPage.this.reportRevisionChangeButton.setEnabled((CoreExtensionsManager.instance().getSVNConnectorFactory(SVNTeamPreferencesPage.this.svnConnector).getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.REPORT_REVISION_CHANGE) != 0);
			}
		});
		
		// Merge settings group
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mergeGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mergePrompt"));
		
		this.useInteractiveMergeButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.useInteractiveMergeButton.setLayoutData(data);
		this.useInteractiveMergeButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mergeUseInteractiveMerge"));
		this.useInteractiveMergeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.useJavaHLMerge = !SVNTeamPreferencesPage.this.useInteractiveMergeButton.getSelection();
			}
		});
		
		return composite;
	}

	protected Control createGeneralPage(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		// Commit settings group
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.resourceSelectionGroupName"));
		
		Label label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.resourceSelectionPrompt"));
		
		this.btnDetectDeletedProjects = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnDetectDeletedProjects.setLayoutData(data);
		this.btnDetectDeletedProjects.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.detectDeletedProjects"));
		this.btnDetectDeletedProjects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.detectDeletedProjects = SVNTeamPreferencesPage.this.btnDetectDeletedProjects.getSelection();
			}
		});
		
		this.btnResourceSelectionNew = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnResourceSelectionNew.setLayoutData(data);
		this.btnResourceSelectionNew.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.resourceSelectionNew"));
		this.btnResourceSelectionNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.commitSelectNewResources = SVNTeamPreferencesPage.this.btnResourceSelectionNew.getSelection();
			}
		});
		
		this.btnResourceSelectionExternal = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnResourceSelectionExternal.setLayoutData(data);
		this.btnResourceSelectionExternal.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.resourceSelectionExternal"));
		this.btnResourceSelectionExternal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.useSubversionExternalsBehaviour = SVNTeamPreferencesPage.this.btnResourceSelectionExternal.getSelection();
			}
		});
		
		// Project share settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.shareGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.sharePrompt"));
		
		this.enableAutoShareButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.enableAutoShareButton.setLayoutData(data);
		this.enableAutoShareButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.shareEnableAuto"));
		this.enableAutoShareButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.enableAutoShare = SVNTeamPreferencesPage.this.enableAutoShareButton.getSelection();
			}
		});
		
		// Keywords property settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.keywordsGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.keywordsPrompt"));
		
		this.computeKeywordsValuesButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.computeKeywordsValuesButton.setLayoutData(data);
		this.computeKeywordsValuesButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.keywordsComputeRecursively"));
		this.computeKeywordsValuesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.computeKeywordsValues = SVNTeamPreferencesPage.this.computeKeywordsValuesButton.getSelection();
			}
		});
		
		// Table sorting settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.tableSortingGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.tableSortingPrompt"));
		
		this.caseInsensitiveSortingButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.caseInsensitiveSortingButton.setLayoutData(data);
		this.caseInsensitiveSortingButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.tableSortingCaseInsensitive"));
		this.caseInsensitiveSortingButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.caseInsensitiveSorting = SVNTeamPreferencesPage.this.caseInsensitiveSortingButton.getSelection();
			}
		});
		
		return composite;
	}
	
	protected Control createViewSettingsPage(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Group synchViewGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		synchViewGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		synchViewGroup.setLayoutData(data);	
		
		Group historyViewGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		historyViewGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		historyViewGroup.setLayoutData(data);
		
		Group propertiesViewGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		propertiesViewGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		propertiesViewGroup.setLayoutData(data);		
		
		//Synchronize View group
		synchViewGroup.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.synchronizeGroupName"));
		
		Label label = new Label(synchViewGroup, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.synchronizePrompt"));
		
		this.reportRevisionChangeButton = new Button(synchViewGroup, SWT.CHECK);
		data = new GridData();
		this.reportRevisionChangeButton.setLayoutData(data);
		this.reportRevisionChangeButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.synchronizeReportRevisionChangeName"));
		this.reportRevisionChangeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.reportRevisionChange = SVNTeamPreferencesPage.this.reportRevisionChangeButton.getSelection();
			}
		});
		this.reportRevisionChangeButton.setEnabled((CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.REPORT_REVISION_CHANGE) != 0);
		
		this.fastReportButton = new Button(synchViewGroup, SWT.CHECK);
		data = new GridData();
		this.fastReportButton.setLayoutData(data);
		this.fastReportButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.synchronizeFastReportName"));
		this.fastReportButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.fastReport = SVNTeamPreferencesPage.this.fastReportButton.getSelection();
			}
		});
		
		//History View group
		historyViewGroup.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.historyGroupName"));
		
		label = new Label(historyViewGroup, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.historyPrompt"));
		
		this.showMultilineCommentButton = new Button(historyViewGroup, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.showMultilineCommentButton.setLayoutData(data);
		this.showMultilineCommentButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.historyShowMultilineComment"));
		this.showMultilineCommentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.showMultilineComment = SVNTeamPreferencesPage.this.showMultilineCommentButton.getSelection();
			}
		});
		
		this.showAffectedPathsButton = new Button(historyViewGroup, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.showAffectedPathsButton.setLayoutData(data);
		this.showAffectedPathsButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.historyShowAffectedPaths"));
		this.showAffectedPathsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.showAffectedPaths = SVNTeamPreferencesPage.this.showAffectedPathsButton.getSelection();
			}
		});
		
		this.enablePagingButton = new Button(historyViewGroup, SWT.CHECK);
		data = new GridData();
		this.enablePagingButton.setLayoutData(data);
		String labelText = SVNTeamUIPlugin.instance().getResource("MainPreferencePage.historyEnablePaging");
		this.enablePagingButton.setText(labelText);
		this.enablePagingButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (SVNTeamPreferencesPage.this.enablePagingButton.getSelection()) {
					SVNTeamPreferencesPage.this.pageSizeField.setEnabled(true);
					int tmpPageSize = Integer.parseInt(SVNTeamPreferencesPage.this.pageSizeField.getText());
					SVNTeamPreferencesPage.this.pageSize = tmpPageSize == 0 ? SVNTeamPreferences.HISTORY_PAGE_SIZE_DEFAULT : tmpPageSize;
					SVNTeamPreferencesPage.this.pageSizeField.setText(String.valueOf(SVNTeamPreferencesPage.this.pageSize));
					SVNTeamPreferencesPage.this.pagingEnable = true;
				}
				else {
					SVNTeamPreferencesPage.this.pagingEnable = false;
					SVNTeamPreferencesPage.this.pageSizeField.setEnabled(false);
				}
			}
		});
		this.enablePagingButton.setSelection(this.pagingEnable);
		
		this.pageSizeField = new Text(historyViewGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.pageSizeField.setLayoutData(data);
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new IntegerFieldVerifier(labelText, true));
		this.attachTo(this.pageSizeField, verifier);
		this.pageSizeField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					SVNTeamPreferencesPage.this.pageSize = Integer.parseInt(SVNTeamPreferencesPage.this.pageSizeField.getText());
				}
				catch (Exception ex) {

				}
			}
		});
		
		//Properties View group
		propertiesViewGroup.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.propertiesGroupName"));
		
		label = new Label(propertiesViewGroup, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.propertiesPrompt"));
		
		this.usePropertiesViewButton = new Button(propertiesViewGroup, SWT.RADIO);
		this.usePropertiesViewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.usePropertiesViewButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.propertiesUseView"));
		this.usePropertiesViewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.usePropertiesView = SVNTeamPreferencesPage.this.usePropertiesViewButton.getSelection();
			}
		});
		
		this.usePropertiesEditorButton = new Button(propertiesViewGroup, SWT.RADIO);
		this.usePropertiesEditorButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.usePropertiesEditorButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.propertiesUseEditor"));
		this.usePropertiesEditorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.usePropertiesView = SVNTeamPreferencesPage.this.usePropertiesViewButton.getSelection();
			}
		});
		
		return composite;
	}
	
	protected Control createRepositorySettingsPage(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Group group = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.structureGroupName"));
		
		Label label = new Label(group, SWT.NULL);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.repositoryPrompt"));
		
		label = new Label(group, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		String labelText = SVNTeamUIPlugin.instance().getResource("MainPreferencePage.repositoryHeadName");
		label.setText(labelText);
		
		this.headField = new Text(group, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.headField.setLayoutData(data);
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new ResourceNameVerifier(labelText, false));
		this.attachTo(this.headField, verifier);
		this.headField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SVNTeamPreferencesPage.this.head = SVNTeamPreferencesPage.this.headField.getText();
			}
		});

		label = new Label(group, SWT.NULL);
		label.setLayoutData(new GridData());
		labelText = SVNTeamUIPlugin.instance().getResource("MainPreferencePage.repositoryBranchesName");
		label.setText(labelText);
		
		this.branchesField = new Text(group, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.branchesField.setLayoutData(data);
		verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new ResourceNameVerifier(labelText, false));
		this.attachTo(this.branchesField, verifier);
		this.branchesField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SVNTeamPreferencesPage.this.branches = SVNTeamPreferencesPage.this.branchesField.getText();
			}
		});

		label = new Label(group, SWT.NULL);
		label.setLayoutData(new GridData());
		labelText = SVNTeamUIPlugin.instance().getResource("MainPreferencePage.repositoryTagsName");
		label.setText(labelText);
		
		this.tagsField = new Text(group, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		this.tagsField.setLayoutData(data);
		verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new ResourceNameVerifier(labelText, false));
		this.attachTo(this.tagsField, verifier);
		this.tagsField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SVNTeamPreferencesPage.this.tags = SVNTeamPreferencesPage.this.tagsField.getText();
			}
		});

		this.showExternalsButton = new Button(group, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		this.showExternalsButton.setLayoutData(data);
		this.showExternalsButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.showExternals"));
		this.showExternalsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.showExternals = ((Button)e.widget).getSelection();
			}
		});
		
		// Checkout settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.checkoutGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.checkoutPrompt"));
		
		this.checkoutUsingDotProjectNameButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.checkoutUsingDotProjectNameButton.setLayoutData(data);
		this.checkoutUsingDotProjectNameButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.checkoutUsingDotProjectName"));
		this.checkoutUsingDotProjectNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.checkoutUsingDotProjectName = SVNTeamPreferencesPage.this.checkoutUsingDotProjectNameButton.getSelection();
			}
		});
		
//		Branch/tag settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.branchTagGroupName"));
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.branchTagPrompt"));
		
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.branchTagConsiderStructure = SVNTeamPreferencesPage.this.branchTagConsiderStructureButton.getSelection();
			}
		};
		this.branchTagConsiderStructureButton = new Button(group, SWT.RADIO);
		this.branchTagConsiderStructureButton.setLayoutData(new GridData());
		this.branchTagConsiderStructureButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.branchTagConsiderStructureLabel"));
		this.branchTagConsiderStructureButton.addSelectionListener(listener);
		
		this.branchTagManualUrlEditButton = new Button(group, SWT.RADIO);
		this.branchTagManualUrlEditButton.setLayoutData(new GridData());
		this.branchTagManualUrlEditButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.branchTagManualLabel"));
		this.branchTagManualUrlEditButton.addSelectionListener(listener);
		
		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);
		
		this.forceExternalsFreezeButton = new Button(group, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.forceExternalsFreezeButton.setLayoutData(data);
		this.forceExternalsFreezeButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.forceFreezeExternals"));
		this.forceExternalsFreezeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.forceExternalsFreeze = ((Button)e.widget).getSelection();
			}
		});
		
		return composite;
	}
	
	protected Control createErrorReportingSettingsPage(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mailReporterPrompt"));
		
		this.mailReporterEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		this.mailReporterEnabledButton.setLayoutData(data);
		this.mailReporterEnabledButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mailReporterEnabledName"));
		this.mailReporterEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.mailReporterEnabled = SVNTeamPreferencesPage.this.mailReporterEnabledButton.getSelection();
			}
		});
		
		this.mailReporterErrorsEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		this.mailReporterErrorsEnabledButton.setLayoutData(data);
		this.mailReporterErrorsEnabledButton.setText(SVNTeamUIPlugin.instance().getResource("MainPreferencePage.mailReporterErrorsEnabledName"));
		this.mailReporterErrorsEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.mailReporterErrorsEnabled = SVNTeamPreferencesPage.this.mailReporterErrorsEnabledButton.getSelection();
			}
		});
		
		return composite;
	}
	
}
