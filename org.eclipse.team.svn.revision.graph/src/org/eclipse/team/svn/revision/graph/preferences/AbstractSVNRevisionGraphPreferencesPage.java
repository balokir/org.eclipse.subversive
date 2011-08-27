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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.ui.preferences.AbstractSVNTeamPreferencesPage;

/**
 * Abstract preferences page for revision graph
 *  
 * @author Igor Burilo
 */
public abstract class AbstractSVNRevisionGraphPreferencesPage extends AbstractSVNTeamPreferencesPage {
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return SVNRevisionGraphPlugin.instance().getPreferenceStore();
	}
	
	@Override
	public boolean performOk() {
		this.saveValues(this.getPreferenceStore());
		
		SVNRevisionGraphPlugin.instance().savePreferences();
		
		return true;
	}
}
