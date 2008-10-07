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

import java.io.File;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.CreateFolderOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.TestPlugin;

/**
 * Test operation workflow
 * 
 * @author Alexander Gurov
 */
public abstract class TestWorkflow extends TestCase {
    protected IRepositoryLocation location;

	protected void setUp() throws Exception {
		super.setUp();
		
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		storage.initialize(TestPlugin.instance().getStateLocation());
		
		this.location = storage.newRepositoryLocation();
		this.location.setUrl(bundle.getString("Repository.URL"));
		this.location.setTrunkLocation(bundle.getString("Repository.Trunk"));
		this.location.setBranchesLocation(bundle.getString("Repository.Branches"));
		this.location.setTagsLocation(bundle.getString("Repository.Tags"));
		this.location.setStructureEnabled(true);
		this.location.setLabel(bundle.getString("Repository.Label"));
		this.location.setUsername(bundle.getString("Repository.Username"));
		this.location.setPassword(bundle.getString("Repository.Password"));
		this.location.setPasswordSaved("true".equals(bundle.getString("Repository.SavePassword")));
		
		storage.addRepositoryLocation(this.location);
		this.location = storage.getRepositoryLocation(this.location.getId());
		
		this.deleteRepositoryNode(SVNUtility.getProposedTrunk(this.location));
		this.deleteRepositoryNode(SVNUtility.getProposedBranches(this.location));
		this.deleteRepositoryNode(SVNUtility.getProposedTags(this.location));
		
		CreateFolderOperation op = new CreateFolderOperation(this.location.getRoot(), this.location.getTrunkLocation(), "create trunk");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.location.getRoot(), this.location.getBranchesLocation(), "create branches");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.location.getRoot(),  this.location.getTagsLocation(), "createTags");
		op.run(new NullProgressMonitor());
	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		root.delete(true, true, null);
		
		String demoDataLocation = TestPlugin.instance().getLocation() + bundle.getString("DemoData.Location") + "/";
		
		String prj1Name = bundle.getString("Project1.Name");
		String prj2Name = bundle.getString("Project2.Name");

		FileUtility.copyAll(root.getLocation().toFile(), new File(demoDataLocation + prj1Name), new NullProgressMonitor());
		FileUtility.copyAll(root.getLocation().toFile(), new File(demoDataLocation + prj2Name), new NullProgressMonitor());

		IProject prj = root.getProject(prj1Name);
		prj.create(null);
		prj.open(null);
		FileUtility.removeSVNMetaInformation(prj, new NullProgressMonitor());
		prj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		SVNRemoteStorage.instance().refreshLocalResources(new IResource[] {prj}, IResource.DEPTH_INFINITE);
		
		prj = root.getProject(prj2Name);
		prj.create(null);
		prj.open(null);
		FileUtility.removeSVNMetaInformation(prj, new NullProgressMonitor());
		prj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		SVNRemoteStorage.instance().refreshLocalResources(new IResource[] {prj}, IResource.DEPTH_INFINITE);
	}
	
	protected void tearDown() throws Exception {
	    this.cleanupTestEnvironment();
	    
		super.tearDown();
	}

	protected void cleanupTestEnvironment() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		try {root.delete(true, true, null);} catch (Exception ex) {}
		
	    try {this.cleanRepositoryNode(SVNUtility.getProposedTags(this.location));} catch (Exception ex) {}
	    try {this.cleanRepositoryNode(SVNUtility.getProposedBranches(this.location));} catch (Exception ex) {}
	    try {this.cleanRepositoryNode(SVNUtility.getProposedTrunk(this.location));} catch (Exception ex) {}
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation []locations = storage.getRepositoryLocations();
		
		for (int i = 0; i < locations.length; i++) {
		    locations[i].dispose();
		    try {storage.removeRepositoryLocation(locations[i]);} catch (Exception ex) {}
		}
	}
	
	protected void cleanRepositoryNode(IRepositoryContainer node) throws Exception {
		if (node.exists()) {
			IRepositoryResource []children = node.getChildren();
			if (children != null && children.length > 0) {
				String []toDelete = new String[children.length];
				for (int i = 0; i < children.length; i++) {
				    toDelete[i] = SVNUtility.encodeURL(children[i].getUrl());
				}
				ISVNConnector proxy = this.location.acquireSVNProxy();
				try {
				    proxy.remove(toDelete, "Test Done", ISVNConnector.Options.FORCE, null, new SVNNullProgressMonitor());
				}
				finally {
				    this.location.releaseSVNProxy(proxy);
				}
			}
		}
	}
	
	protected void deleteRepositoryNode(IRepositoryContainer node) throws Exception {
		if (node.exists()) {
			DeleteResourcesOperation op = new DeleteResourcesOperation(new IRepositoryResource[] {node}, "test delete");
			op.run(new NullProgressMonitor());
		}
	}
	
}
