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
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Abstract operation that implement functionality to detect unresolved conflicts in time of WC operations
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractConflictDetectionOperation extends AbstractWorkingCopyOperation implements IUnresolvedConflictDetector {

	protected UnresolvedConflictDetectorHelper conflictDetectorHelper;

    public AbstractConflictDetectionOperation(String operationName, Class<? extends NLS> messagesClass, IResource []resources) {
        super(operationName, messagesClass, resources);
        this.conflictDetectorHelper = new UnresolvedConflictDetectorHelper();
    }

    public AbstractConflictDetectionOperation(String operationName, Class<? extends NLS> messagesClass, IResourceProvider provider) {
        super(operationName, messagesClass, provider);
        this.conflictDetectorHelper = new UnresolvedConflictDetectorHelper();
    }
    
    public void setUnresolvedConflict(boolean hasUnresolvedConflict) {
		this.conflictDetectorHelper.setUnresolvedConflict(hasUnresolvedConflict);
	}	
    
    public boolean hasUnresolvedConflicts() {
        return this.conflictDetectorHelper.hasUnresolvedConflicts();
    }
    
    public String getMessage() {
    	return this.conflictDetectorHelper.getMessage();
    }
    
    public IResource []getUnprocessed() {
		return this.conflictDetectorHelper.getUnprocessed();
    }

	public IResource []getProcessed() {
		return this.conflictDetectorHelper.getProcessed();
	}
	
	public void defineInitialResourceSet(IResource []resources) {
		this.conflictDetectorHelper.defineInitialResourceSet(resources);
	}
	
	public void addUnprocessed(IResource unprocessed) {
		this.conflictDetectorHelper.addUnprocessed(unprocessed);
	}

	public void setConflictMessage(String message) {
		this.conflictDetectorHelper.setConflictMessage(message);		
	}
	
	 public void removeProcessed(IResource resource) {
		 this.conflictDetectorHelper.removeProcessed(resource);
	 }
	
}
