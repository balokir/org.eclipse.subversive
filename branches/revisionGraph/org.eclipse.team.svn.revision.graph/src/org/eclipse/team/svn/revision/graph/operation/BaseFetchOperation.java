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
package org.eclipse.team.svn.revision.graph.operation;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * 
 * @author Igor Burilo
 */
public abstract class BaseFetchOperation extends AbstractActionOperation {

	protected IRepositoryResource resource;
	protected CheckRepositoryConnectionOperation checkConnectionOp;
	
	//should be filled by derived classes
	protected boolean canRun;
	protected long startRevision;
	protected long endRevision;	
	
	public BaseFetchOperation(String operationName, IRepositoryResource resource, CheckRepositoryConnectionOperation checkConnectionOp) {
		super(operationName);
		this.resource = resource;
		this.checkConnectionOp = checkConnectionOp;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {				
		if (this.checkConnectionOp.hasConnection()) {
			File cacheFolder = RevisionGraphUtility.getCacheFolder(this.resource);
			CacheMetadata metadata = new CacheMetadata(cacheFolder);
			metadata.load();
			
			this.prepareData(metadata, monitor);
			if (this.canRun) {
				LogEntriesCallback callback = new LogEntriesCallback(this, monitor, (int) (this.endRevision - this.startRevision + 1), cacheFolder, metadata);
				
				ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
				try {
					proxy.logEntries(
						SVNUtility.getEntryReference(this.resource.getRepositoryLocation().getRepositoryRoot()),								
						SVNRevision.fromNumber(this.endRevision),
						SVNRevision.fromNumber(this.startRevision),
						ISVNConnector.DEFAULT_LOG_ENTRY_PROPS,
						0,
						Options.DISCOVER_PATHS/*TODO | Options.INCLUDE_MERGED_REVISIONS*/,
						callback,
						new SVNProgressMonitor(this, monitor, null));	
				} finally {
					this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
					
					callback.dispose();
				} 		
			}						
		}		
	}
	
	protected abstract void prepareData(CacheMetadata metadata, IProgressMonitor monitor) throws Exception;

}
