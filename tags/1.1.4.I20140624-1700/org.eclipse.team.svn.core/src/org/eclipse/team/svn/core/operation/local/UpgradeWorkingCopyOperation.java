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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Revert operation implementation
 * 
 * @author Alexander Gurov
 */
public class UpgradeWorkingCopyOperation extends AbstractWorkingCopyOperation {

	public UpgradeWorkingCopyOperation(IResource []resources) {
		super("Operation_Upgrade", SVNMessages.class, resources); //$NON-NLS-1$
	}

	public UpgradeWorkingCopyOperation(IResourceProvider provider) {
		super("Operation_Upgrade", SVNMessages.class, provider); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
	    resources = FileUtility.shrinkChildNodes(resources);

		final ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().newInstance();
		try {
			for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
				final String wcPath = FileUtility.getWorkingCopyPath(resources[i]);
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn upgrade \"" + FileUtility.normalizePath(wcPath) + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						proxy.upgrade(wcPath, new SVNProgressMonitor(UpgradeWorkingCopyOperation.this, monitor, null));
					}
				}, monitor, resources.length);
			}
		}
		finally {
			proxy.dispose();
		}
	}

}
