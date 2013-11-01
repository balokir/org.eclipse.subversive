/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies, Polarion Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.ui.discovery.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.discovery.model.ConnectorDescriptorKind;
import org.eclipse.team.svn.core.discovery.model.ConnectorDiscovery;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.discovery.IConnectorsInstallJob;
import org.eclipse.team.svn.ui.discovery.util.DiscoveryUiUtil;

/**
 * A wizard for performing discovery of connectors and selecting connectors to install. When finish is pressed, selected
 * connectors are downloaded and installed.
 * 
 * @see ConnectorDiscoveryWizardMainPage
 * 
 * @author David Green
 * @author Igor Burilo
 */
public class ConnectorDiscoveryWizard extends Wizard {

	private ConnectorDiscoveryWizardMainPage mainPage;

	protected IConnectorsInstallJob installJob;
	
	private final Map<ConnectorDescriptorKind, Boolean> connectorDescriptorKindToVisibility = new HashMap<ConnectorDescriptorKind, Boolean>();
	{
		for (ConnectorDescriptorKind kind : ConnectorDescriptorKind.values()) {
			connectorDescriptorKindToVisibility.put(kind, true);
		}
	}

	private boolean showConnectorDescriptorKindFilter = true;

	private boolean showConnectorDescriptorTextFilter = true;

	private Dictionary<Object, Object> environment;

	public ConnectorDiscoveryWizard(IConnectorsInstallJob installJob) {
		this.installJob = installJob;
		
		setWindowTitle(SVNUIMessages.ConnectorDiscoveryWizard_connectorDiscovery);
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		createEnvironment();
	}

	private void createEnvironment() {
		environment = new Hashtable<Object, Object>(System.getProperties());
	}

	@Override
	public void addPages() {
		addPage(mainPage = new ConnectorDiscoveryWizardMainPage());
	}

	@Override
	public boolean performFinish() {
		try {
			this.installJob.setInstallableConnectors(this.mainPage.getInstallableConnectors());
			this.getContainer().run(true, true, this.installJob);
			return true;
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, SVNTeamPlugin.NATURE_ID, SVNUIMessages.format(
					SVNUIMessages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
					e.getCause());
			DiscoveryUiUtil.logAndDisplayStatus(SVNUIMessages.ConnectorDiscoveryWizard_cannotInstall, status);
		} catch (InterruptedException e) {
			// canceled
		}
		return false;
	}

	/**
	 * configure the page to show or hide connector descriptors of the given kind
	 * 
	 * @see #connectorDescriptorKindVisibilityUpdated()
	 */
	public void setVisibility(ConnectorDescriptorKind kind, boolean visible) {
		if (kind == null) {
			throw new IllegalArgumentException();
		}
		connectorDescriptorKindToVisibility.put(kind, visible);
	}

	/**
	 * indicate if the given kind of connector is currently visible in the wizard
	 * 
	 * @see #setVisibility(ConnectorDescriptorKind, boolean)
	 */
	public boolean isVisible(ConnectorDescriptorKind kind) {
		if (kind == null) {
			throw new IllegalArgumentException();
		}
		return connectorDescriptorKindToVisibility.get(kind);
	}

	/**
	 * indicate if the connector descriptor filters should be shown in the UI. Changing this setting only has an effect
	 * before the UI is presented.
	 */
	public boolean isShowConnectorDescriptorKindFilter() {
		return this.showConnectorDescriptorKindFilter & false;	//TODO always disabled
	}

	/**
	 * indicate if the connector descriptor filters should be shown in the UI. Changing this setting only has an effect
	 * before the UI is presented.
	 */
	public void setShowConnectorDescriptorKindFilter(boolean showConnectorDescriptorKindFilter) {
		this.showConnectorDescriptorKindFilter = showConnectorDescriptorKindFilter;
	}

	/**
	 * indicate if a text field should be provided to allow the user to filter connector descriptors
	 */
	public boolean isShowConnectorDescriptorTextFilter() {
		return showConnectorDescriptorTextFilter;
	}

	/**
	 * indicate if a text field should be provided to allow the user to filter connector descriptors
	 */
	public void setShowConnectorDescriptorTextFilter(boolean showConnectorDescriptorTextFilter) {
		this.showConnectorDescriptorTextFilter = showConnectorDescriptorTextFilter;
	}

	/**
	 * the environment in which discovery should be performed.
	 * 
	 * @see ConnectorDiscovery#getEnvironment()
	 */
	public Dictionary<Object, Object> getEnvironment() {
		return environment;
	}

	/**
	 * the environment in which discovery should be performed.
	 * 
	 * @see ConnectorDiscovery#getEnvironment()
	 */
	public void setEnvironment(Dictionary<Object, Object> environment) {
		if (environment == null) {
			throw new IllegalArgumentException();
		}
		this.environment = environment;
	}

}
