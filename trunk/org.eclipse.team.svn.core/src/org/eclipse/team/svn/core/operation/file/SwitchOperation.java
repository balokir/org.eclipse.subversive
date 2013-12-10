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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNConflictDetectionProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * The operation switches working copy base url
 * 
 * @author Alexander Gurov
 */
public class SwitchOperation extends AbstractFileConflictDetectionOperation {
	protected IRepositoryResource destination;
	protected long options;
	
	public SwitchOperation(File file, IRepositoryResource destination, boolean ignoreExternals) {
		this(file, destination, ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE);
	}

	public SwitchOperation(File file, IRepositoryResource destination, long options) {
		super("Operation_SwitchFile", SVNMessages.class, new File[] {file}); //$NON-NLS-1$
		this.destination = destination;
		this.options = options & ISVNConnector.CommandMasks.SWITCH;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
		IRepositoryLocation location = this.destination.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch \"" + this.destination.getUrl() + "\" \"" + FileUtility.normalizePath(file.getAbsolutePath()) + "\" -r " + this.destination.getSelectedRevision() + SVNUtility.getIgnoreExternalsArg(this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			proxy.switchTo(file.getAbsolutePath(), SVNUtility.getEntryRevisionReference(this.destination), SVNDepth.infinityOrFiles(true), this.options, new ConflictDetectionProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected class ConflictDetectionProgressMonitor extends SVNConflictDetectionProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		protected void processConflict(ItemState state) {
			SwitchOperation.this.hasUnresolvedConflict = true;
		}
	}

}
