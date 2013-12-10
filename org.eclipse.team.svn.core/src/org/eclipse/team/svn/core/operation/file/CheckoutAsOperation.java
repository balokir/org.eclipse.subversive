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
	protected int depth;
	protected long options;
	protected boolean override;

	public CheckoutAsOperation(File to, IRepositoryResource resource, int depth, boolean ignoreExternals, boolean override) {
		this(to, resource, depth, ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE, override);
	}

	public CheckoutAsOperation(File to, IRepositoryResource resource, int depth, long options, boolean override) {
		super("Operation_CheckoutAsFile", SVNMessages.class, new File[] {to}); //$NON-NLS-1$
		this.resource = resource;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.CHECKOUT;
		this.override = override;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File to = this.operableData()[0];
		if (this.override) {
			File []children = to.listFiles();
			if (children != null) {
				for (File child : children) {
					FileUtility.deleteRecursive(child, monitor);
				}
			}
		}
		
		to.mkdirs();
		
		IRepositoryLocation location = this.resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			String path = to.getAbsolutePath();
			//this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn checkout \"" + this.resource.getUrl() + "@" + this.resource.getPegRevision() + "\" -r " + this.resource.getSelectedRevision() + (this.recursive ? "" : " -N") + " --ignore-externals \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.checkout(SVNUtility.getEntryRevisionReference(this.resource), path, this.depth, this.options, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
