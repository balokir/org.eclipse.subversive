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

package org.eclipse.team.svn.ui.panel.reporting;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.ui.composite.ReportingComposite;
import org.eclipse.team.svn.ui.extension.factory.IMailSettingsProvider;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Mail report panel allows user to create standard mail report.
 * 
 * @author Alexander Gurov
 */
public class MailReportPanel extends AbstractDialogPanel {
	protected ReportingComposite reportingComposite;
	protected boolean isError;
	
	public MailReportPanel(String dialogTitle, String dialogDescription, String defaultMessage, boolean isError) {
		super();
		this.isError = isError;
		this.dialogTitle = dialogTitle;
		this.dialogDescription = dialogDescription;
		this.defaultMessage = defaultMessage;
	}
	
	public IMailSettingsProvider getMailSettingsProvider() {
		return this.reportingComposite.getMailSettingsProvider();
	}

    public String getComment() {
    	return this.reportingComposite.getComment();
    }
    
    public String getEmail() {
    	return this.reportingComposite.getEmail();
    }
    
    public String getName() {
    	return this.reportingComposite.getUserName();
    }

	public String getReportId() {
		return this.reportingComposite.getReportId();
	}
	
	public String getReport() {
		return this.reportingComposite.getReport();
	}
    
	public boolean isNotShowAgain() {
		return this.reportingComposite.isNotShowAgain();
	}
    
    public void createControls(Composite parent) {
    	this.parent = parent;
    	GridData data = null;
    	
    	this.reportingComposite = new ReportingComposite(parent, this.dialogTitle, SVNTeamPlugin.NATURE_ID, null, null, this.isError, this);
		data = new GridData(GridData.FILL_BOTH);
		this.reportingComposite.setLayoutData(data);
    }
    
    public Point getPrefferedSizeImpl() {
        return new Point(570, SWT.DEFAULT);
    }
    
	protected void saveChanges() {
		this.retainSize();
		this.reportingComposite.saveChanges();
	}

	protected void cancelChanges() {
		this.retainSize();
		this.reportingComposite.cancelChanges();
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.reportDialogContext";
	}
}
