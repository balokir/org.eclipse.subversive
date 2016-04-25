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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.preferences.SVNRevisionGraphPreferences;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show revision graph options panel
 * 
 * @author Igor Burilo
 */
public class ShowRevisionGraphPanel extends AbstractDialogPanel {
	
	protected boolean isShowAllRevisions;
	protected boolean canIncludeMergeInfo;
	
	protected IRepositoryResource resource;
	protected IRepositoryResource initFromResource;
	protected IRepositoryResource initToResource;
	protected boolean reversed;
	
	protected RevisionComposite fromComposite;
	protected RevisionComposite toComposite;
	
	/**
	 * If resource is null then don't allow to select revision 
	 */
	public ShowRevisionGraphPanel(IRepositoryResource resource) {
		 this.dialogTitle = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Title;
         this.dialogDescription = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Description;
         this.defaultMessage = SVNRevisionGraphMessages.ShowRevisionGraphPanel_Message;
         
         this.resource = resource;
         if (this.resource != null) {
        	 this.initFromResource = SVNUtility.copyOf(this.resource);         
             this.initToResource = SVNUtility.copyOf(this.resource); 
         }                                      
	}
	
	protected void createControlsImpl(Composite parent) {
		if (this.resource != null) {
			Composite cmp = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = layout.marginWidth = 0;
			cmp.setLayout(layout);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			cmp.setLayoutData(data);
			
			String defaultRevision = SVNRevisionGraphMessages.ShowRevisionGraphPanel_RevisionDefault;
			this.fromComposite = new RevisionComposite(cmp, this, true, new String[] {SVNRevisionGraphMessages.ShowRevisionGraphPanel_FromRevision, defaultRevision}, null, false);
			this.fromComposite.setBaseResource(this.resource);
			this.fromComposite.setSelectedResource(this.initFromResource);
			this.fromComposite.setRevisionValue(SVNRevision.HEAD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.fromComposite.setLayoutData(data);
			
			this.toComposite = new RevisionComposite(cmp, this, true, new String[] {SVNRevisionGraphMessages.ShowRevisionGraphPanel_ToRevision, defaultRevision}, null, false);
			this.toComposite.setBaseResource(this.resource);
			this.toComposite.setSelectedResource(this.initToResource);
			this.toComposite.setRevisionValue(SVNRevision.HEAD);
			data = new GridData(GridData.FILL_HORIZONTAL);		
			this.toComposite.setLayoutData(data);			
		}				
		
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
		
		this.isShowAllRevisions = SVNRevisionGraphPreferences.getGraphBoolean(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.GRAPH_SHOW_ALL_REVISIONS);
        boolean isMergeSupported = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() > ISVNConnectorFactory.APICompatibility.SVNAPI_1_4_x;

        this.canIncludeMergeInfo = isMergeSupported ? SVNRevisionGraphPreferences.getGraphBoolean(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.GRAPH_SHOW_MERGE_INFO) : false;
        
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
		return new Point(715, SWT.DEFAULT);
	}
	
	protected void saveChangesImpl() {
		SVNRevisionGraphPreferences.setGraphBoolean(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.GRAPH_SHOW_MERGE_INFO, this.canIncludeMergeInfo);
		SVNRevisionGraphPreferences.setGraphBoolean(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.GRAPH_SHOW_ALL_REVISIONS, this.isShowAllRevisions);
		
		if (this.getFromRevision() == null || this.getToRevision() == null) {
			return;
		}
		this.initFromResource.setSelectedRevision(this.getFromRevision());
		this.initToResource.setSelectedRevision(this.getToRevision());

		UIMonitorUtility.doTaskNowDefault(new AbstractActionOperation("Operation_CheckRevisions", SVNUIMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				ISVNConnector proxy = ShowRevisionGraphPanel.this.initFromResource.getRepositoryLocation().acquireSVNProxy();
				try {
					ShowRevisionGraphPanel.this.reversed = SVNUtility.compareRevisions(ShowRevisionGraphPanel.this.initFromResource.getSelectedRevision(), ShowRevisionGraphPanel.this.initToResource.getSelectedRevision(), SVNUtility.getEntryRevisionReference(ShowRevisionGraphPanel.this.initFromResource), SVNUtility.getEntryRevisionReference(ShowRevisionGraphPanel.this.initToResource), proxy) == 1;
				}
				finally {
					ShowRevisionGraphPanel.this.initFromResource.getRepositoryLocation().releaseSVNProxy(proxy);
				}
			}
		}, false);
	}
	
	protected void cancelChangesImpl() {		
	}
	
	public boolean canIncludeMergeInfo() {	
		return this.canIncludeMergeInfo;
	}
	
	public boolean isShowAllRevisions() {
		return this.isShowAllRevisions;
	}
	
	public SVNRevision getFromRevision() {
		if (this.resource != null) {
			return (this.reversed ? this.toComposite : this.fromComposite).getSelectedRevision();	
		}
		return null;
	}

	public SVNRevision getToRevision() {
		if (this.resource != null) {
			return (this.reversed ? this.fromComposite : this.toComposite).getSelectedRevision();	
		}
		return null;
	}
}
