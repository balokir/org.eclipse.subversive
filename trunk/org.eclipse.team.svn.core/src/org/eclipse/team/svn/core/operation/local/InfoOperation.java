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
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation provide Info2 information for local resource
 * 
 * @author Alexander Gurov
 */
public class InfoOperation extends AbstractActionOperation {
    protected IResource resource;
    protected ILocalResource local;
    protected SVNEntryInfo info;

    public InfoOperation(IResource resource) {
        super("Operation_Info", SVNMessages.class); //$NON-NLS-1$
        this.resource = resource;
    }

    public SVNEntryInfo getInfo() {
        return this.info;
    }

    public ILocalResource getLocal() {
        return this.local;
    }

    protected void runImpl(IProgressMonitor monitor) throws Exception {
        this.info = null;
        this.local = SVNRemoteStorage.instance().asLocalResourceAccessible(this.resource);
        
        if (IStateFilter.SF_ONREPOSITORY.accept(this.local)) {
            IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.resource);
            ISVNConnector proxy = location.acquireSVNProxy();
            try {
//    			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn info \"" + this.local.getWorkingCopyPath() + "\"\n");
            	String path = FileUtility.getWorkingCopyPath(this.resource);
                SVNEntryInfo []infos = SVNUtility.info(proxy, new SVNEntryRevisionReference(path), Depth.EMPTY, new SVNProgressMonitor(this, monitor, null));
                if (infos != null && infos.length > 0) {
                    this.info = infos[0];
                }	
            }
            finally {
                location.releaseSVNProxy(proxy);
            }
        }
    }
    
    protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.resource.getName()});
	}

}
