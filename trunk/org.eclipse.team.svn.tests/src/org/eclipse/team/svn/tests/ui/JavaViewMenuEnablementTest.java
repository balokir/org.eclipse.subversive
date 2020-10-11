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
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.tests.workflow.ActionOperationWorkflowBuilder;
import org.eclipse.team.svn.ui.action.FilterManager;
import org.eclipse.team.svn.ui.action.local.AddToSVNAction;
import org.eclipse.team.svn.ui.action.local.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.action.local.BranchAction;
import org.eclipse.team.svn.ui.action.local.CommitAction;
import org.eclipse.team.svn.ui.action.local.CompareWithLatestRevisionAction;
import org.eclipse.team.svn.ui.action.local.CompareWithRevisionAction;
import org.eclipse.team.svn.ui.action.local.CompareWithWorkingCopyAction;
import org.eclipse.team.svn.ui.action.local.EditPropertiesAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithLatestRevisionAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithRevisionAction;
import org.eclipse.team.svn.ui.action.local.RevertAction;
import org.eclipse.team.svn.ui.action.local.ShowAnnotationAction;
import org.eclipse.team.svn.ui.action.local.ShowHistoryAction;
import org.eclipse.team.svn.ui.action.local.SwitchAction;
import org.eclipse.team.svn.ui.action.local.SynchronizeAction;
import org.eclipse.team.svn.ui.action.local.TagAction;
import org.eclipse.team.svn.ui.action.local.UpdateAction;
import org.eclipse.ui.IActionDelegate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Menu enablement test for the Subversive menus in Java View
 *
 * @author Sergiy Logvin
 */
public class JavaViewMenuEnablementTest {

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
		File newResource = new File(this.getFirstProject().getLocation().toString() + "/newResource");
		newResource.mkdir();
		newResource = new File(this.getSecondProject().getLocation().toString() + "/newResource");
		newResource.mkdir();
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
	public void testIgnoreAction() throws Exception {
		IActionDelegate action = new AddToSVNIgnoreAction();
		this.assertEnablement(action, this.getModifiedResources(), false);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, this.getAddedResources(), true);
		this.assertEnablement(action, this.getNewResources(), true);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testAddToSVNAction() throws Exception {
		IActionDelegate action = new AddToSVNAction();
		this.assertEnablement(action, this.getModifiedResources(), false);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getNonversionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), true);
		this.assertEnablement(action, this.getAddedResources(), true);
		this.assertEnablement(action, this.getNewResources(), true);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testBranchAction() throws Exception {
		IActionDelegate action = new BranchAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), true);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testCommitAction() throws Exception {
		IActionDelegate action = new CommitAction();
		this.assertEnablement(action, this.getChangedResources(), true);
		this.assertEnablement(action, new IResource[] { this.getChangedResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, this.getNewResources(), true);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testCompareWithLatestRevisionAction() throws Exception {
		IActionDelegate action = new CompareWithLatestRevisionAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testCompareWithRevisionAction() throws Exception {
		IActionDelegate action = new CompareWithRevisionAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testReplaceWithRevisionAction() throws Exception {
		IActionDelegate action = new ReplaceWithRevisionAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testCompareWithWorkingCopyAction() throws Exception {
		IActionDelegate action = new CompareWithWorkingCopyAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testEditPropertiesAction() throws Exception {
		IActionDelegate action = new EditPropertiesAction();
		this.assertEnablement(action, this.getExcludePrereplacedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getExcludePrereplacedResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testReplaceWithLatestRevisionAction() throws Exception {
		IActionDelegate action = new ReplaceWithLatestRevisionAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), true);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testRevertAction() throws Exception {
		IActionDelegate action = new RevertAction();
		this.assertEnablement(action, this.getRevertableResources(), true);
		this.assertEnablement(action, new IResource[] { this.getRevertableResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testShowAnnotationAction() throws Exception {
		IActionDelegate action = new ShowAnnotationAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testShowResourceHistoryAction() throws Exception {
		IActionDelegate action = new ShowHistoryAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testSwitchAction() throws Exception {
		IActionDelegate action = new SwitchAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), false);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), false);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testSynchronizeAction() throws Exception {
		IActionDelegate action = new SynchronizeAction();
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), true);
		this.assertEnablement(action, this.getNewResources(), true);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testTagAction() throws Exception {
		IActionDelegate action = new TagAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), true);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, new IResource[] { this.getVersionedResources()[0] }, true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, new IResource[] { this.getIgnoredResources()[0] }, false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertEnablement(action, new IResource[] { this.getNewResources()[0] }, false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	@Test
	public void testUpdateAction() throws Exception {
		IActionDelegate action = new UpdateAction();
		this.assertEnablement(action, this.getOnRepositoryResources(), true);
		this.assertEnablement(action, new IResource[] { this.getOnRepositoryResources()[0] }, true);
		this.assertEnablement(action, this.getVersionedResources(), true);
		this.assertEnablement(action, this.getIgnoredResources(), false);
		this.assertEnablement(action, this.getNewResources(), false);
		this.assertDisabledForCommonReasons(action, this.getAllResources());
	}

	private ISelection asSelection(IResource[] resources) {
		return new StructuredSelection(resources);
	}

	protected void assertDisabledForCommonReasons(IActionDelegate action, IResource[] resources) throws Exception {
		assertDisabledForNoSelection(action);
	}

	protected void assertDisabledForNoSelection(IActionDelegate actionDelegate) {
		assertEnablement(actionDelegate, StructuredSelection.EMPTY, false);
	}

	protected void assertEnablement(IActionDelegate actionDelegate, IResource[] resources, boolean expectedEnablement) {
		IAction action = new Action() {
		};
		action.setEnabled(false);
		ISelection selection = this.asSelection(resources);
		FilterManager.instance().clear();
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}

	protected void assertEnablement(IActionDelegate actionDelegate, ISelection selection, boolean expectedEnablement) {
		IAction action = new Action() {
		};
		FilterManager.instance().clear();
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}

	protected String getName(IActionDelegate actionDelegate) {
		return actionDelegate.getClass().getName();
	}

	protected IResource[] getAllResources() {
		IResource[] allResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ALL);
		return allResources;
	}

	protected IResource[] getIgnoredResources() {
		IResource[] ignoredResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_IGNORED);
		return ignoredResources;
	}

	protected IResource[] getNonversionedResources() {
		IResource[] nonversionedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_UNVERSIONED);
		return nonversionedResources;
	}

	protected IResource[] getVersionedResources() {
		IResource[] versionedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_VERSIONED);
		return versionedResources;
	}

	protected IResource[] getOnRepositoryResources() {
		IResource[] onRepositoryResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ONREPOSITORY);
		return onRepositoryResources;
	}

	protected IResource[] getNewResources() {
		IResource[] newResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_NEW);
		return newResources;
	}

	protected IResource[] getAddedResources() {
		IResource[] addedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ADDED);
		return addedResources;
	}

	protected IResource[] getModifiedResources() {
		IResource[] modifiedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_MODIFIED);
		return modifiedResources;
	}

	protected IResource[] getNotmodifiedResources() {
		IResource[] notModifiedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_NOTMODIFIED);
		return notModifiedResources;
	}

	protected IResource[] getDeletedResources() {
		IResource[] deletedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_DELETED);
		return deletedResources;
	}

	protected IResource[] getChangedResources() {
		IResource[] changedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_ANY_CHANGE);
		return changedResources;
	}

	protected IResource[] getReplacedResources() {
		IResource[] replacedResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_REPLACED);
		return replacedResources;
	}

	protected IResource[] getExcludePrereplacedResources() {
		return FileUtility.getResourcesRecursive(new IResource[] { this.getFirstProject(), this.getSecondProject() },
				new IStateFilter() {
					public boolean accept(IResource resource, String state, int mask) {
						return IStateFilter.SF_VERSIONED.accept(resource, state, mask)
								&& !IStateFilter.SF_PREREPLACED.accept(resource, state, mask);
					}

					public boolean allowsRecursion(IResource resource, String state, int mask) {
						return true;
					}

					public boolean accept(ILocalResource resource) {
						return IStateFilter.SF_VERSIONED.accept(resource)
								&& !IStateFilter.SF_PREREPLACED.accept(resource);
					}

					public boolean allowsRecursion(ILocalResource resource) {
						return true;
					}
				});
	}

	protected IResource[] getRevertableResources() {
		IResource[] revertableResources = FileUtility.getResourcesRecursive(
				new IResource[] { this.getFirstProject(), this.getSecondProject() }, IStateFilter.SF_REVERTABLE);
		return revertableResources;
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
