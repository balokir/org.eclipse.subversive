/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import org.eclipse.team.svn.core.extension.properties.PredefinedProperty;
import org.eclipse.team.svn.core.extension.properties.PredefinedPropertySet;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.preferences.SVNTeamPropsPreferencePage;

/**
 * Custom properties provider
 *
 * @author Alexander Gurov
 */
public class CustomPropertySet extends PredefinedPropertySet {
	
	protected void init() {
		this.registerProperty(new PredefinedProperty(SVNUIMessages.AbstractPropertyEditPanel_custom_description, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_COMMON));
		PredefinedProperty []customProps = SVNTeamPropsPreferencePage.loadCustomProperties(SVNTeamPreferences.getCustomPropertiesList(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME));
		if (customProps.length > 0) {
			this.registerProperties(customProps);
		}
		else {
			this.registerProperty(new PredefinedProperty("    " + SVNUIMessages.AbstractPropertyEditPanel_custom_hint, PredefinedProperty.TYPE_GROUP | PredefinedProperty.TYPE_COMMON)); //$NON-NLS-1$
		}
	}
	
}
