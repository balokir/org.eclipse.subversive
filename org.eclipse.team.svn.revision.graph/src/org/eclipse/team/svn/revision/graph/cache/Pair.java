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


/**
 * 
 * @author Igor Burilo
 */
public class Pair {

	public final int parentIndex;
	public final int stringIndex;		
	
	public Pair(int parentIndex, int childIndex) {
		this.parentIndex = parentIndex;
		this.stringIndex = childIndex;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) { 
			Pair p = (Pair) obj;
			return this.parentIndex == p.parentIndex && this.stringIndex == p.stringIndex;
		}
		return false;
	}

	@Override
	public int hashCode() {			
		final int prime = 31;		
		int result = 17;
		result += prime * this.parentIndex;
		result += prime * this.stringIndex;				
		return result;			
	}
	
	public boolean isRoot() {
		return this.parentIndex == PathStorage.ROOT_INDEX && this.stringIndex == PathStorage.ROOT_INDEX;
	}
	
	@Override
	public String toString() {		
		return "parent: " + this.parentIndex + " child: " + this.stringIndex;
	}
}
