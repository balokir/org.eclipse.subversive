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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** 
 * @author Igor Burilo
 */
public class BytesUtility {
	
    public static byte[] convertStringToBytes(String val) {
    	try {
			return val.getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			//ignore
			return new byte[0];
		}
    }
    
    public static String getString(byte[] b) {
    	try {
			return new String(b, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			//ignore
			return null;
		}    	    	
    }
	
	public static void writeBytesWithLength(DataOutput out, byte[] bytes) throws IOException {
		out.writeInt(bytes.length);
		if (bytes.length > 0) {
			out.write(bytes);
		}		
	}
	
	public static byte[] readBytesWithLength(DataInput in) throws IOException {
		int length = in.readInt();
		byte[] b = new byte[length];
		if (length > 0) {
			in.readFully(b);
		}
		return b;
	}
	
	/**
	 * Write: 
	 * 	encoded bytes length
	 * 	encoded bytes
	 * 	raw bytes length
	 */
	public static void compressAndWrite(byte[] bytes, DataOutput out, Deflater encoder) throws IOException {	
		byte[] compressedBytes = BytesUtility.compressBytes(encoder, bytes);
		BytesUtility.writeBytesWithLength(out, compressedBytes);
		out.writeInt(bytes.length);
	}
	
	public static byte[] decompressAndRead(DataInput in, Inflater decoder) throws IOException {	
		byte[] compressedBytes = BytesUtility.readBytesWithLength(in);
		int rawLength = in.readInt();
		try {
			return BytesUtility.decompressBytes(decoder, compressedBytes, rawLength);
		} catch (DataFormatException e) {
			throw new IOException(e.getMessage());
		} 
	}
	
	public static byte[] compressBytes(Deflater encoder, byte[] bytes) {			
		encoder.setInput(bytes);
		encoder.finish();	
						
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
		byte[] buffer = new byte[bytes.length];
		while(!encoder.finished()) {
			int bytesCompressed = encoder.deflate(buffer);
			bos.write(buffer, 0, bytesCompressed);
		}
		
		//reset for next input
		encoder.reset();
							
		return bos.toByteArray();
	}	
	
	public static byte[] decompressBytes(Inflater decoder, byte[] compressedBytes, int uncompressedLength) throws DataFormatException {
		byte[] result = new byte[uncompressedLength];		
		
		decoder.setInput(compressedBytes);
		/*int readBytes = */decoder.inflate(result);		
		//Assert.isTrue(uncompressedLength == readBytes);
				
		//reset for next	
		decoder.reset();
		
		return result;
	}
}
