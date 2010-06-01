/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Store merge info: revision and merged to it revisions
 * 
 * @author Igor Burilo
 */
public class MergeInfoStorage {

	protected Map<Long, Set<Long>> mergeInfo = new HashMap<Long, Set<Long>>();
	
	public MergeInfoStorage() {		
	}
	
	public MergeInfoStorage(byte[] bytes) {
		this.fromBytes(bytes);
	}
	
	public void addMergeInfo(long mergeTargetRevision, long mergeSourceRevision) {
		Set<Long> revs = this.mergeInfo.get(mergeTargetRevision);
		if (revs == null) {
			revs = new HashSet<Long>();
			this.mergeInfo.put(mergeTargetRevision, revs);
		}
		revs.add(mergeSourceRevision);
	} 
	
	public long[] getMergeTargetRevisions() {
		Set<Long> revisions =  this.mergeInfo.keySet();
		long[] result = new long[revisions.size()];
		int i = 0;
		for (long revision : revisions) {
			result[i ++] = revision;
		}
		return result;
	}
	
	public long[] getMergeSourceRevisions(long mergeTargetRevision) {
		long result[];
		Set<Long> revs = this.mergeInfo.get(mergeTargetRevision);
		if (revs != null && !revs.isEmpty()) {
			result = new long[revs.size()];			
			int i = 0;
			Iterator<Long> iter = revs.iterator();
			while (iter.hasNext()) {
				result[i ++] = iter.next();
			}
		} else {
			result = new long[0];
		}
		return result;
	}
	
	protected final void fromBytes(byte[] bytes) {
		try {
			DataInput bytesIn = new DataInputStream(new ByteArrayInputStream(bytes));
			
			int count = bytesIn.readInt();
			for (int i = 0; i < count; i ++) {
				long targetRev = bytesIn.readLong();
				int sourceCount = bytesIn.readInt();
				Set<Long> sourceRevs = new HashSet<Long>(sourceCount);
				this.mergeInfo.put(targetRev, sourceRevs);
				for (int j = 0; j < sourceCount; j ++) {
					sourceRevs.add(bytesIn.readLong());
				}
			}	
		} catch (IOException e) {
			//ignore
		}
	}
	
	public byte[] toBytes() {
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutput bytes = new DataOutputStream(byteArray);	
			
			bytes.writeInt(this.mergeInfo.size());
			for (Map.Entry<Long, Set<Long>> entry : this.mergeInfo.entrySet()) {
				long targetRev = entry.getKey();
				Set<Long> sourceRevs = entry.getValue();
				
				/*
				 * TODO possible improvement:
				 * save revisions by ranges instead of individually ?
				 */
				bytes.writeLong(targetRev);
				bytes.writeInt(sourceRevs.size());
				for (long sourceRev : sourceRevs) {
					bytes.writeLong(sourceRev);
				}
			}
			return byteArray.toByteArray();	
		} catch (IOException ie) {
			//ignore
			return new byte[0];
		}
	}
}
