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

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.DiffFormatComposite;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Compare operation repository resource selection panel
 * 
 * @author Alexander Gurov
 */
public class ComparePanel extends AbstractRepositoryResourceSelectionPanel {
	
	protected DiffFormatComposite diffFormatComposite;
	
    public ComparePanel(IRepositoryResource baseResource) {    	
    	super(baseResource, SVNRevision.INVALID_REVISION_NUMBER, SVNUIMessages.ComparePanel_Title, SVNUIMessages.ComparePanel_Description, "compareUrl", SVNUIMessages.ComparePanel_Selection_Title, SVNUIMessages.ComparePanel_Selection_Description, RepositoryResourceSelectionComposite.TEXT_BASE); //$NON-NLS-1$
    	this.defaultMessage = SVNUIMessages.ComparePanel_Message;
    }
    
    public ComparePanel(IRepositoryResource baseResource, long revision) {    	
    	super(baseResource, revision, SVNUIMessages.ComparePanel_Title, SVNUIMessages.ComparePanel_Description, "compareUrl", SVNUIMessages.ComparePanel_Selection_Title, SVNUIMessages.ComparePanel_Selection_Description, RepositoryResourceSelectionComposite.TEXT_BASE); //$NON-NLS-1$
    	this.defaultMessage = SVNUIMessages.ComparePanel_Message;
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_compareDialogContext"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel#createControlsImpl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControlsImpl(Composite parent) {
		super.createControlsImpl(parent);		
		this.diffFormatComposite = new DiffFormatComposite(parent, this);			
	}
	
	public String getDiffFile() {			
		return this.diffFormatComposite.getDiffFile();
	}
	
}
