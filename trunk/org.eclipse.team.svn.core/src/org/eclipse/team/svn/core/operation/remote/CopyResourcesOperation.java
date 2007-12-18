/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Copy remote resources
 * 
 * @author Alexander Gurov
 */
public class CopyResourcesOperation extends AbstractCopyMoveResourcesOperation {
	public CopyResourcesOperation(IRepositoryResource destinationResource, IRepositoryResource[] selectedResources, String message) {
		super("Operation.CopyRemote", destinationResource, selectedResources, message);
	}

	protected String []getRevisionPaths(String srcUrl, String dstUrl) {
		return new String [] {dstUrl};
	}

	protected void processEntry(ISVNConnector proxy, String sourceUrl, String destinationUrl, IRepositoryResource current, IProgressMonitor monitor) throws Exception {
		this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + SVNUtility.decodeURL(sourceUrl) + "\" \"" + SVNUtility.decodeURL(destinationUrl) + "\" -r " + current.getSelectedRevision() + " -m \"" + this.message + "\"" + FileUtility.getUsernameParam(current.getRepositoryLocation().getUsername()) + "\n");
		SVNEntryRevisionReference []src = new SVNEntryRevisionReference[] {new SVNEntryRevisionReference(sourceUrl, current.getPegRevision(), current.getSelectedRevision())};
		proxy.copy(src, destinationUrl, this.message, true, false, new SVNProgressMonitor(this, monitor, null));
	}
	
}
