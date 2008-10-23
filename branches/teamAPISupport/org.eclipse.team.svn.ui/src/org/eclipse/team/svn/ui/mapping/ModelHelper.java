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

package org.eclipse.team.svn.ui.mapping;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Simple helper for getting modelSync flag
 * 
 * @author Igor Burilo
 *
 */
public class ModelHelper {

	public static boolean isShowModelSync() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		return SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.ENABLE_MODEL_SYNC_NAME);
	}
}
