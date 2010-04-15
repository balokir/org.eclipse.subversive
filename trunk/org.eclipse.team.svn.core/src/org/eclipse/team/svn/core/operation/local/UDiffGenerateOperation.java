/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Generate file in unified diff format operation
 * 
 * @author Igor Burilo
 */
public class UDiffGenerateOperation extends AbstractActionOperation {

	protected ILocalResource local;
	protected IRepositoryResource remote;
	protected String diffFile;
	
	public UDiffGenerateOperation(ILocalResource local, IRepositoryResource remote, String diffFile) {
		super("Operation_UDiffGenerate", SVNMessages.class); //$NON-NLS-1$
		this.local = local;
		this.remote = remote;
		this.diffFile = diffFile;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.local.getResource());
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String wcPath = FileUtility.getWorkingCopyPath(this.local.getResource());
			SVNEntryRevisionReference refPrev = new SVNEntryRevisionReference(wcPath, null, SVNRevision.WORKING);			
			SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(this.remote);					
			String outFileName = this.diffFile;			
						
			String projectPath = FileUtility.getWorkingCopyPath(this.local.getResource().getProject());
			String relativeToDir = projectPath;
								
			int depth = Depth.INFINITY;							
			long options = ISVNConnector.Options.NONE;
			//ISVNConnector.Options.IGNORE_ANCESTRY;					
			String[] changelistNames = new String[0];
								
			this.writeToConsole(
					IConsoleStream.LEVEL_CMD, "svn diff -r " //$NON-NLS-1$
					+ refNext.revision
					+ " \"" + wcPath + "\"" //$NON-NLS-1$ //$NON-NLS-2$
					//+ "@" + refNext.pegRevision + "\""
					+ FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$
			
			proxy.diff(refPrev, refNext, relativeToDir, outFileName, depth, options, changelistNames, new SVNProgressMonitor(this, monitor, null));			
		} finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
