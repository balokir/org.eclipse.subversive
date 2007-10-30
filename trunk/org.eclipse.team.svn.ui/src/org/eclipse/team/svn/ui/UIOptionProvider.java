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

package org.eclipse.team.svn.ui;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.client.ICredentialsPrompt;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.callback.PromptCredentialsPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamAutoPropsPreferencePage;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * UI Plugin option provider. Implements extension point "coreoptions".
 * 
 * @author Alexander Gurov
 */
public class UIOptionProvider implements IOptionProvider {
	private SVNTeamModificationValidator modificationValidator = new SVNTeamModificationValidator();
	
	public boolean getReportRevisionChange() {
		return SVNTeamPreferences.getSynchronizeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME);
	}
	
	public ICredentialsPrompt getCredentialsPrompt() {
		return PromptCredentialsPanel.DEFAULT_PROMPT;
	}
	
	public ILoggedOperationFactory getLoggedOperationFactory() {
		return UIMonitorUtility.DEFAULT_FACTORY;
	}
	
	public void addProjectSetCapabilityProcessing(CompositeOperation op) {
		op.add(new RefreshRepositoryLocationsOperation(false));
	}
	
	public boolean isAutomaticProjectShareEnabled() {
		return SVNTeamPreferences.getShareBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.SHARE_ENABLE_AUTO_NAME);
	}
	
	public IFileModificationValidator getFileModificationValidator() {
		return this.modificationValidator;
	}
	
	public String getSVNClientId() {
		return SVNTeamPreferences.getCoreString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CORE_SVNCLIENT_NAME);
	}
	
	public String getDefaultBranchesName() {
		String retVal = SVNTeamPreferences.getRepositoryString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.REPOSITORY_BRANCHES_NAME);
		if (retVal == null || retVal.length() == 0) {
			retVal = SVNTeamPreferences.REPOSITORY_BRANCHES_DEFAULT;
		}
		return retVal;
	}
	
	public String getDefaultTagsName() {
		String retVal = SVNTeamPreferences.getRepositoryString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.REPOSITORY_TAGS_NAME);
		if (retVal == null || retVal.length() == 0) {
			retVal = SVNTeamPreferences.REPOSITORY_TAGS_DEFAULT;
		}
		return retVal;
	}
	
	public String getDefaultTrunkName() {
		String retVal = SVNTeamPreferences.getRepositoryString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.REPOSITORY_HEAD_NAME);
		if (retVal == null || retVal.length() == 0) {
			retVal = SVNTeamPreferences.REPOSITORY_HEAD_DEFAULT;
		}
		return retVal;
	}

	public boolean isSVNCacheEnabled() {
		return SVNTeamPreferences.getDecorationBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.DECORATION_ENABLE_CACHE_NAME);
	}
	
	public PropertyData[] getAutomaticProperties(String template) {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		Object[] autoProperties = SVNTeamAutoPropsPreferencePage.loadProperties(SVNTeamPreferences.getAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME));
		for (int i = 0; i < autoProperties.length; i++) {
			SVNTeamAutoPropsPreferencePage.AutoProperty autoProperty =
				(SVNTeamAutoPropsPreferencePage.AutoProperty)autoProperties[i];
			if (!autoProperty.enabled) {
				continue;
			}
			
			StringMatcher matcher = new StringMatcher(autoProperty.fileName);
			if (matcher.match(template)) {
				if (autoProperty.properties.length() == 0) {
					return new PropertyData[0];
				}
				String[] props = autoProperty.properties.split(";");
				PropertyData[] propertyData = new PropertyData[props.length];
				for (int j = 0; j < props.length; j++) {
					String[] propsNameValue = props[j].split("=");
					propertyData[j] = new PropertyData(propsNameValue[0],
							propsNameValue.length == 1 ? "" : propsNameValue[1],
							new byte[0]);
				}
				return propertyData;
			}
		}
		return new PropertyData[0];
	}
	
	public String getResource(String key) {
		return SVNTeamUIPlugin.instance().getResource(key);
	}
	
}
