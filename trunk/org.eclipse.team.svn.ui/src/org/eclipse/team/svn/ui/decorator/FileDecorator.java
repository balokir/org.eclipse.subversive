/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * File resource decorator
 * 
 * @author Alexander Gurov
 */
public class FileDecorator extends AbstractResourceDecorator {
	public FileDecorator(IResourceStatesListener targetListener) {
		super(targetListener);
	}

	protected String getStatus(ILocalResource local) {
		return local.getStatus();
	}

	protected void loadConfiguration() {
		super.loadConfiguration();
		
		this.indicateDeleted = false;
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		String formatLine = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FORMAT_FILE_NAME);
		this.format = this.decorator.parseFormatLine(formatLine);
	}

}
