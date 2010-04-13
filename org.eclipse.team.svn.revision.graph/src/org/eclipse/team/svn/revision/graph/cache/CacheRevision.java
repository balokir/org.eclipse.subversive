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
	protected int authorIndex;	
	protected long date;	
	protected int messageIndex;
	
	protected CacheChangedPath[] changedPaths = new CacheChangedPath[0];
	
	public CacheRevision(long revision, int authorIndex, long date, int messageIndex, CacheChangedPath[] changedPaths) {
		this.revision = revision;		
		this.authorIndex = authorIndex;
		this.date = date;
		this.messageIndex = messageIndex;
		this.changedPaths = changedPaths;
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
	
	public int getAuthorIndex() {
		return this.authorIndex;		
	} 
	
	public long getDate() {
		return this.date;
	}
	
	public int getMessageIndex() {
		return this.messageIndex;
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			this.revision = bytesIn.readLong();
					
			this.date = bytesIn.readLong();
			
			this.authorIndex = bytesIn.readInt();
			
			this.messageIndex = bytesIn.readInt();
			
			//changed paths
			int changedPathsCount = bytesIn.readInt();
			this.changedPaths = new CacheChangedPath[changedPathsCount];
			for (int i = 0; i < changedPathsCount; i ++) {
				byte[] pathBytes = BytesUtility.readBytesWithLength(bytesIn);
				this.changedPaths[i] = new CacheChangedPath(pathBytes);			
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
		 * date
		 * author index
		 * message length
		 * message bytes
		 * changed paths count
		 * for each changed path
		 * 		changed path length
		 *  	changed path bytes
		 */
		
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput revisionBytes = new DataOutputStream(byteArray);
			
			//revision
			revisionBytes.writeLong(this.revision);
			
			//date
			revisionBytes.writeLong(this.date);
			
			//author
			revisionBytes.writeInt(this.authorIndex);

			//message
			revisionBytes.writeInt(this.messageIndex);							
			
			//changed paths
			revisionBytes.writeInt(this.changedPaths.length);
			for (CacheChangedPath changedPath : this.changedPaths) {
				byte[] pathBytes = changedPath.toBytes();
				BytesUtility.writeBytesWithLength(revisionBytes, pathBytes);			
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
