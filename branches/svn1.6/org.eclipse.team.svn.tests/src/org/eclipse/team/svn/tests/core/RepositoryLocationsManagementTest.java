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

package org.eclipse.team.svn.tests.core;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

import junit.framework.TestCase;

/**
 * Manage repository locations using SVNRemoteStorage test
 * 
 * @author Alexander Gurov
 */
public abstract class RepositoryLocationsManagementTest extends TestCase {

	public void testLocationsManagement() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation location = storage.newRepositoryLocation();
		
		location.setUrl("http://testurl");
		location.setLabel("Label");
		location.setPassword("password");
		location.setPasswordSaved(true);
		location.setTagsLocation("tags");
		location.setTrunkLocation("trunk");
		location.setBranchesLocation("branches");
		location.setUsername("username");
		
		storage.addRepositoryLocation(location);
		
		try {
			storage.saveConfiguration();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		String reference = storage.repositoryLocationAsReference(location);
		
		IRepositoryLocation refTest = storage.newRepositoryLocation(reference);
		
		assertTrue(
			"Location reference",
			location.getId().equals(refTest.getId()) &&
			location.getBranchesLocation().equals(refTest.getBranchesLocation()) &&
			location.getLabel().equals(refTest.getLabel()) &&
			location.getName().equals(refTest.getName()) &&
			location.getTagsLocation().equals(refTest.getTagsLocation()) &&
			location.getTrunkLocation().equals(refTest.getTrunkLocation()) &&
			location.getUrl().equals(refTest.getUrl())
		);
		
		storage.removeRepositoryLocation(location);
		
		try {
			storage.saveConfiguration();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		refTest = storage.newRepositoryLocation(reference);
		
		assertTrue(
			"Location reference",
			location.getId().equals(refTest.getId()) &&
			location.getBranchesLocation().equals(refTest.getBranchesLocation()) &&
			location.getLabel().equals(refTest.getLabel()) &&
			location.getName().equals(refTest.getName()) &&
			location.getTagsLocation().equals(refTest.getTagsLocation()) &&
			location.getTrunkLocation().equals(refTest.getTrunkLocation()) &&
			location.getUrl().equals(refTest.getUrl())
		);
	}
	
}
