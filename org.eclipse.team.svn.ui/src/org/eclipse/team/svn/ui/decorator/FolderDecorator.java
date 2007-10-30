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

package org.eclipse.team.svn.ui.decorator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Folder resource decorator
 * 
 * @author Alexander Gurov
 */
public class FolderDecorator extends AbstractResourceDecorator {
	protected boolean computeDeep;

	public FolderDecorator(IResourceStatesListener targetListener) {
		super(targetListener);
	}

	protected String getStatus(ILocalResource local) {
		if (this.computeDeep && local.getStatus() == IStateFilter.ST_NORMAL && 
				FileUtility.checkForResourcesPresenceRecursive(new IResource[] {local.getResource()}, IStateFilter.SF_MODIFIED_NOT_IGNORED)) {
			return IStateFilter.ST_MODIFIED;
		}
		return local.getStatus();
	}

	protected void loadConfiguration() {
		super.loadConfiguration();
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		String formatLine = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FORMAT_FOLDER_NAME);
		this.format = this.decorator.parseFormatLine(formatLine);
		
		this.computeDeep = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_COMPUTE_DEEP_NAME);
	}

}
