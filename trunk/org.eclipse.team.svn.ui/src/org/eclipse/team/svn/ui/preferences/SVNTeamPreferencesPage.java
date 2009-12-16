/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorHelper;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.DateFormatVerifier;
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
	protected boolean fastReport;
	protected boolean enableModelSync;
	protected boolean pagingEnable;
	protected boolean connectToCompareWith;
	protected int pageSize;
	protected int dateFormat;
	protected String dateFormatCustom;
	protected boolean mailReporterEnabled;
	protected boolean mailReporterErrorsEnabled;
	protected boolean commitSelectNewResources;
	protected boolean useSubversionExternalsBehaviour;
	protected boolean showSelectedResourcesInSyncPane;
	protected String svnConnector;
	protected ISVNConnectorFactory []factories;
	protected boolean useJavaHLMerge;
	protected boolean includeMergedRevisions;
	protected boolean checkoutUsingDotProjectName;
	protected boolean checkoutRespectProjectStructure;
	protected boolean branchTagConsiderStructure;
	protected boolean forceExternalsFreeze;
	protected boolean computeKeywordsValues;
	protected boolean enableAutoShare;
	protected boolean caseInsensitiveSorting;	
	protected String consultChangeSets;
	protected boolean ignoreExternals;
	
	protected Text headField;
	protected Text branchesField;
	protected Text tagsField;
	protected Button showExternalsButton;
	protected Combo svnConnectorField;
	protected Button useInteractiveMergeButton;
	protected Button includeMergedRevisionsButton;
	protected Button fastReportButton;
	protected Button enableModelSyncButton;
	protected Button enablePagingButton;
	protected Button connectToCompareWithButton;
	protected Text pageSizeField;
	protected Combo dateFormatField;
	protected Text dateFormatCustomField;
	protected Button mailReporterEnabledButton;
	protected Button mailReporterErrorsEnabledButton;
	protected Button btnResourceSelectionNew;
	protected Button btnResourceSelectionExternal;
	protected Button btnResourceSelectionPresentation;
	protected Button checkoutUsingDotProjectNameButton;
	protected Button checkoutRespectProjectStructureButton;
	protected Button branchTagConsiderStructureButton;
	protected Button branchTagManualUrlEditButton;
	protected Button computeKeywordsValuesButton;
	protected Button enableAutoShareButton;
	protected Button caseInsensitiveSortingButton;
	protected Button forceExternalsFreezeButton;
	protected Button consultCSAlwaysButton;
	protected Button consultCSNeverButton;
	protected Button consultCSPromptButton;
	protected Button ignoreExternalsButton;
	
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
				
		AbstractSVNSubscriber.setSynchInfoContigous(this.fastReport);
		SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.ENABLE_MODEL_SYNC_NAME, this.enableModelSync);
		
		SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME, this.pageSize);
		SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME, this.pagingEnable);
		SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_CONNECT_TO_COMPARE_WITH_NAME, this.connectToCompareWith);
		
		SVNTeamPreferences.setDateFormatInt(store, SVNTeamPreferences.DATE_FORMAT_NAME, this.dateFormat);
		SVNTeamPreferences.setDateFormatString(store, SVNTeamPreferences.DATE_FORMAT_CUSTOM_NAME, this.dateFormatCustom);
		
		SVNTeamPreferences.setConsultChangeSetsInCommit(store, SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT, this.consultChangeSets);
		
		SVNTeamPreferences.setMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ENABLED_NAME, this.mailReporterEnabled);
		SVNTeamPreferences.setMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME, this.mailReporterErrorsEnabled);
		
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_COMMIT_SELECT_NEW_RESOURCES_NAME, this.commitSelectNewResources);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_DO_NOT_SELECT_EXTERNALS_NAME, this.useSubversionExternalsBehaviour);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_SHOW_SELECTED_RESOURCES_IN_SYNC_PANE_NAME, this.showSelectedResourcesInSyncPane);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_ENABLE_AUTO_SHARE_NAME, this.enableAutoShare);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_COMPUTE_KEYWORDS_NAME, this.computeKeywordsValues);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME, this.caseInsensitiveSorting);
		SVNTeamPreferences.setBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME, this.ignoreExternals);
		
		String oldId = CoreExtensionsManager.instance().getSVNConnectorFactory().getId();
		if (!oldId.equals(this.svnConnector)) {
			SVNTeamPreferences.setCoreString(store, SVNTeamPlugin.CORE_SVNCLIENT_NAME, this.svnConnector);
			// destroy all cached proxies
			SVNRemoteStorage.instance().dispose();
		}
		
		SVNTeamPreferences.setMergeBoolean(store, SVNTeamPreferences.MERGE_USE_JAVAHL_NAME, this.useJavaHLMerge);
		SVNTeamPreferences.setMergeBoolean(store, SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME, this.includeMergedRevisions);
		
		SVNTeamPreferences.setCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME, this.checkoutUsingDotProjectName);
		SVNTeamPreferences.setCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_RESPECT_PROJECT_STRUCTURE_NAME, this.checkoutRespectProjectStructure);
	}
	
	protected void loadDefaultValues(IPreferenceStore store) {
		this.head = SVNTeamPreferences.REPOSITORY_HEAD_DEFAULT;
		this.branches = SVNTeamPreferences.REPOSITORY_BRANCHES_DEFAULT;
		this.tags = SVNTeamPreferences.REPOSITORY_TAGS_DEFAULT;
		this.showExternals = SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT;
						
		this.fastReport = Boolean.parseBoolean(AbstractSVNSubscriber.CONTIGOUS_REPORT_DEFAULT);
		this.enableModelSync = SVNTeamPreferences.ENABLE_MODEL_SYNC_DEFAULT;
		
		this.pagingEnable = SVNTeamPreferences.HISTORY_PAGING_ENABLE_DEFAULT;
		this.pageSize = SVNTeamPreferences.HISTORY_PAGE_SIZE_DEFAULT;
		this.connectToCompareWith = SVNTeamPreferences.HISTORY_CONNECT_TO_COMPARE_WITH_DEFAULT;
		
		this.dateFormat = SVNTeamPreferences.DATE_FORMAT_DEFAULT;
		this.dateFormatCustom = SVNTeamPreferences.DATE_FORMAT_CUSTOM_DEFAULT;
		
		this.consultChangeSets = SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_DEFAULT;
		
		this.mailReporterEnabled = SVNTeamPreferences.MAILREPORTER_ENABLED_DEFAULT;
		this.mailReporterErrorsEnabled = SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT;
		
		this.commitSelectNewResources = SVNTeamPreferences.BEHAVIOUR_COMMIT_SELECT_NEW_RESOURCES_DEFAULT;
		this.useSubversionExternalsBehaviour = SVNTeamPreferences.BEHAVIOUR_DO_NOT_SELECT_EXTERNAL_DEFAULT;
		this.showSelectedResourcesInSyncPane = SVNTeamPreferences.BEHAVIOUR_SHOW_SELECTED_RESOURCES_IN_SYNC_PANE_DEFAULT;
		
		this.useJavaHLMerge = SVNTeamPreferences.MERGE_USE_JAVAHL_DEFAULT;
		this.includeMergedRevisions = SVNTeamPreferences.MERGE_INCLUDE_MERGED_DEFAULT;
		
		this.checkoutUsingDotProjectName = SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT;
		this.checkoutRespectProjectStructure = SVNTeamPreferences.CHECKOUT_RESPECT_PROJECT_STRUCTURE_DEFAULT;
		
		this.branchTagConsiderStructure = SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT;
		this.forceExternalsFreeze = SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT;
		
		this.computeKeywordsValues = SVNTeamPreferences.BEHAVIOUR_COMPUTE_KEYWORDS_DEFAULT;
		
		this.enableAutoShare = SVNTeamPreferences.BEHAVIOUR_ENABLE_AUTO_SHARE_DEFAULT;
		
		this.caseInsensitiveSorting = SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_DEFAULT;
		
		this.ignoreExternals = SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_DEFAULT;
		
		this.svnConnector = SVNTeamPreferences.CORE_SVNCONNECTOR_DEFAULT;
	}
	
	protected void loadValues(IPreferenceStore store) {
		this.head = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME);
		this.branches = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME);
		this.tags = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME);
		this.showExternals = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME);
				
		this.fastReport = AbstractSVNSubscriber.getSynchInfoContigous();
		this.enableModelSync = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.ENABLE_MODEL_SYNC_NAME);
		
		this.connectToCompareWith = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_CONNECT_TO_COMPARE_WITH_NAME);
		this.pagingEnable = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME);
		this.pageSize = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
		
		this.dateFormat = SVNTeamPreferences.getDateFormatInt(store, SVNTeamPreferences.DATE_FORMAT_NAME);
		this.dateFormatCustom = SVNTeamPreferences.getDateFormatString(store, SVNTeamPreferences.DATE_FORMAT_CUSTOM_NAME);
		
		this.consultChangeSets = SVNTeamPreferences.getConsultChangeSetsInCommit(store, SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT);
		
		this.mailReporterEnabled = SVNTeamPreferences.getMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ENABLED_NAME);
		this.mailReporterErrorsEnabled = SVNTeamPreferences.getMailReporterBoolean(store, SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME);
		
		this.commitSelectNewResources = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_COMMIT_SELECT_NEW_RESOURCES_NAME);
		this.useSubversionExternalsBehaviour = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_DO_NOT_SELECT_EXTERNALS_NAME);
		this.showSelectedResourcesInSyncPane = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_SHOW_SELECTED_RESOURCES_IN_SYNC_PANE_NAME);
		this.enableAutoShare = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_ENABLE_AUTO_SHARE_NAME);
		this.computeKeywordsValues = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_COMPUTE_KEYWORDS_NAME);
		this.caseInsensitiveSorting = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME);
		this.ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
		
		this.useJavaHLMerge = SVNTeamPreferences.getMergeBoolean(store, SVNTeamPreferences.MERGE_USE_JAVAHL_NAME);
		this.includeMergedRevisions = SVNTeamPreferences.getMergeBoolean(store, SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME);
		
		this.checkoutUsingDotProjectName = SVNTeamPreferences.getCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME);
		this.checkoutRespectProjectStructure = SVNTeamPreferences.getCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_RESPECT_PROJECT_STRUCTURE_NAME);
		
		this.branchTagConsiderStructure = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		this.forceExternalsFreeze = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME);
		
		//Client specified in preferences currently may be uninstalled. So, request real used connector instead of saved.
		this.svnConnector = CoreExtensionsManager.instance().getSVNConnectorFactory().getId();
	}
	
	protected void initializeControls() {
		this.headField.setText(this.head);
		this.branchesField.setText(this.branches);
		this.tagsField.setText(this.tags);
		this.showExternalsButton.setSelection(this.showExternals);
		
		this.fastReportButton.setSelection(this.fastReport);
		this.enableModelSyncButton.setSelection(this.enableModelSync);
		
		this.pageSizeField.setText(String.valueOf(this.pageSize));
		this.enablePagingButton.setSelection(this.pagingEnable);
		this.pageSizeField.setEnabled(this.pagingEnable);
		this.connectToCompareWithButton.setSelection(this.connectToCompareWith);
		
		this.dateFormatField.select(this.dateFormat);
		this.dateFormatCustomField.setText(this.dateFormatCustom);
		
		this.consultCSAlwaysButton.setSelection(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_ALWAYS.equals(this.consultChangeSets));	
		this.consultCSNeverButton.setSelection(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_NEVER.equals(this.consultChangeSets));	
		this.consultCSPromptButton.setSelection(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_PROMPT.equals(this.consultChangeSets));			
		
		this.mailReporterEnabledButton.setSelection(this.mailReporterEnabled);
		this.mailReporterErrorsEnabledButton.setSelection(this.mailReporterErrorsEnabled);
		
		this.btnResourceSelectionNew.setSelection(this.commitSelectNewResources);
		this.btnResourceSelectionExternal.setSelection(this.useSubversionExternalsBehaviour);
		this.btnResourceSelectionPresentation.setSelection(this.showSelectedResourcesInSyncPane);
		
		List<ISVNConnectorFactory> factoriesList = Arrays.asList(this.factories);
		this.svnConnectorField.select(factoriesList.indexOf(CoreExtensionsManager.instance().getSVNConnectorFactory(this.svnConnector)));
		
		this.initializeClientSettings();
		
		this.checkoutUsingDotProjectNameButton.setSelection(this.checkoutUsingDotProjectName);
		this.checkoutRespectProjectStructureButton.setSelection(this.checkoutRespectProjectStructure);
		
		this.branchTagConsiderStructureButton.setSelection(this.branchTagConsiderStructure);
		this.forceExternalsFreezeButton.setSelection(this.forceExternalsFreeze);
		
		this.branchTagManualUrlEditButton.setSelection(!this.branchTagConsiderStructure);
		
		this.computeKeywordsValuesButton.setSelection(this.computeKeywordsValues);
		
		this.enableAutoShareButton.setSelection(this.enableAutoShare);
		
		this.caseInsensitiveSortingButton.setSelection(this.caseInsensitiveSorting);
		
		this.ignoreExternalsButton.setSelection(this.ignoreExternals);
	}
	
	protected void initializeClientSettings() {
		boolean isSVN15CompatibleConnector = CoreExtensionsManager.instance().getSVNConnectorFactory(this.svnConnector).getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
		this.useInteractiveMergeButton.setSelection(!this.useJavaHLMerge);
		this.includeMergedRevisionsButton.setSelection(this.includeMergedRevisions);
		this.includeMergedRevisionsButton.setEnabled(isSVN15CompatibleConnector);
	}
	
	protected Control createContentsImpl(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.MainPreferencePage_generalTabName);
		tabItem.setControl(this.createGeneralPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.MainPreferencePage_svnConnectorTabName);
		tabItem.setControl(this.createSVNConnectorsPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.MainPreferencePage_repositoryTabName);
		tabItem.setControl(this.createRepositorySettingsPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.MainPreferencePage_viewSettingsTabName);
		tabItem.setControl(this.createViewSettingsPage(tabFolder));
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.MainPreferencePage_mailReporterGroupName);
		tabItem.setControl(this.createErrorReportingSettingsPage(tabFolder));

//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.SVNPreferencesContext"); //$NON-NLS-1$
	    
		return tabFolder;
	}
	
	protected Control createSVNConnectorsPage(Composite parent) {
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
		label.setText(SVNUIMessages.MainPreferencePage_svnConnectorPrompt);
		
		label = new Label(composite, SWT.NONE);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_svnConnector);
		
		this.svnConnectorField = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 100;
		this.svnConnectorField.setLayoutData(data);
		Collection<?> fullSet = CoreExtensionsManager.instance().getAccessibleClients();
		this.factories = fullSet.toArray(new ISVNConnectorFactory[fullSet.size()]);
		Arrays.sort(this.factories, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((ISVNConnectorFactory)o1).getName().compareTo(((ISVNConnectorFactory)o2).getName());
			}
		});
		String []items = new String[fullSet.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = SVNConnectorHelper.getConnectorName(this.factories[i]);
		}
		this.svnConnectorField.setItems(items);
		this.svnConnectorField.setVisibleItemCount(items.length);
		this.svnConnectorField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.svnConnector = SVNTeamPreferencesPage.this.factories[SVNTeamPreferencesPage.this.svnConnectorField.getSelectionIndex()].getId();
				SVNTeamPreferencesPage.this.initializeClientSettings();
			}
		});
		
		// Merge settings group
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setText(SVNUIMessages.MainPreferencePage_mergeGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_mergePrompt);
		
		this.useInteractiveMergeButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.useInteractiveMergeButton.setLayoutData(data);
		this.useInteractiveMergeButton.setText(SVNUIMessages.MainPreferencePage_mergeUseInteractiveMerge);
		this.useInteractiveMergeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.useJavaHLMerge = !SVNTeamPreferencesPage.this.useInteractiveMergeButton.getSelection();
			}
		});
		
		label = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setVisible(false);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_includeMergedRevisionsLabel);
		
		this.includeMergedRevisionsButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.includeMergedRevisionsButton.setLayoutData(data);
		this.includeMergedRevisionsButton.setText(SVNUIMessages.MainPreferencePage_includeMergedRevisions);
		this.includeMergedRevisionsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.includeMergedRevisions = SVNTeamPreferencesPage.this.includeMergedRevisionsButton.getSelection();
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
		group.setText(SVNUIMessages.MainPreferencePage_resourceSelectionGroupName);
		
		Label label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_resourceSelectionPrompt);
		
		this.btnResourceSelectionNew = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnResourceSelectionNew.setLayoutData(data);
		this.btnResourceSelectionNew.setText(SVNUIMessages.MainPreferencePage_resourceSelectionNew);
		this.btnResourceSelectionNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.commitSelectNewResources = SVNTeamPreferencesPage.this.btnResourceSelectionNew.getSelection();
			}
		});
		
		this.btnResourceSelectionExternal = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnResourceSelectionExternal.setLayoutData(data);
		this.btnResourceSelectionExternal.setText(SVNUIMessages.MainPreferencePage_resourceSelectionExternal);
		this.btnResourceSelectionExternal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.useSubversionExternalsBehaviour = SVNTeamPreferencesPage.this.btnResourceSelectionExternal.getSelection();
			}
		});
		
		this.btnResourceSelectionPresentation = new Button(group, SWT.CHECK);
		data = new GridData();
		this.btnResourceSelectionPresentation.setLayoutData(data);
		this.btnResourceSelectionPresentation.setText(SVNUIMessages.MainPreferencePage_resourceSelectionPresentation);
		this.btnResourceSelectionPresentation.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.showSelectedResourcesInSyncPane = SVNTeamPreferencesPage.this.btnResourceSelectionPresentation.getSelection();
			}
		});
		
		// Project share settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_shareGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_sharePrompt);
		
		this.enableAutoShareButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.enableAutoShareButton.setLayoutData(data);
		this.enableAutoShareButton.setText(SVNUIMessages.MainPreferencePage_shareEnableAuto);
		this.enableAutoShareButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.enableAutoShare = SVNTeamPreferencesPage.this.enableAutoShareButton.getSelection();
			}
		});
		
		// Keywords property settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_keywordsGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_keywordsPrompt);
		
		this.computeKeywordsValuesButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.computeKeywordsValuesButton.setLayoutData(data);
		this.computeKeywordsValuesButton.setText(SVNUIMessages.MainPreferencePage_keywordsComputeRecursively);
		this.computeKeywordsValuesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.computeKeywordsValues = SVNTeamPreferencesPage.this.computeKeywordsValuesButton.getSelection();
			}
		});
		
		// Table sorting settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_tableSortingGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_tableSortingPrompt);
		
		this.caseInsensitiveSortingButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.caseInsensitiveSortingButton.setLayoutData(data);
		this.caseInsensitiveSortingButton.setText(SVNUIMessages.MainPreferencePage_tableSortingCaseInsensitive);
		this.caseInsensitiveSortingButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.caseInsensitiveSorting = SVNTeamPreferencesPage.this.caseInsensitiveSortingButton.getSelection();
			}
		});
		
		//svn:externals settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_externalsGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_externalsGroupPrompt);
		
		this.ignoreExternalsButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.ignoreExternalsButton.setLayoutData(data);
		this.ignoreExternalsButton.setText(SVNUIMessages.MainPreferencePage_ignoreExternals);
		this.ignoreExternalsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.ignoreExternals = SVNTeamPreferencesPage.this.ignoreExternalsButton.getSelection();
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
				
		Group consultChangeSetsGroup = new Group(composite, SWT.FILL);				
		layout = new GridLayout();		
		consultChangeSetsGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		consultChangeSetsGroup.setLayoutData(data);
		
		Group historyViewGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		historyViewGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		historyViewGroup.setLayoutData(data);
		
		Group dateFormatGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		dateFormatGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		dateFormatGroup.setLayoutData(data);	
		
		//Synchronize View group
		synchViewGroup.setText(SVNUIMessages.MainPreferencePage_synchronizeGroupName);
		
		Label label = new Label(synchViewGroup, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.MainPreferencePage_synchronizePrompt);
		
		this.fastReportButton = new Button(synchViewGroup, SWT.CHECK);
		data = new GridData();
		this.fastReportButton.setLayoutData(data);
		this.fastReportButton.setText(SVNUIMessages.MainPreferencePage_synchronizeFastReportName);
		this.fastReportButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.fastReport = SVNTeamPreferencesPage.this.fastReportButton.getSelection();
			}
		});
		
		//show models
		this.enableModelSyncButton = new Button(synchViewGroup, SWT.CHECK);
		data = new GridData();
		this.enableModelSyncButton.setLayoutData(data);
		this.enableModelSyncButton.setText(SVNUIMessages.MainPreferencePage_allowModelsName);
		this.enableModelSyncButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.enableModelSync = SVNTeamPreferencesPage.this.enableModelSyncButton.getSelection();
			}
		});
		
		//Consult change sets group
		this.createConsultChangeSets(consultChangeSetsGroup);
		
		//History View group
		historyViewGroup.setText(SVNUIMessages.MainPreferencePage_historyGroupName);
		
		label = new Label(historyViewGroup, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_historyPrompt);
		
		this.enablePagingButton = new Button(historyViewGroup, SWT.CHECK);
		data = new GridData();
		this.enablePagingButton.setLayoutData(data);
		String labelText = SVNUIMessages.MainPreferencePage_historyEnablePaging;
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
		
		this.connectToCompareWithButton = new Button(historyViewGroup, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		this.connectToCompareWithButton.setLayoutData(data);
		labelText = SVNUIMessages.MainPreferencePage_historyConnectToCompareWith;
		this.connectToCompareWithButton.setText(labelText);
		this.connectToCompareWithButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.connectToCompareWith = ((Button)e.widget).getSelection();
			}
		});
		
		// Date format group
		dateFormatGroup.setText(SVNUIMessages.MainPreferencePage_dateFormatGroupName);
		
		label = new Label(dateFormatGroup, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		labelText = SVNUIMessages.MainPreferencePage_dateFormatPrompt;
		label.setText(labelText);
		
		this.dateFormatField = new Combo(dateFormatGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] itemsDateFormat = new String[4]; 
		itemsDateFormat[SVNTeamPreferences.DATE_FORMAT_MODE_SHORT] = SVNUIMessages.MainPreferencePage_dateFormatShort;
		itemsDateFormat[SVNTeamPreferences.DATE_FORMAT_MODE_MEDIUM] = SVNUIMessages.MainPreferencePage_dateFormatMedium;
		itemsDateFormat[SVNTeamPreferences.DATE_FORMAT_MODE_LONG] = SVNUIMessages.MainPreferencePage_dateFormatLong;
		itemsDateFormat[SVNTeamPreferences.DATE_FORMAT_MODE_CUSTOM] = SVNUIMessages.MainPreferencePage_dateFormatCustom;
		this.dateFormatField.setItems(itemsDateFormat);
		
		data = new GridData();
		data.widthHint = 100;
		this.dateFormatField.setLayoutData(data);
		this.dateFormatField.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event event) {
				SVNTeamPreferencesPage.this.dateFormat = SVNTeamPreferencesPage.this.dateFormatField.getSelectionIndex();
				if (SVNTeamPreferencesPage.this.dateFormat == SVNTeamPreferences.DATE_FORMAT_MODE_CUSTOM) {
					SVNTeamPreferencesPage.this.dateFormatCustomField.setEnabled(true);
					SVNTeamPreferencesPage.this.dateFormatCustomField.setFocus();
				}
				else {
					SVNTeamPreferencesPage.this.dateFormatCustomField.setEnabled(false);
				}
				SVNTeamPreferencesPage.this.validateContent();
			}
		});
		
		this.dateFormatCustomField = new Text(dateFormatGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.dateFormatCustomField.setLayoutData(data);
		verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(labelText));
		verifier.add(new DateFormatVerifier(labelText));
		this.attachTo(this.dateFormatCustomField, new AbstractVerifierProxy(verifier){
			@Override
			protected boolean isVerificationEnabled(Control input) {
				return SVNTeamPreferencesPage.this.dateFormatField.getSelectionIndex() == SVNTeamPreferences.DATE_FORMAT_MODE_CUSTOM;
			}
		});
		this.dateFormatCustomField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SVNTeamPreferencesPage.this.dateFormatCustom = SVNTeamPreferencesPage.this.dateFormatCustomField.getText();
			}
		});
		
		return composite;
	}
	
	protected void createConsultChangeSets(Group consultChangeSetsGroup) {
		Listener changeSetsSelectionListener = new Listener() {
			public void handleEvent(Event event) {
				if (SVNTeamPreferencesPage.this.consultCSAlwaysButton.getSelection()) {
					SVNTeamPreferencesPage.this.consultChangeSets = SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_ALWAYS;
				} else if (SVNTeamPreferencesPage.this.consultCSNeverButton.getSelection()) {
					SVNTeamPreferencesPage.this.consultChangeSets = SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_NEVER;
				} else if (SVNTeamPreferencesPage.this.consultCSPromptButton.getSelection()) {
					SVNTeamPreferencesPage.this.consultChangeSets = SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_PROMPT;
				}
			}			
		};
				
		consultChangeSetsGroup.setText(SVNUIMessages.MainPreferencePage_consultChangeSetsGroupName);
		GridLayout layout = (GridLayout) consultChangeSetsGroup.getLayout();
		layout.numColumns = 3;
		layout.horizontalSpacing = 40;		
		GridData data = (GridData) consultChangeSetsGroup.getLayoutData();
		data.horizontalSpan = 2;
		data.grabExcessVerticalSpace = false;		
		
		this.consultCSAlwaysButton = new Button(consultChangeSetsGroup, SWT.RADIO);
		data = new GridData();
		this.consultCSAlwaysButton.setLayoutData(data);
		this.consultCSAlwaysButton.setText(SVNUIMessages.MainPreferencePage_consultChangeSetsAlways);
		this.consultCSAlwaysButton.addListener(SWT.Selection, changeSetsSelectionListener);
		
		this.consultCSNeverButton = new Button(consultChangeSetsGroup, SWT.RADIO);
		data = new GridData();
		this.consultCSNeverButton.setLayoutData(data);
		this.consultCSNeverButton.setText(SVNUIMessages.MainPreferencePage_consultChangeSetsNever);
		this.consultCSNeverButton.addListener(SWT.Selection, changeSetsSelectionListener);
		
		this.consultCSPromptButton = new Button(consultChangeSetsGroup, SWT.RADIO);
		data = new GridData();
		this.consultCSPromptButton.setLayoutData(data);		
		this.consultCSPromptButton.setText(SVNUIMessages.MainPreferencePage_consultChangeSetsPrompt);
		this.consultCSPromptButton.addListener(SWT.Selection, changeSetsSelectionListener);
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
		group.setText(SVNUIMessages.MainPreferencePage_structureGroupName);
		
		Label label = new Label(group, SWT.NULL);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_repositoryPrompt);
		
		label = new Label(group, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		String labelText = SVNUIMessages.MainPreferencePage_repositoryHeadName;
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
		labelText = SVNUIMessages.MainPreferencePage_repositoryBranchesName;
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
		labelText = SVNUIMessages.MainPreferencePage_repositoryTagsName;
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
		this.showExternalsButton.setText(SVNUIMessages.MainPreferencePage_showExternals);
		this.showExternalsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.showExternals = ((Button)e.widget).getSelection();
			}
		});
		
		// Checkout settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_checkoutGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_checkoutPrompt);
		
		this.checkoutUsingDotProjectNameButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.checkoutUsingDotProjectNameButton.setLayoutData(data);
		this.checkoutUsingDotProjectNameButton.setText(SVNUIMessages.MainPreferencePage_checkoutUsingDotProjectName);
		this.checkoutUsingDotProjectNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.checkoutUsingDotProjectName = SVNTeamPreferencesPage.this.checkoutUsingDotProjectNameButton.getSelection();
			}
		});
		
		this.checkoutRespectProjectStructureButton = new Button(group, SWT.CHECK);
		data = new GridData();
		this.checkoutRespectProjectStructureButton.setLayoutData(data);
		this.checkoutRespectProjectStructureButton.setText(SVNUIMessages.MainPreferencePage_checkoutRespectProjectStructure);
		this.checkoutRespectProjectStructureButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.checkoutRespectProjectStructure = SVNTeamPreferencesPage.this.checkoutRespectProjectStructureButton.getSelection();
			}
		});
		
//		Branch/tag settings group
		group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(SVNUIMessages.MainPreferencePage_branchTagGroupName);
		
		label = new Label(group, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		label.setLayoutData(data);
		label.setText(SVNUIMessages.MainPreferencePage_branchTagPrompt);
		
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.branchTagConsiderStructure = SVNTeamPreferencesPage.this.branchTagConsiderStructureButton.getSelection();
			}
		};
		this.branchTagConsiderStructureButton = new Button(group, SWT.RADIO);
		this.branchTagConsiderStructureButton.setLayoutData(new GridData());
		this.branchTagConsiderStructureButton.setText(SVNUIMessages.MainPreferencePage_branchTagConsiderStructureLabel);
		this.branchTagConsiderStructureButton.addSelectionListener(listener);
		
		this.branchTagManualUrlEditButton = new Button(group, SWT.RADIO);
		this.branchTagManualUrlEditButton.setLayoutData(new GridData());
		this.branchTagManualUrlEditButton.setText(SVNUIMessages.MainPreferencePage_branchTagManualLabel);
		this.branchTagManualUrlEditButton.addSelectionListener(listener);
		
		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);
		
		this.forceExternalsFreezeButton = new Button(group, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.forceExternalsFreezeButton.setLayoutData(data);
		this.forceExternalsFreezeButton.setText(SVNUIMessages.MainPreferencePage_forceFreezeExternals);
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
		label.setText(SVNUIMessages.MainPreferencePage_mailReporterPrompt);
		
		this.mailReporterEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		this.mailReporterEnabledButton.setLayoutData(data);
		this.mailReporterEnabledButton.setText(SVNUIMessages.MainPreferencePage_mailReporterEnabledName);
		this.mailReporterEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.mailReporterEnabled = SVNTeamPreferencesPage.this.mailReporterEnabledButton.getSelection();
			}
		});
		
		this.mailReporterErrorsEnabledButton = new Button(composite, SWT.CHECK);
		data = new GridData();
		this.mailReporterErrorsEnabledButton.setLayoutData(data);
		this.mailReporterErrorsEnabledButton.setText(SVNUIMessages.MainPreferencePage_mailReporterErrorsEnabledName);
		this.mailReporterErrorsEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNTeamPreferencesPage.this.mailReporterErrorsEnabled = SVNTeamPreferencesPage.this.mailReporterErrorsEnabledButton.getSelection();
			}
		});
		
		return composite;
	}
	
}
