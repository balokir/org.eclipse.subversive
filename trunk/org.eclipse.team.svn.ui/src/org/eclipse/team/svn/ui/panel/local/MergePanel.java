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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.IDialogManager;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Merge panel implementation
 * 
 * @author Alexander Gurov
 */
public class MergePanel extends AbstractRepositoryResourceSelectionPanel {
	protected boolean updateFirstSelected;
	protected String baseResourceURL;
	protected Revision startRevision;
	protected Revision secondRevision;
	
    public MergePanel(IRepositoryResource baseResource, long currentRevision) {
        super(baseResource, currentRevision, SVNTeamUIPlugin.instance().getResource("MergePanel.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Description"), "MergePanel.URL_HISTORY_NAME", true, SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("MergePanel.Selection.Description"), RepositoryResourceSelectionComposite.MODE_TWO | RepositoryResourceSelectionComposite.MODE_AUTO);
        this.updateFirstSelected = true;
        this.baseResourceURL = baseResource.getUrl();
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("MergePanel.Message");
    }

	public boolean isUpdateFirstSelected() {
		return this.updateFirstSelected;
	}
	
	public Revision getStartRevision() {
		return this.startRevision;
	}
	
	public Revision getSecondSelectedRevision() {
		return this.secondRevision;
	}
    
    public Point getPrefferedSize() {
        return new Point(580, SWT.DEFAULT);
    }
    
    public void createControls(Composite parent) {
    	parent.setLayoutData(new GridData(GridData.FILL_BOTH));
    	super.createControls(parent);
    	this.selectionComposite.addVerifier(new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				if (this.getText(input).equals(MergePanel.this.baseResourceURL)) {
					return SVNTeamUIPlugin.instance().getResource("MergePanel.Resource.Verifier.Error");
				}
				return null;
			}
			protected String getWarningMessage(Control input) {
				return null;
			}
    	});

    	final Button updateFirstButton = new Button(parent, SWT.CHECK);
    	GridData data = new GridData();
    	updateFirstButton.setLayoutData(data);
    	updateFirstButton.setText(SVNTeamUIPlugin.instance().getResource("MergePanel.UpdateToHead"));
    	updateFirstButton.setSelection(this.updateFirstSelected);
    	updateFirstButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MergePanel.this.updateFirstSelected = updateFirstButton.getSelection();
				MergePanel.this.validateContent();
			}
		});
    	this.attachTo(updateFirstButton, new AbstractVerifier() {
			protected String getWarningMessage(Control input) {
				return ((Button)input).getSelection() ? null : SVNTeamUIPlugin.instance().getResource("MergePanel.UpdateToHead.Verifier.Warning");
			}
			protected String getErrorMessage(Control input) {
				return null;
			}
		});
    }
	
	public void postInit() {
		super.postInit();
		this.selectionComposite.setUrl("");
		this.manager.setMessage(IDialogManager.LEVEL_OK, this.defaultMessage);
	}
	
	protected void saveChanges() {
    	super.saveChanges();
    	this.startRevision = this.selectionComposite.getStartRevision();
    	this.secondRevision = this.selectionComposite.getSecondSelectedRevision();
    }
	
}
