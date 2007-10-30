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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Abstract operation that implement functionality to detect unresolved conflicts in time of WC operations
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractConflictDetectionOperation extends AbstractWorkingCopyOperation implements IUnresolvedConflictDetector {
    protected Set processed;
    protected Set unprocessed;
	protected boolean hasUnresolvedConflict;
	protected String conflictMessage;

    public AbstractConflictDetectionOperation(String operationName, IResource []resources) {
        super(operationName, resources);
    }

    public AbstractConflictDetectionOperation(String operationName, IResourceProvider provider) {
        super(operationName, provider);
    }

    public boolean hasUnresolvedConflicts() {
        return this.hasUnresolvedConflict;
    }
    
    public String getMessage() {
    	return this.conflictMessage;
    }
    
    public IResource []getUnprocessed() {
		return this.unprocessed == null ? null : (IResource [])this.unprocessed.toArray(new IResource[this.unprocessed.size()]);
    }

	public IResource []getProcessed() {
		return this.processed == null ? null : (IResource [])this.processed.toArray(new IResource[this.processed.size()]);
	}
	
	protected void defineInitialResourceSet(IResource []resources) {
        this.hasUnresolvedConflict = false;
        this.unprocessed = new HashSet();
        this.processed = new HashSet();
		this.processed.addAll(Arrays.asList(resources));
	}
	
}
