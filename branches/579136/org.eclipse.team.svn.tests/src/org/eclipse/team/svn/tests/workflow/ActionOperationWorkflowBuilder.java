package org.eclipse.team.svn.tests.workflow;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.operation.local.refactor.CopyResourceOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryFolder;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.LocalOperationFactory;
import org.eclipse.team.svn.tests.core.RemoteOperationFactory;
import org.eclipse.team.svn.tests.core.file.FileOperationFactory;
import org.eclipse.team.svn.tests.core.misc.AbstractLockingTestOperation;
import org.eclipse.team.svn.tests.core.misc.TestUtil;

/**
 * 
 * @author Sergiy Logvin
 * @author Nicolas Peifer
 */
public class ActionOperationWorkflowBuilder {
	private FileOperationFactory fileOperationFactory;
	private LocalOperationFactory localOperationFactory = new LocalOperationFactory();
	private RemoteOperationFactory remoteOperationFactory = new RemoteOperationFactory();

	public ActionOperationWorkflowBuilder() throws Exception {
		fileOperationFactory = new FileOperationFactory();
	}

	public ActionOperationWorkflow buildCommitUpdateWorkflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				remoteOperationFactory.createCreateRemoteFolderOperation(),
				localOperationFactory.createUpdateOperation(), createCustomLockingOperation(),
				localOperationFactory.createDisconnectWithDropOperation());
	}

	public ActionOperationWorkflow buildPlc312Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				createCustomAddToSvnIgnoreOperation());
	}

	public ActionOperationWorkflow buildPlc314Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				createLockingOperationForPlc314Test());
	}

	public ActionOperationWorkflow buildPlc350Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				createLockingOperationForPlc350Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc350Test() {
		return () -> new AbstractLockingTestOperation("PLC350Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				IRepositoryResource branchedTrunk = new SVNRepositoryFolder(TestUtil.getRepositoryLocation(),
						SVNUtility.getProposedBranchesLocation(TestUtil.getRepositoryLocation()) + "/trunk",
						SVNRevision.HEAD);
				IRepositoryResource taggedTrunk = new SVNRepositoryFolder(TestUtil.getRepositoryLocation(),
						SVNUtility.getProposedTagsLocation(TestUtil.getRepositoryLocation()) + "/trunk",
						SVNRevision.HEAD);
				if (branchedTrunk.exists()) {
					FileUtility.deleteRecursive(new File(branchedTrunk.getUrl()));
				}
				if (taggedTrunk.exists()) {
					FileUtility.deleteRecursive(new File(taggedTrunk.getUrl()));
				}
				IRepositoryResource branchTagResource = SVNUtility.getProposedTrunk(TestUtil.getRepositoryLocation());
				new PreparedBranchTagOperation("Branch", new IRepositoryResource[] { branchTagResource },
						SVNUtility.getProposedBranches(TestUtil.getRepositoryLocation()), "test branch", false)
								.run(monitor);
				branchedTrunk = new SVNRepositoryFolder(TestUtil.getRepositoryLocation(),
						SVNUtility.getProposedBranchesLocation(TestUtil.getRepositoryLocation()) + "/trunk",
						SVNRevision.HEAD);
				assertTrue("PLC350Test", branchedTrunk.exists());
				new PreparedBranchTagOperation("Tag", new IRepositoryResource[] { branchTagResource },
						SVNUtility.getProposedTags(TestUtil.getRepositoryLocation()), "test tag", false).run(monitor);
				taggedTrunk = new SVNRepositoryFolder(TestUtil.getRepositoryLocation(),
						SVNUtility.getProposedTagsLocation(TestUtil.getRepositoryLocation()) + "/trunk",
						SVNRevision.HEAD);
				assertTrue("PLC350Test", taggedTrunk.exists());
			}
		};
	}

	private Supplier<IActionOperation> createCustomLockingOperation() {
		return () -> new AbstractLockingTestOperation("CommitUpdateWorkflowTest") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				File testFolder = TestUtil.getFirstProject().getFolder("src/testFolder").getLocation().toFile();
				assertTrue("CommitUpdateWorkflowTest", testFolder.exists());
			}

		};
	}

	private Supplier<IActionOperation> createLockingOperationForPlc314Test() {
		return () -> new AbstractLockingTestOperation("PLC314Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				FileUtility.copyAll(TestUtil.getFirstProject().getFolder("src").getLocation().toFile(),
						TestUtil.getSecondProject().getFolder("web").getLocation().toFile(), monitor);
				IResource[] ignoreResource = new IResource[] { TestUtil.getFirstProject().getFile("src/web"),
						TestUtil.getFirstProject().getFile("src/web/site.css"),
						TestUtil.getFirstProject().getFile("src/web/site.xsl") };
				new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
				new AddToSVNOperation(new IResource[] { TestUtil.getFirstProject().getFile("src/web/site.css") })
						.run(monitor);
				SVNRemoteStorage storage = SVNRemoteStorage.instance();
				IResource current = TestUtil.getFirstProject().getFile("src/web/site.css");
				IResource parent = current.getParent();
				String name = current.getName();
				IRepositoryLocation location = storage.getRepositoryLocation(parent);
				ISVNConnector proxy = location.acquireSVNProxy();

				SVNProperty data = null;
				try {
					data = proxy.getProperty(new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(parent)),
							BuiltIn.IGNORE, null, new SVNProgressMonitor(this, monitor, null));
				} finally {
					location.releaseSVNProxy(proxy);
				}

				String ignoreValue = data == null ? "" : data.value;
				StringTokenizer tok = new StringTokenizer(ignoreValue, "\n", true);
				while (tok.hasMoreTokens()) {
					String oneOf = tok.nextToken();
					if (oneOf.equals(name)) {
						assertTrue("Name of added to SVN resource was not deleted (PLC314Test)", false);
					}
				}
			};
		};
	}

	private Supplier<IActionOperation> createCustomAddToSvnIgnoreOperation() {
		return () -> new AbstractLockingTestOperation("PLC312Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				FileUtility.copyAll(TestUtil.getFirstProject().getFolder("src").getLocation().toFile(),
						TestUtil.getSecondProject().getFolder("web").getLocation().toFile(), monitor);
				IResource[] ignoreResource = new IResource[] { TestUtil.getFirstProject().getFile("src/web/site.css") };
				new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
			};
		};
	}

	/**
	 * Reproducing steps, which are described in PLC-366 defect (Commit doesn't work
	 * for folders with svn:ignore resources)
	 * 
	 * @author Sergiy Logvin
	 */
	public ActionOperationWorkflow buildPlc366Workflow() {
		return new ActionOperationWorkflow(createLockingOperationForPlc366Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc366Test() {
		return () -> new AbstractLockingTestOperation("PLC366Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				new ShareProjectOperation(new IProject[] { TestUtil.getSecondProject() },
						TestUtil.getRepositoryLocation(), null, "Share Project test").run(monitor);
				IResource[] forAddition = FileUtility
						.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() }, IStateFilter.SF_NEW);
				new AddToSVNOperation(forAddition).run(monitor);
				IResource[] forCommit = FileUtility
						.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() }, IStateFilter.SF_ADDED);
				new CommitOperation(forCommit, "test PLC366", false, false).run(monitor);
				IResource source = TestUtil.getSecondProject().getFile("site.xml");
				IResource destination = TestUtil.getSecondProject().getFile("web/site.xml");
				new CopyResourceOperation(source, destination).run(monitor);
				new AddToSVNIgnoreOperation(new IResource[] { destination }, IRemoteStorage.IGNORE_NAME, "")
						.run(monitor);
				new CommitOperation(new IResource[] { TestUtil.getSecondProject().getFolder("web") }, "test PLC366",
						false, false).run(monitor);
			}
		};
	}

	/**
	 * Reproducing steps, which are described in PLC-375 defect (Committing new
	 * folder with the name of deleted file finishes with error)
	 *
	 * @author Sergiy Logvin
	 */
	public ActionOperationWorkflow buildPlc375Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				createLockingOperationForPlc375Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc375Test() {
		return () -> new AbstractLockingTestOperation("PLC375Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(TestUtil.getSecondProject().getLocation().toString() + "/123");
					fos.write(12345);
				} finally {
					fos.close();
				}
				IResource[] forCommit = FileUtility
						.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() }, IStateFilter.SF_ADDED);
				new CommitOperation(forCommit, "test PLC375", false, false).run(monitor);
				FileUtility.deleteRecursive(TestUtil.getSecondProject().getFile("123").getLocation().toFile());
				forCommit = FileUtility.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() },
						IStateFilter.SF_ANY_CHANGE);
				new CommitOperation(forCommit, "test PLC375", false, false).run(monitor);
				File dir = new File(TestUtil.getSecondProject().getLocation().toString() + "/123");
				dir.mkdir();
				forCommit = FileUtility.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() },
						IStateFilter.SF_ADDED);
				new CommitOperation(forCommit, "test PLC375", false, false).run(monitor);
			}
		};
	}

	public ActionOperationWorkflow buildPlc378Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				createLockingOperationForPlc378Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc378Test() {
		return () -> new AbstractLockingTestOperation("PLC378Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				new CheckoutAsOperation(
						"CopyProject", SVNUtility.getProposedTrunk(TestUtil.getRepositoryLocation())
								.asRepositoryContainer(TestUtil.getFirstProject().getName(), false),
						SVNDepth.INFINITY, true).run(monitor);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(TestUtil.getFirstProject().getLocation().toString() + "/testFile");
					fos.write("testFile contents".getBytes());
				} finally {
					fos.close();
				}
				IResource[] forAddition = new IResource[] { TestUtil.getFirstProject().getFile("testFile") };
				new AddToSVNOperation(forAddition).run(monitor);
				IResource[] forCommit = new IResource[] { TestUtil.getFirstProject().getFile("testFile") };
				new CommitOperation(forCommit, "PLC378Test", false, false).run(monitor);

				try {
					fos = new FileOutputStream(TestUtil.getSecondProject().getLocation().toString() + "/testFile");
					fos.write("some other testFile contents".getBytes());
				} finally {
					fos.close();
				}
				forAddition = new IResource[] { TestUtil.getSecondProject().getFile("testFile") };
				new AddToSVNOperation(forAddition).run(monitor);
				forCommit = new IResource[] { TestUtil.getSecondProject().getFile("testFile") };
				new CommitOperation(forCommit, "PLC378Test", false, false).run(monitor);
				IResource[] forUpdate = new IResource[] { TestUtil.getSecondProject().getFile("testFile") };
				new UpdateOperation(forUpdate, true).run(monitor);
			}
		};
	}

	/**
	 * Reproducing steps, which are described in PLC-379 defect (Incorrect commit of
	 * the file with the name added to svn:ignore in another workspace)
	 *
	 * @author Sergiy Logvin
	 */
	public ActionOperationWorkflow buildPlc379Workflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation(),
				createLockingOperationForPlc379Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc379Test() {
		return () -> new AbstractLockingTestOperation("PLC379Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				new CheckoutAsOperation(
						"TestProject", SVNUtility.getProposedTrunk(TestUtil.getRepositoryLocation())
								.asRepositoryContainer(TestUtil.getSecondProject().getName(), false),
						SVNDepth.INFINITY, true).run(monitor);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(TestUtil.getFirstProject().getLocation().toString() + "/123");
					fos.write("some contents".getBytes());
				} finally {
					fos.close();
				}
				IResource[] forAddition = new IResource[] { TestUtil.getFirstProject().getFile("123") };
				new AddToSVNOperation(forAddition).run(monitor);
				IResource[] forCommit = new IResource[] { TestUtil.getFirstProject().getFile("123") };
				new CommitOperation(forCommit, "PLC379Test", false, false).run(monitor);

				try {
					fos = new FileOutputStream(
							ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getLocation().toString()
									+ "/123");
					fos.write("some other contents".getBytes());
				} finally {
					fos.close();
				}
				IResource[] ignoreResource = new IResource[] {
						ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123") };
				new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
				IResource[] forUpdate = new IResource[] {
						ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123") };
				new UpdateOperation(forUpdate, true).run(monitor);
			}
		};
	}

	/**
	 * Reproducing steps, which are described in PLC-380 defect (File updating error
	 * when repository contains an empty copy of it)
	 *
	 * @author Sergiy Logvin
	 */
	public ActionOperationWorkflow buildPlc380Workflow() {
		return new ActionOperationWorkflow(createLockingOperationForPlc380Test());
	}

	private Supplier<IActionOperation> createLockingOperationForPlc380Test() {
		return () -> new AbstractLockingTestOperation("PLC380Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(TestUtil.getSecondProject().getLocation().toString() + "/123");
					fos.write("some contents".getBytes());
				} finally {
					fos.close();
				}
				new ShareProjectOperation(new IProject[] { TestUtil.getSecondProject() },
						TestUtil.getRepositoryLocation(), null, "Share Project test").run(monitor);
				IResource[] forAddition = FileUtility
						.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() }, IStateFilter.SF_NEW);
				new AddToSVNOperation(forAddition).run(monitor);
				IResource[] forCommit = FileUtility
						.getResourcesRecursive(new IResource[] { TestUtil.getSecondProject() }, IStateFilter.SF_ADDED);
				new CommitOperation(forCommit, "test PLC380", false, false).run(monitor);
				new CheckoutAsOperation(
						"TestProject", SVNUtility.getProposedTrunk(TestUtil.getRepositoryLocation())
								.asRepositoryContainer(TestUtil.getSecondProject().getName(), false),
						SVNDepth.INFINITY, true).run(monitor);
				try {
					fos = new FileOutputStream(TestUtil.getFirstProject().getLocation().toString() + "/123");
					fos.write("".getBytes());
				} finally {
					fos.close();
				}
				new CommitOperation(new IResource[] { TestUtil.getFirstProject().getFile("123") }, "test PLC380", false,
						false).run(monitor);
				new UpdateOperation(
						new IResource[] { ResourcesPlugin.getWorkspace().getRoot().getProjects()[2].getFile("123") },
						true).run(monitor);
			}
		};
	}

	/**
	 * File operations test
	 * 
	 * @author Sergiy Logvin
	 */
	public ActionOperationWorkflow buildFileWorkflow() {
		return new ActionOperationWorkflow(fileOperationFactory.createDisconnectOperation(),
				fileOperationFactory.createShareOperation(), fileOperationFactory.createAddToSvnOperation(),
				fileOperationFactory.createAddToSvnIgnoreOperation(), fileOperationFactory.createCommitOperation(),
				fileOperationFactory.createBranchTagOperation(), fileOperationFactory.createSwitchOperation(),
				fileOperationFactory.createCleanupOperation(), fileOperationFactory.createGetAllFilesOperation(),
				fileOperationFactory.createCheckoutAsOperation(), fileOperationFactory.createSetPropertyOperation(),
				fileOperationFactory.createGetPropertyOperation(false),
				fileOperationFactory.createRemovePropertyOperation(),
				fileOperationFactory.createGetPropertyOperation(true),
				fileOperationFactory.createMultipleCommitOperation(), fileOperationFactory.createRelocateOperation(),
				fileOperationFactory.createCopyOperation(), fileOperationFactory.createMoveOperation(),
				fileOperationFactory.createDeleteOperation(), fileOperationFactory.createCreatePatchOperation(),
				fileOperationFactory.createGetFileContentOperation(), fileOperationFactory.createLocalStatusOperation(),
				fileOperationFactory.createLockOperation(), fileOperationFactory.createUnlockOperation(),
				fileOperationFactory.createRemoteStatusOperation(), fileOperationFactory.createRevertOperation(),
				fileOperationFactory.createUpdateOperation());
	}

	public ActionOperationWorkflow buildCoreWorkflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createFileUtilityTestOperation(),
				localOperationFactory.createSvnUtilityTestOperation(), localOperationFactory.createAddToSvnOperation(),
				fileOperationFactory.createAddToSvnIgnoreOperation(), localOperationFactory.createCommitOperation(),
				remoteOperationFactory.createBranchTagOperation(), localOperationFactory.createSwitchOperation(),
				remoteOperationFactory.createCheckoutOperation(), localOperationFactory.createCleanupOperation(),
				localOperationFactory.createGetAllResourcesOperation(),
				localOperationFactory.createClearLocalStatusesOperation(),
				remoteOperationFactory.createGetLogMessagesOperation(),
				localOperationFactory.createRemoteStatusOperation(), localOperationFactory.createRevertOperation(),
				remoteOperationFactory.createCreateRemoteFolderOperation(),
				remoteOperationFactory.createRenameRemoteResourceOperation(),
				remoteOperationFactory.createGetFileContentOperation(),
				localOperationFactory.createGetRemoteContentsOperation(),
				remoteOperationFactory.createGetResourceAnnotationOperation(),
				localOperationFactory.createInfoOperation(), localOperationFactory.createMoveLocalResourceOperation(),
				remoteOperationFactory.createDeleteRemoteResourceOperation(),
				localOperationFactory.createUpdateOperation(),
				localOperationFactory.createDisconnectWithoutDropOperation(),
				localOperationFactory.createReconnectExistingProjectOperation(),
				localOperationFactory.createCopyLocalResourceOperation(),
				localOperationFactory.createDeleteLocalResourceOperation(),
				localOperationFactory.createDisconnectWithDropOperation(),
				remoteOperationFactory.createDiscardRepositoryOperation());
	}

	public ActionOperationWorkflow buildShareAddCommitWorkflow() {
		return new ActionOperationWorkflow(localOperationFactory.createShareNewProjectOperation(),
				localOperationFactory.createAddToSvnOperation(), localOperationFactory.createCommitOperation());
	}

}
