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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * The operation switches working copy base url
 * 
 * @author Alexander Gurov
 */
public class SwitchOperation extends AbstractFileOperation {
	protected IRepositoryResource destination;
	
	public SwitchOperation(File file, IRepositoryResource destination) {
		super("Operation.SwitchFile", new File[] {file});
		this.destination = destination;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
		IRepositoryLocation location = this.destination.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch \"" + this.destination.getUrl() + "\" \"" + FileUtility.normalizePath(file.getAbsolutePath()) + "\" -r " + this.destination.getSelectedRevision() + FileUtility.getUsernameParam(location.getUsername()) + "\n");
		try {
			proxy.doSwitch(file.getAbsolutePath(), SVNUtility.getEntryReference(this.destination), Depth.unknownOrFiles(true), false, false, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
