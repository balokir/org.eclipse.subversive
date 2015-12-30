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
import org.eclipse.team.svn.core.connector.SVNDepth;
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
	protected long options;
	protected ISVNNotificationCallback notify;
	
	public JavaHLMergeOperation(File localTo, IRepositoryResource from1, IRepositoryResource from2, boolean dryRun, ISVNNotificationCallback notify) {
		this(localTo, from1, from2, dryRun ? ISVNConnector.Options.SIMULATE : ISVNConnector.Options.NONE, notify);
	}

	public JavaHLMergeOperation(File localTo, IRepositoryResource from1, IRepositoryResource from2, long options, ISVNNotificationCallback notify) {
		super("Operation_JavaHLMergeFile", SVNMessages.class, new File[] {localTo}); //$NON-NLS-1$
		this.from1 = from1;
		this.from2 = from2;
		this.options = options & ISVNConnector.CommandMasks.MERGE;
		this.notify = notify;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
		IRepositoryLocation location = this.from1.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		
		if (this.notify != null) {
			SVNUtility.addSVNNotifyListener(proxy, this.notify);
		}
		try {
			proxy.mergeTwo(
				SVNUtility.getEntryRevisionReference(this.from1), SVNUtility.getEntryRevisionReference(this.from2),
				file.getAbsolutePath(), SVNDepth.INFINITY, this.options, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			if (this.notify != null) {
				SVNUtility.removeSVNNotifyListener(proxy, this.notify);
			}
			location.releaseSVNProxy(proxy);
		}
	}

}
