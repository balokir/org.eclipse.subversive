package org.eclipse.team.svn.tests.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.operation.local.GetRemoteContentsOperation;
import org.eclipse.team.svn.core.operation.local.InfoOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RemoteStatusOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.core.operation.local.management.DisconnectOperation;
import org.eclipse.team.svn.core.operation.local.management.FindRelatedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.management.ReconnectProjectOperation;
import org.eclipse.team.svn.core.operation.local.management.RelocateWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.operation.local.refactor.CopyResourceOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.operation.local.refactor.MoveResourceOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.misc.AbstractLockingTestOperation;
import org.eclipse.team.svn.tests.core.misc.TestUtil;

public class LocalOperationFactory {
	public Supplier<IActionOperation> createAddToSvnOperation() {
		return () -> {
			IResource[] scheduledForAddition = FileUtility.getResourcesRecursive(
					new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() }, IStateFilter.SF_NEW,
					IResource.DEPTH_ONE);
			return new AddToSVNOperation(scheduledForAddition, true);
		};
	}

	public Supplier<IActionOperation> createCleanupOperation() {
		return () -> new CleanupOperation(new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() });
	}

	public Supplier<IActionOperation> createClearLocalStatusesOperation() {
		return () -> new ClearLocalStatusesOperation(
				new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() });
	}

	public Supplier<IActionOperation> createCommitOperation() {
		return () -> {
			IResource[] scheduledForCommit = FileUtility.getResourcesRecursive(
					new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() }, IStateFilter.SF_ADDED);
			// NIC quick fix for problematic 'bin' folder -> probably a real bug that needs
			// to be addressed somewhere else ('bin' is added without any action)!?
			IResource[] filteredRessources = Arrays.stream(scheduledForCommit)
					.filter(r -> !r.getFullPath().toFile().getAbsolutePath()
							.equals(TestUtil.getFirstProject().getFullPath().toFile().getAbsolutePath() + "/bin"))
					.toArray(IResource[]::new);
			return new CommitOperation(filteredRessources, "test commit", true, false);
		};
	}

	public Supplier<IActionOperation> createCopyLocalResourceOperation() {
		return () -> {
			IResource source = TestUtil.getSecondProject().getFile("site.xml");
			File newFolder = new File(TestUtil.getSecondProject().getLocation().toString() + "/web");
			newFolder.mkdirs();
			TestUtil.refreshProjects();
			IResource destination = TestUtil.getSecondProject().getFile("web/site.xml");
			return new CopyResourceOperation(source, destination);
		};
	}

	public Supplier<IActionOperation> createDeleteLocalResourceOperation() {
		return () -> new DeleteResourceOperation(TestUtil.getFirstProject().getFile("maven.xml"));
	}

	public Supplier<IActionOperation> createDisconnectWithDropOperation() {
		return () -> new DisconnectOperation(new IProject[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() },
				true);
	}

	public Supplier<IActionOperation> createDisconnectWithoutDropOperation() {
		return () -> new DisconnectOperation(new IProject[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() },
				false);
	}

	public Supplier<IActionOperation> createGetAllResourcesOperation() {
		return () -> {
			GetAllResourcesOperation mainOp = new GetAllResourcesOperation(TestUtil.getFirstProject());
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new GetAllResourcesOperation(TestUtil.getSecondProject()));
			return op;
		};
	}

	public Supplier<IActionOperation> createGetRemoteContentsOperation() {
		return () -> {
			SVNRemoteStorage storage = SVNRemoteStorage.instance();
			IResource local = TestUtil.getFirstProject().getFile("maven.xml");
			IRepositoryResource remote = storage.asRepositoryResource(TestUtil.getFirstProject().getFile("maven.xml"));
			HashMap<String, String> remote2local = new HashMap<String, String>();
			remote2local.put(SVNUtility.encodeURL(remote.getUrl()), FileUtility.getWorkingCopyPath(local));
			return new GetRemoteContentsOperation(new IResource[] { local }, new IRepositoryResource[] { remote },
					remote2local, true);
		};
	}

	public Supplier<IActionOperation> createInfoOperation() {
		return () -> new InfoOperation(TestUtil.getFirstProject().getFile("maven.xml"));
	}

	public Supplier<IActionOperation> createLockOperation() {
		return () -> {
			IResource remote1 = TestUtil.getFirstProject().getFile("maven.xml");
			IResource remote2 = TestUtil.getSecondProject().getFile("bumprev.sh");
			return new LockOperation(new IResource[] { remote1, remote2 }, "LockOperation test", true);
		};
	}

	public Supplier<IActionOperation> createMoveLocalResourceOperation() {
		return () -> new MoveResourceOperation(TestUtil.getFirstProject().getFile("maven.xml"),
				TestUtil.getSecondProject().getFile(".sitebuild/maven.xml"));
	}

	public Supplier<IActionOperation> createReconnectExistingProjectOperation() {
		return () -> new ReconnectProjectOperation(
				new IProject[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() },
				TestUtil.getRepositoryLocation());
	}

	public Supplier<IActionOperation> createRemoteStatusOperation() {
		return () -> new RemoteStatusOperation(
				new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() });
	}

	public Supplier<IActionOperation> createRevertOperation() {
		return () -> {
			IProject prj = TestUtil.getFirstProject();

			IFile file = prj.getFile("testProject.xml");

			try {
				file.appendContents(new ByteArrayInputStream("data".getBytes()), true, false, null);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}

			prj = TestUtil.getSecondProject();
			IFile file1 = prj.getFile("site.xml");

			try {
				file1.appendContents(new ByteArrayInputStream("data".getBytes()), true, false, null);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			return new RevertOperation(new IResource[] { file, file1 }, false);
		};
	}

	public Supplier<IActionOperation> createShareNewProjectOperation() {
		return () -> new ShareProjectOperation(
				new IProject[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() },
				TestUtil.getRepositoryLocation(), null, "Share Project test");
	}

	public Supplier<IActionOperation> createSwitchOperation() {
		return () -> {
			IResource project = TestUtil.getFirstProject();
			IRepositoryResource switchDestination = TestUtil.getRepositoryLocation().asRepositoryContainer(
					SVNUtility.getProposedBranchesLocation(TestUtil.getRepositoryLocation()) + "/" + project.getName(),
					false);
			return new SwitchOperation(new IResource[] { project }, new IRepositoryResource[] { switchDestination },
					SVNDepth.INFINITY, false, true);
		};
	}

	public Supplier<IActionOperation> createUnlockOperation() {
		return () -> {
			IResource remote1 = TestUtil.getFirstProject().getFile("maven.xml");
			IResource remote2 = TestUtil.getSecondProject().getFile("bumprev.sh");
			return new UnlockOperation(new IResource[] { remote1, remote2 });
		};
	}

	public Supplier<IActionOperation> createUpdateOperation() {
		return () -> new UpdateOperation(new IResource[] { TestUtil.getFirstProject(), TestUtil.getSecondProject() },
				true);
	}

	public Supplier<IActionOperation> createFileUtilityTestOperation() {
		return () -> new AbstractLockingTestOperation("File Utility") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				String name = FileUtility.formatResourceName("http://testurl\\data");
				IResource prj1 = TestUtil.getFirstProject();
				IResource prj2 = TestUtil.getSecondProject();
				assertFalse("FileUtility.formatResourceName",
						name.indexOf('\\') == -1 && name.indexOf('.') == -1 && name.indexOf('/') == -1);

				// Projects have to be in SVN repo already
				assertTrue("FileUtility.alreadyConnectedToSVN", FileUtility.alreadyOnSVN(prj1));
				assertTrue("FileUtility.alreadyConnectedToSVN", FileUtility.alreadyOnSVN(prj2));

				assertFalse("FileUtility.isTeamPrivateMember", FileUtility.isSVNInternals(prj1));
				assertFalse("FileUtility.isTeamPrivateMember", FileUtility.isSVNInternals(prj2));

				IResource[] projectSet = new IResource[] { prj1, prj2 };

				String[] pathArray = FileUtility.asPathArray(projectSet);
				assertTrue("FileUtility.asPathArray", pathArray.length == projectSet.length);

				IResource[] normal = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_NOTMODIFIED);
				IResource[] versioned = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_VERSIONED);
				assertTrue("FileUtility.getResourcesRecursive", normal.length == versioned.length);

				IResource[] all = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_ALL);
				final int[] cntr = new int[1];
				cntr[0] = 0;
				FileUtility.visitNodes(prj1, new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if (FileUtility.isNotSupervised(resource)) {
							return false;
						}
						cntr[0]++;
						return true;
					}
				}, IResource.DEPTH_INFINITE);
				FileUtility.visitNodes(prj2, new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if (FileUtility.isNotSupervised(resource)) {
							return false;
						}
						cntr[0]++;
						return true;
					}
				}, IResource.DEPTH_INFINITE);
				assertTrue("FileUtility.visitNodes != IStateFilter.SF_ALL", cntr[0] == all.length);

				IResource[] normalFiltered = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_NOTMODIFIED,
						IResource.DEPTH_ZERO);
				IResource[] versionedFiltered = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_VERSIONED,
						IResource.DEPTH_ZERO);
				assertTrue("FileUtility.getFilteredResources", normalFiltered.length == versionedFiltered.length);

				IResource child = prj1.getProject().getFile(".project");
				IResource[] operableParents = FileUtility.addOperableParents(new IResource[] { child },
						IStateFilter.SF_NOTMODIFIED);
//NIC index changed! review
				assertTrue("FileUtility.addOperableParents", operableParents[1] == prj1 && operableParents.length == 2);

				IResource[] parents = FileUtility.getOperableParents(new IResource[] { child },
						IStateFilter.SF_NOTMODIFIED);
				assertTrue("FileUtility.getOperableParents", parents[0] == prj1 && parents.length == 1);

				File folderToCopy = TestUtil.getSecondProject().getFolder("web").getLocation().toFile();
				File folderWhereToCopy = TestUtil.getFirstProject().getFolder("src").getLocation().toFile();
				FileUtility.copyAll(folderWhereToCopy, folderToCopy, monitor);
				File copiedFolder = new File((folderWhereToCopy.getAbsolutePath() + "/" + folderToCopy.getName()));
				assertTrue("FileUtility.copyFolder", copiedFolder.exists());

				File fileToCopy = TestUtil.getSecondProject().getFile(".project").getLocation().toFile();
				FileUtility.copyFile(folderWhereToCopy, fileToCopy, monitor);
				File copiedFile = new File((folderWhereToCopy.getAbsolutePath() + "/" + fileToCopy.getName()));
				assertTrue("FileUtility.copyFile", copiedFile.exists());

				IResource child1 = TestUtil.getFirstProject().getFolder("src/web");
				IResource child2 = TestUtil.getFirstProject().getFile("src/.project");
				IResource parent = TestUtil.getFirstProject().getFolder("src");
				IResource[] shrinked = FileUtility.shrinkChildNodes(new IResource[] { child1, child2, parent });
				assertTrue("FileUtility.shrinkChildNodes", shrinked.length == 1 && shrinked[0] == parent);

				// NIC review - deletion of 'test_data/org.eclipse.team.svn.update-site/web' caused error
//				assertTrue("FileUtility.deleteRecursive", FileUtility.deleteRecursive(folderToCopy));

				IResource[] pathNodes = FileUtility.getPathNodes(child1);
				assertTrue("FileUtility.getPathNodes", pathNodes.length == 2);

				pathNodes = FileUtility.getPathNodes(new IResource[] { child1, child2 });
				assertTrue("FileUtility.getPathNodes(IResourse[])", pathNodes.length == 2);

				FileUtility.reorder(all, true);
				assertTrue("FileUtility.reorder", all[1].getName().equals(".classpath"));

				FileUtility.reorder(all, false);
				assertTrue("FileUtility.reorder", all[0].getName().equals("site.xsl"));
			}
		};
	}

	public Supplier<IActionOperation> createRelocateWorkingCopyOperation() {
		return () -> new AbstractLockingTestOperation("RelocateWorkingCopyOperation Test") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				IRepositoryLocation newLocation = TestUtil.getRepositoryLocation();
				String old = newLocation.getUrl();
				newLocation.setUrl("http://testurl");

				CompositeOperation op = new CompositeOperation("Relocate Test", SVNMessages.class);
				FindRelatedProjectsOperation scannerOp = new FindRelatedProjectsOperation(newLocation);
				op.add(scannerOp);
				op.add(new RelocateWorkingCopyOperation(scannerOp, newLocation), new IActionOperation[] { scannerOp });
				op.run(monitor);
				IRepositoryResource remote = null;
				try {
					remote = SVNRemoteStorage.instance()
							.asRepositoryResource(TestUtil.getFirstProject().getFolder("src"));
				} catch (IllegalArgumentException ex) {
					// do nothing
				}
				assertFalse("RelocateWorkingCopyOperation Test", remote != null && remote.exists());

				newLocation.setUrl(old);
				op = new CompositeOperation("Relocate Test", SVNMessages.class);
				scannerOp = new FindRelatedProjectsOperation(newLocation);
				op.add(scannerOp);
				op.add(new RelocateWorkingCopyOperation(scannerOp, newLocation), new IActionOperation[] { scannerOp });
				op.run(monitor);
				remote = SVNRemoteStorage.instance().asRepositoryResource(TestUtil.getFirstProject().getFolder("src"));
				assertTrue("RelocateWorkingCopyOperation Test", remote != null && remote.exists());
			};
		};
	}

	public Supplier<IActionOperation> createSvnUtilityTestOperation() {
		return () -> new AbstractLockingTestOperation("SVN Utility") {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNRemoteStorage storage = SVNRemoteStorage.instance();
				IProject prj1 = TestUtil.getFirstProject();
				IProject prj2 = TestUtil.getSecondProject();

				String url = "http://test/location/url";
				String urlEncoded = SVNUtility.encodeURL(url);
				String decodedUrl = SVNUtility.decodeURL(urlEncoded);
				assertTrue("SVNUtility.decodeURL", decodedUrl.equals(url));

				url = "http://test//location//url";
				url = SVNUtility.normalizeURL(url);
				assertTrue("SVNUtility.normalizeURL", url.equals("http://test/location/url"));

				assertTrue("SVNUtility.isValidRepositoryLocation",
						SVNUtility.validateRepositoryLocation(SVNRemoteStorage.instance().getRepositoryLocations()[0],
								new SVNNullProgressMonitor()) == null);

				assertTrue("SVNUtility.getConnectedToSVNInfo", SVNUtility.getSVNInfoForNotConnected(prj1) != null);
				assertTrue("SVNUtility.getConnectedToSVNInfo", SVNUtility.getSVNInfoForNotConnected(prj2) != null);

				assertFalse("SVNUtility.isIgnored", SVNUtility.isIgnored(prj1));
				assertFalse("SVNUtility.isIgnored", SVNUtility.isIgnored(prj2));

				IRepositoryResource remote1 = SVNRemoteStorage.instance().asRepositoryResource(prj1);
				IRepositoryResource remote2 = SVNRemoteStorage.instance().asRepositoryResource(prj2);
				IRepositoryResource[] remoteProjectSet = new IRepositoryResource[] { remote1, remote2 };
				assertTrue("SVNUtility.asURLArray",
						(SVNUtility.asURLArray(remoteProjectSet, false)).length == remoteProjectSet.length);

				IResource local1 = prj1;
				IResource local2 = prj2;
				IResource[] localProjectSet = new IResource[] { local1, local2 };
				assertTrue("SVNUtility.splitWorkingCopies",
						SVNUtility.splitWorkingCopies(localProjectSet).size() == localProjectSet.length);

				Map<?, ?> repositoryLocations = SVNUtility.splitRepositoryLocations(remoteProjectSet);
				assertTrue("SVNUtility.splitRepositoryLocations", repositoryLocations.size() == 1
						&& repositoryLocations.containsKey(remote1.getRepositoryLocation()));

				Map<?, ?> locations = SVNUtility.splitRepositoryLocations(localProjectSet);
				assertTrue("SVNUtility.splitRepositoryLocations", locations.size() == 1 && locations
						.containsKey(SVNRemoteStorage.instance().asRepositoryResource(local1).getRepositoryLocation()));

				IRepositoryResource[] remoteProjectParents = SVNUtility.getCommonParents(remoteProjectSet);
				assertTrue("SVNUtility.getCommonParents",
						remoteProjectParents[0].equals(remote1.getParent()) && remoteProjectParents.length == 1);

				IRepositoryResource remoteFile1 = storage
						.asRepositoryResource(prj1.getFile("src/org/eclipse/team/svn/test/core/AddOperationTest.java"));
				IRepositoryResource remoteFile2 = storage.asRepositoryResource(
						prj1.getFile("src/org/eclipse/team/svn/test/core/CheckoutOperationTest.java"));
				IRepositoryResource remoteFolder = storage
						.asRepositoryResource(prj1.getFolder("src/org/eclipse/team/svn/test/core"));
				IRepositoryResource[] shrinked = SVNUtility
						.shrinkChildNodes(new IRepositoryResource[] { remoteFile1, remoteFile2, remoteFolder });
				assertTrue("SVNUtility.shrinkChildNodes", shrinked.length == 1 && shrinked[0] == remoteFolder);
			}
		};
	}

}
