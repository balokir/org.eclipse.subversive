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
import org.eclipse.team.svn.core.resource.events.IRepositoryLocationStateListener;

/**
 * Repository location
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryLocation extends IRepositoryBase, IRepositoryResourceFactory {
	
	/**
	 * Detect what information should be present while serializing location:
	 * 
	 * ALL - save all data.
	 * 	For example, it's used for storing repository locations between Eclipse sessions. 
	 * 
	 * WITHOUT_REVISION_COMMENTS - it's used for storing all info except revision link comments.
	 * 	For example, it's used for storing locations in project set files, as
	 * 	comment size can be very big.
	 * 
	 * ONLY_REQUIRED_DATA	- it's stored only necessary data required for restoring
	 * 	location. For example, it's used by setting location property for project;
	 *  we make location size as small as possible because project's property
	 *  size is limited by Eclipse.
	 */
	public static enum  LocationReferenceTypeEnum {
		ALL,	
		WITHOUT_REVISION_COMMENTS,
		ONLY_REQUIRED_DATA		
	}
	
    public void addStateListener(IRepositoryLocationStateListener listener);
    public void removeStateListener(IRepositoryLocationStateListener listener);
	
	public String getId();
	
	public void setLabel(String label);
	public String getLabel();
		
	/*
	 * As in some cases there can be limitations on reference string size or some
	 * information is not needed, then we provide a parameter which says what
	 * information should be saved.
	 * For more details, see LocationReferenceTypeEnum
	 */
	public String asReference(LocationReferenceTypeEnum locationReferenceType);
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
