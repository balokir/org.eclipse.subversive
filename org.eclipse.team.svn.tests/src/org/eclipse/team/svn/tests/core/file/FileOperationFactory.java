package org.eclipse.team.svn.tests.core.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.file.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.file.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.file.CommitOperation;
import org.eclipse.team.svn.core.operation.file.CreatePatchOperation;
import org.eclipse.team.svn.core.operation.file.GetAllFilesOperation;
import org.eclipse.team.svn.core.operation.file.GetFileContentOperation;
import org.eclipse.team.svn.core.operation.file.LocalStatusOperation;
import org.eclipse.team.svn.core.operation.file.LockOperation;
import org.eclipse.team.svn.core.operation.file.RemoteStatusOperation;
import org.eclipse.team.svn.core.operation.file.RevertOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.operation.file.SwitchOperation;
import org.eclipse.team.svn.core.operation.file.UnlockOperation;
import org.eclipse.team.svn.core.operation.file.UpdateOperation;
import org.eclipse.team.svn.core.operation.file.management.CleanupOperation;
import org.eclipse.team.svn.core.operation.file.management.DisconnectOperation;
import org.eclipse.team.svn.core.operation.file.management.RelocateOperation;
import org.eclipse.team.svn.core.operation.file.management.ShareOperation;
import org.eclipse.team.svn.core.operation.file.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.file.property.RemovePropertyOperation;
import org.eclipse.team.svn.core.operation.file.property.SetPropertyOperation;
import org.eclipse.team.svn.core.operation.file.refactor.CopyOperation;
import org.eclipse.team.svn.core.operation.file.refactor.DeleteOperation;
import org.eclipse.team.svn.core.operation.file.refactor.MoveOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.misc.TestUtil;
import org.eclipse.team.svn.tests.workflow.repository.FileTestRepositoryManager;
import org.eclipse.team.svn.tests.workflow.repository.TestRepositoryManager;

public class FileOperationFactory {

	private static final String TEST_PROPERTY_NAME = "test-property";
	private static final String TEST_PROPERTY_VALUE = "test-value";

	public FileOperationFactory() throws Exception {
		TestRepositoryManager testRepositoryManager = new FileTestRepositoryManager();
		testRepositoryManager.createRepository();
	}

	public Supplier<IActionOperation> createAddToSvnIgnoreOperation() {
		return () -> {
			try {
				FileUtility.copyFile(new File(TestUtil.getFirstProjectFolder().getPath() + "/src"),
						new File(TestUtil.getSecondProjectFolder().getPath() + "/bumprev.sh"),
						new NullProgressMonitor());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new AddToSVNIgnoreOperation(
					new File[] { new File(TestUtil.getFirstProjectFolder().getPath() + "/src/bumprev.sh") },
					IRemoteStorage.IGNORE_NAME, "");
		};
	}

	public Supplier<IActionOperation> createAddToSvnOperation() {
		return () -> {
			List<File> toCommit = new ArrayList<File>();
			File[] files = TestUtil.getFirstProjectFolder().listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!TestUtil.isSVNInternals(files[i])) {
					toCommit.add(files[i]);
				}
			}
			files = TestUtil.getSecondProjectFolder().listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!TestUtil.isSVNInternals(files[i])) {
					toCommit.add(files[i]);
				}
			}

			AddToSVNOperation mainOp = new AddToSVNOperation(toCommit.toArray(new File[toCommit.size()]), true);
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			return op;
		};
	}

	public Supplier<IActionOperation> createBranchTagOperation() {
		return () -> {
			SVNFileStorage storage = SVNFileStorage.instance();
			IRepositoryResource branchTagResource = storage.asRepositoryResource(TestUtil.getFirstProjectFolder(),
					true);
			PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation("Branch",
					new IRepositoryResource[] { branchTagResource },
					SVNUtility.getProposedBranches(TestUtil.getRepositoryLocation()), "test branch", false);
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new PreparedBranchTagOperation("Tag", new IRepositoryResource[] { branchTagResource },
					SVNUtility.getProposedTags(TestUtil.getRepositoryLocation()), "test branch", false));
			return op;
		};
	}

	public Supplier<IActionOperation> createCheckoutAsOperation() {
		return () -> {
			IRepositoryResource from = TestUtil.getRepositoryLocation()
					.asRepositoryContainer(SVNUtility.getProposedTrunkLocation(TestUtil.getRepositoryLocation()) + "/"
							+ TestUtil.getFirstProjectFolder().getName(), false);
			CompositeOperation composite = new CompositeOperation("Checkout", SVNMessages.class);
			for (int i = 0; i < 10; i++) {
				File to = new File(TestUtil.getFirstProjectFolder().getPath() + "_checkout_" + i);
				CheckoutAsOperation op = new CheckoutAsOperation(to, from, SVNDepth.INFINITY, false, true);
				composite.add(op);
			}
			return composite;
		};
	}

	public Supplier<IActionOperation> createCommitOperation() {
		return () -> {
			List<File> toCommit = new ArrayList<File>();
			File[] files = TestUtil.getFirstProjectFolder().listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!TestUtil.isSVNInternals(files[i])) {
					toCommit.add(files[i]);
				}
			}
			files = TestUtil.getSecondProjectFolder().listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!TestUtil.isSVNInternals(files[i])) {
					toCommit.add(files[i]);
				}
			}
			return new CommitOperation(toCommit.toArray(new File[toCommit.size()]), "test commit", true, false);
		};
	}

	public Supplier<IActionOperation> createCreatePatchOperation() {
		return () -> new CreatePatchOperation(TestUtil.getFirstProjectFolder(),
				TestUtil.getSecondProjectFolder() + "/test.patch", true, true, true, true);
	}

	public Supplier<IActionOperation> createGetAllFilesOperation() {
		return () -> new GetAllFilesOperation(TestUtil.getFirstProjectFolder());
	}

	public Supplier<IActionOperation> createGetFileContentOperation() {
		return () -> {
			OutputStream out = null;
			try {
				out = new FileOutputStream(TestUtil.getFirstProjectFolder().getPath() + "/maven.xml");
			} catch (IOException e) {
				assertFalse(e.getMessage(), true);
			}
			return new GetFileContentOperation(
					new File(TestUtil.getFirstProjectFolder().getPath() + "/plugin.properties"), SVNRevision.HEAD,
					SVNRevision.HEAD, out);
		};
	}

	public Supplier<IActionOperation> createLocalStatusOperation() {
		return () -> new LocalStatusOperation(new File[] { TestUtil.getFirstProjectFolder() }, true);
	}

	public Supplier<IActionOperation> createLockOperation() {
		return () -> new LockOperation(new File[] { new File(TestUtil.getFirstProjectFolder() + "/maven.xml") },
				"Lock Operation Test", true);
	}

	public Supplier<IActionOperation> createMultipleCommitOperation() {
		return () -> new AbstractFileOperation("Commit", SVNMessages.class, TestUtil.getListFilesRecursive()) {

			@Override
			public void runImpl(IProgressMonitor monitor) throws Exception {
				new SetPropertyOperation(this.operableData(), TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE.getBytes(), false)
						.run(monitor);
				LocalStatusOperation op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length > 0);
				new RevertOperation(this.operableData(), true).run(monitor);
				op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length == 1);
				new SetPropertyOperation(this.operableData(), TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE.getBytes(), false)
						.run(monitor);
				op = new LocalStatusOperation(this.operableData(), false);
				op.run(monitor);
				assertTrue(op.getStatuses().length > 0);
				new CommitOperation(this.operableData(), "testCommit", false, false).run(monitor);
			}
		};
	}

	public Supplier<IActionOperation> createRemoteStatusOperation() {
		return () -> new RemoteStatusOperation(TestUtil.getBothFolders(), true);
	}

	public Supplier<IActionOperation> createRevertOperation() {
		return () -> new RevertOperation(TestUtil.getWorkspaceFiles(), true);
	}

	public Supplier<IActionOperation> createSwitchOperation() {
		return () -> {
			IRepositoryResource switchDestination = TestUtil.getRepositoryLocation()
					.asRepositoryContainer(SVNUtility.getProposedBranchesLocation(TestUtil.getRepositoryLocation())
							+ "/" + TestUtil.getFirstProjectFolder().getName(), false);
			return new SwitchOperation(TestUtil.getFirstProjectFolder(), switchDestination, true);
		};
	}

	public Supplier<IActionOperation> createUnlockOperation() {
		return () -> new UnlockOperation(new File[] { new File(TestUtil.getFirstProjectFolder() + "/maven.xml") });
	}

	public Supplier<IActionOperation> createUpdateOperation() {
		return () -> new UpdateOperation(TestUtil.getBothFolders(), SVNRevision.HEAD, true);
	}

	public Supplier<IActionOperation> createDisconnectOperation() {
		return () -> new DisconnectOperation(new File[] { TestUtil.getFirstProjectFolder().getParentFile() });
	}

	public Supplier<IActionOperation> createShareOperation() {
		ShareOperation.IFolderNameMapper folderNameMapper = new ShareOperation.IFolderNameMapper() {
			@Override
			public String getRepositoryFolderName(File folder) {
				return folder.getName();
			}
		};
		return () -> new ShareOperation(TestUtil.getBothFolders(), TestUtil.getRepositoryLocation(), folderNameMapper,
				"rootName", ShareOperation.LAYOUT_DEFAULT, true, "Share Project test", true);

	}

	public Supplier<IActionOperation> createCleanupOperation() {
		return () -> new CleanupOperation(TestUtil.getBothFolders());
	}

	public Supplier<IActionOperation> createSetPropertyOperation() {
		return () -> new SetPropertyOperation(TestUtil.getListFilesRecursive(), TEST_PROPERTY_NAME,
				TEST_PROPERTY_VALUE.getBytes(), false);
	}

	public Supplier<IActionOperation> createGetPropertyOperation(final boolean removed) {
		return () -> new AbstractFileOperation("Get Properties Operation Test", SVNMessages.class,
				TestUtil.getWorkspaceFiles()) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				GetPropertiesOperation getOp = new GetPropertiesOperation(TestUtil.getFirstProjectFolder());
				getOp.run(monitor);
				boolean containsTestProperty = false;
				SVNProperty[] properties = getOp.getProperties();
				for (int i = 0; i < properties.length; i++) {
					if (properties[i].name.equals(TEST_PROPERTY_NAME)
							&& properties[i].value.equals(TEST_PROPERTY_VALUE)) {
						containsTestProperty = true;
						break;
					}
				}
				if (removed) {
					assertFalse(containsTestProperty);
				} else {
					assertTrue(containsTestProperty);
				}
			}
		};
	}

	public Supplier<IActionOperation> createRemovePropertyOperation() {
		return () -> new RemovePropertyOperation(new File[] { TestUtil.getFirstProjectFolder() },
				new String[] { TEST_PROPERTY_NAME }, false);
	}

	public Supplier<IActionOperation> createRelocateOperation() {
		return () -> new AbstractFileOperation("Relocate", SVNMessages.class, TestUtil.getBothFolders()) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNFileStorage storage = SVNFileStorage.instance();
				IRepositoryLocation newLocation = TestUtil.getRepositoryLocation();
				String old = newLocation.getUrl();
				new RelocateOperation(this.operableData(), "http://testurl").run(monitor);
				IRepositoryResource remote = storage.asRepositoryResource(TestUtil.getFirstProjectFolder(), true);
				new RelocateOperation(this.operableData(), old).run(monitor);
				remote = storage.asRepositoryResource(TestUtil.getFirstProjectFolder(), true);
				assertTrue("Relocate Operation Test", remote.exists());
			}
		};
	}

	public Supplier<IActionOperation> createCopyOperation() {
		return () -> new CopyOperation(
				new File[] { new File(TestUtil.getFirstProjectFolder().getPath() + "/maven.xml") },
				new File(TestUtil.getFirstProjectFolder().getPath() + "/src"), true);
	}

	public Supplier<IActionOperation> createMoveOperation() {
		return () -> new MoveOperation(
				new File[] { new File(TestUtil.getFirstProjectFolder().getPath() + "/build.properties") },
				new File(TestUtil.getFirstProjectFolder().getPath() + "/src"), true);
	}

	public Supplier<IActionOperation> createDeleteOperation() {
		return () -> new DeleteOperation(
				new File[] { new File(TestUtil.getFirstProjectFolder().getPath() + "/src/build.properties"),
						new File(TestUtil.getFirstProjectFolder().getPath() + "/src/maven.xml") });
	}

}
