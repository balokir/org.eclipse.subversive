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

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation.LocationReferenceTypeEnum;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Default handler for project set
 * 
 * @author Igor Burilo
 */
public class DefaultProjectSetHandler implements IProjectSetHandler {
	
	protected static final String PLUGIN_INFORMATION = "1.0.1"; //$NON-NLS-1$
	
	public String getProjectNameForReference(String fullReference) {
		String []parts = fullReference.split(","); //$NON-NLS-1$
		if (parts.length < 3 || !(parts[0].equals(DefaultProjectSetHandler.PLUGIN_INFORMATION))) {
			return null;
		}
		return parts[2];
	}
	
	public String asReference(IProject project) throws TeamException {
		IRepositoryResource resource = SVNRemoteStorage.instance().asRepositoryResource(project);
		IRepositoryLocation location = resource.getRepositoryLocation();
		
		// 1) save plugin information
		// 2) save URL
		// 3) save project name
		// non-mandatory part
		// 4) save repository location
		String fullReference = DefaultProjectSetHandler.PLUGIN_INFORMATION;
		fullReference += "," + resource.getUrl(); //$NON-NLS-1$
		fullReference += "," + project.getName(); //$NON-NLS-1$
		
		fullReference += "," + SVNRemoteStorage.instance().repositoryLocationAsReference(location, LocationReferenceTypeEnum.WITHOUT_REVISION_COMMENTS); //$NON-NLS-1$
		
		return fullReference;
	}
	
	public IProject configureCheckoutOperation(CompositeOperation op, IProject project, String fullReference) throws TeamException {
		String []parts = fullReference.split(","); //$NON-NLS-1$
		
		IRepositoryLocation location = this.getLocationForReference(parts);
		IRepositoryResource resource = location.asRepositoryContainer(parts[1], true);

		if (resource != null) {
			String projectLocation = 
				project.exists() ? 
				FileUtility.getResourcePath(project).removeLastSegments(1).toString() : 
				Platform.getLocation().toString();
			CheckoutAsOperation mainOp = new CheckoutAsOperation(project.getName(), resource, projectLocation, SVNDepth.INFINITY, false);
			op.add(mainOp);
			return mainOp.getProject();
		}
		return null;
	}
	
	protected IRepositoryLocation getLocationForReference(String []parts) {
		IRepositoryLocation location = null;
		if (parts.length > 3) {
			location = SVNRemoteStorage.instance().newRepositoryLocation(parts[3]);
			if (SVNRemoteStorage.instance().getRepositoryLocation(location.getId()) != null) {
				return location;
			}
		}
		IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
		IPath awaitingFor = SVNUtility.createPathForSVNUrl(location != null ? location.getUrl() : parts[1]);
		for (int i = 0; i < locations.length; i++) {
			if (SVNUtility.createPathForSVNUrl(locations[i].getUrl()).isPrefixOf(awaitingFor)) {
				return locations[i];
			}
		}
		if (location == null) {
			location = SVNRemoteStorage.instance().newRepositoryLocation();
			location.setUrl(parts[1]);
		}
		SVNRemoteStorage.instance().addRepositoryLocation(location);
		return location;
	}

	public boolean accept(String referenceString) {
		return referenceString.startsWith(DefaultProjectSetHandler.PLUGIN_INFORMATION);
	}

}
