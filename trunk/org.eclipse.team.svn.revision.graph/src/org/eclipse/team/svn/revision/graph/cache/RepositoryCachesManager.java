/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.preferences.SVNRevisionGraphPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Manage repository caches
 *  
 * @author Igor Burilo
 */
public class RepositoryCachesManager implements IPropertyChangeListener {
	
	public final static String ADD_CACHES_PROPERTY = "addCaches"; //$NON-NLS-1$
	public final static String REMOVE_CACHES_PROPERTY = "removeCaches"; //$NON-NLS-1$
	
	protected final static String REPOSITORIES_FILE_NAME = "repositories.data"; //$NON-NLS-1$
	
	//repository root without protocol -> cacheInfo 
	protected Map<String, RepositoryCacheInfo> caches = new HashMap<String, RepositoryCacheInfo>(); 
	
	protected File cacheFolder;
	
	protected final Object lock = new Object();
	
	protected Set<IPropertyChangeListener> listeners = Collections.synchronizedSet(new HashSet<IPropertyChangeListener>());
		
	public RepositoryCachesManager(File cacheFolder) throws IOException {
		this.cacheFolder = cacheFolder;
		if (!this.cacheFolder.exists()) {
			this.cacheFolder.mkdirs();
		} else {
			this.load();
		}		
		
		SVNRevisionGraphPlugin.instance().getPreferenceStore().addPropertyChangeListener(this);
	} 
	
	//don't synchronize as it's synchronized by caller
	protected void save() throws IOException {				
		File file = new File(this.cacheFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);
		RepositoryCachesManager.saveRepositoryCachesData(file, this.caches.values());						
	}
	
	protected static void saveRepositoryCachesData(File file, Collection<RepositoryCacheInfo> caches) throws IOException {
		if (!caches.isEmpty()) {
			PrintWriter out = new PrintWriter(file, "UTF-8"); //$NON-NLS-1$
			try {
				for (RepositoryCacheInfo cache : caches) {
					out.println(cache.getRepositoryName());
					out.println(cache.getMetaDataFile().getName());
				}	
			} finally {
				out.close();	
			}		
		} else if (file.exists()) {
			file.delete();
		}
	}
	
	//don't synchronize as it's synchronized by caller
	protected final void load() throws IOException {
		File file = new File(this.cacheFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);
		Map<String, String> rawData = RepositoryCachesManager.loadRawRepositoryCachesData(file);
		for (Map.Entry<String, String> entry : rawData.entrySet()) {
			String url = entry.getKey();
			String fileName = entry.getValue();
			File metadataFile = new File(this.cacheFolder, fileName);
			RepositoryCacheInfo cacheInfo = new RepositoryCacheInfo(url, metadataFile);
			cacheInfo.load();
			
			this.caches.put(url, cacheInfo);
		}			
	}
	
	/**
	 * Return map where key is repository url and value is metadata file name 
	 */
	protected static Map<String, String> loadRawRepositoryCachesData(File file) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		if (file.exists()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); //$NON-NLS-1$
			try {				
				while (true) {
					String url = in.readLine();
					if (url == null) {
						break;
					} 
					String fileName = in.readLine();
					if (fileName == null) {
						break;
					}

					result.put(url, fileName);					
				}				
			} finally {
				try { in.close(); } catch (IOException e) { /*ignore*/ }
			}
		}
		return result;
	}
	
	public RepositoryCacheInfo getCache(IRepositoryResource resource) throws IOException {
		RepositoryCacheInfo cacheInfo;
		synchronized (this.lock) {
			String url = getRepositoryRoot(resource);
			cacheInfo = this.caches.get(url);
			if (cacheInfo == null) {
				File metadataFile = new File(this.cacheFolder, escapeUrl(url) + ".meta"); //$NON-NLS-1$
				cacheInfo = new RepositoryCacheInfo(url, metadataFile);				
				
				this.caches.put(url, cacheInfo);		
				this.save();									
			}
				
		}		
		this.fireEvent(new PropertyChangeEvent(this, RepositoryCachesManager.ADD_CACHES_PROPERTY, null, new RepositoryCacheInfo[] { cacheInfo }));
		return cacheInfo;
	}

	public RepositoryCacheInfo[] getCaches() {
		synchronized (this.lock) {
			return this.caches.values().toArray(new RepositoryCacheInfo[0]);	
		}		
	}
	
	public void remove(RepositoryCacheInfo[] cachesInfo) {
		if (cachesInfo.length > 0) {			
			List<RepositoryCacheInfo> removedCaches = new ArrayList<RepositoryCacheInfo>(); 
			synchronized (this.lock) {
				for (RepositoryCacheInfo cache : cachesInfo) {
					if (this.caches.containsKey(cache.getRepositoryName())) {
						//TODO add notification to user that cache can't be delete ?
						if (cache.remove()) {
							this.caches.remove(cache.getRepositoryName());
							removedCaches.add(cache);							
						}						
					}										
				}
			
				if (!removedCaches.isEmpty()) {	
					try {
						this.save();
					} catch (IOException e) {
						LoggedOperation.reportError(this.getClass().getName(), e);
					}
				}				
			}
						
			if (!removedCaches.isEmpty()) {
				this.fireEvent(new PropertyChangeEvent(this, RepositoryCachesManager.REMOVE_CACHES_PROPERTY, null, removedCaches.toArray(new RepositoryCacheInfo[0])));	
			}			
		}		
	}	
	

	public void export(final File destinationFolder, final RepositoryCacheInfo[] cachesInfo) {
		if (cachesInfo.length > 0) {
			IActionOperation op = new AbstractActionOperation("Operation_ExportCaches", SVNRevisionGraphMessages.class) { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {					
					if (!destinationFolder.exists()) {
						if (!destinationFolder.mkdirs()) {
							throw new UnreportableException("Failed to create destination folder: " + destinationFolder); //$NON-NLS-1$
						}
					}
					
					//hold lock during exporting, e.g. not to allow remove					
					synchronized (lock) {
						final List<RepositoryCacheInfo> exportedCaches = new ArrayList<RepositoryCacheInfo>();
						
						//copy caches itself
						for (final RepositoryCacheInfo cache : cachesInfo) {
							if (monitor.isCanceled()) {
								return;
							}															
							this.protectStep(new IUnprotectedOperation() {									
								public void run(IProgressMonitor monitor) throws Exception {
									if (caches.containsKey(cache.getRepositoryName())) {
										//TODO add notification to user that cache can't be exported ?
										if (cache.export(destinationFolder, monitor)) {
											exportedCaches.add(cache);
										}
									}
								}
							}, monitor, cachesInfo.length, 1);																									
						}
						
						//create repositories file
						if (!exportedCaches.isEmpty()) {
							File repositoriesFile = new File(destinationFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);
							RepositoryCachesManager.saveRepositoryCachesData(repositoriesFile, exportedCaches);
						}
					}								
				}
			};			
			UIMonitorUtility.doTaskNowDefault(op, true);
		}				
	}
	
	public void importCache(final File cacheDataFolder) {				
		IActionOperation op = new AbstractActionOperation("Operation_ImportCaches", SVNRevisionGraphMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {								
				if (!cacheDataFolder.exists()) {
					return;
				}
				
				//read repositories
				File file = new File(cacheDataFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);
				Map<String, String> rawRepositoriesData = RepositoryCachesManager.loadRawRepositoryCachesData(file);
				if (!rawRepositoriesData.isEmpty()) {
					
					final List<RepositoryCacheInfo> importedCaches = new ArrayList<RepositoryCacheInfo>();
					
					//copy cache data
					for (final Map.Entry<String, String> entry : rawRepositoriesData.entrySet()) {
						if (!monitor.isCanceled()) {								
							this.protectStep(new IUnprotectedOperation() {								
								public void run(IProgressMonitor monitor) throws Exception {
									String url = entry.getKey();
									String metaFileName = entry.getValue();
									
									File metadataFile = new File(cacheDataFolder, metaFileName);
									File cacheDataFile = new File(cacheDataFolder, RepositoryCacheInfo.getCacheDataFileName(metaFileName));
									
									if (metadataFile.exists() && cacheDataFile.exists()) {
										FileUtility.copyFile(RepositoryCachesManager.this.cacheFolder, cacheDataFile, monitor);
										FileUtility.copyFile(RepositoryCachesManager.this.cacheFolder, metadataFile, monitor);	
										
										//record to repositories
										RepositoryCacheInfo cacheInfo = new RepositoryCacheInfo(url, metadataFile);
										cacheInfo.load();
										
										synchronized (lock) {
											caches.put(url, cacheInfo);
											save();
										}
										
										importedCaches.add(cacheInfo);
									}									
								}
							}, monitor, rawRepositoriesData.size(), 1);
						}												
					}
					
					if (!importedCaches.isEmpty()) {
						fireEvent(new PropertyChangeEvent(this, RepositoryCachesManager.ADD_CACHES_PROPERTY, null, importedCaches.toArray(new RepositoryCacheInfo[0])));	
					}
				}
			}
		};
		UIMonitorUtility.doTaskNowDefault(op, true);
	}
	
	protected void changeCacheFolder(File cacheFolder) throws IOException {
		RepositoryCacheInfo[] oldCaches;
		RepositoryCacheInfo[] newCaches;
		synchronized (this.lock) {
			oldCaches = this.getCaches();
			
			//clear previous data structures
			if (!this.caches.isEmpty()) {
				this.caches.clear();
			}
			
			this.cacheFolder = cacheFolder;
			if (!this.cacheFolder.exists()) {
				this.cacheFolder.mkdirs();
			} else {
				this.load();
			}				
			
			newCaches = this.getCaches();
		}
		
		if (oldCaches.length > 0) {
			this.fireEvent(new PropertyChangeEvent(this, RepositoryCachesManager.REMOVE_CACHES_PROPERTY, null, oldCaches));	
		}		
		if (newCaches.length > 0) {
			this.fireEvent(new PropertyChangeEvent(this, RepositoryCachesManager.ADD_CACHES_PROPERTY, null, newCaches));	
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if ((SVNRevisionGraphPreferences.fullCacheName(SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME).equals(event.getProperty()))) {
			String cachePath = SVNRevisionGraphPreferences.getCacheString(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME);
			File cacheFolder = new File(cachePath);
			try {
				this.changeCacheFolder(cacheFolder);
			} catch (Exception e) {
				LoggedOperation.reportError(this.getClass().getName(), e);
			}
		}		
	}
	
	public void dispose() {
		SVNRevisionGraphPlugin.instance().getPreferenceStore().removePropertyChangeListener(this);
	}
	
	/**
	 * Return url without protocol 
	 */
	protected static String getRepositoryRoot(IRepositoryResource resource) {
		String url = resource.getRepositoryLocation().getRepositoryRootUrl();		
		final String[] knownPrefixes = new String[] {"http://", "https://", "svn://", "svn+ssh://", "file:///", "file://" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		for (int i = 0; i < knownPrefixes.length; i ++) {
			if (url.startsWith(knownPrefixes[i])) {				
				url = url.substring(knownPrefixes[i].length());
				break;
			}
		}
		return url;
	}
	
	protected static String escapeUrl(String url) {		
		try {
			url = url.replaceAll("[\\/:*?\"<>|]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
			url = URLEncoder.encode(url, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			//ignore
		}
		return url;
	}
	
	public void addListener(IPropertyChangeListener listener) {
		if (listener != null) {
			this.listeners.add(listener);	
		}		
	}
	
	public void removeListener(IPropertyChangeListener listener) {
		if (listener != null) {
			this.listeners.remove(listener);
		}
	}
	
	protected void fireEvent(PropertyChangeEvent event) {
		//TODO call asynchronously ?		
		IPropertyChangeListener[] listenersArray = this.listeners.toArray(new IPropertyChangeListener[0]);
		for (IPropertyChangeListener listener : listenersArray) {
			listener.propertyChange(event);
		}				
	}

}
