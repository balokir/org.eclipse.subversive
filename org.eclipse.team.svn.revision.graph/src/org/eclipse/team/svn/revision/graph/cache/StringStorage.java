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
 * 
 * @author Igor Burilo
 */
public class StringStorage extends GenericStorage<String> {
	
	public StringStorage() {
		this.addSimple("");
	}
	
	@Override
	protected void save(String data, RevisionDataContainer revisionDataContainer) {
		//don't save root
		if (this.dataList.size() > 1) {
			PrintWriter out = revisionDataContainer.getPathStringsOutStream();
			out.println(data);	
		}
	}
	
	/**  
	 * @return	Flag which indicates whether there's next data to process 
	 */
	public boolean load(RevisionDataContainer revisionDataContainer) throws IOException {
		BufferedReader in = revisionDataContainer.getPathStringsInStream();
		String line = in.readLine();
		if (line != null) {
			this.dataList.add(line);
			this.hash.put(line, this.dataList.size() - 1);								
			return true;
		} else {
			return false;
		}
	}


}
