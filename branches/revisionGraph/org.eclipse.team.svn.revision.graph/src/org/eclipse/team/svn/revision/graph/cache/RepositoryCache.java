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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;

/** 
 * @author Igor Burilo
 */
public class RepositoryCache {	
	
	public final static int UNKNOWN_INDEX = -1;
	
	protected final File cacheFile;
	
	protected final RepositoryCacheInfo cacheInfo;
					
	/*
	 * Index in array corresponds to revision number
	 * May contain null elements
	 */
	protected CacheRevision[] revisions = new CacheRevision[0];
	
	protected PathStorage pathStorage;
	
	protected StringStorage authors;
	
	protected MessageStorage messages;
	
	protected CopyToHelper copyToContainer = new CopyToHelper();
	
	//flag which indicates whether there are not saved revisions
	protected boolean isDirty;
	
	protected RepositoryCacheWriteHelper writeHelper;
	protected RepositoryCacheReadHelper readHelper;
	
	public RepositoryCache(File cacheFile, RepositoryCacheInfo cacheInfo) {
		this.cacheFile = cacheFile;		
		this.cacheInfo = cacheInfo;
	}
	
	public RepositoryCacheInfo getCacheInfo() {
		return this.cacheInfo;
	}
	
	public void expandRevisionsCount(long revisionsCount) {
		revisionsCount += 1;
		
		if (revisionsCount < this.revisions.length) {
			throw new IllegalArgumentException("Revisions: " + revisionsCount + ", size: " + this.revisions.length);
		}
		
		if (this.revisions.length != 0) {			
			CacheRevision[] tmp = this.revisions;			
			this.revisions = new CacheRevision[(int) revisionsCount];			
			System.arraycopy(tmp, 0, this.revisions, 0, tmp.length);
		} else {
			this.revisions = new CacheRevision[(int) revisionsCount];
		}
		
		this.messages.expandMessagesCount(revisionsCount);
	}
	
		
	//--- loaded from cache data
	
	public long getLastProcessedRevision() {
		return this.cacheInfo.getLastProcessedRevision();
	}
	
	public List<CacheRevision> getRevisionsWithoutNulls() {
		List<CacheRevision> revisionsList = new ArrayList<CacheRevision>();
		for (CacheRevision revision : this.revisions) {
			if (revision != null) {
				revisionsList.add(revision);
			}
		}
		return revisionsList;
	}
	
	public CacheRevision getRevision(long revision) {
		if (revision < this.revisions.length) {
			return this.revisions[(int)revision];	
		}
		return null;
	}
	
	public PathStorage getPathStorage() {
		return this.pathStorage;
	}
	
	public StringStorage getAuthorStorage() {
		return this.authors;
	}
	
	public MessageStorage getMessageStorage() {
		return this.messages;
	}
	
	public List<CacheChangedPath> getCopiedToData(int pathId) {		
		List<CacheChangedPath> res = this.copyToContainer.pathCopyToData.get(pathId);
		return res != null ? new ArrayList<CacheChangedPath>(res) : new ArrayList<CacheChangedPath>();
	}
	
	//--- Convert
	
	protected CacheRevision convert(SVNLogEntry entry) {
		//changed paths
		CacheChangedPath[] changedPaths;
		if (entry.changedPaths != null && entry.changedPaths.length > 0) {
			changedPaths = new CacheChangedPath[entry.changedPaths.length];
			for (int i = 0; i < entry.changedPaths.length; i ++) {				
				changedPaths[i] = this.convert(entry.changedPaths[i], entry.revision);
			}
		} else {
			changedPaths = new CacheChangedPath[0];
		}		
											
		int authorIndex = entry.author != null ? this.authors.add(entry.author) : RepositoryCache.UNKNOWN_INDEX;
		
		int messageIndex = this.messages.add(entry.message, entry.revision);
		
		CacheRevision revision = new CacheRevision(entry.revision, authorIndex, entry.date, messageIndex, changedPaths);		
		return revision;
	}
	
	protected CacheChangedPath convert(SVNLogPath logPath, long revision) {
		int pathIndex = this.pathStorage.add(logPath.path);
		int copiedFromPathIndex = this.pathStorage.add(logPath.copiedFromPath);
		CacheChangedPath changedPath = new CacheChangedPath(pathIndex, logPath.action, revision, copiedFromPathIndex, logPath.copiedFromRevision);		
		return changedPath;
	}			
	
	protected void initCopyToData() {
		this.copyToContainer.clear();
					
		for (CacheRevision revisionStructure : this.revisions) {
			if (revisionStructure == null) {
				continue;
			}
						
			for (CacheChangedPath cp : revisionStructure.getChangedPaths()) {
				if (cp.copiedFromPathIndex != RepositoryCache.UNKNOWN_INDEX) {
					this.copyToContainer.add(cp);
				}
			}							
		}	
	}	
	
	public void prepareModel() {		
		this.initCopyToData();
	}
	
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public void addEntry(SVNLogEntry entry) {
		CacheRevision revisionStructure = this.convert(entry);
		this.revisions[(int) revisionStructure.revision] = revisionStructure;
		
		this.isDirty = true;
	}
	
	public void save(IProgressMonitor monitor) throws IOException {
		if (this.writeHelper == null) {
			this.writeHelper = new RepositoryCacheWriteHelper(this);
		}
		
		TimeMeasure compressMeasure = new TimeMeasure("Compress messages");
		this.messages.compress();
		compressMeasure.end();
		
		TimeMeasure saveMeasure = new TimeMeasure("saveMeasure"); 
		this.writeHelper.save();
		saveMeasure.end();
		
		this.cacheInfo.save();
		
		this.isDirty = false;
	}

	public void load(IProgressMonitor monitor) throws IOException {
		long lastProcessedRevision = this.cacheInfo.getLastProcessedRevision();
		
		this.pathStorage = new PathStorage();
		this.authors = new StringStorage();				
		
		if (lastProcessedRevision == 0) {
			this.revisions = new CacheRevision[0];
			this.messages = new MessageStorage(0);
			return;
		}		
				
		this.revisions = new CacheRevision[(int) lastProcessedRevision + 1];
		this.messages = new MessageStorage((int) lastProcessedRevision + 1);
		
		if (this.readHelper == null) {
			this.readHelper = new RepositoryCacheReadHelper(this);	
		}		
		this.readHelper.load();				
		
		this.isDirty = false;
	}
}
