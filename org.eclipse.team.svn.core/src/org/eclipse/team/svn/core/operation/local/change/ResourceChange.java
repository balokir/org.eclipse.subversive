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

package org.eclipse.team.svn.core.operation.local.change;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.SVNProperty;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.FileUtility;


/**
 * Local resource change store
 * 
 * @author Alexander Gurov
 */
public abstract class ResourceChange {
    protected ILocalResource local;
    protected File tmp;
    protected SVNProperty []properties;
    
    public ResourceChange(ResourceChange parent, ILocalResource local, boolean needsTemporary) throws Exception {
        this.local = local;
        if (needsTemporary) {
        	this.tmp = File.createTempFile("merge", ".tmp", parent == null ? SVNTeamPlugin.instance().getStateLocation().toFile() : parent.getTemporary());
        	this.tmp.delete();
        }
		this.properties = null;
    }

    public ILocalResource getLocal() {
        return this.local;
    }
    
	public SVNProperty []getProperties() {
		return this.properties;
	}
	
	public void setProperties(SVNProperty []properties) {
		this.properties = properties;
	}
	
    public File getTemporary() {
        return this.tmp;
    }
    
    public void disposeChangeModel(IProgressMonitor monitor) throws Exception {
    	if (this.tmp != null) {
        	FileUtility.deleteRecursive(this.tmp, monitor);
    	}
    }
    
    public void traverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
    	this.preTraverse(visitor, depth, processor, monitor);
    	this.postTraverse(visitor, depth, processor, monitor);
    }
    
    protected abstract void preTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception;
    protected abstract void postTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception;
    
    public static ResourceChange wrapLocalResource(ResourceChange parent, ILocalResource local, boolean needsTemporary) throws Exception {
    	if (local == null) {
    		return null;
    	}
        return 
        	local instanceof ILocalFile ?
            new FileChange(parent, (ILocalFile)local, needsTemporary) :
            (ResourceChange)new FolderChange(parent, (ILocalFolder)local, needsTemporary);
    }
   
}
