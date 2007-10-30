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

package org.eclipse.team.svn.ui.annotate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.history.ResourceContentStorage;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.history.RepositoryFileEditorInput;

/**
 * Annotate view editor input
 * 
 * @author Alexander Gurov
 */
public class AnnotateEditorInput extends RepositoryFileEditorInput {
	protected byte []data;

	public AnnotateEditorInput(IRepositoryFile resource, byte []data) {
		super(resource);
		this.data = data;
	}
	
	public IStorage getStorage() {
		return new AnnotateStorage(this.getRepositoryResource());
	}

	protected class AnnotateStorage extends ResourceContentStorage {
		public AnnotateStorage(IRepositoryResource remote) {
			super(remote);
		}

		protected AbstractGetFileContentOperation getLoadContentOperation() {
		    return new AbstractGetFileContentOperation("Operation.Annotate") {
                public InputStream getContent() {
        			return new ByteArrayInputStream(AnnotateEditorInput.this.data);
                }
                protected void runImpl(IProgressMonitor monitor) throws Exception {
                }
            };
		}
	}
	
}
