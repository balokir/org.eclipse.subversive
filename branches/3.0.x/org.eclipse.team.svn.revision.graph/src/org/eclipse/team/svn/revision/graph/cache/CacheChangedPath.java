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

import org.eclipse.team.svn.core.connector.SVNLogPath;

/**
 * @author Igor Burilo
 */
public class CacheChangedPath {
		
	protected int pathIndex;	
	protected SVNLogPath.ChangeType action;
	
	protected int copiedFromPathIndex;	
	protected long copiedFromRevision;
	
	public CacheChangedPath(int pathIndex, SVNLogPath.ChangeType action, int copiedFromPathIndex, long copiedFromRevision) {
		this.pathIndex = pathIndex;
		this.action = action;		
		this.copiedFromPathIndex = copiedFromPathIndex;
		this.copiedFromRevision = copiedFromRevision;
	}
	
	public CacheChangedPath(byte[] bytes) {
		this.fromBytes(bytes);
	}

	public int getPathIndex() {
		return pathIndex;
	}

	public SVNLogPath.ChangeType getAction() {
		return action;
	}
	
	public int getCopiedFromPathIndex() {
		return copiedFromPathIndex;
	}

	public long getCopiedFromRevision() {
		return copiedFromRevision;
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput dataIn = new DataInputStream(new ByteArrayInputStream(bytes));
						
			this.pathIndex = dataIn.readInt();
			this.action = SVNLogPath.ChangeType.fromId(dataIn.readChar());
			
			byte copyFlag = dataIn.readByte();
			if (copyFlag == 1) {
				this.copiedFromPathIndex = dataIn.readInt();
				this.copiedFromRevision = dataIn.readLong();
			} else {
				this.copiedFromPathIndex = RepositoryCache.UNKNOWN_INDEX;
				this.copiedFromRevision = 0;
			}	
		} catch (IOException e) {
			//ignore
		}		
	}
	
	public byte[] toBytes() {
		/*
		 * Write:
		 * 	
		 * path index
		 * action		
		 * copied from flag
		 * copied from path index
		 * copied from revision
		 */
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput bytes = new DataOutputStream(byteArray);
								
			bytes.writeInt(this.pathIndex);
			bytes.writeChar(this.action.id);
			
			//copied from
			if (this.copiedFromPathIndex != RepositoryCache.UNKNOWN_INDEX) {
				bytes.writeByte(1);
									
				bytes.writeInt(this.copiedFromPathIndex);
				bytes.writeLong(this.copiedFromRevision);
			} else {
				bytes.writeByte(0);
			}				
			return byteArray.toByteArray();
		} catch (IOException ie) {
			//ignore
			return new byte[0];
		}
	}
	
}
