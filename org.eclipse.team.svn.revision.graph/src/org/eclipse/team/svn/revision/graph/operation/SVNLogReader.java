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
package org.eclipse.team.svn.revision.graph.operation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;

/**
 * Load log entries from cache  
 * 
 * 	we don't need logPathId if we use index files 
 *  
 * @author Igor Burilo
 */
public class SVNLogReader {

	protected File storageDir;
	protected long lastProcessedRevision;		
	
	protected RandomAccessFile logEntryIn;
	protected RandomAccessFile pathIn;
	
	protected IndexData[] index;
	
	protected static class IndexData {		
		public IndexData(long revision) {
			this.revision = revision;
		}
		long revision;
		long logEntryPointer;
		
		long pathPointer;
		int pathLinesCount;						
	}
	
	public SVNLogReader(File storageDir, long lastProcessedRevision) throws IOException {
		this.storageDir = storageDir;
		this.lastProcessedRevision = lastProcessedRevision;
		
		this.init();
	}
	
	protected final void init() throws IOException {		
		this.index = new IndexData[(int) this.lastProcessedRevision + 1];
		
		//read index
		BufferedReader indexReader = new BufferedReader(new FileReader(new File(this.storageDir, SVNLogWriter.INDEX_FILE_NAME))); 
		try {
			String line = null;
			while ((line = indexReader.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length >= 2) {
					long revision = Long.parseLong(parts[0]);
					long logEntryPointer = Long.parseLong(parts[1]);
					
					IndexData indexData = new IndexData(revision);
					indexData.logEntryPointer = logEntryPointer;					
					this.index[(int)revision] = indexData;
					
					if (parts.length == 4) {
						indexData.pathPointer = Long.parseLong(parts[2]);
						indexData.pathLinesCount = Integer.parseInt(parts[3]);												
					}
				}
			}
		} finally {			
			try { indexReader.close(); } catch (IOException e) { /*ignore*/ }			
		}
		
		this.logEntryIn = new RandomAccessFile(new File(this.storageDir, SVNLogWriter.LOG_ENTRY_FILE_NAME), "r");
		this.pathIn = new RandomAccessFile(new File(this.storageDir, SVNLogWriter.PATH_FILE_NAME), "r");
	}
	
	public long getLastProcessedRevision() {
		return this.lastProcessedRevision;
	}
	
	public SVNLogPath[] loadLogPaths(long revision) throws IOException {
		List<SVNLogPath> logPaths = new ArrayList<SVNLogPath>();
		if (this.index.length > revision && this.index[(int)revision] != null) {
			IndexData indexData = this.index[(int)revision];
			long pointer = indexData.pathPointer;
			int linesCount = indexData.pathLinesCount;
			
			this.pathIn.seek(pointer);
			for (int i = 0; i < linesCount; i ++) {
				SVNLogPath logPath = this.loadLogPath();
				logPaths.add(logPath);
			}
		}			
		return logPaths.toArray(new SVNLogPath[0]);
	}
	
	protected SVNLogPath loadLogPath() throws IOException {
		String line = this.pathIn.readUTF();
		int index = line.indexOf(" "); 
		int nextIndex = line.indexOf(" ", index + 1);
		
		boolean hasCopyData = Boolean.valueOf(line.substring(0, index));
		char action = line.substring(index + 1, nextIndex).charAt(0);												
		String path = line.substring(nextIndex + 1);
		
		String copyPath = null;
		long copyRevision = -1;
		if (hasCopyData) {
			String ln = this.pathIn.readUTF();
			index = ln.indexOf(" ");
			copyRevision = Long.parseLong(ln.substring(0, index));
			copyPath = ln.substring(index + 1);			
		}		
		
		SVNLogPath logPath = new SVNLogPath(path, action, copyPath, copyRevision);
		return logPath;
	}
	
	/*
	 * loads log entry without changed paths, merge info
	 */
	public SVNLogEntry loadRawLogEntry(long revision) throws IOException {
		SVNLogEntry logEntry = null;
		if (this.index.length > revision && this.index[(int)revision] != null) {
			IndexData indexData = this.index[(int)revision];
			long pointer = indexData.logEntryPointer;					
			
			this.logEntryIn.seek(pointer);
			String line = SVNLogWriter.readBigString(this.logEntryIn);
			logEntry = this.parseLogEntry(line);						
		}		
		return logEntry;
	}
	
	protected SVNLogEntry parseLogEntry(String line) {
		String[] parts = line.split(";");
		if (parts.length == 5) {
			long revision = Long.parseLong(parts[0]);
			long date = Long.parseLong(parts[1]);
			String author = SVNLogWriter.decode(parts[2]);
			String message = SVNLogWriter.decode(parts[3]);			
						
			SVNLogEntry entry = new SVNLogEntry(revision, date, author, message, new SVNLogPath[0], false);
			return entry;
		}				
		return null;
	}
	
	public void close() {
		if (this.logEntryIn != null) {
			try { this.logEntryIn.close(); } catch (IOException e) { /*ignore*/ }
		}
		if (this.pathIn != null) {
			try { this.pathIn.close(); } catch (IOException e) { /*ignore*/ }
		}				
	}

}
