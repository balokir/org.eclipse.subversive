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

package org.eclipse.team.svn.ui.info;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNLock;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.InfoOperation;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.util.Util;

/**
 * This page allows to view working copy information for local resource
 * 
 * @author Alexander Gurov
 */
public class LocalInfoPage extends PropertyPage {
	protected PropertiesComposite properties;
	
    public LocalInfoPage() {
        super();
    }

    protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		
		this.noDefaultAndApplyButton();
		
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());

		
		IResource resource = (IResource)Util.getAdapter(this.getElement(), IResource.class);
		InfoOperation op = new InfoOperation(resource);
		UIMonitorUtility.doTaskBusyDefault(op);
		
		ILocalResource local = op.getLocal();
		
		Label description = new Label(composite, SWT.WRAP);
		description.setLayoutData(new GridData());
		description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LocalPath"));
		
		Text content = new Text(composite, SWT.WRAP);
		data = new GridData();
		data.widthHint = 300;
		content.setLayoutData(data);
		content.setEditable(false);
		content.setText(resource.getFullPath().toString());
		
		description = new Label(composite, SWT.WRAP);
		description.setLayoutData(new GridData());
		description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.State"));
		
		content = new Text(composite, SWT.SINGLE);
		content.setLayoutData(new GridData());
		content.setEditable(false);
		content.setText(SVNUtility.getStatusText(local.getStatus()));
		
		SVNEntryInfo info = op.getInfo();
		if (IStateFilter.SF_ONREPOSITORY.accept(local) && info != null) {
		    // add space
		    new Label(composite, SWT.WRAP);
		    new Label(composite, SWT.WRAP);
		    
			description = new Label(composite, SWT.WRAP);
			description.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.ResourceURL"));
			
			content = new Text(composite, SWT.WRAP);
			data = new GridData();
			data.widthHint = 300;
			content.setLayoutData(data);
			content.setEditable(false);
			content.setText(SVNUtility.decodeURL(info.url));
					
			description = new Label(composite, SWT.WRAP);
			description.setLayoutData(new GridData());
			description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.Revision"));
			
			content = new Text(composite, SWT.SINGLE);
			content.setLayoutData(new GridData());
			content.setEditable(false);
			content.setText(String.valueOf(info.revision));

			description = new Label(composite, SWT.WRAP);
			description.setLayoutData(new GridData());
			description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LastChangedAuthor"));
			
			content = new Text(composite, SWT.SINGLE);
			content.setLayoutData(new GridData());
			content.setEditable(false);
			content.setText(info.lastChangedAuthor == null ? SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor") : info.lastChangedAuthor);

			description = new Label(composite, SWT.WRAP);
			description.setLayoutData(new GridData());
			description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LastChangedDate"));
			
			content = new Text(composite, SWT.SINGLE);
			content.setLayoutData(new GridData());
			content.setEditable(false);
			content.setText(info.lastChangedDate == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoDate") : dateFormat.format(new Date(info.lastChangedDate)));

			description = new Label(composite, SWT.WRAP);
			description.setLayoutData(new GridData());
			description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LastChangedRevision"));
			
			content = new Text(composite, SWT.SINGLE);
			content.setLayoutData(new GridData());
			content.setEditable(false);
			content.setText(String.valueOf(info.lastChangedRevision));

			SVNLock lock = info.lock;
			if (lock != null) {
			    // add space
			    new Label(composite, SWT.WRAP);
			    new Label(composite, SWT.WRAP);

				description = new Label(composite, SWT.WRAP);
				description.setLayoutData(new GridData());
				description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LockOwner"));
				
				content = new Text(composite, SWT.SINGLE);
				content.setLayoutData(new GridData());
				content.setEditable(false);
				content.setText(lock.owner == null ? SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor") : lock.owner);

				description = new Label(composite, SWT.WRAP);
				description.setLayoutData(new GridData());
				description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LockComment"));
				
				content = new Text(composite, SWT.SINGLE);
				content.setLayoutData(new GridData());
				content.setEditable(false);
				content.setText(lock.comment == null ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : lock.comment);

				description = new Label(composite, SWT.WRAP);
				description.setLayoutData(new GridData());
				description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LockCreationDate"));
				
				content = new Text(composite, SWT.SINGLE);
				content.setLayoutData(new GridData());
				content.setEditable(false);
				content.setText(lock.creationDate == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor") : dateFormat.format(new Date(lock.creationDate)));
				if (lock.expirationDate != 0) {
					description = new Label(composite, SWT.WRAP);
					description.setLayoutData(new GridData());
					description.setText(SVNTeamUIPlugin.instance().getResource("LocalInfoPage.LockExpirationDate"));
					
					content = new Text(composite, SWT.SINGLE);
					content.setLayoutData(new GridData());
					content.setEditable(false);
					content.setText(lock.expirationDate == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoDate") : dateFormat.format(new Date(lock.expirationDate)));
				}
			}
		}
		if (IStateFilter.SF_VERSIONED.accept(local)) {
			//add space
			new Label(composite, SWT.WRAP);
		    new Label(composite, SWT.WRAP);

		    Composite group = new Composite(composite, SWT.BORDER);
			data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 2;
			group.setLayoutData(data);
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			group.setLayout(layout);
		    this.properties = new PropertiesComposite(group);
		    IResourcePropertyProvider propertyProvider = new GetPropertiesOperation(resource);
		    UIMonitorUtility.doTaskBusyDefault(propertyProvider);
			this.properties.setResource(resource, propertyProvider);
			UIMonitorUtility.doTaskBusyDefault(this.properties.getRefreshViewOperation());
			this.properties.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.SVNInfoContext");
		
        return composite;
    }

}
