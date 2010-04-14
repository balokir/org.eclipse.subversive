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
public class StringStorage extends GenericStorage<String> {
	
	public StringStorage() {
		this.addSimple(""); //$NON-NLS-1$
	}
	
	public StringStorage(byte[] bytes) {
		this.fromBytes(bytes);
	}

	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			int stringsCount = bytesIn.readInt();
			for (int i = 0; i < stringsCount; i ++) {
				byte[] strBytes = BytesUtility.readBytesWithLength(bytesIn);
				String str = BytesUtility.getString(strBytes);
				
				this.dataList.add(str);				
				this.hash.put(str, this.dataList.size() - 1);								
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
			for (String str : this.dataList) {
				byte[] strBytes = BytesUtility.convertStringToBytes(str);
				BytesUtility.writeBytesWithLength(bytes, strBytes);														
			}
			return byteArray.toByteArray();	
		} catch (IOException ie) {
			//ignore
			return new byte[0];
		}
	}

}
