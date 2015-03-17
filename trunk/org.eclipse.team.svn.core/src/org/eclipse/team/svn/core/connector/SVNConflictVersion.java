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

package org.eclipse.team.svn.core.connector;


/**
 * The description of a merge conflict, encountered during merge/update/switch operations
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Igor Burilo
 * @since 1.6
 */
public class SVNConflictVersion {

    public final String reposURL;
    /**
     * @since 1.8
     */
    public final String reposUUID;
    public final long pegRevision;
    public final String pathInRepos;

    /**
     * @see SVNEntry.Kind
     */
    public final SVNEntry.Kind nodeKind;
    
    public SVNConflictVersion(String reposURL, long pegRevision, String pathInRepos, SVNEntry.Kind nodeKind) {
    	this(reposURL, null, pegRevision, pathInRepos, nodeKind);
    }
    
    /**
     * @since 1.8
     */
    public SVNConflictVersion(String reposURL, String reposUUID, long pegRevision, String pathInRepos, SVNEntry.Kind nodeKind) {
    	this.reposURL = reposURL;
    	this.reposUUID = reposUUID;
    	this.pegRevision = pegRevision;
    	this.pathInRepos = pathInRepos;
    	this.nodeKind = nodeKind;
    }
}
