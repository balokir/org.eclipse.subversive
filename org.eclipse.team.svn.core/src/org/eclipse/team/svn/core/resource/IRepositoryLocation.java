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

package org.eclipse.team.svn.core.resource;

import java.util.Collection;

import org.eclipse.team.svn.core.connector.ISVNConnector;


/**
 * Repository location
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryLocation extends IRepositoryBase, IRepositoryResourceFactory {
	
	public String getId();
	
	public void setLabel(String label);
	public String getLabel();

	/*
	 * As in some cases there can be limitations on reference string size, e.g.
	 * storing reference string in project set file, 
	 * storing as Eclipse persistent property(which is limited be Eclipse),
	 * we provide an option to not save revision link comments
	 */
	public String asReference(boolean saveRevisionLinksComments);
	public void fillLocationFromReference(String [] referenceParts);
	
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
	
	public IRevisionLink []getRevisionLinks();
	public void addRevisionLink(IRevisionLink link);
	public void removeRevisionLink(IRevisionLink link);

	public String getUsername();
	public void setUsername(String username);
	public String getPassword();
	public void setPassword(String password);
	
	public boolean isPasswordSavedForRealm(String realm);
	public boolean isPasswordSaved();
	public void setPasswordSaved(boolean saved);
	
	public ISVNConnector acquireSVNProxy();
	public void releaseSVNProxy(ISVNConnector proxy);
	public void reconfigure();
	public void dispose();
	
	public SSLSettings getSSLSettings();
	public SSHSettings getSSHSettings();
	
	public Collection<String> getRealms();
	public void addRealm(String realm, IRepositoryLocation location);
	public void removeRealm(String realm);
	public Collection<IRepositoryLocation> getRealmLocations();
	public IRepositoryLocation getLocationForRealm(String realm);
	
	public boolean isAuthorNameEnabled();
	public String getAuthorName();
	
	public void setAuthorNameEnabled(boolean isEnabled);
	public void setAuthorName(String name);
}
