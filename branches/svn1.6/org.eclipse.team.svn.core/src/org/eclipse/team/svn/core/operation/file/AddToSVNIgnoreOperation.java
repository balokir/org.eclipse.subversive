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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * "Add to svn::ignore" operation implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNIgnoreOperation extends AbstractFileOperation {
	protected int ignoreType;
	protected String pattern;

	public AddToSVNIgnoreOperation(File []files, int ignoreType, String pattern) {
		super("Operation_AddToSVNIgnoreFile", files); //$NON-NLS-1$
		
		this.ignoreType = ignoreType;
		this.pattern = pattern;
	}

	public AddToSVNIgnoreOperation(IFileProvider provider, int ignoreType, String pattern) {
		super("Operation_AddToSVNIgnoreFile", provider); //$NON-NLS-1$
		
		this.ignoreType = ignoreType;
		this.pattern = pattern;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					AddToSVNIgnoreOperation.this.handleResource(current);
				}
			}, monitor, files.length);
		}
	}

	protected void handleResource(File current) throws Exception {
		File parent = current.getParentFile();
		if (parent == null) {
			return;
		}
		IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(parent, false);
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation.changeIgnoreProperty(proxy, this.ignoreType, this.pattern, parent.getAbsolutePath(), current.getName());
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
}
