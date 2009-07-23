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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.IDynamicExtensionRegistry;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.osgi.framework.Bundle;

/**
 * @author David Green
 * @author Igor Burilo
 */
class DiscoveryRegistryStrategy extends RegistryStrategy {

	private final List<JarFile> jars = new ArrayList<JarFile>();

	private final Map<IContributor, File> contributorToJarFile = new HashMap<IContributor, File>();

	private final Map<IContributor, String> contributorToDirectoryEntry = new HashMap<IContributor, String>();

	private final Object token;

	private File bundleFile;
	private String discoveryUrl;
	
	public DiscoveryRegistryStrategy(File[] storageDirs, boolean[] cacheReadOnly, Object token) {
		super(storageDirs, cacheReadOnly);
		this.token = token;
	}

	@Override
	public void onStart(IExtensionRegistry registry, boolean loadedFromCache) {
		super.onStart(registry, loadedFromCache);
		if (!loadedFromCache) {
			processDiscoveryCoreBundle(registry);
			processBundles(registry);
		}
	}

	private void processDiscoveryCoreBundle(IExtensionRegistry registry) {
		// we must add a contribution from the core bundle so that we get the
		// extension point itself
		try {			
			Bundle bundle = Platform.getBundle(CoreExtensionsManager.EXTENSION_NAMESPACE);
			IContributor contributor = new RegistryContributor(bundle.getSymbolicName(), bundle.getSymbolicName(),
					null, null);

			InputStream inputStream = bundle.getEntry("plugin.xml").openStream(); //$NON-NLS-1$
			try {
				registry.addContribution(inputStream, contributor, false, bundle.getSymbolicName(), null, token);
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	private void processBundles(IExtensionRegistry registry) {
		if (this.bundleFile == null || this.discoveryUrl == null) {
			throw new IllegalStateException();
		}
		
		try {
			processBundle(registry);
		} catch (Exception e) {
			String errMessage = NLS.bind(
					Messages.DiscoveryRegistryStrategy_cannot_load_bundle, new Object[] {
							this.bundleFile.getName(), this.discoveryUrl, e.getMessage() }); 
			LoggedOperation.reportError(this.getClass().getName(), new Exception(errMessage, e));						
		}		
	}

	private void processBundle(IExtensionRegistry registry) throws IOException {
		JarFile jarFile = new JarFile(bundleFile);
		jars.add(jarFile);

		ZipEntry pluginXmlEntry = jarFile.getEntry("plugin.xml"); //$NON-NLS-1$
		if (pluginXmlEntry == null) {
			throw new IOException(Messages.DiscoveryRegistryStrategy_missing_pluginxml);
		}
		IContributor contributor = new RegistryContributor(bundleFile.getName(), bundleFile.getName(), null, null);
		if (((IDynamicExtensionRegistry) registry).hasContributor(contributor)) {
			jarFile.close();
			return;
		}
		contributorToJarFile.put(contributor, this.bundleFile);
		contributorToDirectoryEntry.put(contributor, this.discoveryUrl);

		ResourceBundle translationBundle = loadTranslationBundle(jarFile);

		InputStream inputStream = jarFile.getInputStream(pluginXmlEntry);
		try {
			registry.addContribution(inputStream, contributor, false, bundleFile.getPath(), translationBundle, token);
		} finally {
			inputStream.close();
		}
	}

	private ResourceBundle loadTranslationBundle(JarFile jarFile) throws IOException {
		List<String> bundleNames = computeBundleNames("plugin"); //$NON-NLS-1$
		for (String bundleName : bundleNames) {
			ZipEntry entry = jarFile.getEntry(bundleName);
			if (entry != null) {
				InputStream inputStream = jarFile.getInputStream(entry);
				try {
					PropertyResourceBundle resourceBundle = new PropertyResourceBundle(inputStream);
					return resourceBundle;
				} finally {
					inputStream.close();
				}
			}
		}
		return null;
	}

	private List<String> computeBundleNames(String baseName) {
		String suffix = ".properties"; //$NON-NLS-1$
		String name = baseName;
		List<String> bundleNames = new ArrayList<String>();
		Locale locale = Locale.getDefault();
		bundleNames.add(name + suffix);
		if (locale.getLanguage() != null && locale.getLanguage().length() > 0) {
			name = name + '_' + locale.getLanguage();
			bundleNames.add(0, name + suffix);
		}
		if (locale.getCountry() != null && locale.getCountry().length() > 0) {
			name = name + '_' + locale.getCountry();
			bundleNames.add(0, name + suffix);
		}
		if (locale.getVariant() != null && locale.getVariant().length() > 0) {
			name = name + '_' + locale.getVariant();
			bundleNames.add(0, name + suffix);
		}
		return bundleNames;
	}

	@Override
	public void onStop(IExtensionRegistry registry) {
		try {
			super.onStop(registry);
		} finally {
			for (JarFile jar : jars) {
				try {
					jar.close();
				} catch (Exception e) {
				}
			}
			jars.clear();
		}
	}

	/**
	 * get the jar file that corresponds to the given contributor.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given contributor is unknown
	 */
	public File getJarFile(IContributor contributor) {
		File file = contributorToJarFile.get(contributor);
		if (file == null) {
			throw new IllegalArgumentException(contributor.getName());
		}
		return file;
	}

	public void setDiscoveryInfo(File bundleFile, String discoveryUrl) {
		this.bundleFile = bundleFile;
		this.discoveryUrl = discoveryUrl;
	}

}
