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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * JavaHL-mode merge implementation
 * 
 * @author Alexander Gurov
 */
public class JavaHLMergeOperation extends AbstractFileOperation {
	protected IRepositoryResource from1;
	protected IRepositoryResource from2;
	protected boolean dryRun;
	protected ISVNNotificationCallback notify;
	
	public JavaHLMergeOperation(File localTo, IRepositoryResource from1, IRepositoryResource from2, boolean dryRun, ISVNNotificationCallback notify) {
		super("Operation_JavaHLMergeFile", SVNMessages.class, new File[] {localTo}); //$NON-NLS-1$
		this.from1 = from1;
		this.from2 = from2;
		this.dryRun = dryRun;
		this.notify = notify;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
		IRepositoryLocation location = this.from1.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		proxy.setTouchUnresolved(true);
		if (this.notify != null) {
			SVNUtility.addSVNNotifyListener(proxy, this.notify);
		}
		try {
			proxy.merge(
				SVNUtility.getEntryRevisionReference(this.from1), SVNUtility.getEntryRevisionReference(this.from2),
				file.getAbsolutePath(), Depth.INFINITY, this.dryRun ? ISVNConnector.Options.SIMULATE : ISVNConnector.Options.NONE, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			if (this.notify != null) {
				SVNUtility.removeSVNNotifyListener(proxy, this.notify);
			}
			proxy.setTouchUnresolved(false);
			location.releaseSVNProxy(proxy);
		}
	}

}
