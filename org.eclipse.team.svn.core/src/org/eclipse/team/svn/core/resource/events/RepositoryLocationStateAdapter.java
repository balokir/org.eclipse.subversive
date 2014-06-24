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

package org.eclipse.team.svn.core.resource.events;

import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRevisionLink;

/**
 * Repository location change listener
 * 
 * @author Alexander Gurov
 */
public class RepositoryLocationStateAdapter
	implements IRepositoryLocationStateListener
{
	public void sshChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue) {
		
	}

	public void sslChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue) {
		
	}

	public void changed(IRepositoryLocation where, String field, Object oldValue, Object newValue) {
		
	}

	public void realmAdded(IRepositoryLocation where, String realm, IRepositoryLocation location) {
		
	}

	public void realmRemoved(IRepositoryLocation where, String realm) {
		
	}

	public void revisionLinkAdded(IRepositoryLocation where, IRevisionLink link) {
		
	}

	public void revisionLinkRemoved(IRepositoryLocation where, IRevisionLink link) {
		
	}

	public void proxyAcquired(IRepositoryLocation where, ISVNConnector proxy) {
		
	}

	public void proxyDisposed(IRepositoryLocation where, ISVNConnector proxy) {
		
	}
}
