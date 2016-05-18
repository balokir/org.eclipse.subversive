/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic;

/**
 * @author Igor Burilo
 */
public class SearchOptions {

	//if revision is -1, then don't look at it
	public final long revision;
	//if path is null then don't look at it
	public final String path;
	
	public SearchOptions(long revision, String path) {
		this.revision = revision;
		this.path = path;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SearchOptions) {
			SearchOptions op = (SearchOptions) obj;
			if (this.revision == op.revision && eq(this.path, op.path)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean eq(String o1, String o2) {
		return (o1==null ? o2==null : o1.equals(o2));
	}
}
