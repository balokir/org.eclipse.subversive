/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;

/**
 * Provides info about history view modes
 * 
 * @author Alexander Gurov
 */
public interface ISVNHistoryViewInfo {
	public static final int MODE_BOTH = 0x20;
	public static final int MODE_LOCAL = 0x40;
	public static final int MODE_REMOTE = 0x80;
	
	public static final int MODE_MASK = ISVNHistoryViewInfo.MODE_BOTH | ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_LOCAL;
	
	public IResource getResource();
	public IRepositoryResource getRepositoryResource();
	public SVNRevision getCurrentRevision();
	
	public boolean isRelatedPathsOnly();
	public boolean isGrouped();
	public int getMode();
	
	public SVNLocalFileRevision []getLocalHistory();
	public SVNLogEntry []getRemoteHistory();
}
