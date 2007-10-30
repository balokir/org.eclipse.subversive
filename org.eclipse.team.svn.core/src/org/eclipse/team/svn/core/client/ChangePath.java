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

package org.eclipse.team.svn.core.client;

/**
 * Repalcement for org.tigris.subversion.javahl.ChangePath
 * 
 * @author Alexander Gurov
 */
public class ChangePath {
    /** Path of commited item */
    public final String path;

    /** Source revision of copy (if any). */
    public final long copySrcRevision;

    /** Source path of copy (if any). */
    public final String copySrcPath;

    /** 'A'dd, 'D'elete, 'R'eplace, 'M'odify */
    public final char action;

    /**
     * Constructor to be called from the native code
     * @param path              path of the commit item
     * @param copySrcRevision   copy source revision (if any)
     * @param copySrcPath       copy source path (if any)
     * @param action            action performed
     */
	public ChangePath(String path, long copySrcRevision, String copySrcPath, char action) {
        this.path = path;
        this.copySrcRevision = copySrcRevision;
        this.copySrcPath = copySrcPath;
        this.action = action;
    }
	
}
