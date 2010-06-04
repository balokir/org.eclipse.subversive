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
package org.eclipse.team.svn.revision.graph.graphic;

/**
 * Merge data for revision node
 * 
 * @author Igor Burilo
 */
public class NodeMergeData {

	public final String path;
	protected final long[] revisions;
	
	public NodeMergeData(String path, long revision) {
		this.path = path;
		this.revisions = new long[] {revision};
	}
	
	public NodeMergeData(String path, long[] revisions) {
		this.path = path;
		this.revisions = revisions;
	}
	
	public long[] getRevisions() {
		long[] result = new long[this.revisions.length];
		System.arraycopy(this.revisions, 0, result, 0, this.revisions.length);
		return result;
	}
	
	public int getRevisionsCount() {
		return this.revisions.length;
	}
}
