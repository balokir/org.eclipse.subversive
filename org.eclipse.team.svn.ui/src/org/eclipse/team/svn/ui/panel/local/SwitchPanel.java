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
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
    	super(baseResource, currentRevision, SVNUIMessages.SwitchPanel_Title, SVNUIMessages.SwitchPanel_Description, "SwitchPanel_URL_HISTORY_NAME", SVNUIMessages.SwitchPanel_Selection_Title, SVNUIMessages.SwitchPanel_Selection_Description, RepositoryResourceSelectionComposite.TEXT_BASE); //$NON-NLS-1$
    	this.defaultMessage = SVNUIMessages.SwitchPanel_Message;
    	this.containFolders = containFolders;
    }
    
    public void createControlsImpl(Composite parent) {
    	super.createControlsImpl(parent);
    	if (this.containFolders) {
			Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			separator.setVisible(false);
			
			boolean canShowUpdateDepthPath = false;
    		this.depthSelector = new DepthSelectionComposite(parent, SWT.NONE, true, true, canShowUpdateDepthPath, this.selectedResource, this);

    		GridData data = new GridData(GridData.FILL_HORIZONTAL);
    		this.depthSelector.setLayoutData(data);
    	}
    }
    
    public SVNDepth getDepth() {
    	if (this.depthSelector == null) {
    		return SVNDepth.INFINITY;
    	}
    	return this.depthSelector.getDepth();
    }
    
    public boolean isStickyDepth() {
    	if (this.depthSelector == null) {
    		return false;
    	}
    	return this.depthSelector.isStickyDepth(); 
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.switchDialogContext"; //$NON-NLS-1$
	}

}
