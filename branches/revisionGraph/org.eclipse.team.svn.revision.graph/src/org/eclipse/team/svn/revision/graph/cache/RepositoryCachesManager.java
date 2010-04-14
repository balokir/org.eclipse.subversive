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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Manage repository caches
 *  
 * @author Igor Burilo
 */
public class RepositoryCachesManager {

	protected final static String REPOSITORIES_FILE_NAME = "repositories.data"; //$NON-NLS-1$
	
	//repository root without protocol -> cacheInfo 
	protected Map<String, RepositoryCacheInfo> caches = new HashMap<String, RepositoryCacheInfo>(); 
	
	protected final File cacheFolder;
	
	protected Object lock = new Object();
	
	public RepositoryCachesManager(File cacheFolder) throws IOException {
		this.cacheFolder = cacheFolder;
		if (!this.cacheFolder.exists()) {
			this.cacheFolder.mkdirs();
		}
		this.load();
	} 
	
	protected void save() throws IOException {				
		File file = new File(this.cacheFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);		
		PrintWriter out = new PrintWriter(file, "UTF-8"); //$NON-NLS-1$
		try {
			for (Map.Entry<String, RepositoryCacheInfo> entry : this.caches.entrySet()) {
				out.println(entry.getKey());
				out.println(entry.getValue().getMetaDataFile().getName());
			}	
		} finally {
			out.close();	
		}				
	}
	
	protected final void load() throws IOException {
		File file = new File(this.cacheFolder, RepositoryCachesManager.REPOSITORIES_FILE_NAME);
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
					
					File metadataFile = new File(this.cacheFolder, fileName);
					RepositoryCacheInfo cacheInfo = new RepositoryCacheInfo(metadataFile);
					cacheInfo.load();
					
					this.caches.put(url, cacheInfo);
				}
			} finally {
				try { in.close(); } catch (IOException e) { /*ignore*/ }
			}
		}
	}
	
	public RepositoryCacheInfo getCache(IRepositoryResource resource) throws IOException {
		synchronized (this.lock) {
			String url = getRepositoryRoot(resource);
			RepositoryCacheInfo cacheInfo = this.caches.get(url);
			if (cacheInfo == null) {
				File metadataFile = new File(this.cacheFolder, escapeUrl(url) + ".meta"); //$NON-NLS-1$
				cacheInfo = new RepositoryCacheInfo(metadataFile);
				cacheInfo.init();
				
				this.caches.put(url, cacheInfo);		
				this.save();			
			}
			return cacheInfo;	
		}
	}

	public RepositoryCacheInfo[] getCaches() {
		synchronized (this.lock) {
			return this.caches.values().toArray(new RepositoryCacheInfo[0]);	
		}		
	}
	
	public void importCache(File cacheData) {
		//TODO implement
	}
	
	public void deleteCache(RepositoryCacheInfo cache) {
		//TODO implement
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
}
