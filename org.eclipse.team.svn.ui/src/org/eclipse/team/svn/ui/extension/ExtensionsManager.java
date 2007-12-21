/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrej Zachar - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.decorator.IDecorationFilter;
import org.eclipse.team.svn.ui.extension.factory.ICheckoutFactory;
import org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory;
import org.eclipse.team.svn.ui.extension.factory.IHistoryViewFactory;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;
import org.eclipse.team.svn.ui.extension.factory.IPredefinedPropertySet;
import org.eclipse.team.svn.ui.extension.factory.IShareProjectFactory;
import org.eclipse.team.svn.ui.extension.factory.ISynchronizeViewActionContributor;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory.ReportType;
import org.eclipse.team.svn.ui.extension.impl.DefaultCheckoutFactory;
import org.eclipse.team.svn.ui.extension.impl.DefaultCommitActionFactory;
import org.eclipse.team.svn.ui.extension.impl.DefaultDecorationFilter;
import org.eclipse.team.svn.ui.extension.impl.DefaultHistoryViewFactory;
import org.eclipse.team.svn.ui.extension.impl.DefaultShareProjectFactory;
import org.eclipse.team.svn.ui.extension.impl.DefaultSynchronizeViewActionContributor;
import org.eclipse.team.svn.ui.extension.impl.PredefinedPropertySet;

/**
 * Manager for extension components. Used to extend Subversive without direct dependencies.
 * 
 * @author Andrej Zachar
 */
public class ExtensionsManager {
	private static final String UI_EXTENSION_NAMESPACE = "org.eclipse.team.svn.ui";
	
	private ICommitActionFactory currentCommitFactory;
	private IHistoryViewFactory currentMessageFactory;
	private ICheckoutFactory currentCheckoutFactory;
	private IShareProjectFactory currentShareProjectFactory;
	private IDecorationFilter currentDecorationFilter;
	private IPredefinedPropertySet predefinedPropertySet;
	private ISynchronizeViewActionContributor currentActionContributor;

	private IReportingDescriptor []reportingDescriptors;
	private IReporterFactory []reporterFactories;

	private static ExtensionsManager instance;

	public synchronized static ExtensionsManager getInstance() {
		if (ExtensionsManager.instance == null) {
			ExtensionsManager.instance = new ExtensionsManager();
		}
		return ExtensionsManager.instance;
	}
	
	public ISynchronizeViewActionContributor getCurrentSynchronizeActionContributor() {
		if (this.currentActionContributor == null) {
			this.currentActionContributor = ExtensionsManager.getDefaultSynchronizeViewActionContributor();
		}
		return this.currentActionContributor;
	}
	
	public void setCurrentSynchronizeActionContributor(ISynchronizeViewActionContributor contributor) {
		this.currentActionContributor = contributor;
	}
	
	public IDecorationFilter getCurrentDecorationFilter() {
		if (this.currentDecorationFilter == null) {
			this.currentDecorationFilter = ExtensionsManager.getDefaultDecorationFilter();
		}
		return this.currentDecorationFilter;
	}
	
	public void setCurrentDecorationFilter(IDecorationFilter filter) {
		this.currentDecorationFilter = filter;
	}

	public ICheckoutFactory getCurrentCheckoutFactory() {
		if (this.currentCheckoutFactory == null) {
			this.currentCheckoutFactory = ExtensionsManager.getDefaultCheckoutFactory();
		}
		return this.currentCheckoutFactory;
	}
	
	public void setCurrentCheckoutFactory(ICheckoutFactory factory) {
		this.currentCheckoutFactory = factory;
	}
	
	public IShareProjectFactory getCurrentShareProjectFactory() {
		if (this.currentShareProjectFactory == null) {
			this.currentShareProjectFactory = ExtensionsManager.getDefaultShareProjectFactory();
		}
		return this.currentShareProjectFactory;
	}
	
	public void setCurrentShareProjectFactory(IShareProjectFactory factory) {
		this.currentShareProjectFactory = factory;
	}
	
	public IReportingDescriptor []getReportingDescriptors() {
		return this.reportingDescriptors;
	}
	
	public IReporter getReporter(IReportingDescriptor descriptor, ReportType type) {
		for (int i = 0; i < this.reporterFactories.length; i++) {
			IReporter reporter = this.reporterFactories[i].newReporter(descriptor, type);
			if (reporter != null) {
				return reporter;
			}
		}
		return null;
	}
	
	public ICommitActionFactory getCurrentCommitFactory() {
		if (this.currentCommitFactory == null) {
			this.currentCommitFactory = getDefaultTeamCommitFactory();
		}
		return this.currentCommitFactory;
	}
	
	public void setCurrentCommitFactory(ICommitActionFactory currentFactory) {
		this.currentCommitFactory = currentFactory;
	}

	public IHistoryViewFactory getCurrentMessageFactory() {
		if (this.currentMessageFactory == null) {
			this.currentMessageFactory = getDefaultCommitMessageFactory();
		}
		return this.currentMessageFactory;
	}

	public void setCurrentMessageFactory(IHistoryViewFactory currentMessageFactory) {
		this.currentMessageFactory = currentMessageFactory;
	}

	public void setPredefinedPropertySet(IPredefinedPropertySet provider) {
		this.predefinedPropertySet = provider;
	}
	
	public IPredefinedPropertySet getPredefinedPropertySet() {		
		return this.predefinedPropertySet;		
	}

	public static ICommitActionFactory getDefaultTeamCommitFactory() {
		return new DefaultCommitActionFactory();
	}

	public static IHistoryViewFactory getDefaultCommitMessageFactory() {
		return new DefaultHistoryViewFactory();
	}
	
	public static ICheckoutFactory getDefaultCheckoutFactory() {
		return new DefaultCheckoutFactory();
	}
	
	public static IShareProjectFactory getDefaultShareProjectFactory() {
		return new DefaultShareProjectFactory();
	}
	
	public static IDecorationFilter getDefaultDecorationFilter() {
		return new DefaultDecorationFilter();
	}
	
	public static ISynchronizeViewActionContributor getDefaultSynchronizeViewActionContributor() {
		return new DefaultSynchronizeViewActionContributor();
	}
	
	private ExtensionsManager() {
		this.predefinedPropertySet = new PredefinedPropertySet();
		this.currentDecorationFilter = (IDecorationFilter)this.loadUIExtension("decoration");
		this.currentMessageFactory = (IHistoryViewFactory)this.loadUIExtension("history");
		this.currentCommitFactory = (ICommitActionFactory)this.loadUIExtension("commit");
		this.currentCheckoutFactory = (ICheckoutFactory)this.loadUIExtension("checkout");
		this.currentShareProjectFactory = (IShareProjectFactory)this.loadUIExtension("shareproject");
		this.currentActionContributor = (ISynchronizeViewActionContributor)this.loadUIExtension("synchronizeActionContribution");
		Object []extensions = this.loadUIExtensions("reportingdescriptor");
		this.reportingDescriptors = (IReportingDescriptor [])Arrays.asList(extensions).toArray(new IReportingDescriptor[extensions.length]);
		extensions = this.loadUIExtensions("reporterfactory");
		this.reporterFactories = (IReporterFactory [])Arrays.asList(extensions).toArray(new IReporterFactory[extensions.length]);
		Arrays.sort(this.reporterFactories, new Comparator() {
			public int compare(Object o1, Object o2) {
				IReporterFactory f1 = (IReporterFactory)o1;
				IReporterFactory f2 = (IReporterFactory)o2;
				return f1.isCustomEditorSupported() ^ f2.isCustomEditorSupported() ? (f1.isCustomEditorSupported() ? -1 : 1) : 0;
			}
		});
	}

	private Object loadUIExtension(String extensionPoint) {
		Object []extensions = this.loadUIExtensions(extensionPoint);
		return extensions.length == 0 ? null : extensions[0];
	}
	
	private Object []loadUIExtensions(String extensionPoint) {
		return this.loadExtensions(ExtensionsManager.UI_EXTENSION_NAMESPACE, extensionPoint);
	}
	
	private Object []loadExtensions(String namespace, String extensionPoint) {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(namespace, extensionPoint);
		IExtension []extensions = extension.getExtensions();
		ArrayList retVal = new ArrayList();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				try {
					retVal.add(configElements[j].createExecutableExtension("class"));
				}
				catch (CoreException ex) {
				    LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.LoadUIExtension"), ex);
				}
			}
		}
		return retVal.toArray();
	}

}
