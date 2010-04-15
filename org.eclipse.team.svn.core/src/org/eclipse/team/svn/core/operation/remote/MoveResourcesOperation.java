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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Move remote resources
 * 
 * @author Alexander Gurov
 */
public class MoveResourcesOperation extends AbstractCopyMoveResourcesOperation {
	public MoveResourcesOperation(IRepositoryResource destinationResource, IRepositoryResource[] selectedResources, String message, String name) {
		super("Operation_MoveRemote", SVNMessages.class, destinationResource, selectedResources, message, name); //$NON-NLS-1$
	}
	
	protected String[] getRevisionPaths(String srcUrl, String dstUrl) {
		return new String [] {srcUrl, dstUrl};
	}

	protected void runCopyMove(ISVNConnector proxy, SVNEntryRevisionReference[] source, String destinationUrl, IProgressMonitor monitor) throws Exception {
		//this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn move \"" + SVNUtility.decodeURL(sourceUrl) + "\" \"" + SVNUtility.decodeURL(destinationUrl) + "\" -m \"" + this.message + "\"" + FileUtility.getUsernameParam(current.getRepositoryLocation().getUsername()) + "\n");
		long options = ISVNConnector.CommandMasks.COPY_SERVER;
		if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() < ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x) {
			options &= ~ISVNConnector.Options.INTERPRET_AS_CHILD /*do not use SVN 1.5 options*/;
		}
		proxy.move(source, destinationUrl, this.message, options, null, new SVNProgressMonitor(this, monitor, null));
	}

}
