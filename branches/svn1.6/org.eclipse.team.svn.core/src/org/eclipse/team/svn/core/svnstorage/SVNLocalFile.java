/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor;
import org.eclipse.team.svn.core.resource.ILocalFile;

/**
 * Working Copy file representation
 * 
 * @author Alexander Gurov
 */
public class SVNLocalFile extends SVNLocalResource implements ILocalFile {
	public SVNLocalFile(IResource resource, long revision, long baseRevision, String status, int changeMask, String author, long lastCommitDate, SVNConflictDescriptor treeConflictDescriptor) {
		super(resource, revision, baseRevision, status, changeMask, author, lastCommitDate, treeConflictDescriptor);
	}

}
