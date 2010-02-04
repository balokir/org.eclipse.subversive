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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Fetch previously skipped revisions
 * 
 * @author Igor Burilo
 */
public class FetchSkippedRevisionsOperation extends BaseFetchOperation {
	
	public FetchSkippedRevisionsOperation(IRepositoryResource resource, CheckRepositoryConnectionOperation checkConnectionOp) {
		super("Fetch Skipped Revisions", resource, checkConnectionOp);	
	}

	@Override
	protected void prepareData(CacheMetadata metadata, IProgressMonitor monitor) throws Exception {		
		this.startRevision = metadata.getStartSkippedRevision();
		this.endRevision = metadata.getEndSkippedRevision();			
		this.canRun = this.startRevision != 0;							
	}

}
