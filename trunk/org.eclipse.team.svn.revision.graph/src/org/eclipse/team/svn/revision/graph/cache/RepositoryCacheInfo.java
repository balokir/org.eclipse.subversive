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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.operation.FetchNewMergeInfoOperation;
import org.eclipse.team.svn.revision.graph.operation.FetchNewRevisionsOperation;
import org.eclipse.team.svn.revision.graph.operation.FetchSkippedMergeInfoOperation;
import org.eclipse.team.svn.revision.graph.operation.FetchSkippedRevisionsOperation;
import org.eclipse.team.svn.revision.graph.operation.PrepareRevisionDataOperation;
import org.eclipse.team.svn.revision.graph.operation.RepositoryConnectionInfo;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Contain cache meta data information
 * Provide operations to manipulate with cache, e.g. remove, export
 *  
 * @author Igor Burilo
 */
public class RepositoryCacheInfo {

	protected final static String START_SKIPPED_REVISION = "startSkippedRevision"; //$NON-NLS-1$
	protected final static String END_SKIPPED_REVISION = "endSkippedRevision"; //$NON-NLS-1$
	protected final static String LAST_PROCESSED_REVISION = "lastProcessedRevision"; //$NON-NLS-1$
	
	protected final static String MERGE_START_SKIPPED_REVISION = "mergeStartSkippedRevision"; //$NON-NLS-1$
	protected final static String MERGE_END_SKIPPED_REVISION = "mergeEndSkippedRevision"; //$NON-NLS-1$
	protected final static String MERGE_LAST_PROCESSED_REVISION = "mergeLastProcessedRevision"; //$NON-NLS-1$
	
	protected final static String CACHE_DATA_FILE_NAME = "dataFileName"; //$NON-NLS-1$
	
	protected long startSkippedRevision;
	protected long endSkippedRevision;
	protected long lastProcessedRevision; 
	protected String cacheDataFileName;
	
	protected long mergeStartSkippedRevision;
	protected long mergeEndSkippedRevision;	
	protected long mergeLastProcessedRevision; 
	
	protected final String repositoryName;
	protected final File metadataFile;
	
	protected boolean isDirty;
	
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
	
	public RepositoryCacheInfo(String repositoryName, File metadataFile) {
		this.repositoryName = repositoryName;
		this.metadataFile = metadataFile;		
		
		this.startSkippedRevision = 0;
		this.endSkippedRevision = 0;
		this.lastProcessedRevision = 0;
				
		this.mergeStartSkippedRevision = 0;
		this.mergeEndSkippedRevision = 0;
		this.mergeLastProcessedRevision = 0;
		
		this.cacheDataFileName = RepositoryCacheInfo.getCacheDataFileName(this.metadataFile.getName());
	}
	
	public static String getCacheDataFileName(String metadataFileName) {		
		int index = metadataFileName.lastIndexOf("."); //$NON-NLS-1$
		String cacheDataFileName;
		if (index != -1) {
			cacheDataFileName = metadataFileName.substring(0, index) + ".data"; //$NON-NLS-1$
		} else {
			cacheDataFileName = metadataFileName + ".data"; //$NON-NLS-1$
		}		
		return cacheDataFileName;
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
				
				this.mergeStartSkippedRevision = this.getLongProperty(props, RepositoryCacheInfo.MERGE_START_SKIPPED_REVISION);
				this.mergeEndSkippedRevision = this.getLongProperty(props, RepositoryCacheInfo.MERGE_END_SKIPPED_REVISION);
				this.mergeLastProcessedRevision = this.getLongProperty(props, RepositoryCacheInfo.MERGE_LAST_PROCESSED_REVISION);
				
				this.cacheDataFileName = this.getProperty(props, RepositoryCacheInfo.CACHE_DATA_FILE_NAME); 
			} finally {
				try { in.close(); } catch (IOException e) { /*ignore*/ }
			}			
		}
		
		this.isDirty = false;
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
		
		props.put(RepositoryCacheInfo.MERGE_START_SKIPPED_REVISION, String.valueOf(this.mergeStartSkippedRevision));
		props.put(RepositoryCacheInfo.MERGE_END_SKIPPED_REVISION, String.valueOf(this.mergeEndSkippedRevision));
		props.put(RepositoryCacheInfo.MERGE_LAST_PROCESSED_REVISION, String.valueOf(this.mergeLastProcessedRevision));
		
		props.put(RepositoryCacheInfo.CACHE_DATA_FILE_NAME, this.cacheDataFileName);
		
		FileOutputStream out = new FileOutputStream(this.metadataFile);		
		try {
			props.store(out, null);
		} finally {
			try { out.close(); } catch (IOException e) {/*ignore*/}
		}
		
		this.isDirty = false;
	}	
	
	//--- general revisions
	
	public long getLastProcessedRevision() {
		return this.lastProcessedRevision;
	}
	
	public void setLastProcessedRevision(long revision) {
		this.lastProcessedRevision = revision;
		
		this.isDirty = true;
	}

	public long getStartSkippedRevision() {
		return this.startSkippedRevision;
	}
	
	public long getEndSkippedRevision() {
		return this.endSkippedRevision;
	}	
	
	public void setSkippedRevisions(long start, long end) {
		this.startSkippedRevision = start;
		this.endSkippedRevision = end;	
		
		this.isDirty = true;
	}
	
	//--- merge revisions
	
	public long getMergeLastProcessedRevision() {
		return this.mergeLastProcessedRevision;
	}
	
	public void setMergeLastProcessedRevision(long revision) {
		this.mergeLastProcessedRevision = revision;
		
		this.isDirty = true;
	}
	
	public long getMergeStartSkippedRevision() {
		return this.mergeStartSkippedRevision;
	}

	public long getMergeEndSkippedRevision() {
		return this.mergeEndSkippedRevision;
	}
	
	public void setMergeSkippedRevisions(long start, long end) {
		this.mergeStartSkippedRevision = start;
		this.mergeEndSkippedRevision = end;	
		
		this.isDirty = true;
	}			
	
	public String getCacheDataFileName() {
		return this.cacheDataFileName;
	}
	
	public File getMetaDataFile() {
		return this.metadataFile;
	}
	
	public String getRepositoryName() {	
		return this.repositoryName;
	}
	
	/**
	 * @return true if there's not yet saved data
	 */
	public boolean isDirty() {
		return this.isDirty;
	}
	
	/** 
	 * @return Flag which indicates whether cache is calculating now
	 * 
	 * We need this flag in order not to show or pre-calculate any needed cache
	 * information if cache is calculating now, e.g. check connection, customize graph settings etc.
	 */
	public boolean isCacheDataCalculating() {
		synchronized (this.calculateLock) {
			return this.isCalculating;
		}
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
	public CacheResult createCacheData(IRepositoryResource resource,
		RepositoryConnectionInfo connectionData, IProgressMonitor monitor) {
		
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
				op = this.getCreateOperation(resource, previousCache, true, connectionData);
				cache = previousCache;
			} else {
				cache = new RepositoryCache(this.getCacheDataFile(), this);			
				op = this.getCreateOperation(resource, cache, false, connectionData);
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
	
	protected File getCacheDataFile() {
		File cacheDataFile = new File(this.metadataFile.getParentFile(), this.cacheDataFileName);
		return cacheDataFile;
	}
	
	public CacheResult refreshCacheData(IRepositoryResource resource, 
		RepositoryConnectionInfo connectionData, IProgressMonitor monitor) {
		
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
			IActionOperation op = this.getCreateOperation(resource, previousCache, true, connectionData);				
			
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
	
	protected IActionOperation getCreateOperation(IRepositoryResource resource, RepositoryCache cache, 
		boolean isRefresh, RepositoryConnectionInfo connectionData) {
		
		CompositeOperation op = new CompositeOperation(isRefresh ? "Operation_RefreshCache" : "Operation_CreateCache", SVNRevisionGraphMessages.class); //$NON-NLS-1$ //$NON-NLS-2$						
		
		PrepareRevisionDataOperation prepareDataOp = null;
		if (!isRefresh) {
			prepareDataOp = new PrepareRevisionDataOperation(cache);
			op.add(prepareDataOp);
		}							
		
		if (connectionData.hasConnection) {
			FetchSkippedRevisionsOperation fetchSkippedOp = new FetchSkippedRevisionsOperation(resource, cache);
			op.add(fetchSkippedOp, prepareDataOp != null ? new IActionOperation[]{prepareDataOp} : new IActionOperation[0]);
			
			FetchNewRevisionsOperation fetchNewOp = new FetchNewRevisionsOperation(resource, cache, connectionData.lastRepositoryRevision);
			op.add(fetchNewOp, new IActionOperation[]{fetchSkippedOp});
									
			if (connectionData.isSupportMergeInfo) {
				FetchSkippedMergeInfoOperation fetchSkippedMergeOp = new FetchSkippedMergeInfoOperation(resource, cache);
				op.add(fetchSkippedMergeOp, new IActionOperation[]{fetchNewOp});
				
				FetchNewMergeInfoOperation fetchNewMergeOp = new FetchNewMergeInfoOperation(resource, cache, connectionData.lastRepositoryRevision);
				op.add(fetchNewMergeOp, new IActionOperation[]{fetchSkippedMergeOp});		
			}
		}	
		
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
	
	/**	
	 * Export cache to destination folder
	 * 
	 * @return false if cache can't be exported because it's calculating at this moment
	 */
	public boolean export(File destination, IProgressMonitor monitor) throws Exception {
		synchronized (this.calculateLock) {
			if (this.isCalculating) {
				return false;
			}
			
			File cacheDataFile = this.getCacheDataFile();
			if (this.metadataFile.exists() && cacheDataFile.exists()) {
				FileUtility.copyFile(destination, cacheDataFile, monitor);				
				FileUtility.copyFile(destination, this.metadataFile, monitor);
			}	
			
			return true;
		}
	}

	/**	
	 * Remove cache from file system
	 * 
	 * @return false if cache can't be deleted because it's calculating at this moment
	 */
	public boolean remove() {
		synchronized (this.calculateLock) {
			if (this.isCalculating) {
				return false;
			}
			
			this.metadataFile.delete();
			
			File cacheDataFile = this.getCacheDataFile();
			cacheDataFile.delete();	
			
			return true;
		}			
	}
	
	public String toString() {
		return this.repositoryName;
	}
}
