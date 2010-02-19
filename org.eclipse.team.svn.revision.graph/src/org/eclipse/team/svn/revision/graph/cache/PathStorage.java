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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Effectively stores paths
 * 
 * @author Igor Burilo
 */
public class PathStorage {
		
	public final static int UNKNOWN_INDEX = -1;	
	public final static int ROOT_INDEX = 0;			
	
	protected final static String PATH_SEPARATOR = "/";
	
	//contains strings
	protected StringStorage strings = new StringStorage();		
	
	//contains indexes which compose paths
	protected IndexPairsStorage pathIndex = new IndexPairsStorage();
	
	public int add(String path) {
		try {
			return this.add(path, null);
		} catch (IOException e) {
			//never happens
			return PathStorage.UNKNOWN_INDEX;
		}
	}
		
	/*
	 * Split path by parts and add them
	 */
	public int add(String path, RevisionDataContainer revisionDataContainer)  throws IOException {		
		if (path == null) {
			return UNKNOWN_INDEX;
		}		
		int resultIndex = ROOT_INDEX;
		String[] parts = path.split(PATH_SEPARATOR);
		for (String part : parts) {			
			int stringIndex = this.strings.add(part, revisionDataContainer);						
			resultIndex = this.pathIndex.add(new Pair(resultIndex, stringIndex), revisionDataContainer);
		}		
		return resultIndex;
	}
	
	public String getPath(int index) {				
		List<Pair> pairs = this.getPathPairs(index);
		if (!pairs.isEmpty()) {
			StringBuffer res = new StringBuffer();
			for (Pair pair : pairs) {				
				String path = this.strings.getValue(pair.stringIndex);
				res.append(PATH_SEPARATOR).append(path);
			}		
			return res.toString();
		} else {
			return null;
		}		
	}	
	
	protected List<Pair> getPathPairs(int index) {
		LinkedList<Pair> list = new LinkedList<Pair>();
		int tmpIndex = index;
		Pair pair = null;
		while ((pair = this.pathIndex.getValue(tmpIndex)) != null && pair != IndexPairsStorage.ROOT_PAIR) {
			list.addFirst(pair);
			tmpIndex = pair.parentIndex;
		}		
		return list;		
	}
	
	protected Pair getParentPathPair(int pathIndex) {
		return this.pathIndex.getValue(pathIndex);
	}
	
	public int getParentPathIndex(int pathIndex) {
		return this.getParentPathPair(pathIndex).parentIndex;
	} 
	
	public int add(int parentIndex, int[] childParts) {
		int res = parentIndex;
		try {
			for (int childPart : childParts) {			
				res = this.pathIndex.add(new Pair(res, childPart), null);
			}
		} catch (IOException ie) {
			//never happens
		}
		return res;
	}
	
	/*
	 * Parent childPathIndex is more or equal to parentPathIndex
	 */
	public boolean isParentIndex(int parentPathIndex, int childPathIndex) {		
		if (childPathIndex >= parentPathIndex) {
			int index = childPathIndex;
			do {
				if (index == parentPathIndex) {
					return true;
				}
			} while ((index = this.getParentPathIndex(index)) != ROOT_INDEX);	
		}		
		return false;
	}
	
	/*
	 * Return array of element indexes which comprise relative path 
	 */
	public int[] makeRelative(int parentPathIndex, int childPathIndex) {			
		/*
		 * Example:
		 * parent:  /subversion/branches
		 * child:   /subversion/branches/br1/file.txt 
		 */			
		if (!this.isParentIndex(parentPathIndex, childPathIndex)) {
			return new int[0];
		}		
		LinkedList<Integer> elements = new LinkedList<Integer>();		
		int index = childPathIndex;
		while (index != parentPathIndex) {
			Pair indexPair = this.pathIndex.getValue(index);			
			elements.addFirst(indexPair.stringIndex);
			index = indexPair.parentIndex;
		}				
		return this.collection2Array(elements);		
	}
	
	protected int[] collection2Array(Collection<Integer> col) {
		if (!col.isEmpty()) {
			int[] res = new int[col.size()];
			int i = 0;
			for (int element : col) {
				res[i ++] = element;
			}
			return res;
		} else {
			return new int[0];
		}				
	}
	
	public void load(RevisionDataContainer revisionDataContainer) throws IOException {		
		while (true) {
			boolean hasNext = this.strings.load(revisionDataContainer);
			if (!hasNext) {
				break;
			}
		}
		
		while (true) {
			boolean hasNext = this.pathIndex.load(revisionDataContainer);
			if (!hasNext) {
				break;
			}
		}		
	}
	
	public static void main(String[] s) throws Exception {
		String s1 = "/test/wiki-migration/project_wiki-plus-polarion/_wiki/Space_1/Page_1/attachment-1.txt";
		String s2 = "/sss";
		String s3 = "/test";
		String s4 = "/test/wiki-migration/project_wiki-plus-polarion";
		
		PathStorage instance = new PathStorage();
		int i1 = instance.add(s1, null);
		int i2 = instance.add(s2, null);
		int i3 = instance.add(s3, null);
		int i4 = instance.add(s4, null);
		
		System.out.println(instance.getPath(i1));		
		System.out.println(instance.getPath(i2));
		
		//test makeRelative
		int i10 = instance.add("/g1/g2/g3", null);
		int[] res = instance.makeRelative(i2, i1);						
		System.out.println(instance.getPath(instance.add(i10, res)));
		
		res = instance.makeRelative(i3, i1);						
		System.out.println(instance.getPath(instance.add(i10, res)));
		
		res = instance.makeRelative(i4, i1);						
		System.out.println(instance.getPath(instance.add(i10, res)));
	}
		
}
