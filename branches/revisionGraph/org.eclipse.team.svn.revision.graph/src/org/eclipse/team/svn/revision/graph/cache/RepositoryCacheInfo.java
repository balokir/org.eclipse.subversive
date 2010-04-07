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
import java.util.Properties;

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

	protected final static String START_SKIPPED_REVISION = "startSkippedRevision";
	protected final static String END_SKIPPED_REVISION = "endSkippedRevision";
	protected final static String LAST_PROCESSED_REVISION = "lastProcessedRevision";
	protected final static String CACHE_DATA_FILE_NAME = "dataFileName";
	
	protected long startSkippedRevision;
	protected long endSkippedRevision;
	protected long lastProcessedRevision; 
	protected String cacheDataFileName;
	
	protected final File metadataFile;	
	
	public RepositoryCacheInfo(File metadataFile) {		
		this.metadataFile = metadataFile;		
	}
	
	public void init() {
		this.startSkippedRevision = 0;
		this.endSkippedRevision = 0;
		this.lastProcessedRevision = 0;
		
		String metaName = this.metadataFile.getName();
		int index = metaName.lastIndexOf(".");
		if (index != -1) {
			this.cacheDataFileName = metaName.substring(0, index) + ".data";
		} else {
			this.cacheDataFileName = metaName + ".data";
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
	 * Calculate cache data
	 * 
	 * If there's error during cache creating, then return null
	 */
	public RepositoryCache createCacheData(IRepositoryResource resource, IProgressMonitor monitor) {		
		File cacheDataFile = new File(this.metadataFile.getParentFile(), this.cacheDataFileName);		
		RepositoryCache repositoryCache = new RepositoryCache(cacheDataFile, this);
		
		final CompositeOperation op = new CompositeOperation("Create Cache Data Operation");
		
		CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(resource);
		op.add(checkConnectionOp);
		
		PrepareRevisionDataOperation prepareDataOp = new PrepareRevisionDataOperation(repositoryCache);
		op.add(prepareDataOp, new IActionOperation[]{checkConnectionOp});
					
		FetchSkippedRevisionsOperation fetchSkippedOp = new FetchSkippedRevisionsOperation(resource, checkConnectionOp, repositoryCache);
		op.add(fetchSkippedOp, new IActionOperation[]{prepareDataOp});
		
		FetchNewRevisionsOperation fetchNewOp = new FetchNewRevisionsOperation(resource, checkConnectionOp, repositoryCache);
		op.add(fetchNewOp, new IActionOperation[]{fetchSkippedOp});	
		
		//call synchronously				
		ProgressMonitorUtility.doTask(UIMonitorUtility.DEFAULT_FACTORY.getLogged(op), monitor, 1, 1);
		
		return op.getExecutionState() == IActionOperation.OK ? repositoryCache : null;
	}
	
	public void export(File destination) {
		//TODO
	}
}
