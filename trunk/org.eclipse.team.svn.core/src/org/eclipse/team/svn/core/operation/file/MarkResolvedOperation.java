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
import org.eclipse.team.svn.core.connector.SVNConflictResolution;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Mark conflicts as resolved
 * 
 * @author Alexander Gurov
 */
public class MarkResolvedOperation extends AbstractFileOperation {
	protected boolean recursive;
	
	public MarkResolvedOperation(File []files, boolean recursive) {
		super("Operation.MarkResolvedFile", files);
		this.recursive = recursive;
	}

	public MarkResolvedOperation(IFileProvider provider, boolean recursive) {
		super("Operation.MarkResolvedFile", provider);
		this.recursive = recursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		
		if (this.recursive) {
			files = FileUtility.shrinkChildNodes(files, false);
		}
		else {
			FileUtility.reorder(files, true);
		}
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			
			IRepositoryLocation location = SVNFileStorage.instance().asRepositoryResource(current, false).getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.resolved(current.getAbsolutePath(), SVNConflictResolution.CHOOSE_MERGED, Depth.infinityOrEmpty(MarkResolvedOperation.this.recursive), new SVNProgressMonitor(MarkResolvedOperation.this, monitor, null));
				}
			}, monitor, files.length);
			
			location.releaseSVNProxy(proxy);
		}
	}

}
