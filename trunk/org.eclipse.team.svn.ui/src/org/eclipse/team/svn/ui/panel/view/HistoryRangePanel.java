/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Allows to define the history fetch range
 * 
 * @author Alexander Gurov
 */
public class HistoryRangePanel extends AbstractDialogPanel {
	protected IRepositoryResource resource;
	protected IRepositoryResource initStartResource;
	protected IRepositoryResource initStopResource;
	
	protected RevisionComposite startComposite;
	protected RevisionComposite stopComposite;
	
	protected boolean reversed;

	public HistoryRangePanel(IRepositoryResource resource, SVNRevision initStartRevision, SVNRevision initStopRevision) {
        this.dialogTitle = SVNUIMessages.HistoryRangePanel_Title;
        this.dialogDescription = SVNUIMessages.HistoryRangePanel_Description;
        this.defaultMessage = SVNUIMessages.HistoryRangePanel_Message;
        
        this.resource = resource;
        this.initStartResource = SVNUtility.copyOf(resource);
        this.initStartResource.setSelectedRevision(initStartRevision);
        this.initStopResource = SVNUtility.copyOf(resource);
        this.initStopResource.setSelectedRevision(initStopRevision);
	}
	
	protected Point getPrefferedSizeImpl() {
        return new Point(715, SWT.DEFAULT);
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.historyRangeDialogContext"; //$NON-NLS-1$
    }
    
	public SVNRevision getStartRevision() {
		return (this.reversed ? this.stopComposite : this.startComposite).getSelectedRevision();
	}

	public SVNRevision getStopRevision() {
		return (this.reversed ? this.startComposite : this.stopComposite).getSelectedRevision();
	}

	protected void createControlsImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite cmp = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		cmp.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		cmp.setLayoutData(data);
		
		String defaultRevision = SVNUIMessages.HistoryRangePanel_Default;
		this.startComposite = new RevisionComposite(cmp, this, true, new String[] {SVNUIMessages.HistoryRangePanel_StartRevision, defaultRevision}, null, false);
		this.startComposite.setBaseResource(this.resource);
		this.startComposite.setSelectedResource(this.initStartResource);
		this.stopComposite = new RevisionComposite(cmp, this, true, new String[] {SVNUIMessages.HistoryRangePanel_StopRevision, defaultRevision}, null, false);
		this.stopComposite.setBaseResource(this.resource);
		this.stopComposite.setSelectedResource(this.initStopResource);
	}

	protected void cancelChangesImpl() {

	}

	protected void saveChangesImpl() {
		if (this.getStartRevision() == null || this.getStopRevision() == null) {
			return;
		}
		this.initStartResource.setSelectedRevision(this.getStartRevision());
		this.initStopResource.setSelectedRevision(this.getStopRevision());

		UIMonitorUtility.doTaskNowDefault(new AbstractActionOperation("Operation_CheckRevisions") { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				ISVNConnector proxy = HistoryRangePanel.this.initStartResource.getRepositoryLocation().acquireSVNProxy();
				try {
					HistoryRangePanel.this.reversed = SVNUtility.compareRevisions(HistoryRangePanel.this.initStartResource.getSelectedRevision(), HistoryRangePanel.this.initStopResource.getSelectedRevision(), SVNUtility.getEntryRevisionReference(HistoryRangePanel.this.initStartResource), SVNUtility.getEntryRevisionReference(HistoryRangePanel.this.initStopResource), proxy) == -1;
				}
				finally {
					HistoryRangePanel.this.initStartResource.getRepositoryLocation().releaseSVNProxy(proxy);
				}
			}
		}, false);
	}

}
