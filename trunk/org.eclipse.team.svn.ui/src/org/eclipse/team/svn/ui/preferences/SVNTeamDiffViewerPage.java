/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameters;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.DiffViewerCompareEditorComposite;
import org.eclipse.team.svn.ui.composite.DiffViewerFileAssociationsComposite;

/**
 * Diff Viewer preference page
 * 
 * @author Igor Burilo
 */
public class SVNTeamDiffViewerPage extends AbstractSVNTeamPreferencesPage {
	
	protected DiffViewerCompareEditorComposite compareEditorComposite;
	protected DiffViewerFileAssociationsComposite fileAssociationsComposite;
	
	protected DiffViewerSettings diffSettings;
	
	protected Control createContentsImpl(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData());
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.SVNTeamDiffViewerPage_CompareEditor_Tab);
		this.compareEditorComposite = new DiffViewerCompareEditorComposite(tabFolder, this);
		tabItem.setControl(this.compareEditorComposite);
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(SVNUIMessages.SVNTeamDiffViewerPage_File_Associations_Tab);
		
		this.fileAssociationsComposite = new DiffViewerFileAssociationsComposite(tabFolder, this);
		tabItem.setControl(this.fileAssociationsComposite);		
		
		return tabFolder;
	}
	
	protected void initializeControls() {			
		this.compareEditorComposite.setExternalDefaultCompare(this.diffSettings.isExternalDefaultCompare());
		ExternalProgramParameters externalProgramParams = this.diffSettings.getDefaultExternalParameters();

		this.compareEditorComposite.setExternalProgramParameters(externalProgramParams);		
		this.compareEditorComposite.initializeControls();				
		this.fileAssociationsComposite.initializeControls(this.diffSettings);				
	}

	protected void loadDefaultValues(IPreferenceStore store) {
		this.diffSettings = DiffViewerSettings.getDefaultDiffViewerSettings();				
	}

	protected void loadValues(IPreferenceStore store) {
		this.diffSettings = SVNTeamDiffViewerPage.loadDiffViewerSettings(store);
	}
	
	protected void saveValues(IPreferenceStore store) {
		this.diffSettings.setExternalDefaultCompare(this.compareEditorComposite.isExternalDefaultCompare());
		this.diffSettings.setDefaultExternalParameters(this.compareEditorComposite.getExternalProgramParameters());				
		
		SVNTeamDiffViewerPage.saveDiffViewerSettings(this.diffSettings, store);
	}
	
	public static DiffViewerSettings loadDiffViewerSettings() {
		return SVNTeamDiffViewerPage.loadDiffViewerSettings(SVNTeamUIPlugin.instance().getPreferenceStore());
	}
	
	public static DiffViewerSettings loadDiffViewerSettings(IPreferenceStore store) {
		DiffViewerSettings diffSettings = new DiffViewerSettings();
		
		diffSettings.setExternalDefaultCompare(SVNTeamPreferences.getDiffViewerBoolean(store, SVNTeamPreferences.DIFF_VIEWER_EXTERNAL_DEFAULT_COMPARE));
		diffSettings.setDefaultExternalParameters(
			SVNTeamPreferences.getDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_DIFF_PATH),
			SVNTeamPreferences.getDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_MERGE_PATH),
			SVNTeamPreferences.getDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_DIFF_PARAMETERS),
			SVNTeamPreferences.getDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_MERGE_PARAMETERS));
		
		String encodedString = SVNTeamPreferences.getDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_RESOURCES_SPECIFIC_PARAMETERS);
		String[] stringArray = FileUtility.decodeStringToArray(encodedString);
		if (stringArray.length > 0 && stringArray.length % ResourceSpecificParameters.FIELDS_COUNT == 0) {
			int paramsCount = stringArray.length / ResourceSpecificParameters.FIELDS_COUNT;
			for (int i = 0; i < paramsCount; i ++) {
				String[] strings = new String[ResourceSpecificParameters.FIELDS_COUNT];
				for (int j = 0; j < ResourceSpecificParameters.FIELDS_COUNT; j ++) {
					strings[j] = stringArray[i * ResourceSpecificParameters.FIELDS_COUNT + j]; 
				}
				ResourceSpecificParameters param = ResourceSpecificParameters.createFromStrings(strings);
				diffSettings.addResourceSpecificParameters(param);
			}
		}	
		
		return diffSettings;
	}
	
	public static void saveDiffViewerSettings(DiffViewerSettings diffSettings, IPreferenceStore store) {
		SVNTeamDiffViewerPage.saveDiffViewerSettings(diffSettings, store, false);
	}
	
	public static void saveDiffViewerSettings(DiffViewerSettings diffSettings, IPreferenceStore store, boolean isDefault) {							
		SVNTeamPreferences.setDiffViewerBoolean(store, SVNTeamPreferences.DIFF_VIEWER_EXTERNAL_DEFAULT_COMPARE, diffSettings.isExternalDefaultCompare(), isDefault);
		String diffProgramPath = null;
		String mergeProgramPath = null;
		String diffParamatersString = null;
		String mergeParamatersString = null;
		if (diffSettings.isExternalDefaultCompare()) {
			diffProgramPath = diffSettings.getDefaultExternalParameters().diffProgramPath;
			mergeProgramPath = diffSettings.getDefaultExternalParameters().mergeProgramPath;
			diffParamatersString = diffSettings.getDefaultExternalParameters().diffParamatersString;			
			mergeParamatersString = diffSettings.getDefaultExternalParameters().mergeParamatersString;
		}
		diffProgramPath = diffProgramPath == null ? "" : diffProgramPath; //$NON-NLS-1$
		mergeProgramPath = mergeProgramPath == null ? "" : mergeProgramPath; //$NON-NLS-1$
		diffParamatersString = diffParamatersString == null ? "" : diffParamatersString; //$NON-NLS-1$
		mergeParamatersString = mergeParamatersString == null ? "" : mergeParamatersString; //$NON-NLS-1$
		SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_DIFF_PATH, diffProgramPath, isDefault);	
		SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_MERGE_PATH, mergeProgramPath, isDefault);
		SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_DIFF_PARAMETERS, diffParamatersString, isDefault);
		SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_DEFAULT_EXTERNAL_PROGRAM_MERGE_PARAMETERS, mergeParamatersString, isDefault);
		
		ResourceSpecificParameters[] resourceParams = diffSettings.getResourceSpecificParameters();
		if (resourceParams.length > 0) {
			String[] stringArray = new String[ResourceSpecificParameters.FIELDS_COUNT * resourceParams.length];
			for (int i = 0; i < resourceParams.length; i ++) {
				ResourceSpecificParameters resourceParam = resourceParams[i];
				String[] strings = resourceParam.getAsStrings();
				System.arraycopy(strings, 0, stringArray, ResourceSpecificParameters.FIELDS_COUNT * i, strings.length);
			}	
			String encodedString = FileUtility.encodeArrayToString(stringArray);
			SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_RESOURCES_SPECIFIC_PARAMETERS, encodedString, isDefault);
		} else {
			SVNTeamPreferences.setDiffViewerString(store, SVNTeamPreferences.DIFF_VIEWER_RESOURCES_SPECIFIC_PARAMETERS, "", isDefault); //$NON-NLS-1$
		}
	}
}
