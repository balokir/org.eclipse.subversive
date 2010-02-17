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
 * firstIndex  points to IndexPairsStorage
 * secondIndex points to StringStorage
 * 
 * @author Igor Burilo
 */
public class IndexPairsStorage extends GenericStorage<Pair> {

	public static Pair ROOT_PAIR = new Pair(PathStorage.ROOT_INDEX, PathStorage.ROOT_INDEX);
	
	public IndexPairsStorage() {
		this.addSimple(ROOT_PAIR);							
	}
	
	@Override
	protected void save(Pair data, RevisionDataContainer revisionDataContainer) {
		//don't save root
		if (this.dataList.size() > 1) {
			PrintWriter out = revisionDataContainer.getPathIndexesOutStream();
			out.println(data.parentIndex + " " + data.stringIndex);					
		}
			
	}
	
	/**  
	 * @return	Flag which indicates whether there's next data to process 
	 */
	public boolean load(RevisionDataContainer revisionDataContainer) throws IOException {
		BufferedReader in = revisionDataContainer.getPathIndexesInStream();
		String line = in.readLine();
		if (line != null) {				
			String[] parts = line.split(" "); 					
			Pair pair = new Pair(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			this.dataList.add(pair);
			this.hash.put(pair, this.dataList.size() - 1);
			
			return true;
		} else {
			return false;
		}
	}
}
