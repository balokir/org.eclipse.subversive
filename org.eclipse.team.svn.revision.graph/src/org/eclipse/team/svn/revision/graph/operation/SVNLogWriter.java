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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Save log entries in cache
 * 
 * TODO
 * 	improve saving log entries (after which remove not needed methods, e.g. encode, writeBigString)
 * 
 * @author Igor Burilo
 */
public class SVNLogWriter {

	public final static String LOG_ENTRY_FILE_NAME = "logEntries.data";	
	public final static String PATH_FILE_NAME = "changedPaths.data";	
	public final static String INDEX_FILE_NAME = "index.data";
	
	protected File storageDir;	
		
	protected RandomAccessFile logEntryOut;
	protected RandomAccessFile pathOut;
	
	protected PrintWriter indexWriter;
	
	public SVNLogWriter(File storageDir) throws IOException {		
		this.storageDir = storageDir;
		if (!this.storageDir.exists()) {
			this.storageDir.mkdirs();
		}		
		
		//enable auto flush		
		this.logEntryOut = new RandomAccessFile(new File(this.storageDir, SVNLogWriter.LOG_ENTRY_FILE_NAME), "rws");
		this.logEntryOut.seek(this.logEntryOut.length());
				
		this.pathOut = new RandomAccessFile(new File(this.storageDir, SVNLogWriter.PATH_FILE_NAME), "rws");
		this.pathOut.seek(this.pathOut.length());
		
		this.indexWriter = new PrintWriter(new FileWriter(new File(this.storageDir, SVNLogWriter.INDEX_FILE_NAME), true), true);
	}
	
	public void save(SVNLogEntry entry) throws IOException {
		String indexStr = entry.revision + " " + this.logEntryOut.getFilePointer();		
		this.saveEntry(entry);		
						
		//changed paths
		if (entry.changedPaths != null && entry.changedPaths.length > 0) {
			long pathPointer = this.pathOut.getFilePointer();
			
			for (int i = 0; i < entry.changedPaths.length; i ++) {
				SVNLogPath logPath = entry.changedPaths[i];
				this.saveLogPath(logPath);
			}
			
			indexStr += " " + pathPointer + " " + entry.changedPaths.length;
		}							
		
		//save index: revision logEntryPointer [pathPointer pathLinesCount]
		this.indexWriter.println(indexStr);
	}
	
	protected void saveEntry(SVNLogEntry log) throws IOException {		
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
			SVNLogWriter.encode(log.author) + ";" + 
			SVNLogWriter.encode(log.message) + ";" + 			
			strChildren;
		
		SVNLogWriter.writeBigString(str, this.logEntryOut);
	}
	
	protected void saveLogPath(SVNLogPath log) throws IOException {				
		boolean hasCopyData = log.copiedFromPath != null && log.copiedFromPath.length() > 0;		
		this.pathOut.writeUTF(hasCopyData + " " + log.action + " " + log.path);
		if (hasCopyData) {
			this.pathOut.writeUTF(log.copiedFromRevision + " " + log.copiedFromPath);
		}
	}		
	
	public void close() {			
		if (this.logEntryOut != null) {
			try { this.logEntryOut.close(); } catch (IOException e) { /*ignore*/ }
		}
		if (this.pathOut != null) {
			try { this.pathOut.close(); } catch (IOException e) { /*ignore*/ }
		}
		
		if (this.indexWriter != null) {
			this.indexWriter.close();
		}		
	}
	
	public static String encode(String str) {
		return str != null ? SVNUtility.base64Encode(str) : "null";
	}
		
	public static String decode(String str) {		
		return "null".equals(str) ? null : SVNUtility.base64Decode(str);
	}
	
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
