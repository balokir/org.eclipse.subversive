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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container which contains 'copy to' data for particular path 
 * 
 * @author Igor Burilo
 */
public class CopyToContainer {

	/*
	 * pathIndex -> list of ChangedPathStructure
	 * 
	 * there can be several copies from the same path@rev 
	 */	
	protected Map<Integer, List<ChangedPathStructure>> pathCopyToData = new HashMap<Integer, List<ChangedPathStructure>>();

	public void add(ChangedPathStructure changedPath) {
		int pathIndex = changedPath.getCopiedFromPathIndex();	
		List<ChangedPathStructure> list = this.pathCopyToData.get(pathIndex);
		if (list == null) {
			list = new ArrayList<ChangedPathStructure>();
			this.pathCopyToData.put(pathIndex, list);			
		}
		list.add(changedPath);
	}
	
	public List<ChangedPathStructure> getCopyTo(int pathId) {
		return this.pathCopyToData.get(pathId);
	}

	public void clear() {
		if (!this.pathCopyToData.isEmpty()) {
			this.pathCopyToData.clear();
		}		
	}
	
}
