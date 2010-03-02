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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Entry point to cache 
 * 
 * @author Igor Burilo
 */
public class RevisionDataContainer {	

	//TODO rename
	protected final static String REVISIONS_FILE = "revisions.newdata";
	protected final static String CHANGED_PATHS_FILE = "changedPaths.newdata";
	protected final static String REVISION_DATA_FILE = "revisionsData.newdata";
	protected final static String PATH_STRINGS_FILE = "pathStrings.newdata";
	protected final static String PATH_INDEXES_FILE = "pathIndexes.newdata";
	
	protected File cacheDir;
	
	protected IRepositoryResource resource;
	
	protected CacheMetadata metadata;
		
	//used only when saving
	protected long lastChangedPathId;
	
	//all paths are loaded in memory
	protected PathStorage pathStorage;
	
	//--- loaded from cache data
	
	/*
	 * Index in array corresponds to revision number
	 * May contain null elements
	 */
	protected RevisionStructure[] revisions;
	
	protected CopyToContainer copyToContainer = new CopyToContainer();	
	
	//--- streams
	
	//in
	protected BufferedReader revisionsInStream;
	protected BufferedReader changedPathsInStream;	
	protected RandomAccessFile revisionDataInStream;	
	protected BufferedReader pathStringsInStream;
	protected BufferedReader pathIndexesInStream;	
		
	//out
	protected PrintWriter revisionsOutStream;
	protected PrintWriter changedPathsOutStream;	
	protected RandomAccessFile revisionDataOutStream;
	protected PrintWriter pathStringsOutStream;
	protected PrintWriter pathIndexesOutStream;
	
	public RevisionDataContainer(File cacheDir, IRepositoryResource resource) {
		this.cacheDir = cacheDir;
		this.resource = resource;
	}
	
	public void prepareData(IProgressMonitor monitor) throws IOException {
		if (!this.cacheDir.exists()) {
			this.cacheDir.mkdirs();
		}		
		
		this.metadata = new CacheMetadata(this.cacheDir);
		this.metadata.load();
		
		this.pathStorage = new PathStorage();
		this.loadPaths();		
	}
	
	public CacheMetadata getCacheMetadata() {
		return this.metadata;
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
	
	protected RevisionStructure convert(SVNLogEntry entry) throws IOException {		
		RevisionStructure revision = new RevisionStructure(entry.revision);
		
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
		revision.setChangedPaths(changedPaths);
		
		//data
		RevisionDataStructure revisionData = new RevisionDataStructure(entry.date, entry.author, entry.message);
		revision.setRevisionData(revisionData);
				
		return revision;
	}
	
	protected ChangedPathStructure convert(SVNLogPath logPath, long revision) throws IOException {
		int id = (int) ++ this.lastChangedPathId;
		int pathIndex = this.pathStorage.add(logPath.path, this);
		int copiedFromPathIndex = this.pathStorage.add(logPath.copiedFromPath, this);
		ChangedPathStructure changedPath = new ChangedPathStructure(id, pathIndex, logPath.action, revision, copiedFromPathIndex, logPath.copiedFromRevision);		
		return changedPath;
	}
	
	
	//--- Input, Output	
	
	protected PrintWriter getRevisionsOutStream() {
		return this.revisionsOutStream;
	}

	protected BufferedReader getRevisionsInStream() {
		return this.revisionsInStream;
	}

	protected PrintWriter getChangedPathsOutStream() {
		return this.changedPathsOutStream;
	}

	protected BufferedReader getChangedPathsInStream() {
		return this.changedPathsInStream;
	}

	protected RandomAccessFile getRevisionDataOutStream() {
		return this.revisionDataOutStream;
	}
	
	protected RandomAccessFile getRevisionDataInStream() {		
		return this.revisionDataInStream;
	}
		
	public PrintWriter getPathStringsOutStream() {
		return this.pathStringsOutStream;
	}
	
	public BufferedReader getPathStringsInStream() {	
		return this.pathStringsInStream;
	}
	
	public PrintWriter getPathIndexesOutStream() {
		return this.pathIndexesOutStream;
	}
	
	public BufferedReader getPathIndexesInStream() {
		return pathIndexesInStream;
	}
	
	protected ChangedPathStructure[] readChangedPaths(IProgressMonitor monitor, IActionOperation calledFrom) throws IOException {		
		if (calledFrom != null) {
			ProgressMonitorUtility.setTaskInfo(monitor, calledFrom, "Load changed paths");	
		}	
		
		/*
		 * As there can be many changed paths, we provide progress here, e.g.
		 * for Apache repository it's about 7 million paths.
		 * 
		 * Now it takes about 24 sec to load data
		 */		
		int size = (int) this.metadata.getChangedPathsCount();
		ChangedPathStructure[] changedPaths = new ChangedPathStructure[size];
		for (int i = 0; i < size; i ++) {
			
			//show progress for each 100.000 path
			if (i % 100000 == 0 && i != 0) {
				if (calledFrom != null) {
					ProgressMonitorUtility.setTaskInfo(monitor, calledFrom, "Load changed paths: " + i + " from: " + size);	
				}
				if (monitor.isCanceled()) {
					throw new ActivityCancelledException();
				}
			}
			
			ChangedPathStructure changedPath = new ChangedPathStructure();
			boolean hasNext = changedPath.load(this);
			changedPaths[i] = changedPath;			
			if (!hasNext) {
				break;
			}						
		}
		return changedPaths;
	}
	
	protected RevisionStructure[] readRevisions(IProgressMonitor monitor, IActionOperation calledFrom) throws IOException {
		if (calledFrom != null) {
			ProgressMonitorUtility.setTaskInfo(monitor, calledFrom, "Load revisions");	
		}	
		
		int size = (int) this.metadata.getLastProcessedRevision() + 1;		
		RevisionStructure[] revisions = new RevisionStructure[size];
		int count = size - 1;
		for (int i = 0; i < count; i ++) {
			
			//show progress for each 100.000 revision
			if (i % 100000 == 0 && i != 0) {
				if (calledFrom != null) {
					ProgressMonitorUtility.setTaskInfo(monitor, calledFrom, "Load revisions: " + i + " from: " + count);	
				}
				if (monitor.isCanceled()) {
					throw new ActivityCancelledException();
				}
			}
			
			RevisionStructure revision = new RevisionStructure();
			boolean hasNext = revision.load(this);
			revisions[(int) revision.getRevision()] = revision;			
			if (!hasNext) {
				break;
			}
		}
		return revisions;
	}
	
	protected void initCopyToData() {
		for (RevisionStructure revisionStructure : this.revisions) {
			if (revisionStructure == null) {
				continue;
			}
			
			ChangedPathStructure[] revisionChangedPaths = revisionStructure.changedPaths;
			if (revisionChangedPaths.length > 0) {
				for (ChangedPathStructure cp : revisionChangedPaths) {
					if (cp.copiedFromPathIndex != PathStorage.UNKNOWN_INDEX) {
						this.copyToContainer.add(cp);
					}
				}				
			}	
		}	
	}
	
	protected void initRevisionsToChangedPathsConnections(ChangedPathStructure[] changedPaths) {
		for (RevisionStructure revision : this.revisions) {
			if (revision == null) {
				continue;
			}
						
			List<Integer> changedPathIds = revision.changedPathIds;
			if (changedPathIds != null && !changedPathIds.isEmpty()) {
				List<ChangedPathStructure> revisionPaths = new ArrayList<ChangedPathStructure>();
				for (int changedPathId : changedPathIds) {
					if (changedPathId < changedPaths.length) {
						ChangedPathStructure chPath = changedPaths[changedPathId];
						if (chPath != null) {
							revisionPaths.add(chPath);
						}
					}
				}
				revision.setChangedPaths(revisionPaths.toArray(new ChangedPathStructure[0]));
			}
		}
	}
	
	public void initForRead(IProgressMonitor monitor, IActionOperation calledFrom) throws IOException {
		if (this.metadata.getLastProcessedRevision() == 0) {
			this.revisions = new RevisionStructure[0];
			return;
		}
		
		this.revisions = new RevisionStructure[(int) this.metadata.getLastProcessedRevision() + 1];
		
		this.revisionsInStream = new BufferedReader(new FileReader(new File(this.cacheDir, RevisionDataContainer.REVISIONS_FILE)));
		this.changedPathsInStream = new BufferedReader(new FileReader(new File(this.cacheDir, RevisionDataContainer.CHANGED_PATHS_FILE)));
		this.revisionDataInStream = new RandomAccessFile(new File(this.cacheDir, RevisionDataContainer.REVISION_DATA_FILE), "r");
		
		try {		
			//read changed paths
			TimeMeasure changedPathsMeasure = new TimeMeasure("Load changed paths");	
			ChangedPathStructure[] changedPaths = this.readChangedPaths(monitor, calledFrom);
			changedPathsMeasure.end();
			
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
			
			//read revisions			
			TimeMeasure revisionsMeasure = new TimeMeasure("Load revisions");
			this.revisions = this.readRevisions(monitor, calledFrom);
			revisionsMeasure.end();
			
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
			
			//set connection between revisions and changed paths
			this.initRevisionsToChangedPathsConnections(changedPaths);		
			
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
			
			//init copy to
			TimeMeasure copyToMeasure = new TimeMeasure("Init copy to");
			this.initCopyToData();
			copyToMeasure.end();
			
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
		} finally {
			//close not used any more streams			
			try { this.revisionsInStream.close(); } catch (IOException e) { /*ignore*/ }
			this.revisionsInStream = null;
			
			try { this.changedPathsInStream.close(); } catch (IOException e) { /*ignore*/ }
			this.changedPathsInStream = null;
		}						
	}
	
	public void initForWrite() throws IOException {		
		//auto flush, append
		this.revisionsOutStream = new PrintWriter(new FileWriter(new File(this.cacheDir, RevisionDataContainer.REVISIONS_FILE), true), true);
		this.changedPathsOutStream = new PrintWriter(new FileWriter(new File(this.cacheDir, RevisionDataContainer.CHANGED_PATHS_FILE), true), true);
		
		this.pathStringsOutStream = new PrintWriter(new FileWriter(new File(this.cacheDir, RevisionDataContainer.PATH_STRINGS_FILE), true), true);
		this.pathIndexesOutStream = new PrintWriter(new FileWriter(new File(this.cacheDir, RevisionDataContainer.PATH_INDEXES_FILE), true), true);
		
		this.revisionDataOutStream = new RandomAccessFile(new File(this.cacheDir, RevisionDataContainer.REVISION_DATA_FILE), "rws");
		this.revisionDataOutStream.seek(this.revisionDataOutStream.length());				
		
		this.lastChangedPathId = this.metadata.getChangedPathsCount() - 1;
	}	
	
	protected void loadPaths() throws IOException {				
		File stringsFile = new File(this.cacheDir, RevisionDataContainer.PATH_STRINGS_FILE);
		File indexesFile = new File(this.cacheDir, RevisionDataContainer.PATH_INDEXES_FILE);
		
		if (!stringsFile.exists() || !indexesFile.exists()) {
			return;
		}
		
		this.pathStringsInStream = new BufferedReader(new FileReader(stringsFile));
		this.pathIndexesInStream = new BufferedReader(new FileReader(indexesFile));	
				
		try {
			this.pathStorage.load(this);
		} finally {
			try { this.pathStringsInStream.close(); } catch (IOException ie) {/*ignore*/}
			this.pathStringsInStream = null;
			
			try { this.pathIndexesInStream.close(); } catch (IOException ie) {/*ignore*/}
			this.pathIndexesInStream = null;
		}			
	}
	
	public void saveEntry(SVNLogEntry entry) throws IOException {
		this.convert(entry).save(this);
	}
	
	public void closeForRead() throws IOException {
		if (this.revisionDataInStream != null) {
			try { this.revisionDataInStream.close(); } catch (IOException e) { /*ignore*/ }
			this.revisionDataInStream = null;
		}	
		
		//clear not needed resources
		this.revisions = null;
		this.copyToContainer = null;
	}
	
	public void closeForWrite() {
		if (this.revisionsOutStream != null) {
			this.revisionsOutStream.close();
			this.revisionsOutStream = null;
		}		
		if (this.changedPathsOutStream != null) {
			this.changedPathsOutStream.close();
			this.changedPathsOutStream = null;
		}
		if (this.pathStringsOutStream != null) {
			this.pathStringsOutStream.close();
			this.pathStringsOutStream = null;
		}		
		if (this.pathIndexesOutStream != null) {
			this.pathIndexesOutStream.close();
			this.pathIndexesOutStream = null;
		}		
				
		if (this.revisionDataOutStream != null) {
			try { this.revisionDataOutStream.close(); } catch (IOException e) { /*ignore*/ }
			this.revisionDataOutStream = null;
		}						
	}
	
	public void loadRevisionData(RevisionStructure revision) throws IOException {
		if (revision.dataAddress != RevisionStructure.UNKNOWN_ADDRESS) {
			RevisionDataStructure revisionData = new RevisionDataStructure(revision.dataAddress);
			revisionData.load(this);
			revision.setRevisionData(revisionData);	
		}		
	}
	
	
	//--- utility
	
	/*
	 * As there's a limitation for string length in RandomAccessFile#writeUTF (65535),
	 * we write such big strings by using bytes.
	 */
	public static void writeBigString(String str, RandomAccessFile out) throws IOException {
		byte[] bytes = str.getBytes();
		out.writeInt(bytes.length);
		out.write(bytes);
	}
	
	public static String readBigString(RandomAccessFile in) throws IOException {
		int length = in.readInt();
		byte[] bytes = new byte[length];
		in.read(bytes);
		return new String(bytes);
	}
}
