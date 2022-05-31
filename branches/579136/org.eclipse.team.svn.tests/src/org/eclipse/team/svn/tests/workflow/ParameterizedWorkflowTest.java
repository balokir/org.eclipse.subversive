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

package org.eclipse.team.svn.tests.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.misc.TestUtil;
import org.eclipse.team.svn.tests.workflow.repository.RemoteTestRepositoryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test operation workflow
 * 
 * @author Alexander Gurov
 * @author Nicolas Peifer
 */
@RunWith(Parameterized.class)
public class ParameterizedWorkflowTest {

	@Parameter
	public ActionOperationWorkflow workflow;
	private RemoteTestRepositoryManager repoManager;

	@Parameters()
	public static Collection<ActionOperationWorkflow> createTestData() throws Exception {
		ActionOperationWorkflowBuilder workflowBuilder = new ActionOperationWorkflowBuilder();
		List<ActionOperationWorkflow> result = new ArrayList<ActionOperationWorkflow>();
		result.add(workflowBuilder.buildCoreWorkflow());
		result.add(workflowBuilder.buildCommitUpdateWorkflow());
		result.add(workflowBuilder.buildPlc312Workflow());
		result.add(workflowBuilder.buildPlc314Workflow());
		result.add(workflowBuilder.buildPlc350Workflow());
		result.add(workflowBuilder.buildPlc366Workflow());
		result.add(workflowBuilder.buildPlc375Workflow());
		result.add(workflowBuilder.buildPlc378Workflow());
		result.add(workflowBuilder.buildPlc379Workflow());
		result.add(workflowBuilder.buildPlc380Workflow());
		result.add(workflowBuilder.buildFileWorkflow());
		return result;
	}

	@Before
	public void beforeEach() throws CoreException, Exception {
		TestUtil.resetTestDataFolder();
		repoManager = new RemoteTestRepositoryManager();
		repoManager.createRepository();
		TestUtil.refreshProjects();
	}

	@Test
	public void test() {
		workflow.execute();
		System.out.println("Test finished.");
	}

	@After
	public void after() {
		cleanupTestEnvironment();
	}

	protected void cleanupTestEnvironment() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		try {
			root.delete(true, true, null);
		} catch (Exception ex) {
		}

		try {
			this.cleanRepositoryNode(SVNUtility.getProposedTags(repoManager.getRepositoryLocation()));
		} catch (Exception ex) {
		}
		try {
			this.cleanRepositoryNode(SVNUtility.getProposedBranches(repoManager.getRepositoryLocation()));
		} catch (Exception ex) {
		}
		try {
			this.cleanRepositoryNode(SVNUtility.getProposedTrunk(repoManager.getRepositoryLocation()));
		} catch (Exception ex) {
		}

		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation[] locations = storage.getRepositoryLocations();

		for (int i = 0; i < locations.length; i++) {
			locations[i].dispose();
			try {
				storage.removeRepositoryLocation(locations[i]);
			} catch (Exception ex) {
			}
		}
	}

	protected void cleanRepositoryNode(IRepositoryContainer node) throws Exception {
		if (node.exists()) {
			IRepositoryResource[] children = node.getChildren();
			if (children != null && children.length > 0) {
				String[] toDelete = new String[children.length];
				for (int i = 0; i < children.length; i++) {
					toDelete[i] = SVNUtility.encodeURL(children[i].getUrl());
				}
				ISVNConnector proxy = repoManager.getRepositoryLocation().acquireSVNProxy();
				try {
					proxy.removeRemote(toDelete, "Test Done", ISVNConnector.Options.FORCE, null,
							new SVNNullProgressMonitor());
				} finally {
					repoManager.getRepositoryLocation().releaseSVNProxy(proxy);
				}
			}
		}
	}

}
