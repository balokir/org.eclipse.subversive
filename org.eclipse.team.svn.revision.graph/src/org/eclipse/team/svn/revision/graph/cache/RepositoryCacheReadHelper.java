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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Inflater;

/**
 * @author Igor Burilo
 */
public class RepositoryCacheReadHelper {

	protected final RepositoryCache repositoryCache;	
	
	public RepositoryCacheReadHelper(RepositoryCache repositoryCache) {
		this.repositoryCache = repositoryCache;		
	}
	
	public void load() throws IOException {
		File cacheFile = this.repositoryCache.cacheFile;
		if (!cacheFile.exists()) {
			return;
		}			
		DataInputStream bytesReader = new DataInputStream(new FileInputStream(cacheFile));
		Inflater decoder = new Inflater();
		try {
			this.repositoryCache.cacheVersion = bytesReader.readInt();
			
			this.loadRevisions(bytesReader, decoder);
			this.loadPaths(bytesReader, decoder);
			this.loadAuthors(bytesReader, decoder);
			this.loadMessages(bytesReader, decoder);
			//merge info is added in '2' version
			if (this.repositoryCache.getCacheVersion() >= 2) {
				this.loadMergeInfo(bytesReader, decoder);
			}
		}
		catch (EOFException e) {
			// do nothing, it's just the end of stream...
		}
		finally {
			try { bytesReader.close(); } catch (IOException ie) { /*ignore*/ }
			decoder.end();
		}		
	}
	
	protected void loadRevisions(DataInput in, Inflater decoder) throws IOException {
		int revisionsCountWithNulls = in.readInt();				
		int revisionsCountWithoutNulls = in.readInt();
		int revisionsInBlock = in.readInt();
		int blocksCount = in.readInt();
		
		this.repositoryCache.revisions = new CacheRevision[revisionsCountWithNulls];
		
		//process blocks		 
		for (int i = 0; i < blocksCount; i ++) {
			byte[] blockBytes = BytesUtility.decompressAndRead(in, decoder);
			
			int revisionsCount = revisionsInBlock;
			//handle last block
			if (i == blocksCount - 1) {	
				revisionsCount = RepositoryCacheWriteHelper.getRevisionsCountInLastBlock(revisionsCountWithoutNulls, revisionsInBlock);
			}
			
			this.readRevisionsFromBlock(revisionsCount, blockBytes);			
		}	
	}
	
	protected void readRevisionsFromBlock(int revisionsCount, byte[] blockBytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(blockBytes));
			for (int i = 0; i < revisionsCount; i ++) {												
				byte[] revisionBytes = BytesUtility.readBytesWithLength(bytesIn);			
				CacheRevision revisionStructure = new CacheRevision(revisionBytes);	
				this.repositoryCache.revisions[(int) revisionStructure.revision] = revisionStructure;
			}				
		} catch (IOException ie) {
			//ignore
		}		
	}
	
	protected void loadPaths(DataInput in, Inflater decoder) throws IOException {
		this.repositoryCache.pathStorage.load(in, decoder);		
	}
	
	protected void loadAuthors(DataInput in, Inflater decoder) throws IOException {
		byte[] bytes = BytesUtility.decompressAndRead(in, decoder);
		this.repositoryCache.authors = new StringStorage(bytes);		
	}
	
	protected void loadMessages(DataInput in, Inflater decoder) throws IOException {
		byte[] bytes = BytesUtility.decompressAndRead(in, decoder);
		this.repositoryCache.messages = new MessageStorage(bytes);
	}
	
	protected void loadMergeInfo(DataInput in, Inflater decoder) throws IOException {
		byte[] bytes = BytesUtility.decompressAndRead(in, decoder);
		this.repositoryCache.mergeInfo = new MergeInfoStorage(bytes);
	}
}
