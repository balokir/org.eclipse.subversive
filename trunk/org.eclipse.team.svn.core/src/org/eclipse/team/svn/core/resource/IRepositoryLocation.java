/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import java.util.Collection;

import org.eclipse.team.svn.core.client.ISVNConnector;


/**
 * Repository location
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryLocation extends IRepositoryBase, IRepositoryResourceFactory {
	
	public String getId();
	
	public void setLabel(String label);
	public String getLabel();

	public String getUrlAsIs();
	public void setUrl(String url);
	public String getRepositoryRootUrl();
	public String getRepositoryUUID();
	public IRepositoryRoot getRepositoryRoot();
	public IRepositoryRoot getRoot();

	public void setStructureEnabled(boolean enabled);
	public boolean isStructureEnabled();

	public void setTrunkLocation(String location);
	public String getTrunkLocation();
	public String getUserInputTrunk();
	
	public String getBranchesLocation();
	public void setBranchesLocation(String location);
	public String getUserInputBranches();
	
	public String getTagsLocation();
	public void setTagsLocation(String location);
	public String getUserInputTags();
	
	public IRepositoryResource []getRevisionLinks();
	public void addRevisionLink(IRepositoryResource link);
	public void removeRevisionLink(IRepositoryResource link);

	public String getUsername();
	public void setUsername(String username);
	public String getPassword();
	public void setPassword(String password);
	
	public boolean isPasswordSaved();
	public void setPasswordSaved(boolean saved);
	
	public ISVNConnector acquireSVNProxy();
	public void releaseSVNProxy(ISVNConnector proxy);
	public void reconfigure();
	public void dispose();
	
	public ProxySettings getProxySettings();
	public SSLSettings getSSLSettings();
	public SSHSettings getSSHSettings();
	
	public Collection getRealms();
	public void addRealm(String realm, IRepositoryLocation location);
	public void removeRealm(String realm);
	public Collection getRealmLocations();
	public IRepositoryLocation getLocationForRealm(String realm);
}
