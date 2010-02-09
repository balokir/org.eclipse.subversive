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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Igor Burilo
 */
public class CacheMetadata {

	protected final static String START_SKIPPED_REVISION = "startSkippedRevision";
	protected final static String END_SKIPPED_REVISION = "endSkippedRevision";
	protected final static String LAST_PROCESSED_REVISION = "lastProcessedRevision";
	
	protected File metadataFile;
	
	protected long startSkippedRevision;
	protected long endSkippedRevision;
	protected long lastProcessedRevision;	
			
	public CacheMetadata(File cacheFolder) {		
		this.metadataFile = new File(cacheFolder, "metadata.txt");
		if (!cacheFolder.exists()) {
			cacheFolder.mkdirs();
		}
	}
	
	public void load() throws IOException {
		if (this.metadataFile.exists()) {
			Properties props = new Properties();
			FileInputStream in = new FileInputStream(this.metadataFile);
			try {
				props.load(in);
				this.startSkippedRevision = this.getLongProperty(props, CacheMetadata.START_SKIPPED_REVISION);
				this.endSkippedRevision = this.getLongProperty(props, CacheMetadata.END_SKIPPED_REVISION);
				this.lastProcessedRevision = this.getLongProperty(props, CacheMetadata.LAST_PROCESSED_REVISION);				
			} finally {
				try { in.close(); } catch (IOException e) { /*ignore*/ }
			}			
		} else {
			this.startSkippedRevision = 0;
			this.endSkippedRevision = 0;
			this.lastProcessedRevision = 0;
		} 
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
	
	/*
	 * If you call this method, don't forget to call 'close' 
	 */
	public void save() throws IOException {
		Properties props = new Properties();
		props.put(CacheMetadata.START_SKIPPED_REVISION, String.valueOf(this.startSkippedRevision));
		props.put(CacheMetadata.END_SKIPPED_REVISION, String.valueOf(this.endSkippedRevision));
		props.put(CacheMetadata.LAST_PROCESSED_REVISION, String.valueOf(this.lastProcessedRevision));	
		
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
}
