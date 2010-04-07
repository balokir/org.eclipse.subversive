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
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Entry point to cache 
 * 
 * @author Igor Burilo
 */
public class RevisionDataContainer {	
	
	protected final File cacheDir;
	
	protected final IRepositoryResource resource;
	
	protected CacheMetadata metadata;
					
	/*
	 * Index in array corresponds to revision number
	 * May contain null elements
	 */
	protected RevisionStructure[] revisions = new RevisionStructure[0];
	
	protected PathStorage pathStorage;
	
	protected CopyToContainer copyToContainer = new CopyToContainer();
	
	//flag which indicates whether there are not saved revisions
	protected boolean isDirty;
	
	protected RevisionsContainerWriteHelper writeHelper;
	protected RevisionsContainerReadHelper readHelper;
	
	public RevisionDataContainer(File cacheDir, IRepositoryResource resource) {
		this.cacheDir = cacheDir;
		this.resource = resource;
		
		if (!this.cacheDir.exists()) {
			this.cacheDir.mkdirs();
		}
	}
	
	public CacheMetadata getCacheMetadata() {
		return this.metadata;
	}
	
	public void expandRevisionsCount(long revisionsCount) {
		revisionsCount += 1;
		
		if (revisionsCount < this.revisions.length) {
			throw new IllegalArgumentException("Revisions: " + revisionsCount + ", size: " + this.revisions.length);
		}
		
		if (this.revisions.length != 0) {			
			RevisionStructure[] tmp = this.revisions;			
			this.revisions = new RevisionStructure[(int) revisionsCount];			
			System.arraycopy(tmp, 0, this.revisions, 0, tmp.length);
		} else {
			this.revisions = new RevisionStructure[(int) revisionsCount];
		}
	}
	
	/** 
	 * @param pathIndex
	 * @return	full path which contains repository root 
	 */
	public String getRevisionFullPath(int pathIndex) {
		String url = this.resource.getRepositoryLocation().getRepositoryRootUrl();
		url += this.pathStorage.getPath(pathIndex);
		return url;
	}
	
	/** 
	 * @return resource for which revision graph is launched
	 */
	public IRepositoryResource getRepositoryResource() {
		return this.resource;
	}
	
		
	//--- loaded from cache data
	
	public long getLastProcessedRevision() {
		return this.metadata.getLastProcessedRevision();
	}
	
	public List<RevisionStructure> getRevisionsWithoutNulls() {
		List<RevisionStructure> revisionsList = new ArrayList<RevisionStructure>();
		for (RevisionStructure revision : this.revisions) {
			if (revision != null) {
				revisionsList.add(revision);
			}
		}
		return revisionsList;
	}
	
	public RevisionStructure getRevision(long revision) {
		if (revision < this.revisions.length) {
			return this.revisions[(int)revision];	
		}
		return null;
	}
	
	public PathStorage getPathStorage() {
		return this.pathStorage;
	}
	
	public List<ChangedPathStructure> getCopiedToData(int pathId) {		
		List<ChangedPathStructure> res = this.copyToContainer.pathCopyToData.get(pathId);
		return res != null ? new ArrayList<ChangedPathStructure>(res) : new ArrayList<ChangedPathStructure>();
	}
	
	//--- Convert
	
	protected RevisionStructure convert(SVNLogEntry entry) {
		//changed paths
		ChangedPathStructure[] changedPaths;
		if (entry.changedPaths != null && entry.changedPaths.length > 0) {
			changedPaths = new ChangedPathStructure[entry.changedPaths.length];
			for (int i = 0; i < entry.changedPaths.length; i ++) {				
				changedPaths[i] = this.convert(entry.changedPaths[i], entry.revision);
			}
		} else {
			changedPaths = new ChangedPathStructure[0];
		}		
				
		//data
		RevisionDataStructure revisionData = new RevisionDataStructure(entry.date, entry.author, entry.message);
			
		RevisionStructure revision = new RevisionStructure(entry.revision, changedPaths, revisionData);		
		return revision;
	}
	
	protected ChangedPathStructure convert(SVNLogPath logPath, long revision) {
		int pathIndex = this.pathStorage.add(logPath.path);
		int copiedFromPathIndex = this.pathStorage.add(logPath.copiedFromPath);
		ChangedPathStructure changedPath = new ChangedPathStructure(pathIndex, logPath.action, revision, copiedFromPathIndex, logPath.copiedFromRevision);		
		return changedPath;
	}			
	
	protected void initCopyToData() {
		this.copyToContainer.clear();
					
		for (RevisionStructure revisionStructure : this.revisions) {
			if (revisionStructure == null) {
				continue;
			}
						
			for (ChangedPathStructure cp : revisionStructure.getChangedPaths()) {
				if (cp.copiedFromPathIndex != PathStorage.UNKNOWN_INDEX) {
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
		RevisionStructure revisionStructure = this.convert(entry);
		this.revisions[(int) revisionStructure.revision] = revisionStructure;
		
		this.isDirty = true;
	}
	
	public void save(IProgressMonitor monitor) throws IOException {
		if (this.writeHelper == null) {
			this.writeHelper = new RevisionsContainerWriteHelper(this);
		}
		TimeMeasure saveMeasure = new TimeMeasure("saveMeasure"); 
		this.writeHelper.save();
		saveMeasure.end();
		
		this.metadata.save();
		
		this.isDirty = false;
	}

	public void load(IProgressMonitor monitor) throws IOException {
		this.metadata = new CacheMetadata(new File(this.cacheDir, RevisionDataContainer.getCacheMetaDataFileName(this.resource)));
		this.metadata.load();
		
		this.pathStorage = new PathStorage();
		
		if (this.metadata.getLastProcessedRevision() == 0) {
			this.revisions = new RevisionStructure[0];
			return;
		}		
		this.revisions = new RevisionStructure[(int) this.metadata.getLastProcessedRevision() + 1];
				
		if (this.readHelper == null) {
			this.readHelper = new RevisionsContainerReadHelper(this);	
		}		
		this.readHelper.load();				
		
		this.isDirty = false;
	}

	public static String getCacheFileName(IRepositoryResource resource) {		
		return RevisionDataContainer.getCacheName(resource) + ".data";
	}
	
	public static String getCacheMetaDataFileName(IRepositoryResource resource) {
		return RevisionDataContainer.getCacheName(resource) + ".meta";
	}
	
	protected static String getCacheName(IRepositoryResource resource) {
		//TODO add correct implementation		
		//ignore protocol		
		String root = resource.getRepositoryLocation().getRepositoryRootUrl();		
		root = root.replaceAll("[\\/:*?\"<>|]", "_");						
		return root;
	}
}
