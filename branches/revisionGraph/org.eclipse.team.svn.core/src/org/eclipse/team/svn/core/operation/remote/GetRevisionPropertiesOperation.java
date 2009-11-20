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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.property.IRevisionPropertiesProvider;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Get revision properties operation implementation
 * 
 * @author Alexei Goncharov
 */
public class GetRevisionPropertiesOperation extends AbstractActionOperation implements IRevisionPropertiesProvider {
	protected IRepositoryLocation location;
	protected SVNRevision revision;
	protected SVNProperty[] revProperties;

	/**
	 * Creates an instance of GetRevisionPropertiesOperation depending on the repository location.
	 * 
	 * @param location - repository location to get properties for
	 * @param revision - revision to get properties for
	 * 
	 * @author Alexei Goncharov
	 */
	public GetRevisionPropertiesOperation(IRepositoryLocation location, SVNRevision revision) {
		super("Operation_GetRevisionProperties"); //$NON-NLS-1$
		this.revision = revision;
		this.location = location;
	}
	
	public SVNProperty[] getRevisionProperties() {
		if (this.revProperties == null) {
			return new SVNProperty[0];
		}
		return this.revProperties;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNEntryRevisionReference reference = new SVNEntryRevisionReference(this.location.getUrl(), this.revision, this.revision);
		ISVNConnector proxy = this.location.acquireSVNProxy();
		try {
			this.revProperties = proxy.getRevisionProperties(reference, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			this.location.releaseSVNProxy(proxy);
		}
	}
}
