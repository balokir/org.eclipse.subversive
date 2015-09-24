/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *    Yann Andenmatten - [patch] add default preference initializer
 *******************************************************************************/
package org.eclipse.team.svn.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Default preferences initializer
 * 
 * @author Yann Andenmatten
 */
public class SVNTeamPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		SVNTeamPreferences.setDefaultValues(SVNTeamUIPlugin.instance().getPreferenceStore());
	}
}
