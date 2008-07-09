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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.DepthSelectionComposite;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Switch panel implementation
 * 
 * @author Alexander Gurov
 */
public class SwitchPanel extends AbstractRepositoryResourceSelectionPanel {
	
	protected boolean containFolders;
	protected DepthSelectionComposite depthSelector;
	
    public SwitchPanel(IRepositoryResource baseResource, long currentRevision, boolean containFolders) {    	
    	super(baseResource, currentRevision, SVNTeamUIPlugin.instance().getResource("SwitchPanel.Title"), SVNTeamUIPlugin.instance().getResource("SwitchPanel.Description"), "SwitchPanel.URL_HISTORY_NAME", false, SVNTeamUIPlugin.instance().getResource("SwitchPanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("SwitchPanel.Selection.Description"), RepositoryResourceSelectionComposite.TEXT_NONE);
    	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("SwitchPanel.Message");
    	this.containFolders = containFolders;
    }
    
    public void createControlsImpl(Composite parent) {
    	super.createControlsImpl(parent);
    	if (this.containFolders) {
			Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			separator.setVisible(false);
			
    		this.depthSelector = new DepthSelectionComposite(parent, SWT.NONE);
    		if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x)
    		{
    			this.depthSelector.addAndSelectWorkingCopyDepth();
    		}
    		GridData data = new GridData(GridData.FILL_HORIZONTAL);
    		this.depthSelector.setLayoutData(data);
    	}
    }
    
    public int getDepth() {
    	if (this.depthSelector == null) {
    		return Depth.INFINITY;
    	}
    	return this.depthSelector.getDepth();
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.switchDialogContext";
	}

}
