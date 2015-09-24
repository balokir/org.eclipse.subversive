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

package org.eclipse.team.svn.core.operation.local;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;

/**
 * Used by operations from different hierarchies
 * 
 * @author Igor Burilo
 */
public class UnresolvedConflictDetectorHelper implements IUnresolvedConflictDetector {

    protected Set<IResource> processed;
    protected Set<IResource> unprocessed;
	protected boolean hasUnresolvedConflict;
	protected String conflictMessage;
	
	public void setUnresolvedConflict(boolean hasUnresolvedConflict) {
		this.hasUnresolvedConflict = hasUnresolvedConflict;
	}
	
	public boolean hasUnresolvedConflicts() {
        return this.hasUnresolvedConflict;
    }
    
    public String getMessage() {
    	return this.conflictMessage;
    }
    
    public IResource []getUnprocessed() {
		return this.unprocessed == null ? new IResource[0] : this.unprocessed.toArray(new IResource[this.unprocessed.size()]);
    }

	public IResource []getProcessed() {
		return this.processed == null ? new IResource[0] : this.processed.toArray(new IResource[this.processed.size()]);
	}
	
	protected void defineInitialResourceSet(IResource []resources) {
        this.hasUnresolvedConflict = false;
        this.unprocessed = new HashSet<IResource>();
        this.processed = new HashSet<IResource>();
		this.processed.addAll(Arrays.asList(resources));
	}

	public void addUnprocessed(IResource unprocessed) {
		this.unprocessed.add(unprocessed);
	}

	public void setConflictMessage(String message) {
		this.conflictMessage = message;
		
	}
	
	public void removeProcessed(IResource resource) {
		this.unprocessed.remove(resource);		
	}
}
