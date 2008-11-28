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

package org.eclipse.team.svn.core.operation.file.management;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Disconnect files from source control by SVN meta-information deletion. Only folders could be disconnected.
 * 
 * @author Alexander Gurov
 */
public class DisconnectOperation extends AbstractFileOperation {
	public DisconnectOperation(File []files) {
		super("Operation_DisconnectFile", files); //$NON-NLS-1$
	}

	public DisconnectOperation(IFileProvider provider) {
		super("Operation_DisconnectFile", provider); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.disconnect(this.operableData(), monitor);
	}

	protected void disconnect(File []files, IProgressMonitor monitor) {
		files = FileUtility.shrinkChildNodes(files, true);
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			File meta = new File(files[i].getAbsolutePath() + "/" + SVNUtility.getSVNFolderName()); //$NON-NLS-1$
			FileUtility.deleteRecursive(meta, monitor);
			if (files[i].isDirectory()) {
				this.disconnect(files[i].listFiles(), monitor);
			}
		}
	}
	
}
