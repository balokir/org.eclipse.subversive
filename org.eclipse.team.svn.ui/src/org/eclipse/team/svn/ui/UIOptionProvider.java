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

import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.callback.PromptCredentialsPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPropsPreferencePage;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * UI Plugin option provider. Implements extension point "coreoptions".
 * 
 * @author Alexander Gurov
 */
public class UIOptionProvider implements IOptionProvider {
	private SVNTeamModificationValidator modificationValidator = new SVNTeamModificationValidator();
	
	public ISVNCredentialsPrompt getCredentialsPrompt() {
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
	
	public FileModificationValidator getFileModificationValidator() {
		return this.modificationValidator;
	}
	
	public String getSVNConnectorId() {
		return SVNTeamPreferences.getCoreString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CORE_SVNCONNECTOR_NAME);
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
	
	public SVNProperty[] getAutomaticProperties(String template) {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		Object[] autoProperties = SVNTeamPropsPreferencePage.loadAutoProperties(SVNTeamPreferences.getAutoPropertiesList(store, SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME));
		for (int i = 0; i < autoProperties.length; i++) {
			SVNTeamPropsPreferencePage.AutoProperty autoProperty =
				(SVNTeamPropsPreferencePage.AutoProperty)autoProperties[i];
			if (!autoProperty.enabled) {
				continue;
			}
			
			StringMatcher matcher = new StringMatcher(autoProperty.fileName);
			if (matcher.match(template)) {
				if (autoProperty.properties.length() == 0) {
					return new SVNProperty[0];
				}
				String[] props = autoProperty.properties.split(";");
				SVNProperty[] propertyData = new SVNProperty[props.length];
				for (int j = 0; j < props.length; j++) {
					String[] propsNameValue = props[j].split("=");
					propertyData[j] = new SVNProperty(propsNameValue[0], propsNameValue.length == 1 ? "" : propsNameValue[1]);
				}
				return propertyData;
			}
		}
		return new SVNProperty[0];
	}
	
	public String getResource(String key) {
		return SVNTeamUIPlugin.instance().getResource(key);
	}
	
}
