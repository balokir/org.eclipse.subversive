/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.IRevisionProvider.RevisionPair;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Set revision author name operation implementation
 * 
 * @author Alexei Goncharov
 */
public class SetRevisionAuthorNameOperation extends AbstractActionOperation {
	
	protected IRevisionProvider provider;
	protected long options;

	public SetRevisionAuthorNameOperation(final RevisionPair [] revisions, long options) {
		this(new IRevisionProvider() {
			public RevisionPair[] getRevisions() {
				return revisions;
			}
		}, options);
		
	}
	
	public SetRevisionAuthorNameOperation(IRevisionProvider provider, long options) {
		super("Operation_SetRevisionAuthorName", SVNMessages.class); //$NON-NLS-1$
		this.provider = provider;
		this.options = options;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final RevisionPair [] revisions = this.provider.getRevisions();
		if (revisions == null) {
			return;
		}
		for (int i = 0; i < revisions.length; i++) {
			if (revisions[i] == null) {
				continue;
			}
			final IRepositoryLocation location = revisions[i].location;
			if (!location.isAuthorNameEnabled()) {
				continue;
			}
			final ISVNConnector proxy =  location.acquireSVNProxy();
			final SVNRevision rev =  SVNRevision.fromNumber(revisions[i].revision);
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.setRevisionProperty(new SVNEntryReference(location.getUrl(), rev) , "svn:author", location.getAuthorName(), null, SetRevisionAuthorNameOperation.this.options, new SVNProgressMonitor(SetRevisionAuthorNameOperation.this, monitor, null)); //$NON-NLS-1$
				}
			}
			, monitor, 1);			
			location.releaseSVNProxy(proxy);
		}
	}

}
