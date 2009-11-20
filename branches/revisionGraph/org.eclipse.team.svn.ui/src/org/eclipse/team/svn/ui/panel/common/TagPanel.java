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

package org.eclipse.team.svn.ui.panel.common;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Select tag folder panel
 * 
 * @author Alexander Gurov
 */
public class TagPanel extends AbstractBranchTagPanel {

    public TagPanel(IRepositoryRoot tagTo, boolean showStartsWith, Set existingNames, IRepositoryResource[] selectedRemoteResources) {
    	this(tagTo, showStartsWith, existingNames, new IResource[0], selectedRemoteResources);
    }
    
    public TagPanel(IRepositoryRoot tagTo, boolean showStartsWith, Set existingNames, IResource[] resources, IRepositoryResource[] selectedRemoteResources) {
    	super(tagTo, showStartsWith, existingNames, "TagPanel", "tag", resources, selectedRemoteResources);
    }

    public void createControlsImpl(Composite parent) {
    	super.createControlsImpl(parent);
    	if (this.startsWith && SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME)) {
    		this.freezeExternalsCheck.setSelection(true);
    		this.freezeExternalsCheck.setEnabled(false);
    	} 	
    }
    
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.tagDialogContext"; //$NON-NLS-1$
    }
    
}
