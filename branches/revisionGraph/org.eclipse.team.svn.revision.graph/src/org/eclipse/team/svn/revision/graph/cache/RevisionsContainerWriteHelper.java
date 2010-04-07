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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;

/** 
 * @author Igor Burilo
 */
public class RevisionsContainerWriteHelper {

	protected final RevisionDataContainer dataContainer;	
	
	public RevisionsContainerWriteHelper(RevisionDataContainer dataContainer) {
		this.dataContainer = dataContainer;		
	}
	
	public void save() throws IOException {		
		File cacheFile = new File(this.dataContainer.cacheDir, RevisionDataContainer.getCacheFileName(this.dataContainer.getRepositoryResource()));
		DataOutputStream bytesWriter = new DataOutputStream(new FileOutputStream(cacheFile));
		Deflater encoder = new Deflater();
		try {
			this.saveRevisions(bytesWriter, encoder);
			this.savePaths(bytesWriter, encoder);
		} finally {
			try { bytesWriter.close(); } catch (IOException ie) { /*ignore*/ }
			encoder.end();
		}		
	}

	protected void saveRevisions(DataOutput out, Deflater encoder) throws IOException {
		//TODO handle if there's no data
		List<RevisionStructure> revisionsWithoutNulls = this.dataContainer.getRevisionsWithoutNulls();		
		int revisionsCountWithNulls = this.dataContainer.revisions.length;
		
		final int revisionsInBlock = 1000;
		
		//blocks count
		int blocksCount = revisionsWithoutNulls.size() / revisionsInBlock;
		if (revisionsWithoutNulls.size() % revisionsInBlock != 0) {
			blocksCount ++;
		}
				
		/*
		 * Write:
		 * 
		 * revisions count with nulls
		 * revisions count without nulls
		 * revisions count in block
		 * blocks count
		 */		
		out.writeInt(revisionsCountWithNulls);				
		out.writeInt(revisionsWithoutNulls.size());
		out.writeInt(revisionsInBlock);		
		out.writeInt(blocksCount);				
		
		this.convertRevisionsToBytes(out, encoder, revisionsWithoutNulls, blocksCount, revisionsInBlock);	
	}
	
	/*
	 * Save revisions on equal blocks in order not to keep in memory large amount of data
	 */
	protected void convertRevisionsToBytes(DataOutput out, Deflater encoder,
		List<RevisionStructure> revisions, int blocksCount, int revisionsInBlock) throws IOException {
		
		/*
		 * For each block write:
		 * 	encoded block bytes
		 */
		
		//temporary storage for raw block bytes
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		DataOutput revisionsBytes = new DataOutputStream(byteArray);
		
		int revisionsCounter = 0;				
		int revisionsInLastBlock = RevisionsContainerWriteHelper.getRevisionsCountInLastBlock(revisions.size(), revisionsInBlock);		
		for (int i = 0; i < blocksCount; i ++) {			
			int revisionsCount = revisionsInBlock;
			if (i == (blocksCount - 1)) {
				revisionsCount = revisionsInLastBlock;
			}
			
			for (int j = 0; j < revisionsCount; j ++) {							
				RevisionStructure revision = revisions.get(revisionsCounter ++);				
				byte[] revisionBytes = revision.toBytes();
				BytesUtility.writeBytesWithLength(revisionsBytes, revisionBytes);		
			}					
			
			BytesUtility.compressAndWrite(byteArray.toByteArray(), out, encoder);
			
			byteArray.reset();			
		}		
	}
	
	public static int getRevisionsCountInLastBlock(int revisionsCount, int revisionsInBlock) {
		int tmp = revisionsCount % revisionsInBlock;
		int revisionsInLastBlock = tmp == 0 ? revisionsInBlock : tmp;
		return revisionsInLastBlock;
	}
	
	protected void savePaths(DataOutput out, Deflater encoder) throws IOException {
		this.dataContainer.pathStorage.save(out, encoder);
	}
}
