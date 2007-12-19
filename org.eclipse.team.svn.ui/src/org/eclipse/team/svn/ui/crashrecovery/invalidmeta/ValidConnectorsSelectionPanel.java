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
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * The panel allows us to acquire user solution about which valid connector we should use in order to access the project.
 * 
 * @author Alexander Gurov
 */
public class ValidConnectorsSelectionPanel extends AbstractDialogPanel {
	protected Combo svnConnectorField;
	protected ISVNConnectorFactory []factories;
	protected String svnConnector;

	public ValidConnectorsSelectionPanel(IProject project, List validClients) {
		super();
		this.dialogTitle = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("ValidConnectorsSelectionPanel.Title"), new String[] {project.getName()});
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("ValidConnectorsSelectionPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ValidConnectorsSelectionPanel.Message");
		
		this.factories = (ISVNConnectorFactory [])validClients.toArray(new ISVNConnectorFactory[validClients.size()]);
	}
	
    public Point getPrefferedSizeImpl() {
        return new Point(500, 60);
    }
    
    public void postInit() {
    	super.postInit();
		this.svnConnector = this.factories[this.svnConnectorField.getSelectionIndex()].getId();
    }
    
	protected void createControlsImpl(Composite parent) {
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
		label.setText(SVNTeamUIPlugin.instance().getResource("ValidConnectorsSelectionPanel.Clients"));
		
		this.svnConnectorField = new Combo(composite, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.svnConnectorField.setLayoutData(data);
		FileUtility.sort(this.factories, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((ISVNConnectorFactory)o1).getName().compareTo(((ISVNConnectorFactory)o2).getName());
			}
		});
		String []items = new String[this.factories.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = this.factories[i].getName() + " (" + this.factories[i].getClientVersion().replace('\n', ' ') + ")";
		}
		this.svnConnectorField.setItems(items);
		this.svnConnectorField.select(0);
		this.svnConnectorField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ValidConnectorsSelectionPanel.this.svnConnector = ValidConnectorsSelectionPanel.this.factories[ValidConnectorsSelectionPanel.this.svnConnectorField.getSelectionIndex()].getId();
			}
		});
	}

	protected void cancelChangesImpl() {
	}

	protected void saveChangesImpl() {
		String oldId = CoreExtensionsManager.instance().getSVNConnectorFactory().getId();
		if (!oldId.equals(this.svnConnector)) {
			SVNTeamPreferences.setCoreString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CORE_SVNCONNECTOR_NAME, this.svnConnector);
			SVNTeamUIPlugin.instance().savePluginPreferences();
			// destroy all cached proxies
			SVNRemoteStorage.instance().dispose();
		}
	}

}
