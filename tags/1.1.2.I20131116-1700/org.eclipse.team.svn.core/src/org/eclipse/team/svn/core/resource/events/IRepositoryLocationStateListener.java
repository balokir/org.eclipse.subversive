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
public interface IRepositoryLocationStateListener
	extends ISSHSettingsStateListener, ISSLSettingsStateListener
{
	public final String LABEL = "label";
	public final String URL = "url";
	public final String STRUCTURE_ENABLED = "structureEnabled";
	public final String TRUNK_LOCATION = "trunkLocation";
	public final String BRANCHES_LOCATION = "branchesLocation";
	public final String TAGS_LOCATION = "tagsLocation";
	public final String USERNAME = "username";
	public final String PASSWORD = "password";
	public final String PASSWORD_SAVED = "passwordSaved";
	public final String AUTHOR_NAME = "authorName";
	public final String AUTHOR_NAME_ENABLED = "authorNameEnabled";

	public void changed(IRepositoryLocation where, String field, Object oldValue, Object newValue);
	public void realmAdded(IRepositoryLocation where, String realm, IRepositoryLocation location);
	public void realmRemoved(IRepositoryLocation where, String realm);
	public void revisionLinkAdded(IRepositoryLocation where, IRevisionLink link);
	public void revisionLinkRemoved(IRepositoryLocation where, IRevisionLink link);
	public void proxyAcquired(IRepositoryLocation where, ISVNConnector proxy);
	public void proxyDisposed(IRepositoryLocation where, ISVNConnector proxy);
}
