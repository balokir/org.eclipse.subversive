/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import java.util.Collection;

import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.core.resource.SSHSettings;
import org.eclipse.team.svn.core.resource.SSLSettings;

/**
 * Allows to redefine the original location URL
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryLocationWrapper implements IRepositoryLocation {
	protected IRepositoryLocation location;
	protected String url;

	protected String repositoryRootUrl;
	protected String repositoryUUID;
	
	public SVNRepositoryLocationWrapper(IRepositoryLocation location, String mappedUrl) {
		this.location = location;
		this.url = mappedUrl;
	}
	
	public String asReference(boolean saveRevisionLinksComments) {
		return this.location.asReference(saveRevisionLinksComments);
	}
	
	public void fillLocationFromReference(String[] referenceParts) {
		this.location.fillLocationFromReference(referenceParts);
	}
	
	public String getUrlAsIs() {
		return this.url;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public IRepositoryContainer asRepositoryContainer(String url, boolean allowsNull) {
    	return SVNRepositoryLocation.asRepositoryContainer(this, url, allowsNull);
	}

	public IRepositoryFile asRepositoryFile(String url, boolean allowsNull) {
    	return SVNRepositoryLocation.asRepositoryFile(this, url, allowsNull);
	}

	public IRepositoryRoot getRepositoryRoot() {
		return new SVNRepositoryRoot(this);
	}

	public IRepositoryRoot getRoot() {
		return new SVNRepositoryLocationRoot(this);
	}

	public String getRepositoryRootUrl() {
		if (this.repositoryRootUrl == null) {
			this.fetchRepoInfo();
		}
		return this.repositoryRootUrl == null ? this.getUrl() : this.repositoryRootUrl;
	}

	public String getRepositoryUUID() {
		if (this.repositoryUUID == null) {
			this.fetchRepoInfo();
		}
		return this.repositoryUUID;
	}

	public ISVNConnector acquireSVNProxy() {
		return this.location.acquireSVNProxy();
	}

	public void addRealm(String realm, IRepositoryLocation location) {
		this.location.addRealm(realm, location);
	}

	public void addRevisionLink(IRevisionLink link) {
		this.location.addRevisionLink(link);
	}

	public void dispose() {
		this.location.dispose();
	}

	public String getBranchesLocation() {
		return this.location.getBranchesLocation();
	}

	public String getId() {
		return this.location.getId();
	}

	public String getLabel() {
		return this.location.getLabel();
	}

	public IRepositoryLocation getLocationForRealm(String realm) {
		return this.location.getLocationForRealm(realm);
	}

	public String getPassword() {
		return this.location.getPassword();
	}

	public Collection<IRepositoryLocation> getRealmLocations() {
		return this.location.getRealmLocations();
	}

	public Collection<String> getRealms() {
		return this.location.getRealms();
	}

	public IRevisionLink []getRevisionLinks() {
		return this.location.getRevisionLinks();
	}

	public SSHSettings getSSHSettings() {
		return this.location.getSSHSettings();
	}

	public SSLSettings getSSLSettings() {
		return this.location.getSSLSettings();
	}

	public String getTagsLocation() {
		return this.location.getTagsLocation();
	}

	public String getTrunkLocation() {
		return this.location.getTrunkLocation();
	}

	public String getUserInputBranches() {
		return this.location.getUserInputBranches();
	}

	public String getUserInputTags() {
		return this.location.getUserInputTags();
	}

	public String getUserInputTrunk() {
		return this.location.getUserInputTrunk();
	}

	public String getUsername() {
		return this.location.getUsername();
	}

	public boolean isPasswordSaved() {
		return this.location.isPasswordSaved();
	}

	public boolean isStructureEnabled() {
		return this.location.isStructureEnabled();
	}

	public void reconfigure() {
		this.location.reconfigure();
	}

	public void releaseSVNProxy(ISVNConnector proxy) {
		this.location.releaseSVNProxy(proxy);
	}

	public void removeRealm(String realm) {
		this.location.removeRealm(realm);
	}

	public void removeRevisionLink(IRevisionLink link) {
		this.location.removeRevisionLink(link);
	}

	public void setBranchesLocation(String location) {
		this.location.setBranchesLocation(location);
	}

	public void setLabel(String label) {
		this.location.setLabel(label);
	}

	public void setPassword(String password) {
		this.location.setPassword(password);
	}

	public void setPasswordSaved(boolean saved) {
		this.location.setPasswordSaved(saved);
	}

	public void setStructureEnabled(boolean enabled) {
		this.location.setStructureEnabled(enabled);
	}

	public void setTagsLocation(String location) {
		this.location.setTagsLocation(location);
	}

	public void setTrunkLocation(String location) {
		this.location.setTrunkLocation(location);
	}

	public void setUsername(String username) {
		this.location.setUsername(username);
	}

	public String getName() {
		return this.location.getName();
	}
	
	public boolean isAuthorNameEnabled() {
		return this.location.isAuthorNameEnabled();
	}
	
	public String getAuthorName() {
		return this.location.getAuthorName();
	}
	
	public void setAuthorNameEnabled(boolean isEnabled) {
		this.location.setAuthorNameEnabled(isEnabled);
	}
	
	public void setAuthorName(String name) {
		this.location.setAuthorName(name);
	}

	public Object getAdapter(Class adapter) {
		return this.location.getAdapter(adapter);
	}

	protected void fetchRepoInfo() {
		String []values = SVNRepositoryLocation.fetchRepoInfo(this, true);
		this.repositoryRootUrl = values[0];
		this.repositoryUUID = values[1];
	}

	public boolean isPasswordSavedForRealm(String realm) {
		return this.location.isPasswordSavedForRealm(realm);
	}
	
}
