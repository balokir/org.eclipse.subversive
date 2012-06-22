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
package org.eclipse.team.svn.revision.graph.operation;

/**
 * Contain information about SVN repository connection
 * 
 * @author Igor Burilo
 */
public class RepositoryConnectionInfo {

	public final boolean hasConnection;
	public final long lastRepositoryRevision;
	public final boolean isSupportMergeInfo;
	
	public RepositoryConnectionInfo(boolean hasConnection, long lastRepositoryRevision, boolean isSupportMergeInfo) {
		this.hasConnection = hasConnection;
		this.lastRepositoryRevision = lastRepositoryRevision;
		this.isSupportMergeInfo = isSupportMergeInfo;
	}
	
	public interface IRepositoryConnectionInfoProvider {
		RepositoryConnectionInfo getRepositoryConnectionInfo();
	}
}
