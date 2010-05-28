/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Default preferences initializer
 * 
 * @author Igor Burilo
 */
public class SVNRevisionGraphPreferencesInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		SVNRevisionGraphPreferences.setDefaultValues(SVNTeamUIPlugin.instance().getPreferenceStore());
	}
}
