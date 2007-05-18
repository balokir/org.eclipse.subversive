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
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Get all children for the specified folder (including non-committed deletions)
 * 
 * @author Alexander Gurov
 */
public class GetAllFilesOperation extends AbstractFileOperation implements IFileProvider {
	protected File []children;
	
	public GetAllFilesOperation(File file) {
		super("Operation.GetAllFiles", new File[] {file});
	}

	public File []getFiles() {
		return this.children;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.children = new File[0];
		
		File file = this.operableData()[0];
		if (!file.exists() || file.isFile()) {
			return;
		}
		
		HashSet allFiles = new HashSet();
		
		File []children = file.listFiles(new FileFilter() {
			public boolean accept(File arg0) {
				return !arg0.getName().equals(SVNUtility.getSVNFolderName());
			}
		});
		if (children != null) {
			allFiles.addAll(Arrays.asList(children));
		}
		
		IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, true);
		if (remote != null) {
			IRepositoryLocation location = remote.getRepositoryLocation();
			ISVNClientWrapper proxy = location.acquireSVNProxy();
			try {
				Status []statuses = proxy.status(file.getAbsolutePath(), false, false, true, true, false, false, new SVNProgressMonitor(this, monitor, null));
				if (statuses != null) {
					for (int i = 0; i < statuses.length; i++) {
						allFiles.add(new File(statuses[i].path));
					}
				}
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
		}
		
		this.children = (File [])allFiles.toArray(new File[allFiles.size()]);
	}

}
