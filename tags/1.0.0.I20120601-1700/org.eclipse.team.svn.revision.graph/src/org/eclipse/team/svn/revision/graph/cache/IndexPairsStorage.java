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
 * 
 * @author Igor Burilo
 */
public class IndexPairsStorage extends GenericStorage<Pair> {
	
	public IndexPairsStorage() {
		this.addSimple(new Pair(PathStorage.ROOT_INDEX, PathStorage.ROOT_INDEX));							
	}
	
	public IndexPairsStorage(byte[] bytes) {
		this.fromBytes(bytes);
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			int count = bytesIn.readInt();
			for (int i = 0; i < count; i ++) {
				int parentIndex = bytesIn.readInt();
				int stringIndex = bytesIn.readInt();
				Pair pair = new Pair(parentIndex, stringIndex);
				
				this.dataList.add(pair);
				this.hash.put(pair, this.dataList.size() - 1);
			}	
		} catch (IOException e) {
			//ignore
		}
	}
	
	public byte[] toBytes() {
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();			
			DataOutput bytes = new DataOutputStream(byteArray);	
			
			bytes.writeInt(this.dataList.size());
			for (Pair pair : this.dataList) {
				bytes.writeInt(pair.first);						
				bytes.writeInt(pair.second);	
			}
			return byteArray.toByteArray();	
		} catch (IOException ie) {
			//ignore
			return new byte[0];
		}
	}
}
