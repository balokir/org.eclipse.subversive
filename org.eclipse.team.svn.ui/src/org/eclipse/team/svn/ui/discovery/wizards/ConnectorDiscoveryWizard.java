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
import org.eclipse.equinox.internal.provisional.p2.ui.IProvHelpContextIds;
import org.eclipse.equinox.internal.provisional.p2.ui.QueryableMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.ui.dialogs.PreselectedIUInstallWizard;
import org.eclipse.equinox.internal.provisional.p2.ui.dialogs.ProvisioningWizardDialog;
import org.eclipse.equinox.internal.provisional.p2.ui.policy.Policy;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.discovery.model.ConnectorDescriptorKind;
import org.eclipse.team.svn.core.discovery.model.ConnectorDiscovery;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.discovery.util.DiscoveryUiUtil;
import org.eclipse.ui.PlatformUI;

/**
 * A wizard for performing discovery of connectors and selecting connectors to install. When finish is pressed, selected
 * connectors are downloaded and installed.
 * 
 * @see PrepareInstallProfileJob
 * @see ConnectorDiscoveryWizardMainPage
 * 
 * @author David Green
 * @author Igor Burilo
 */
@SuppressWarnings("restriction")
public class ConnectorDiscoveryWizard extends Wizard {

	private ConnectorDiscoveryWizardMainPage mainPage;

	private final Map<ConnectorDescriptorKind, Boolean> connectorDescriptorKindToVisibility = new HashMap<ConnectorDescriptorKind, Boolean>();
	{
		for (ConnectorDescriptorKind kind : ConnectorDescriptorKind.values()) {
			connectorDescriptorKindToVisibility.put(kind, true);
		}
	}

	private boolean showConnectorDescriptorKindFilter = true;

	private boolean showConnectorDescriptorTextFilter = true;

	private Dictionary<Object, Object> environment;

	public ConnectorDiscoveryWizard() {
		setWindowTitle(Messages.ConnectorDiscoveryWizard_connectorDiscovery);
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/banner-discovery.png"));
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
			final PrepareInstallProfileJob job = new PrepareInstallProfileJob(mainPage.getInstallableConnectors());
			getContainer().run(true, true, job);

			if (job.getPlannerResolutionOperation() != null
					&& job.getPlannerResolutionOperation().getProvisioningPlan() != null) {
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						PreselectedIUInstallWizard wizard = new PreselectedIUInstallWizard(Policy.getDefault(),
								job.getProfileId(), job.getIUs(), job.getPlannerResolutionOperation(),
								new QueryableMetadataRepositoryManager(Policy.getDefault().getQueryContext(), false));
						WizardDialog dialog = new ProvisioningWizardDialog(getShell(), wizard);
						dialog.create();
						PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
								IProvHelpContextIds.INSTALL_WIZARD);

						dialog.open();
					}
				});
			}
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, SVNTeamPlugin.NATURE_ID, NLS.bind(
					Messages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
					e.getCause());
			DiscoveryUiUtil.logAndDisplayStatus(Messages.ConnectorDiscoveryWizard_cannotInstall, status);
			return false;
		} catch (InterruptedException e) {
			// canceled
		}
		return true;
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
		//TODO uncomment to add filters
		//return showConnectorDescriptorKindFilter;		
		return false;
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
