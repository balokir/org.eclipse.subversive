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

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Show annotation panel implementation
 * It allows to select from and to revisions
 * 
 * @author Igor Burilo
 */
public class ShowAnnotationPanel extends AbstractDialogPanel {

	protected IRepositoryResource resource;	
	
	protected RevisionComposite fromRevision;
	protected RevisionComposite toRevision;

	protected SVNRevisionRange revisions;
	
	public ShowAnnotationPanel(IRepositoryResource resource) {		
		this.dialogTitle = SVNUIMessages.ShowAnnotationPanel_Title; //$NON-NLS-1$
		this.dialogDescription = SVNUIMessages.ShowAnnotationPanel_Description; //$NON-NLS-1$
		this.defaultMessage = SVNUIMessages.ShowAnnotationPanel_DefaultMessage; //$NON-NLS-1$
		
		this.resource = resource;		
	}
	
	protected void createControlsImpl(Composite parent) {
		//from
		this.fromRevision = new RevisionComposite(parent, this, false, new String[]{SVNUIMessages.ShowAnnotationPanel_FromRevision, SVNUIMessages.RevisionComposite_HeadRevision}, SVNRevision.HEAD, false); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		GridData data = new GridData(GridData.FILL_HORIZONTAL);		
		this.fromRevision.setLayout(layout);
		this.fromRevision.setLayoutData(data);
				
		IRepositoryResource fromResource = SVNUtility.copyOf(this.resource);
		fromResource.setSelectedRevision(this.resource.getSelectedRevision());
		this.fromRevision.setSelectedResource(fromResource);
		this.fromRevision.setRevisionValue(SVNRevision.fromNumber(1));
		
		//to
		this.toRevision = new RevisionComposite(parent, this, false, new String[]{SVNUIMessages.ShowAnnotationPanel_ToRevision, SVNUIMessages.RevisionComposite_HeadRevision}, SVNRevision.HEAD, false); //$NON-NLS-1$
		layout = new GridLayout();
		data = new GridData(GridData.FILL_HORIZONTAL);		
		this.toRevision.setLayout(layout);
		this.toRevision.setLayoutData(data);
		
		IRepositoryResource toResource = SVNUtility.copyOf(this.resource);
		toResource.setSelectedRevision(this.resource.getSelectedRevision());		
		this.toRevision.setSelectedResource(toResource);		
	}
	
	public SVNRevisionRange getRevisions() {
		return this.revisions;
	}
	
	protected void cancelChangesImpl() {
		
	}

	protected void saveChangesImpl() {
		this.revisions = new SVNRevisionRange(this.fromRevision.getSelectedRevision(), this.toRevision.getSelectedRevision());
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.showAnnotationDialogContext"; //$NON-NLS-1$
	}
}
