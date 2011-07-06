/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.extension.factory.ThreadNameModifierFactory;
import org.eclipse.team.svn.core.extension.options.IIgnoreRecommendations;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.LoggedOperation;

/**
 * Manager for extension components. Used to extend Subversive without direct dependencies.
 * 
 * @author Alexander Gurov
 */
public class CoreExtensionsManager {
	public static final String EXTENSION_NAMESPACE = "org.eclipse.team.svn.core"; //$NON-NLS-1$
	public static final String SVN_CONNECTOR = "svnconnector"; //$NON-NLS-1$
	public static final String CORE_OPTIONS = "coreoptions"; //$NON-NLS-1$
	public static final String CRASH_RECOVERY = "crashrecovery"; //$NON-NLS-1$
	public static final String IGNORE_RECOMMENDATIONS = "resourceIgnoreRules"; //$NON-NLS-1$

	private HashMap<String, ISVNConnectorFactory> connectors;
	private HashSet<String> validConnectors;
	private IOptionProvider optionProvider;
	private IResolutionHelper []helpers;
	private IIgnoreRecommendations []ignoreRecommendations;

	private static CoreExtensionsManager instance = new CoreExtensionsManager();
	
	private boolean disableHelpers;
	
	public static CoreExtensionsManager instance() {
		return CoreExtensionsManager.instance;
	}
	
	public IIgnoreRecommendations []getIgnoreRecommendations() {
		return this.ignoreRecommendations;
	}
	
	public IResolutionHelper []getResolutionHelpers() {
		return this.disableHelpers ? new IResolutionHelper[0] : this.helpers;
	}
	
	public void setResolutionHelpersDisabled(boolean disable) {
		this.disableHelpers = disable;
	}
	
	public boolean isResoultionHelpersDisabled() {
		return this.disableHelpers;
	}
	
	public IOptionProvider getOptionProvider() {
		return this.optionProvider;
	}
	
	//FIXME remove later after Integration API is changed: SVN Team Core should supports multiple IOptionProvider instances, each of those should provide also configuration ID which will be used in order to create separate SVN Team Core instance. This will allows simultaneous work of different tools with SVN Team Core module without loss of stability and supportability.
	public void setOptionProvider(IOptionProvider optionProvider) {
		this.optionProvider = optionProvider;
	}
	
	public Collection<String> getAccessibleClientIds() {
		this.initializeConnectors();
		return this.connectors.keySet();
	}
	
	public Collection<ISVNConnectorFactory> getAccessibleClients() {
		this.initializeConnectors();
		return this.connectors.values();
	}
	
	public ISVNConnectorFactory getSVNConnectorFactory() {
		String id = this.getOptionProvider().getSVNConnectorId();
		return this.getSVNConnectorFactory(id);
	}
	
	public ISVNConnectorFactory getSVNConnectorFactory(String id) {
		ISVNConnectorFactory retVal = this.getFirstValidConnector(id);
		if (retVal == null) {
			retVal = ISVNConnectorFactory.EMPTY;
		}
		return retVal;
	}
	
	private ISVNConnectorFactory getFirstValidConnector(String id) {
		this.initializeConnectors();
		
		if (this.validConnectors.contains(id)) {
			return this.connectors.get(id);
		}
		else if (this.validConnectors.contains(ISVNConnectorFactory.DEFAULT_ID)) {
			return this.connectors.get(ISVNConnectorFactory.DEFAULT_ID);
		}
		for (Iterator<ISVNConnectorFactory> it = this.connectors.values().iterator(); it.hasNext(); ) {
			ISVNConnectorFactory connector = it.next(); 
			if (this.validConnectors.contains(connector.getId())) {
				return connector;
			}
		}
		return null;
	}
	
	private CoreExtensionsManager() {
		this.disableHelpers = false;
		
		Object []extensions = this.loadCoreExtensions(CoreExtensionsManager.CORE_OPTIONS);
		this.optionProvider = extensions.length != 0 ? (IOptionProvider)extensions[0] : IOptionProvider.DEFAULT;
		extensions = this.loadCoreExtensions(CoreExtensionsManager.CRASH_RECOVERY);
		this.helpers = Arrays.asList(extensions).toArray(new IResolutionHelper[extensions.length]);
		extensions = this.loadCoreExtensions(CoreExtensionsManager.IGNORE_RECOMMENDATIONS);
		this.ignoreRecommendations = Arrays.asList(extensions).toArray(new IIgnoreRecommendations[extensions.length]);
	}
	
	private synchronized void initializeConnectors() {
		if (this.connectors == null) {
			this.connectors = new HashMap<String, ISVNConnectorFactory>();
			this.validConnectors = new HashSet<String>();
			Object []extensions = this.loadCoreExtensions(CoreExtensionsManager.SVN_CONNECTOR);
			for (int i = 0; i < extensions.length; i++) {
				ISVNConnectorFactory factory = new ThreadNameModifierFactory((ISVNConnectorFactory)extensions[i]);
				try {
					// extension point API changed and old connectors will be declined due to version changes or AbstractMethodError.
					if (factory.getCompatibilityVersion().compareTo(ISVNConnectorFactory.CURRENT_COMPATIBILITY_VERSION) != 0) {
						continue;
					}
				}
				catch (Throwable ex) {
					continue;
				}
				this.connectors.put(factory.getId(), factory);
				this.validateClient(factory);
			}
		}
	}
	
	private void validateClient(ISVNConnectorFactory connector) {
		try {
			connector.newInstance().dispose();
			this.validConnectors.add(connector.getId());
		}
		catch (Throwable ex) {
			// do nothing
		}
	}
	
	private Object []loadCoreExtensions(String extensionPoint) {
		return this.loadExtensions(CoreExtensionsManager.EXTENSION_NAMESPACE, extensionPoint);
	}
	
	private Object []loadExtensions(String namespace, String extensionPoint) {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(namespace, extensionPoint);
		if (extension == null) {
			String errMessage = SVNMessages.formatErrorString("Error_InvalidExtensionPoint", new String[] {namespace, extensionPoint});				 //$NON-NLS-1$
			throw new RuntimeException(errMessage);
		}
		IExtension []extensions = extension.getExtensions();
		ArrayList<Object> retVal = new ArrayList<Object>();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				try {
					retVal.add(configElements[j].createExecutableExtension("class")); //$NON-NLS-1$
				}
				catch (CoreException ex) {
					LoggedOperation.reportError(SVNMessages.getErrorString("Error_LoadExtensions"), ex); //$NON-NLS-1$
				}
			}
		}
		return retVal.toArray();
	}

}
