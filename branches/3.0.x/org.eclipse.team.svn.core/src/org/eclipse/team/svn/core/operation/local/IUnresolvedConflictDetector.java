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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;

/**
 * Interface that enable us to detect unresolved conflict situation
 * 
 * @author Alexander Gurov
 */
public interface IUnresolvedConflictDetector {
	public void setUnresolvedConflict(boolean hasUnresolvedConflict);
    public boolean hasUnresolvedConflicts();
    public void addUnprocessed(IResource unprocessed);
    public IResource []getUnprocessed();
    public void removeProcessed(IResource resource);
    public IResource []getProcessed();
    public void setConflictMessage(String message);
    public String getMessage();
}
