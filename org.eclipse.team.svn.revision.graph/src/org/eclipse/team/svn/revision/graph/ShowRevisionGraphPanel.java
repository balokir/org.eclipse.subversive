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
package org.eclipse.team.svn.revision.graph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Show revision graph options panel
 * 
 * @author Igor Burilo
 */
public class ShowRevisionGraphPanel extends AbstractDialogPanel {
	
	protected boolean isShowAllRevisions;
	protected boolean canIncludeMergeInfo;
	
	public ShowRevisionGraphPanel() {
		 this.dialogTitle = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Title;
         this.dialogDescription = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Description;
         this.defaultMessage = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Message;
	}
	
	protected void createControlsImpl(Composite parent) {
		final Button showAllRevisionsButton = new Button(parent, SWT.CHECK);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		showAllRevisionsButton.setLayoutData(data);		
		showAllRevisionsButton.setText(SVNRevisionGraphMessages.ShowRevisionGraphPanel_ShowAllRevisions);
		showAllRevisionsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShowRevisionGraphPanel.this.isShowAllRevisions = showAllRevisionsButton.getSelection();			  
			}
		});
				
		final Button includeMergeInfoButton = new Button(parent, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		includeMergeInfoButton.setLayoutData(data);		
		includeMergeInfoButton.setText(SVNRevisionGraphMessages.ShowRevisionGraphPanel_ShowMergeInfo);
		includeMergeInfoButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShowRevisionGraphPanel.this.canIncludeMergeInfo = includeMergeInfoButton.getSelection();			  
			}
		});		
		
        boolean isMergeSupported = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() > ISVNConnectorFactory.APICompatibility.SVNAPI_1_4_x;

        this.isShowAllRevisions = false;
        this.canIncludeMergeInfo = isMergeSupported;
        
        showAllRevisionsButton.setSelection(this.isShowAllRevisions);
        includeMergeInfoButton.setSelection(this.canIncludeMergeInfo);
        if (!isMergeSupported) {
        	 includeMergeInfoButton.setEnabled(false);
        }
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.showRevisionGraphDialogContext"; //$NON-NLS-1$
	}
	
	public Point getPrefferedSizeImpl() {
		return new Point(470, 130);
	}
	
	protected void saveChangesImpl() {		
	}
	
	protected void cancelChangesImpl() {		
	}
	
	public boolean canIncludeMergeInfo() {	
		return this.canIncludeMergeInfo;
	}
	
	public boolean isShowAllRevisions() {
		return this.isShowAllRevisions;
	}
}
