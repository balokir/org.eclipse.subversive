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
 * Fetch revisions after last processed revision
 * 
 * @author Igor Burilo
 */
public class FetchNewRevisionsOperation extends BaseFetchOperation {
	
	public FetchNewRevisionsOperation(IRepositoryResource resource, CheckRepositoryConnectionOperation checkConnectionOp) {
		super("Fetch New Revisions", resource, checkConnectionOp);
	}

	@Override
	protected void prepareData(CacheMetadata metadata, IProgressMonitor monitor) throws Exception {
		this.startRevision = metadata.getLastProcessedRevision() + 1;
		this.endRevision = this.checkConnectionOp.getLastRepositoryRevision();
		
		this.canRun = this.checkConnectionOp.getLastRepositoryRevision() > metadata.getLastProcessedRevision();
		if (this.canRun) {
			metadata.setSkippedRevisions(this.startRevision, this.endRevision);
			metadata.setLastProcessedRevision(this.endRevision);
			metadata.save();	
		}
	}
		
}
