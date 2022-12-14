/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.tests.workflow.ActionOperationWorkflowBuilder;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.action.local.management.CleanupAction;
import org.eclipse.team.svn.ui.action.remote.BranchAction;
import org.eclipse.team.svn.ui.action.remote.CompareAction;
import org.eclipse.team.svn.ui.action.remote.CopyAction;
import org.eclipse.team.svn.ui.action.remote.CreateFolderAction;
import org.eclipse.team.svn.ui.action.remote.CutAction;
import org.eclipse.team.svn.ui.action.remote.DeleteAction;
import org.eclipse.team.svn.ui.action.remote.PasteAction;
import org.eclipse.team.svn.ui.action.remote.RefreshAction;
import org.eclipse.team.svn.ui.action.remote.RenameAction;
import org.eclipse.team.svn.ui.action.remote.ShowAnnotationAction;
import org.eclipse.team.svn.ui.action.remote.ShowHistoryAction;
import org.eclipse.team.svn.ui.action.remote.TagAction;
import org.eclipse.team.svn.ui.action.remote.management.CreateProjectStructureAction;
import org.eclipse.team.svn.ui.action.remote.management.EditRepositoryLocationPropertiesAction;
import org.eclipse.team.svn.ui.operation.PrepareRemoteResourcesTransferrableOperation;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.ui.IActionDelegate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Menu enablement test for the Subversive menus in Repository View
 *
 * @author Sergiy Logvin
 */
public class RepositoryViewMenuEnablementTest {

	@BeforeClass
	public static void beforeAll() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		boolean workbenchEnabled = "true".equals(bundle.getString("UI.WorkbenchEnabled"));
		assumeTrue(workbenchEnabled);
	}

	@Before
	public void setUp() throws Exception {
		ActionOperationWorkflowBuilder workflowBuilder = new ActionOperationWorkflowBuilder();
		workflowBuilder.buildShareAddCommitWorkflow().execute();
		File newFolder = new File(this.getFirstProject().getLocation().toString() + "/testFolder");
		newFolder.mkdir();
		newFolder = new File(this.getSecondProject().getLocation().toString() + "/testFolder");
		newFolder.mkdir();
		IResource[] projects = new IResource[] { this.getFirstProject(), this.getSecondProject() };
		new RefreshResourcesOperation(projects).run(new NullProgressMonitor());
		new AddToSVNOperation(new IResource[] { getSecondProject().getFolder("testFolder") })
				.run(new NullProgressMonitor());
	}

	@Test
	public void testPasteRemoteResourceAction() {
		RepositoryResource[] resources = this.getTwoRepositoryFiles();
		new PrepareRemoteResourcesTransferrableOperation(
				new IRepositoryResource[] { resources[0].getRepositoryResource(),
						resources[1].getRepositoryResource() },
				RemoteResourceTransferrable.OP_COPY, TestPlugin.instance().getWorkbench().getDisplay())
						.run(new NullProgressMonitor());
		IActionDelegate action = new PasteAction();
		this.assertEnablement(action, this.getAllRepositoryResources(), false);
		this.assertEnablement(action, this.getOneRepositoryContainer(), true);
		this.assertEnablement(action, this.getNotHeadRevisionFiles(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getNotHeadRevisionFiles()[0] }, false);
	}

	@Test
	public void testBranchRemoteAction() {
		IActionDelegate action = new BranchAction();
		this.assertEnablement(action, this.getAllRepositoryResources(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	@Test
	public void testTagRemoteAction() {
		IActionDelegate action = new TagAction();
		this.assertEnablement(action, this.getAllRepositoryResources(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	@Test
	public void testCleanupAction() {
		IActionDelegate action = new CleanupAction();
		this.assertEnablement(action, this.getSelectedProjects(), true);
		this.assertEnablement(action, new IResource[] { this.getSelectedProjects()[0] }, true);
	}

	@Test
	public void testCompareTwoRepositoryResourcesAction() {
		IActionDelegate action = new CompareAction();
		this.assertEnablement(action, new IResource[] { this.getSelectedProjects()[0] }, false);
		this.assertEnablement(action, this.getOneRepositoryContainer(), true);
		this.assertEnablement(action, this.getOneRepositoryFile(), true);
		this.assertEnablement(action, this.getAllRepositoryResources(), false);
	}

	@Test
	public void testCopyRemoteResourceAction() {
		IActionDelegate action = new CopyAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
		this.assertEnablement(action, this.getAllRepositoryResources(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	@Test
	public void testCreateProjectStructureAction() {
		IActionDelegate action = new CreateProjectStructureAction();
		this.assertEnablement(action, this.getOneRepositoryContainer(), true);
		this.assertEnablement(action, this.getRepositoryLocation(), true);
	}

	@Test
	public void testCreateRemoteFolderAction() {
		IActionDelegate action = new CreateFolderAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getTwoRepositoryContainers()[0] }, true);
		this.assertEnablement(action, new RepositoryResource[] { this.getNotHeadRevisionFiles()[0] }, false);
	}

	@Test
	public void testCutRemoteResourceAction() {
		IActionDelegate action = new CutAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
		this.assertEnablement(action, this.getAllRepositoryResources(), true);
		this.assertEnablement(action, this.getNotHeadRevisionFiles(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getRepositoryLocation(), false);
		this.assertEnablement(action, this.getRepositoryRoots(), false);
	}

	@Test
	public void testDeleteRemoteResourceAction() {
		IActionDelegate action = new DeleteAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
		this.assertEnablement(action, this.getAllRepositoryResources(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getNotHeadRevisionFiles()[0] }, false);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getRepositoryLocation(), false);
		this.assertEnablement(action, this.getRepositoryRoots(), false);
	}

	@Test
	public void testEditRepositoryLocationPropertiesAction() {
		IActionDelegate action = new EditRepositoryLocationPropertiesAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
		this.assertEnablement(action, this.getRepositoryLocation(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, false);
	}

	@Test
	public void testRefreshRemoteAction() {
		IActionDelegate action = new RefreshAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
		this.assertEnablement(action, this.getNotHeadRevisionFiles(), true);
		this.assertEnablement(action, this.getRepositoryLocation(), true);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	@Test
	public void testRenameRemoteResourceAction() {
		IActionDelegate action = new RenameAction();
		this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
		this.assertEnablement(action, this.getAllRepositoryResources(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getNotHeadRevisionFiles()[0] }, false);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getRepositoryLocation(), false);
		this.assertEnablement(action, this.getRepositoryRoots(), false);
	}

	@Test
	public void testShowRemoteAnnotationAction() {
		IActionDelegate action = new ShowAnnotationAction();
		this.assertEnablement(action, this.getAllRepositoryResources(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	@Test
	public void testShowRemoteResourceHistoryAction() {
		IActionDelegate action = new ShowHistoryAction();
		this.assertEnablement(action, this.getAllRepositoryResources(), false);
		this.assertEnablement(action, new RepositoryResource[] { this.getAllRepositoryResources()[0] }, true);
	}

	protected void assertEnablement(IActionDelegate actionDelegate, RepositoryResource[] resources,
			boolean expectedEnablement) {
		IAction action = new Action() {
		};
		ISelection selection = this.asSelection(resources);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}

	protected void assertEnablement(IActionDelegate actionDelegate, IResource[] resources, boolean expectedEnablement) {
		IAction action = new Action() {
		};
		ISelection selection = this.asSelection(resources);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}

	protected void assertEnablement(IActionDelegate actionDelegate, RepositoryLocation[] locations,
			boolean expectedEnablement) {
		IAction action = new Action() {
		};
		ISelection selection = this.asSelection(locations);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}

	protected ISelection asSelection(Object[] resources) {
		return new StructuredSelection(resources);
	}

	protected String getName(IActionDelegate actionDelegate) {
		return actionDelegate.getClass().getName();
	}

	protected RepositoryLocation[] getRepositoryLocation() {
		return new RepositoryLocation[] { new RepositoryLocation(
				this.getAllRepositoryResources()[0].getRepositoryResource().getRepositoryLocation()) };
	}

	protected RepositoryResource[] getAllRepositoryResources() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		List<RepositoryResource> remoteResources = new ArrayList<RepositoryResource>();
		IResource[] resources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ONREPOSITORY);
		for (int i = 0; i < resources.length; i++) {
			remoteResources.add(RepositoryFolder.wrapChild(null, storage.asRepositoryResource(resources[i]), null));
		}
		return remoteResources.toArray(new RepositoryResource[remoteResources.size()]);
	}

	protected RepositoryResource[] getOneRepositoryFile() {
		return new RepositoryResource[] { this.getTwoRepositoryFiles()[0] };
	}

	protected RepositoryResource[] getTwoRepositoryFiles() {
		List<RepositoryResource> twoRemoteFiles = new ArrayList<RepositoryResource>();
		RepositoryResource[] resources = this.getAllRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof RepositoryFile) {
				twoRemoteFiles.add(resources[i]);
				if (twoRemoteFiles.size() == 2) {
					return twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
				}
			}
		}
		return twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
	}

	protected RepositoryResource[] getNotHeadRevisionFiles() {
		List<RepositoryResource> twoRemoteFiles = new ArrayList<RepositoryResource>();
		RepositoryResource[] resources = this.getAllRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof RepositoryFile) {
				resources[i].getRepositoryResource().setSelectedRevision(SVNRevision.fromNumber(123));
				twoRemoteFiles.add(resources[i]);
				if (twoRemoteFiles.size() == 2) {
					return twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
				}
			}
		}
		return twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
	}

	protected RepositoryResource[] getOneRepositoryContainer() {
		return new RepositoryResource[] { this.getTwoRepositoryContainers()[0] };
	}

	protected RepositoryResource[] getTwoRepositoryContainers() {
		List<RepositoryResource> twoRemoteFolders = new ArrayList<RepositoryResource>();
		RepositoryResource[] resources = this.getAllRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof RepositoryFolder) {
				twoRemoteFolders.add(resources[i]);
				if (twoRemoteFolders.size() == 2) {
					return twoRemoteFolders.toArray(new RepositoryResource[twoRemoteFolders.size()]);
				}
			}
		}
		return twoRemoteFolders.toArray(new RepositoryResource[twoRemoteFolders.size()]);
	}

	protected IResource[] getSelectedProjects() {
		IResource[] selectedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ONREPOSITORY);
		;
		ArrayList<IResource> projects = new ArrayList<IResource>();
		for (int i = 0; i < selectedResources.length; i++) {
			IResource resource = selectedResources[i];
			if (resource.getType() == IResource.PROJECT) {
				projects.add(resource);
			}
		}
		return projects.toArray(new IResource[projects.size()]);
	}

	protected RepositoryResource[] getRepositoryRoots() {
		List<RepositoryResource> roots = new ArrayList<RepositoryResource>();
		RepositoryResource[] resources = this.getAllRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			if (resources[0].getRepositoryResource() instanceof IRepositoryRoot) {
				roots.add(resources[i]);
			}
		}
		return roots.toArray(new RepositoryResource[roots.size()]);
	}

	protected IProject getFirstProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project1.Name"));
	}

	protected IProject getSecondProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project2.Name"));
	}

}
