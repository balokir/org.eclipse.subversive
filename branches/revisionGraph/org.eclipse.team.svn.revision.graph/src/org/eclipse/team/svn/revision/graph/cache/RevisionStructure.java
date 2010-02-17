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
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Igor Burilo
 */
public class RevisionStructure {

	public final static int UNKNOWN_ADDRESS = -1;
	
	protected long revision;
		
	protected long dataAddress;
	protected List<Integer> changedPathIds;

	protected RevisionDataStructure revisionData;
	protected ChangedPathStructure[] changedPaths = new ChangedPathStructure[0];
	
	public RevisionStructure()  {		
	}
	
	public RevisionStructure(long revision) {
		this.revision = revision;		
	}
	
	protected void addChangedPathId(int changedPathId) {
		if (this.changedPathIds == null) {
			this.changedPathIds = new ArrayList<Integer>();
		}
		this.changedPathIds.add(changedPathId);
	}
	
	public boolean hasChangedPaths() {
		return this.changedPaths.length > 0;
	}
	
	public ChangedPathStructure[] getChangedPaths() {
		return this.changedPaths;
	} 
	
	public long getRevision() {
		return this.revision;
	}
	
	public String getAuthor() {
		return this.revisionData != null ? this.revisionData.getAuthor() : null;		
	} 
	
	public long getDate() {
		return this.revisionData != null ? this.revisionData.getDate() : 0;
	}
	
	public String getMessage() {
		return this.revisionData != null ? this.revisionData.getMessage() : null;
	}
	
	protected void setRevisionData(RevisionDataStructure revisionData) {
		this.revisionData = revisionData;
	}
	
	protected void setChangedPaths(ChangedPathStructure[] changedPaths) {
		this.changedPaths = changedPaths;
		if (this.changedPathIds != null) {
			this.changedPathIds = null;
		}
	}
	
	public void save(RevisionDataContainer revisionDataContainer) throws IOException {
		long revisionDataAddress = RevisionStructure.UNKNOWN_ADDRESS;
		if (this.revisionData != null) {
			revisionDataAddress = this.revisionData.save(revisionDataContainer);	
		}		

		String pathsString = "";
		ChangedPathStructure[] changedPaths = this.getChangedPaths();
		for (ChangedPathStructure changedPath : changedPaths) {				
			changedPath.save(revisionDataContainer);				
			pathsString += " " + changedPath.getId();
		} 
				
		PrintWriter revisionsOut = revisionDataContainer.getRevisionsOutStream();
		revisionsOut.println(this.revision + " " + revisionDataAddress + pathsString);
	}
	
	/**  
	 * @return	Flag which indicates whether there's next data to process 
	 */
	public boolean load(RevisionDataContainer revisionDataContainer) throws IOException {
		BufferedReader revisionsIn = revisionDataContainer.getRevisionsInStream();
		String line = revisionsIn.readLine();
		if (line != null) {
			String[] parts = line.split(" ");
			this.revision = Long.parseLong(parts[0]);
			this.dataAddress = Long.parseLong(parts[1]);							
						
			if (parts.length > 2) {
				for (int i = 2; i < parts.length; i ++) {
					int ref = Integer.parseInt(parts[i]);			
					this.addChangedPathId(ref);					
				}				
			}
			return true;
		} else {
			return false;
		}
	}
	
}
