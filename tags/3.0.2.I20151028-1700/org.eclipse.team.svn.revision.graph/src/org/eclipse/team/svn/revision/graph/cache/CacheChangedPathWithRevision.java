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

import org.eclipse.team.svn.core.connector.SVNLogPath;

/**
 * Add revision info to changed path class
 *  
 * @author Igor Burilo
 */
public class CacheChangedPathWithRevision {

	protected final CacheChangedPath changedPath;
	protected final long revision;
	
	public CacheChangedPathWithRevision(CacheChangedPath changedPath, long revision) {
		this.changedPath = changedPath;
		this.revision = revision;
	}
	
	public int getPathIndex() {
		return this.changedPath.getPathIndex();
	}

	public SVNLogPath.ChangeType getAction() {
		return this.changedPath.getAction();
	}
	
	public int getCopiedFromPathIndex() {
		return this.changedPath.getCopiedFromPathIndex();
	}

	public long getCopiedFromRevision() {
		return this.changedPath.getCopiedFromRevision();
	}
	
	public long getRevision() {
		return this.revision;
	}

}
