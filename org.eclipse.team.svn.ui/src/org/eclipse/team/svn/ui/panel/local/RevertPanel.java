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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Revert resources state panel implementation
 * 
 * @author Alexander Gurov
 */
public class RevertPanel extends AbstractResourceSelectionPanel {

    protected boolean removeNonVersioned;
    protected boolean disableRemoveNonVersionedChange;
	
	public RevertPanel(IResource []resources) {
    	this(resources, null);
    }
    
    public RevertPanel(IResource[] resources, IResource[] userSelectedResources) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("RevertPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("RevertPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("RevertPanel.Message");
        IResource[] nonVersionedResources = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_NEW, IResource.DEPTH_ZERO);
        this.disableRemoveNonVersionedChange = nonVersionedResources.length == resources.length;
    	this.removeNonVersioned = this.disableRemoveNonVersionedChange;
    }
    
    public void createControls(Composite parent) {
    	GridLayout layout;
    	GridData data;
    	
    	layout = new GridLayout();
    	layout.horizontalSpacing = 0;
    	layout.verticalSpacing = 0;
    	layout.marginWidth = 0;
    	layout.marginHeight = 0;
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	Composite composite = new Composite(parent, SWT.NONE);
    	composite.setLayout(layout);
    	composite.setLayoutData(data);
    	
    	super.createControls(composite);
    	
    	this.createVerticalStrut(composite, 4);
    	
    	Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	separator.setLayoutData(data);
    	
    	this.createVerticalStrut(composite, 7);
    	
    	final Button removeNonVersionedButton = new Button(composite, SWT.CHECK);
    	data = new GridData();
    	removeNonVersionedButton.setLayoutData(data);
    	removeNonVersionedButton.setText(SVNTeamUIPlugin.instance().getResource("RevertPanel.Button.RemoveNonVersioned"));
    	removeNonVersionedButton.setSelection(this.removeNonVersioned);
    	removeNonVersionedButton.setEnabled(!this.disableRemoveNonVersionedChange);
    	removeNonVersionedButton.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(SelectionEvent e) {
    			RevertPanel.this.removeNonVersioned = removeNonVersionedButton.getSelection();
    		}
    	});
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.revertDialogContext";
    }
    
    public boolean getRemoveNonVersioned() {
    	return this.removeNonVersioned;
    }
    
    protected void createVerticalStrut(Composite parent, int height) {
    	Label strut = new Label(parent, SWT.NONE);
    	GridData data = new GridData();
    	data.heightHint = height;
    	strut.setLayoutData(data);
    }

}
