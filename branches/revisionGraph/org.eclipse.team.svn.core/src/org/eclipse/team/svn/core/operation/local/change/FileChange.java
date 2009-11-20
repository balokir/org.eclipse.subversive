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

package org.eclipse.team.svn.core.operation.local.change;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalFile;

/**
 * Local file change store
 * 
 * @author Alexander Gurov
 */
public class FileChange extends ResourceChange {
    public FileChange(ResourceChange parent, ILocalFile local, boolean needsTemporary) throws Exception {
        super(parent, local, needsTemporary);
    }

    protected void preTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
    	visitor.preVisit(this, processor, monitor);
    }
    
    protected void postTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
    	visitor.postVisit(this, processor, monitor);
    }
    
}
