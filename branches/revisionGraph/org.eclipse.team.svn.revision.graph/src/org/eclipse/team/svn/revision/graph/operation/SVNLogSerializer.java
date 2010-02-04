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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * 
 * @author Igor Burilo
 */
public class SVNLogSerializer {	
			
	protected final static String LOG_ENTRY_FILE_NAME = "logEntries.txt";	
	protected final static String PATH_FILE_NAME = "changedPaths.txt";
	protected final static String META_INFO_FILE_NAME = "meta-info.txt";
		
	protected File storageDir;	
	protected long logPathId;
	
	protected PrintWriter logEntryWriter;
	protected PrintWriter pathWriter;
	
	protected static class SVNLogEntryExt extends SVNLogEntry {

		public final Long[] children;
		
		public SVNLogEntryExt(long revision, long date, String author, String message, SVNLogPath[] changedPaths, boolean hasChildren, Long[] children) {
			super(revision, date, author, message, changedPaths, hasChildren);
			this.children = children;
		}		
	}
	
	public SVNLogSerializer(File storageDir) {		
		this.storageDir = storageDir;
		if (!this.storageDir.exists()) {
			this.storageDir.mkdirs();
		}
	}	
		
	public SVNLogEntry[] load(IProgressMonitor monitor) throws IOException {
		this.logPathId = 0;
		
		BufferedReader logEntryReader = null;
		BufferedReader pathReader = null;
		try {
			logEntryReader = new BufferedReader(new FileReader(new File(this.storageDir, SVNLogSerializer.LOG_ENTRY_FILE_NAME)));		
			pathReader = new BufferedReader(new FileReader(new File(this.storageDir, SVNLogSerializer.PATH_FILE_NAME)));
			
			Map<Long, SVNLogPath> changedPaths = this.loadChangedPaths(pathReader, monitor);			
			Map<Long, SVNLogEntryExt> logEntries = this.loadLogEntries(logEntryReader, changedPaths, monitor);
			
			Iterator<SVNLogEntryExt> iter = logEntries.values().iterator();
			while (iter.hasNext()) {
				SVNLogEntryExt entry = iter.next();
				Long[] children = entry.children;
				for (int i = 0; i < children.length; i ++) {					
					if (logEntries.containsKey(children[i])) {
						entry.add(logEntries.get(children[i]));
					}
				}				
			}						
			return logEntries.values().toArray(new SVNLogEntry[0]);			
		} finally {
			if (logEntryReader != null) {
				try { logEntryReader.close(); } catch (IOException ie) { /*ignore*/ }				
			}			
			if (pathReader != null) {
				try { pathReader.close(); } catch (IOException ie) { /*ignore*/ }				
			}
		}	
	}
	
	protected Map<Long, SVNLogEntryExt> loadLogEntries(BufferedReader reader, Map<Long, SVNLogPath> changedPaths, IProgressMonitor monitor) throws IOException {
//		log.revision + ";" + 
//		log.date + ";" + 
//		this.encode(log.author) + ";" + 
//		this.encode(log.message) + ";" + 
//		strChangedPaths + ";" + 
//		strChildren;
		
		Map<Long, SVNLogEntryExt> entries = new LinkedHashMap<Long, SVNLogEntryExt>();				
		String line = null;
		while ((line = reader.readLine()) != null) {			
			String[] parts = line.split(";");
			if (parts.length != 6) {
				continue;
			}
			long revision = Long.parseLong(parts[0]);
			long date = Long.parseLong(parts[1]);
			String author = this.decode(parts[2]);
			String message = this.decode(parts[3]);
			List<SVNLogPath> logPaths = new ArrayList<SVNLogPath>();
			List<Long> children = new ArrayList<Long>();
			
			String strChangedPaths = parts[4];						
			if (!"null".equals(strChangedPaths)) {
				String[] strParts = strChangedPaths.split(",");				
				if (strParts.length > 0) {
					for (int j = 0; j < strParts.length; j ++) {
						long pathId = Long.parseLong(strParts[j]);
						SVNLogPath logPath = changedPaths.get(pathId);
						if (logPath != null) {
							logPaths.add(logPath);
						}
					}					
				}
			}
			 
			String strChildren = parts[5];
			if (!"null".equals(strChildren)) {
				String[] strParts = strChildren.split(",");
				for (int j = 0; j < strParts.length; j ++) {
					long childId = Long.parseLong(strParts[j]);
					children.add(childId);
				}
			} 
			
			SVNLogEntryExt entry = new SVNLogEntryExt(revision, date, author, message, logPaths.toArray(new SVNLogPath[0]), !children.isEmpty() , children.toArray(new Long[0]));
			entries.put(revision, entry);
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
		}
		return entries;
	}		
	
	protected Map<Long, SVNLogPath> loadChangedPaths(BufferedReader pathReader, IProgressMonitor monitor) throws IOException {
		Map<Long, SVNLogPath> childPaths = new HashMap<Long, SVNLogPath>();		
		String line = null;
		while ((line = pathReader.readLine()) != null) {			
			String[] parts = line.split(";");
			if (parts.length != 5) {
				continue;
			}
			long id = Long.parseLong(parts[0]);
			String path = this.decode(parts[1]);
			char action = parts[2].charAt(0);
			String copiedFromPath = this.decode(parts[3]);			
			long copiedFromRevision = Long.parseLong(parts[4]);			
			SVNLogPath logPath = new SVNLogPath(path, action, copiedFromPath, copiedFromRevision);
			childPaths.put(id, logPath);	
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
		}
		return childPaths;
	}

	/*
	 * Entries must be sorted alphabetically
	 * 
	 * Don't support progress monitor here because this method
	 * can be called if caller task is canceled
	 */
	public void save(SVNLogEntry[] entries, boolean isAppend) throws IOException {
		CacheMetadata metadata = new CacheMetadata(this.storageDir);
		if (isAppend) {
			this.logPathId = metadata.getLogPathId();	
		} else {
			this.logPathId = 0;
		}		
		
		PrintWriter logEntryWriter = null;
		PrintWriter pathWriter = null;
		try {
			logEntryWriter = new PrintWriter(new FileWriter(new File(this.storageDir, SVNLogSerializer.LOG_ENTRY_FILE_NAME), isAppend));			
			pathWriter = new PrintWriter(new FileWriter(new File(this.storageDir, SVNLogSerializer.PATH_FILE_NAME), isAppend));
			
			for (int i = 0; i < entries.length; i ++) {
				SVNLogEntry entry = entries[i];
				this.saveEntry(entry, logEntryWriter, pathWriter);				 
			}	
			
			metadata.setLogPathId(this.logPathId);
			metadata.save();			
		} finally {
			if (logEntryWriter != null) {
				logEntryWriter.close();
			}
			if (pathWriter != null) {
				pathWriter.close();
			}
		}
	}				
	
	/*
	 * If you call this method, don't forget to call 'close' 
	 */
	public void save(SVNLogEntry entry, CacheMetadata metadata) throws IOException {
		if (this.logEntryWriter == null) {
			//enable auto flush
			this.logEntryWriter = new PrintWriter(new FileWriter(new File(this.storageDir, SVNLogSerializer.LOG_ENTRY_FILE_NAME), true), true);			
			this.pathWriter = new PrintWriter(new FileWriter(new File(this.storageDir, SVNLogSerializer.PATH_FILE_NAME), true), true);
			
			this.logPathId = metadata.getLogPathId();
		}
		
		this.saveEntry(entry, this.logEntryWriter, this.pathWriter);		
				
		metadata.setLogPathId(this.logPathId);		
	}
	
	public void close() {
		if (this.logEntryWriter != null) {
			this.logEntryWriter.close();
		}		
		if (this.pathWriter != null) {
			this.pathWriter.close();
		}
		
		this.logEntryWriter = null;
		this.pathWriter = null;
	}
	
	protected void saveEntry(SVNLogEntry log, PrintWriter logEntryWriter, PrintWriter pathWriter) {		
		//changed paths
		String strChangedPaths = "";
		if (log.changedPaths != null && log.changedPaths.length > 0) {
			for (int i = 0; i < log.changedPaths.length; i ++) {
				SVNLogPath logPath = log.changedPaths[i];
				long logPathId = this.encodeLogPath(logPath, pathWriter);				
				strChangedPaths += logPathId;
				if (i < log.changedPaths.length - 1) {
					strChangedPaths += ",";
				}
			}
		} 
		strChangedPaths = "".equals(strChangedPaths) ? "null" : strChangedPaths;
		
		//children
		String strChildren = "";			
		SVNLogEntry[] children = log.getChildren();
		if (children != null && children.length > 0) {
			for (int i = 0; i < children.length; i ++) {
				SVNLogEntry child = children[i];
				strChildren += child.revision;
				if (i < children.length - 1) {
					strChildren += ",";
				}								
			}		
		}		
		strChildren = "".equals(strChildren) ? "null" : strChildren;
		
		String str = 
			log.revision + ";" + 
			log.date + ";" + 
			this.encode(log.author) + ";" + 
			this.encode(log.message) + ";" + 
			strChangedPaths + ";" + 
			strChildren;
			
		logEntryWriter.println(str);				
	}
	
	protected long encodeLogPath(SVNLogPath log, PrintWriter pathWriter) {
		long logPathId = ++ this.logPathId;
		String str = 	
			logPathId + ";" +
			this.encode(log.path) + ";" + 
			log.action + ";" + 
			this.encode(log.copiedFromPath) + ";" +
			log.copiedFromRevision;
		
		pathWriter.println(str);
		
		return logPathId;
	}
	
	protected String encode(String str) {
		return str != null ? SVNUtility.base64Encode(str) : "null";
	}
	
	protected String decode(String str) {		
		return "null".equals(str) ? null : SVNUtility.base64Decode(str);
	}	
}
