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
import java.util.Map;

/**
 * Allow to effectively search data either by index or by its hash
 * 
 * @author Igor Burilo
 */
public abstract class GenericStorage<T> {

	protected ArrayList<T> dataList = new ArrayList<T>();
	protected Map<T, Integer> hash = new HashMap<T, Integer>();				
	
	public int add(T data) {			
		int index = this.find(data);
		if (index == RepositoryCache.UNKNOWN_INDEX) {
			index = this.addSimple(data);
		}
		return index;										
	}
		
	public int find(T data) {
		Integer index = this.hash.get(data);
		return index == null ? RepositoryCache.UNKNOWN_INDEX : index;
	}

	public int addSimple(T data) {
		int index = this.dataList.size();
		this.dataList.add(data);
		this.hash.put(data, index);
		return index;
	}
	
	public T getValue(int index) {
		return this.dataList.get(index);
	}
}
