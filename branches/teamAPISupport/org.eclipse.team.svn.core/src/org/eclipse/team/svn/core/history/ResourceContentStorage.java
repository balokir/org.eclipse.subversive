/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.history;

import java.io.InputStream;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Resource content storage
 * 
 * @author Alexander Gurov
 */
public class ResourceContentStorage implements IEncodedStorage {
    protected AbstractGetFileContentOperation op;
    protected String charSet;
	protected IRepositoryResource remote;
	
	public ResourceContentStorage(IRepositoryResource remote) {
		this.remote = remote;
	}
    
	public InputStream getContents() {
		this.fetchContents(null);
		return this.op.getContent();
	}

	public synchronized void fetchContents(IProgressMonitor monitor) {
	    if (this.op == null) {
			this.op = this.getLoadContentOperation();
	        CompositeOperation composite = new CompositeOperation(this.op.getId());
	        composite.add(this.op);
	        composite.add(new AbstractActionOperation("Operation.DetectCharset") {
                protected void runImpl(IProgressMonitor monitor) throws Exception {
                    ResourceContentStorage.this.detectCharset(ResourceContentStorage.this.op.getContent());
                }
	        }, new IActionOperation[] {this.op});
	        if (monitor == null) {
				monitor = new NullProgressMonitor();
	        }
        	ProgressMonitorUtility.doTaskExternalDefault(composite, monitor);
	    }
	}

	public IPath getFullPath() {
		return new Path(this.remote.getUrl());
	}
	
	public IPath getTemporaryPath() {
		this.fetchContents(null);
		return new Path(this.op.getTemporaryPath());
	}

	public String getName() {
		return this.remote.getName();
	}

	public boolean isReadOnly() {
		return true;
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public String getCharset() {
	    return this.charSet;
	}
	
	protected AbstractGetFileContentOperation getLoadContentOperation() {
	    return new GetFileContentOperation(this.remote);
	}
	
	protected void detectCharset(InputStream stream) throws Exception {
		try {
			IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(stream, this.getName(), IContentDescription.ALL);
            this.charSet = description == null ? null : description.getCharset();
		} 
		finally {
			try {stream.close();} catch (Exception ex) {}
		}
	}
	
}
