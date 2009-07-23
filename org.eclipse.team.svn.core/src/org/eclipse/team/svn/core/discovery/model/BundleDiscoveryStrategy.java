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
package org.eclipse.team.svn.core.discovery.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.osgi.framework.Bundle;

/**
 * a strategy for discovering via installed platform {@link Bundle bundles}.
 * 
 * @author David Green
 * @author Igor Burilo
 */
public class BundleDiscoveryStrategy extends AbstractDiscoveryStrategy {

	@Override
	public void performDiscovery(IProgressMonitor monitor) throws CoreException {
		if (connectors == null || categories == null) {
			throw new IllegalStateException();
		}
		IExtensionPoint extensionPoint = getExtensionRegistry().getExtensionPoint(
				ConnectorDiscoveryExtensionReader.EXTENSION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		monitor.beginTask(SVNMessages.BundleDiscoveryStrategy_task_loading_local_extensions, extensions.length == 0 ? 1
				: extensions.length);
		try {
			if (extensions.length > 0) {
				processExtensions(new SubProgressMonitor(monitor, extensions.length), extensions);
			}
		} finally {
			monitor.done();
		}
	}

	protected void processExtensions(IProgressMonitor monitor, IExtension[] extensions) {
		monitor.beginTask(SVNMessages.BundleDiscoveryStrategy_task_processing_extensions, extensions.length == 0 ? 1
				: extensions.length);
		try {
			ConnectorDiscoveryExtensionReader extensionReader = new ConnectorDiscoveryExtensionReader();

			for (IExtension extension : extensions) {
				AbstractDiscoverySource discoverySource = computeDiscoverySource(extension.getContributor());
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (monitor.isCanceled()) {
						return;
					}
					try {
						if (ConnectorDiscoveryExtensionReader.CONNECTOR_DESCRIPTOR.equals(element.getName())) {
							DiscoveryConnector descriptor = extensionReader.readConnectorDescriptor(element,
									DiscoveryConnector.class);
							descriptor.setSource(discoverySource);
							connectors.add(descriptor);
						} else if (ConnectorDiscoveryExtensionReader.CONNECTOR_CATEGORY.equals(element.getName())) {
							DiscoveryCategory category = extensionReader.readConnectorCategory(element,
									DiscoveryCategory.class);
							category.setSource(discoverySource);
							categories.add(category);
						} else {
							throw new ValidationException(SVNMessages.format(SVNMessages.BundleDiscoveryStrategy_unexpected_element, element.getName()));
						}
					} catch (ValidationException e) {
						LoggedOperation.reportError(this.getClass().getName(), e);						
					}
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	protected AbstractDiscoverySource computeDiscoverySource(IContributor contributor) {
		BundleDiscoverySource bundleDiscoverySource = new BundleDiscoverySource(
				Platform.getBundle(contributor.getName()));
		return bundleDiscoverySource;
	}

	protected IExtensionRegistry getExtensionRegistry() {
		return Platform.getExtensionRegistry();
	}

}
