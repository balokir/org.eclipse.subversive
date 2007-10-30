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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract operation that implement functionality to detect unresolved conflicts in time of WC operations
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractFileConflictDetectionOperation extends AbstractFileOperation implements IUnresolvedConflictDetector {
    protected Set processed;
    protected Set unprocessed;
	protected boolean hasUnresolvedConflict;
	protected String conflictMessage;

	public AbstractFileConflictDetectionOperation(String operationName, File[] files) {
		super(operationName, files);
	}

	public AbstractFileConflictDetectionOperation(String operationName, IFileProvider provider) {
		super(operationName, provider);
	}

    public boolean hasUnresolvedConflicts() {
        return this.hasUnresolvedConflict;
    }
    
    public String getMessage() {
    	return this.conflictMessage;
    }
    
	public File []getProcessed() {
		return this.processed == null ? null : (File [])this.processed.toArray(new File[this.processed.size()]);
	}

	public File []getUnprocessed() {
		return this.unprocessed == null ? null : (File [])this.unprocessed.toArray(new File[this.unprocessed.size()]);
	}

	protected void defineInitialResourceSet(File []resources) {
        this.hasUnresolvedConflict = false;
        this.unprocessed = new HashSet();
        this.processed = new HashSet();
		this.processed.addAll(Arrays.asList(resources));
	}
	
}
