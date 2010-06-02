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
import java.util.Collections;
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
				
	//merge info is added in '2' version
	protected int cacheVersion = 2;
	
	/*
	 * Index in array corresponds to revision number
	 * May contain null elements
	 */
	protected CacheRevision[] revisions = new CacheRevision[0];
	
	protected PathStorage pathStorage;
	
	protected StringStorage authors;
	
	protected MessageStorage messages;
	
	protected CopyToHelper copyToContainer = new CopyToHelper();
	
	protected MergeInfoStorage mergeInfo;
		
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
			throw new IllegalArgumentException("Revisions: " + revisionsCount + ", size: " + this.revisions.length); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	public MergeInfoStorage getMergeInfoStorage() {
		return this.mergeInfo;
	}
	
	public List<CacheChangedPathWithRevision> getCopiedToData(int pathId) {		
		List<CacheChangedPathWithRevision> res = this.copyToContainer.getCopyTo(pathId);
		return res != null ? new ArrayList<CacheChangedPathWithRevision>(res) : Collections.<CacheChangedPathWithRevision>emptyList();
	}
	
	public int getCacheVersion() {
		return this.cacheVersion;
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
		CacheChangedPath changedPath = new CacheChangedPath(pathIndex, logPath.action, copiedFromPathIndex, logPath.copiedFromRevision);		
		return changedPath;
	}			
	
	protected void initCopyToData() {
		this.copyToContainer.clear();
					
		for (CacheRevision cacheRevision : this.revisions) {
			if (cacheRevision == null) {
				continue;
			}
						
			for (CacheChangedPath cp : cacheRevision.getChangedPaths()) {
				if (cp.copiedFromPathIndex != RepositoryCache.UNKNOWN_INDEX) {
					this.copyToContainer.add(cp, cacheRevision.getRevision());
				}
			}							
		}	
	}	
	
	public void prepareModel() {		
		this.initCopyToData();
	}
	
	/**
	 * @return true if there's not yet saved data, e.g. revisions, merge info
	 */
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public void addEntry(SVNLogEntry entry) {
		CacheRevision revisionStructure = this.convert(entry);
		this.revisions[(int) revisionStructure.revision] = revisionStructure;
		
		this.isDirty = true;
	}
	
	public void addMergeInfo(SVNLogEntry entryWithMerges) {
		SVNLogEntry[] children = entryWithMerges.getChildren();
		if (children != null && children.length > 0) {
			long[] mergeRevs = new long[children.length];
			for (int i = 0; i < children.length; i ++) {
				mergeRevs[i] = children[i].revision;				
			}		
			this.mergeInfo.addMergeInfo(entryWithMerges.revision, mergeRevs);
			
			this.isDirty = true;	
		}				
	}
	
	public void save(IProgressMonitor monitor) throws IOException {
		if (this.writeHelper == null) {
			this.writeHelper = new RepositoryCacheWriteHelper(this);
		}
		
		TimeMeasure compressMeasure = new TimeMeasure("Compress messages"); //$NON-NLS-1$
		this.messages.compress();
		compressMeasure.end();
		
		TimeMeasure saveMeasure = new TimeMeasure("Save measure");  //$NON-NLS-1$
		this.writeHelper.save();
		saveMeasure.end();
		
		/*
		 * save also cache info as there's no sense to save cache data but
		 * not to save its meta data
		 */
		this.cacheInfo.save();
		
		this.isDirty = false;
	}

	public void load(IProgressMonitor monitor) throws IOException {
		this.pathStorage = new PathStorage();
		this.authors = new StringStorage();				
		this.mergeInfo = new MergeInfoStorage();
		
		long lastProcessedRevision = this.cacheInfo.getLastProcessedRevision();
		if (lastProcessedRevision == 0) {
			this.revisions = new CacheRevision[0];
			this.messages = new MessageStorage(0);
		} else {
			this.revisions = new CacheRevision[(int) lastProcessedRevision + 1];
			this.messages = new MessageStorage((int) lastProcessedRevision + 1);
			
			if (this.readHelper == null) {
				this.readHelper = new RepositoryCacheReadHelper(this);	
			}		
			this.readHelper.load();
		}		

		this.isDirty = false;
	}
}
