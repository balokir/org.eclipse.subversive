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

package org.eclipse.team.svn.test.core;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.test.TestPlugin;

import junit.framework.TestCase;

/**
 * Core functionality test
 * 
 * @author Alexander Gurov
 */
public class CoreTest extends TestCase {
	
	protected IProject prj; 

	protected void setUp() throws Exception {
		super.setUp();
		
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		storage.initialize(TestPlugin.instance().getStateLocation());
		
		IRepositoryLocation location = storage.newRepositoryLocation();
		location.setUrl(bundle.getString("Repository.URL"));
		location.setTrunkLocation(bundle.getString("Repository.Head"));
		location.setBranchesLocation(bundle.getString("Repository.Branches"));
		location.setTagsLocation(bundle.getString("Repository.Tags"));
		location.setLabel(bundle.getString("Repository.Label"));
		location.setUsername(bundle.getString("Repository.Username"));
		location.setPassword(bundle.getString("Repository.Password"));
		location.setPasswordSaved("true".equals(bundle.getString("Repository.SavePassword")));
		
		storage.addRepositoryLocation(location);
		
		this.prj = ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project1.Name"));
		
		if (this.prj.exists()) {
			this.prj.open(null);
			this.prj.delete(true, true, null);
		}
		
		this.prj.create(null);
	}
	
	public void testCore() {
		new RepositoryLocationsManagementTest() {}.testLocationsManagement();
		new CheckoutOperationTest() {}.testOperation();
		new FileUtilityTest() {}.testOperation();
		new SVNUtilityTest() {}.testOperation();
		new CleanupOperationTest() {}.testOperation();
		new GetAllResourcesOperationTest() {}.testOperation();
		new ClearLocalStatusesOperationTest() {}.testOperation();
		new GetLogMessagesOperationTest() {}.testOperation();
		new RemoteStatusOperationTest() {}.testOperation();
		new AddOperationTest() {}.testOperation();
		new CommitOperationTest() {}.testOperation();
		new RevertOperationTest() {}.testOperation();
		new DeleteRemoteResourceOperationTest() {}.testOperation();
		new UpdateOperationTest() {}.testOperation();
		new DisconnectOperationTest() {}.testOperation();
	}

	protected void tearDown() throws Exception {
		this.prj.delete(true, true, null);
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation [] locations = storage.getRepositoryLocations();
		
		for (int i = 0; i < locations.length; i++) {
			storage.removeRepositoryLocation(locations[i]);
		}

		super.tearDown();
	}

}
