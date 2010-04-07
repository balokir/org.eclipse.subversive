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
 * For 45000 revisions it takes to create needed objects:
 * date: 0.5 sec, memory: 29MB
 * (45000 revisions is number of revisions for subversion/trunk on apache repository) 
 * 
 * @author Igor Burilo
 */
public class CacheRevisionData {
	
	//TODO use indexes instead of strings
	protected String author;
	
	protected long date;
	protected String message;
	
	public CacheRevisionData(long date, String author, String message) {
		this.date = date;
		this.author = author;
		this.message = message;
	}
	
	public CacheRevisionData(byte[] bytes) {
		this.fromBytes(bytes);
	}
	
	public String getAuthor() {
		return author;
	}

	public long getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}
		
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			this.date = bytesIn.readLong();
			
			int authorLength = bytesIn.readInt();
			if (authorLength > 0) {
				byte[] strBytes = new byte[authorLength];
				bytesIn.readFully(strBytes);
				this.author = BytesUtility.getString(strBytes);
			}
			
			int messageLength = bytesIn.readInt();
			if (messageLength > 0) {
				byte[] strBytes = new byte[messageLength];
				bytesIn.readFully(strBytes);
				this.message = BytesUtility.getString(strBytes);
			}
		} catch (IOException ie) {
			//ignore
		} 			
	}
	
	public byte[] toBytes() {
		/*
		 * Write:
		 * 
		 * date
		 * author length
		 * author bytes
		 * message length
		 * message bytes
		 */
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput bytes = new DataOutputStream(byteArray);				
			
			//date
			bytes.writeLong(this.date);
			
			//author
			if (this.author != null && this.author.length() > 0) {
				byte[] authorBytes = BytesUtility.convertStringToBytes(this.author);
				BytesUtility.writeBytesWithLength(bytes, authorBytes);						
			} else {
				bytes.writeInt(0);
			}

			//message	
			if (this.message != null && this.message.length() > 0) {
				byte[] messageBytes = BytesUtility.convertStringToBytes(this.message);
				BytesUtility.writeBytesWithLength(bytes, messageBytes);
			} else {
				bytes.writeInt(0);
			}			
			return byteArray.toByteArray();
		} catch (IOException e) {
			//ignore
			return new byte[0];
		}
	}

}
