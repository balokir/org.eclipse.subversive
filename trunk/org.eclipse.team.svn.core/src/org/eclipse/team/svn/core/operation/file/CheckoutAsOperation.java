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
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Checkout specified resource from repository
 * 
 * @author Alexander Gurov
 */
public class CheckoutAsOperation extends AbstractFileOperation {
	protected IRepositoryResource resource;
	protected boolean recursive;
	protected boolean ignoreExternals;
	protected boolean override;

	public CheckoutAsOperation(File to, IRepositoryResource resource, boolean recursive, boolean ignoreExternals, boolean override) {
		super("Operation.CheckoutAsFile", new File[] {to});
		this.resource = resource;
		this.recursive = recursive;
		this.ignoreExternals = ignoreExternals;
		this.override = override;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File to = this.operableData()[0];
		if (this.override) {
			FileUtility.deleteRecursive(to, monitor);
		}
		
		to.mkdirs();
		
		IRepositoryLocation location = this.resource.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			String path = to.getAbsolutePath();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn checkout \"" + this.resource.getUrl() + "@" + this.resource.getPegRevision() + "\" -r " + this.resource.getSelectedRevision() + (this.recursive ? "" : " -N") + " --ignore-externals \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.checkout(
					SVNUtility.encodeURL(this.resource.getUrl()), 
					path, 
					this.resource.getSelectedRevision(), 
					this.resource.getPegRevision(), 
					this.recursive,
					this.ignoreExternals, 
					new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
