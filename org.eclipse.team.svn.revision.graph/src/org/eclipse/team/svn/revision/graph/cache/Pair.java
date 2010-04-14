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

	public final int first;
	public final int second;		
	
	public Pair(int first, int second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) { 
			Pair p = (Pair) obj;
			return this.first == p.first && this.second == p.second;
		}
		return false;
	}

	@Override
	public int hashCode() {			
		final int prime = 31;		
		int result = 17;
		result += prime * this.first;
		result += prime * this.second;				
		return result;			
	}
	
	@Override
	public String toString() {		
		return "first: " + this.first + " second: " + this.second; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
