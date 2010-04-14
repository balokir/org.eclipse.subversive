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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.operation.CheckRepositoryConnectionOperation;
import org.eclipse.team.svn.revision.graph.operation.FetchNewRevisionsOperation;
import org.eclipse.team.svn.revision.graph.operation.FetchSkippedRevisionsOperation;
import org.eclipse.team.svn.revision.graph.operation.PrepareRevisionDataOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * @author Igor Burilo
 */
public class RepositoryCacheInfo {

	protected final static String START_SKIPPED_REVISION = "startSkippedRevision"; //$NON-NLS-1$
	protected final static String END_SKIPPED_REVISION = "endSkippedRevision"; //$NON-NLS-1$
	protected final static String LAST_PROCESSED_REVISION = "lastProcessedRevision"; //$NON-NLS-1$
	protected final static String CACHE_DATA_FILE_NAME = "dataFileName"; //$NON-NLS-1$
	
	protected long startSkippedRevision;
	protected long endSkippedRevision;
	protected long lastProcessedRevision; 
	protected String cacheDataFileName;
	
	protected final File metadataFile;
	
	
	//--- access to below resources require synchronization
	
	//indicates whether we're calculating cache now
	protected boolean isCalculating;
	
	protected int cacheReferencesCount;
	protected RepositoryCache repositoryCache;
	
	protected Set<IRepositoryResource> resourcesForWhichCacheIsOpened = new HashSet<IRepositoryResource>();
	
	protected final Object calculateLock = new Object();
	
	public enum CacheResultEnum {
		OK, BROKEN, CALCULATING
	} 
	
	public static class CacheResult {

		public final CacheResultEnum status;
		public final RepositoryCache repositoryCache;
		
		public CacheResult(CacheResultEnum status, RepositoryCache repositoryCache) {
			this.status = status;
			this.repositoryCache = repositoryCache;
		}		
	}
	
	public RepositoryCacheInfo(File metadataFile) {		
		this.metadataFile = metadataFile;		
	}
	
	public void init() {
		this.startSkippedRevision = 0;
		this.endSkippedRevision = 0;
		this.lastProcessedRevision = 0;
		
		String metaName = this.metadataFile.getName();
		int index = metaName.lastIndexOf("."); //$NON-NLS-1$
		if (index != -1) {
			this.cacheDataFileName = metaName.substring(0, index) + ".data"; //$NON-NLS-1$
		} else {
			this.cacheDataFileName = metaName + ".data"; //$NON-NLS-1$
		}			
	}
	
	public void load() throws IOException {
		if (this.metadataFile.exists()) {
			Properties props = new Properties();
			FileInputStream in = new FileInputStream(this.metadataFile);
			try {
				props.load(in);
				this.startSkippedRevision = this.getLongProperty(props, RepositoryCacheInfo.START_SKIPPED_REVISION);
				this.endSkippedRevision = this.getLongProperty(props, RepositoryCacheInfo.END_SKIPPED_REVISION);
				this.lastProcessedRevision = this.getLongProperty(props, RepositoryCacheInfo.LAST_PROCESSED_REVISION);
				this.cacheDataFileName = this.getProperty(props, RepositoryCacheInfo.CACHE_DATA_FILE_NAME); 
			} finally {
				try { in.close(); } catch (IOException e) { /*ignore*/ }
			}			
		} else {
			this.init();
		} 
	}
	
	protected String getProperty(Properties props, String propertyName) {
		String value = props.getProperty(propertyName);
		if (value != null) { 
			value = value.trim();
			if (value.length() == 0) {
				value = null;
			}
		}		
		return value;
	}
	
	protected long getLongProperty(Properties props, String propertyName) {
		long res = 0;
		String value = props.getProperty(propertyName);
		if (value != null && (value = value.trim()).length() > 0) {
			try {
				res = Long.parseLong(value);
			} catch (NumberFormatException ne) {
				//ignore
			}
		}
		return res;
	}
	
	public void save() throws IOException {
		Properties props = new Properties();
		props.put(RepositoryCacheInfo.START_SKIPPED_REVISION, String.valueOf(this.startSkippedRevision));
		props.put(RepositoryCacheInfo.END_SKIPPED_REVISION, String.valueOf(this.endSkippedRevision));
		props.put(RepositoryCacheInfo.LAST_PROCESSED_REVISION, String.valueOf(this.lastProcessedRevision));	
		props.put(RepositoryCacheInfo.CACHE_DATA_FILE_NAME, this.cacheDataFileName);
		
		FileOutputStream out = new FileOutputStream(this.metadataFile);		
		try {
			props.store(out, null);
		} finally {
			try { out.close(); } catch (IOException e) {/*ignore*/}
		}
	}
	
	public long getStartSkippedRevision() {
		return this.startSkippedRevision;
	}

	public long getEndSkippedRevision() {
		return this.endSkippedRevision;
	}

	public long getLastProcessedRevision() {
		return this.lastProcessedRevision;
	}
	
	public void setSkippedRevisions(long start, long end) {
		this.startSkippedRevision = start;
		this.endSkippedRevision = end;		
	}
	
	public void setLastProcessedRevision(long revision) {
		this.lastProcessedRevision = revision;
	}	
	
	public String getCacheDataFileName() {
		return this.cacheDataFileName;
	}
	
	public File getMetaDataFile() {
		return this.metadataFile;
	}
	
	/**
	 * Calculate cache data.
	 * 
	 * In order to release cache from memory call <code>disposeRepositoryCache</code>.
	 * 
	 * Policies how cache data is created:
	 * 
	 * - if another task is calculating/refreshing cache, 
	 *   then don't calculate and return {@link CacheResultEnum.CALCULATING} status
	 *   
	 * - if cache was already calculated for passed resource and dispose method wasn't called for it, 
	 *   then don't calculate and return {@link CacheResultEnum.BROKEN} status
	 *   
	 * - if error or canceling happened during calculating, then return {@link CacheResultEnum.BROKEN}  
	 * 
	 * - if cache is successfully calculated, then return {@link CacheResultEnum.OK} and resulted cache 
	 * 
	 * - if there are references to cache and this method is called, then update cache
	 */
	public CacheResult createCacheData(IRepositoryResource resource, IProgressMonitor monitor) {		
		boolean isRefresh = false;
		RepositoryCache previousCache = null;
		synchronized (this.calculateLock) {
			
			if (this.resourcesForWhichCacheIsOpened.contains(resource)) {
				return new CacheResult(CacheResultEnum.BROKEN, null);
			}
			
			if (this.isCalculating) {
				return new CacheResult(CacheResultEnum.CALCULATING, null);
			} else {
				this.isCalculating = true;
			}			
			
			previousCache = this.repositoryCache;
			
			if (previousCache != null) {	
				isRefresh = true;				
			}						
		}		
		
		try {
			RepositoryCache cache;
			IActionOperation op;
			if (isRefresh) {
				op = this.getRefreshOperation(resource, previousCache);
				cache = previousCache;
			} else {
				File cacheDataFile = new File(this.metadataFile.getParentFile(), this.cacheDataFileName);		
				cache = new RepositoryCache(cacheDataFile, this);			
				op = this.getCreateOperation(resource, cache);
			}			
			
			//call synchronously				
			ProgressMonitorUtility.doTask(UIMonitorUtility.DEFAULT_FACTORY.getLogged(op), monitor, 1, 1);
			
			if (op.getExecutionState() == IActionOperation.OK) {				
				synchronized (this.calculateLock) {
					if (!this.resourcesForWhichCacheIsOpened.contains(resource)) {
						this.cacheReferencesCount ++;
						this.resourcesForWhichCacheIsOpened.add(resource);
					}
					this.repositoryCache = cache;	
				}				
				cache.prepareModel();
				return new CacheResult(CacheResultEnum.OK, cache);
			} else {
				return new CacheResult(CacheResultEnum.BROKEN, null);
			}
			
		} finally {
			synchronized (this.calculateLock) {
				this.isCalculating = false;
			}
		}
	}
	
	public CacheResult refreshCacheData(IRepositoryResource resource, IProgressMonitor monitor) {
		RepositoryCache previousCache = null;
		synchronized (this.calculateLock) {
			previousCache = this.repositoryCache;			
			if (previousCache == null) {
				return new CacheResult(CacheResultEnum.BROKEN, null);
			}
			
			if (this.isCalculating) {
				return new CacheResult(CacheResultEnum.CALCULATING, null);
			} else {
				this.isCalculating = true;
			}	
		}
		
		try {		
			IActionOperation op = this.getRefreshOperation(resource, previousCache);				
			
			//call synchronously				
			ProgressMonitorUtility.doTask(UIMonitorUtility.DEFAULT_FACTORY.getLogged(op), monitor, 1, 1);			
			
			if (op.getExecutionState() == IActionOperation.OK) {
				previousCache.prepareModel();								
				return new CacheResult(CacheResultEnum.OK, previousCache);
			} else {
				return new CacheResult(CacheResultEnum.BROKEN, null);
			}
						
		} finally {
			synchronized (this.calculateLock) {
				this.isCalculating = false;
			}
		}
	}
	
	protected IActionOperation getCreateOperation(IRepositoryResource resource, RepositoryCache cache) {
		CompositeOperation op = new CompositeOperation("Operation_CreateCache"); //$NON-NLS-1$
		
		CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(resource);
		op.add(checkConnectionOp);
		
		PrepareRevisionDataOperation prepareDataOp = new PrepareRevisionDataOperation(cache);
		op.add(prepareDataOp, new IActionOperation[]{checkConnectionOp});
					
		FetchSkippedRevisionsOperation fetchSkippedOp = new FetchSkippedRevisionsOperation(resource, checkConnectionOp, cache);
		op.add(fetchSkippedOp, new IActionOperation[]{prepareDataOp});
		
		FetchNewRevisionsOperation fetchNewOp = new FetchNewRevisionsOperation(resource, checkConnectionOp, cache);
		op.add(fetchNewOp, new IActionOperation[]{fetchSkippedOp});	
		
		return op;
	}
	
	protected IActionOperation getRefreshOperation(IRepositoryResource resource, RepositoryCache cache) {
		CompositeOperation op = new CompositeOperation("Operation_RefreshCache"); //$NON-NLS-1$
		
		CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(resource);
		op.add(checkConnectionOp);						
					
		FetchSkippedRevisionsOperation fetchSkippedOp = new FetchSkippedRevisionsOperation(resource, checkConnectionOp, cache);
		op.add(fetchSkippedOp, new IActionOperation[]{checkConnectionOp});
		
		FetchNewRevisionsOperation fetchNewOp = new FetchNewRevisionsOperation(resource, checkConnectionOp, cache);
		op.add(fetchNewOp, new IActionOperation[]{fetchSkippedOp});	
		
		return op;
	}
	
	/** 
	 * Only if there are no more references to cache, dispose it
	 */
	public void disposeRepositoryCache(IRepositoryResource resource) {
		synchronized (this.calculateLock) {			
			this.resourcesForWhichCacheIsOpened.remove(resource);
			
			if (this.cacheReferencesCount > 0 && -- this.cacheReferencesCount == 0) {
				//clear cache
				this.repositoryCache = null;		
			}
		}		
	}
	
	public void export(File destination) {
		//TODO implement
	}
}
