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

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * TODO save author and message in optimized way
 * 
 * @author Igor Burilo
 */
public class RevisionDataStructure {

	protected long address;
	
	protected String author;
	protected long date;
	protected String message;
	
	public RevisionDataStructure(long address) {
		this.address = address;
	}
	
	public RevisionDataStructure(long date, String author, String message) {
		this.date = date;
		this.author = author;
		this.message = message;
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
	
	public long save(RevisionDataContainer revisionDataContainer) throws IOException {
		RandomAccessFile out = revisionDataContainer.getRevisionDataOutStream();		
		long address = out.getFilePointer();
		String author = this.author == null ? "" : this.author;
		String message = this.message == null ? "" : this.message;
				
		RevisionDataContainer.writeBigString(this.date + " " + author + " " + message, out);
		
		return address;
	}	
		
	public void load(RevisionDataContainer revisionDataContainer) throws IOException {
		RandomAccessFile in = revisionDataContainer.getRevisionDataInStream();
		in.seek(this.address);
				
		String line = RevisionDataContainer.readBigString(in);
		
		int index = line.indexOf(" ");
		int nextIndex = line.indexOf(" ", index + 1);
		
		long date = Long.parseLong(line.substring(0, index));
		String author = line.substring(index + 1, nextIndex);
		String message = line.substring(nextIndex + 1);
		
		this.date = date;
		this.author = author;
		this.message = message;						
	}

}
