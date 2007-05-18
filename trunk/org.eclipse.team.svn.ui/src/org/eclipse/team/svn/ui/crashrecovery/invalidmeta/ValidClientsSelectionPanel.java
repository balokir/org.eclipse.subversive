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

package org.eclipse.team.svn.ui.crashrecovery.invalidmeta;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNClientWrapperFactory;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * The panel allows us to acquire user solution about which valid client we should use in order to access the project.
 * 
 * @author Alexander Gurov
 */
public class ValidClientsSelectionPanel extends AbstractDialogPanel {
	protected Combo svnClientField;
	protected ISVNClientWrapperFactory []factories;
	protected String svnClient;

	public ValidClientsSelectionPanel(IProject project, List validClients) {
		super();
		this.dialogTitle = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("ValidClientsSelectionPanel.Title"), new String[] {project.getName()});
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("ValidClientsSelectionPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ValidClientsSelectionPanel.Message");
		
		this.factories = (ISVNClientWrapperFactory [])validClients.toArray(new ISVNClientWrapperFactory[validClients.size()]);
	}
	
    public Point getPrefferedSize() {
        return new Point(500, 60);
    }
    
    public void postInit() {
    	super.postInit();
		this.svnClient = this.factories[this.svnClientField.getSelectionIndex()].getId();
    }
    
	public void createControls(Composite parent) {
		super.createControls(parent);
		
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NULL);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("ValidClientsSelectionPanel.Clients"));
		
		this.svnClientField = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.svnClientField.setLayoutData(data);
		FileUtility.sort(this.factories, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((ISVNClientWrapperFactory)o1).getName().compareTo(((ISVNClientWrapperFactory)o2).getName());
			}
		});
		String []items = new String[this.factories.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = this.factories[i].getName() + " (" + this.factories[i].getClientVersion().replace('\n', ' ') + ")";
		}
		this.svnClientField.setItems(items);
		this.svnClientField.select(0);
		this.svnClientField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ValidClientsSelectionPanel.this.svnClient = ValidClientsSelectionPanel.this.factories[ValidClientsSelectionPanel.this.svnClientField.getSelectionIndex()].getId();
			}
		});
	}

	protected void cancelChanges() {

	}

	protected void saveChanges() {
		String oldId = CoreExtensionsManager.instance().getSVNClientWrapperFactory().getId();
		if (!oldId.equals(this.svnClient)) {
			SVNTeamPreferences.setCoreString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CORE_SVNCLIENT_NAME, this.svnClient);
			SVNTeamUIPlugin.instance().savePluginPreferences();
			// destroy all cached proxies
			SVNRemoteStorage.instance().dispose();
		}
	}

}
