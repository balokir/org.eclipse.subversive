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

import org.eclipse.team.svn.core.connector.SVNLogPath;

/**
 * Changed path for revision node
 * 
 * @author Igor Burilo
 */
public class ChangedPath {
	
	public final String path;
	public final SVNLogPath.ChangeType action;
	public final String copiedFromPath;
	public final long copiedFromRevision;
	
	public ChangedPath(String path, SVNLogPath.ChangeType action, long revision) {
		this(path, action, null, -1);
	}
	
	public ChangedPath(String path, SVNLogPath.ChangeType action, String copiedFromPath, long copiedFromRevision) {
		this.path = path;		
		this.action = action;
		this.copiedFromPath = copiedFromPath;
		this.copiedFromRevision = copiedFromRevision;
	}
	
}
