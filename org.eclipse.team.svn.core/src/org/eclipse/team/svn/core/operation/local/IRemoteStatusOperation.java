/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IResourceChange;

/**
 * Remote status calculation operation interface
 * 
 * @author Alexander Gurov
 */
public interface IRemoteStatusOperation extends IActionOperation {
	public IResource []getScope();
    public SVNEntryStatus []getStatuses();
    public void setPegRevision(IResourceChange change);
}
