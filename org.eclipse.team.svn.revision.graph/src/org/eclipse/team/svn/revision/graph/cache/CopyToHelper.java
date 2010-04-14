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
 * Helper which contains 'copy to' data for particular path 
 * 
 * @author Igor Burilo
 */
public class CopyToHelper {

	/*
	 * pathIndex -> list of ChangedPathStructure
	 * 
	 * there can be several copies from the same path@rev 
	 */	
	protected Map<Integer, List<CacheChangedPathWithRevision>> pathCopyToData = new HashMap<Integer, List<CacheChangedPathWithRevision>>();

	public void add(CacheChangedPath changedPath, long revision) {
		int pathIndex = changedPath.getCopiedFromPathIndex();	
		List<CacheChangedPathWithRevision> list = this.pathCopyToData.get(pathIndex);
		if (list == null) {
			list = new ArrayList<CacheChangedPathWithRevision>();
			this.pathCopyToData.put(pathIndex, list);			
		}
		list.add(new CacheChangedPathWithRevision(changedPath, revision));
	}
	
	public List<CacheChangedPathWithRevision> getCopyTo(int pathId) {
		return this.pathCopyToData.get(pathId);
	}

	public void clear() {
		if (!this.pathCopyToData.isEmpty()) {
			this.pathCopyToData.clear();
		}		
	}
	
}
