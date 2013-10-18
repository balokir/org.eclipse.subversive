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

package org.eclipse.team.svn.ui.operation;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Treat replacements as edits operation implementation
 * 
 * @author Alexander Gurov
 */
public class TreatAsEditsOperation extends AbstractWorkingCopyOperation {
	public TreatAsEditsOperation(IResource[] resources) {
		super("Operation_TreatAsEdits", SVNUIMessages.class, resources); //$NON-NLS-1$
	}

	public TreatAsEditsOperation(IResourceProvider provider) {
		super("Operation_TreatAsEdits", SVNUIMessages.class, provider); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			if (resources[i].getType() == IResource.FILE) {
				IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resources[i]);
				final File originalFile = new File(FileUtility.getWorkingCopyPath(resources[i]));
				File tmpFile = new File(originalFile.getAbsolutePath() + ".svntmp"); //$NON-NLS-1$
				originalFile.renameTo(tmpFile);
				final ISVNConnector proxy = location.acquireSVNProxy();
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						proxy.revert(originalFile.getAbsolutePath(), Depth.EMPTY, null, new SVNProgressMonitor(TreatAsEditsOperation.this, monitor, null));
					}
				}, monitor, resources.length);
				location.releaseSVNProxy(proxy);
				originalFile.delete();
				tmpFile.renameTo(originalFile);
			}
		}
	}

}
