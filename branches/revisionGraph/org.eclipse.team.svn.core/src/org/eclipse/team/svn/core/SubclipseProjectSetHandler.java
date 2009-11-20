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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Subclipse handler for project set
 * 
 * @author Igor Burilo
 */
public class SubclipseProjectSetHandler implements IProjectSetHandler {

	protected static final String PLUGIN_INFORMATION = "0.9.3"; //$NON-NLS-1$
	
	public String getProjectNameForReference(String fullReference) {
		String []parts = fullReference.split(","); //$NON-NLS-1$
		if (parts.length < 3 || !parts[0].equals(SubclipseProjectSetHandler.PLUGIN_INFORMATION)) {
			return null;
		}
		return parts[2];
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
			CheckoutAsOperation mainOp = new CheckoutAsOperation(project.getName(), resource, projectLocation, Depth.INFINITY, false);
			op.add(mainOp);
			return mainOp.getProject();
		}
		return null;
	}

	protected IRepositoryLocation getLocationForReference(String []parts) {
		IRepositoryLocation location = null;
		String url = parts[1];		
		IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
		Path awaitingFor = new Path(url);
		for (int i = 0; i < locations.length; i++) {
			if (new Path(locations[i].getUrl()).isPrefixOf(awaitingFor)) {
				return locations[i];
			}
		}
		if (location == null) {
			location = SVNRemoteStorage.instance().newRepositoryLocation();
			location.setUrl(url);
		}
		SVNRemoteStorage.instance().addRepositoryLocation(location);
		return location;
	}
	
	public String asReference(IProject project) throws TeamException {
		throw new RuntimeException("Unsupported operation");
	}

	public boolean accept(String referenceString) {
		return referenceString.startsWith(PLUGIN_INFORMATION);
	}
}
