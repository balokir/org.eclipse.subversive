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
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * @author Igor Burilo
 */
public class CacheRevision {
	
	protected long revision;

	//TODO don't separate it to another class
	protected CacheRevisionData revisionData;
	
	protected CacheChangedPath[] changedPaths = new CacheChangedPath[0];
	
	public CacheRevision(long revision, CacheChangedPath[] changedPaths, CacheRevisionData revisionData) {
		this.revision = revision;
		this.changedPaths = changedPaths;
		this.revisionData = revisionData;
	}
	
	public CacheRevision(byte[] bytes) {
		this.fromBytes(bytes);
	}
	
	public boolean hasChangedPaths() {
		return this.changedPaths.length > 0;
	}
	
	public CacheChangedPath[] getChangedPaths() {
		return this.changedPaths;
	} 
	
	public long getRevision() {
		return this.revision;
	}
	
	public String getAuthor() {
		return this.revisionData != null ? this.revisionData.getAuthor() : null;		
	} 
	
	public long getDate() {
		return this.revisionData != null ? this.revisionData.getDate() : 0;
	}
	
	public String getMessage() {
		return this.revisionData != null ? this.revisionData.getMessage() : null;
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			this.revision = bytesIn.readLong();
							
			//changed paths
			int changedPathsCount = bytesIn.readInt();
			this.changedPaths = new CacheChangedPath[changedPathsCount];
			for (int i = 0; i < changedPathsCount; i ++) {
				byte[] pathBytes = BytesUtility.readBytesWithLength(bytesIn);
				this.changedPaths[i] = new CacheChangedPath(pathBytes);			
			}
			
			//revision data		
			int revisionDataLength = bytesIn.readInt();		
			if (revisionDataLength > 0) {
				byte[] revisionDataBytes = new byte[revisionDataLength];
				bytesIn.readFully(revisionDataBytes);
				this.revisionData = new CacheRevisionData(revisionDataBytes);
			}	
		} catch (IOException e) {
			//ignore
		}				
	}
	
	public byte[] toBytes() {
		/*
		 * Write:
		 * 
		 * revision
		 * changed paths count
		 * for each changed path
		 * 		changed path length
		 *  	changed path bytes
		 *  revision data length
		 *  revision data bytes
		 */
		
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput revisionBytes = new DataOutputStream(byteArray);
			
			//revision
			revisionBytes.writeLong(this.revision);
			
			//changed paths
			revisionBytes.writeInt(this.changedPaths.length);
			for (CacheChangedPath changedPath : this.changedPaths) {
				byte[] pathBytes = changedPath.toBytes();
				BytesUtility.writeBytesWithLength(revisionBytes, pathBytes);			
			}
			
			//revision data		
			if (this.revisionData != null) {
				byte[] dataBytes = this.revisionData.toBytes();
				BytesUtility.writeBytesWithLength(revisionBytes, dataBytes);						
			} else {			
				revisionBytes.writeInt(0);
			}			
			return byteArray.toByteArray();
		} catch (IOException e) {
			//ignore
			return new byte[0];
		}		
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.revision);
	}
	
}
