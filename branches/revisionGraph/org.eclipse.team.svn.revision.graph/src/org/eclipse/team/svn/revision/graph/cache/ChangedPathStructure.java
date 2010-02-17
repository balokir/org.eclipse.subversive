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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * TODO
 *  always save copied from info ?
 *  
 * @author Igor Burilo
 */
public class ChangedPathStructure {

	protected int id;		
	protected int pathIndex;	
	protected char action;
	protected long revision;	
	protected int copiedFromPathIndex;	
	protected long copiedFromRevision;

	public ChangedPathStructure() {		
	}
	
	public ChangedPathStructure(int id, int pathIndex, char action, long revision, int copiedFromPathIndex, long copiedFromRevision) {
		this.id = id;
		this.pathIndex = pathIndex;
		this.action = action;
		this.revision = revision;
		this.copiedFromPathIndex = copiedFromPathIndex;
		this.copiedFromRevision = copiedFromRevision;
	}
	
	public long getId() {
		return id;
	}

	public int getPathIndex() {
		return pathIndex;
	}

	public char getAction() {
		return action;
	}

	public long getRevision() {
		return revision;
	}

	public int getCopiedFromPathIndex() {
		return copiedFromPathIndex;
	}

	public long getCopiedFromRevision() {
		return copiedFromRevision;
	}

	public void save(RevisionDataContainer revisionDataContainer) throws IOException {
		PrintWriter out = revisionDataContainer.getChangedPathsOutStream();
				
		String separator = " ";
		out.println(
			this.id + separator +  	
			this.pathIndex + separator + 
			this.action + separator + 
			this.revision + separator + 
			this.copiedFromPathIndex + separator +
			this.copiedFromRevision);
		
		CacheMetadata metadata = revisionDataContainer.getCacheMetadata();
		metadata.setChangedPathsCount(metadata.getChangedPathsCount() + 1);
	}
	
	public boolean load(RevisionDataContainer revisionDataContainer) throws IOException {
		BufferedReader changedPathsIn = revisionDataContainer.getChangedPathsInStream();
		String line = changedPathsIn.readLine();
		if (line != null) {			
			String[] parts = line.split(" ");
			this.id = Integer.parseInt(parts[0]);
			this.pathIndex = Integer.parseInt(parts[1]);
			this.action = parts[2].charAt(0);
			
			this.revision = Long.parseLong(parts[3]);
			this.copiedFromPathIndex = Integer.parseInt(parts[4]);
			this.copiedFromRevision = Long.parseLong(parts[5]);
											
			return true;
		} else {
			return false;
		}
	}
	
}
