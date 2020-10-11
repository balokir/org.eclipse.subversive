package org.eclipse.team.svn.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.CreateFolderOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.remote.GetResourceAnnotationOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.operation.remote.RenameResourceOperation;
import org.eclipse.team.svn.core.operation.remote.management.DiscardRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.core.misc.TestUtil;

public class RemoteOperationFactory {
	public IActionOperation createCheckoutOperation() {
		IRepositoryResource trunk = SVNUtility.getProposedTrunk(TestUtil.getLocation());
		CheckoutAsOperation mainOp = new CheckoutAsOperation(TestUtil.getFirstProject().getName(),
				trunk.asRepositoryContainer(TestUtil.getFirstProject().getName(), false), SVNDepth.INFINITY, true);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		op.add(new CheckoutAsOperation(TestUtil.getSecondProject().getName(),
				trunk.asRepositoryContainer(TestUtil.getSecondProject().getName(), false), SVNDepth.INFINITY, true));
		return op;
	}

	public IActionOperation createBranchTagOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource branchTagResource = storage.asRepositoryResource(TestUtil.getFirstProject());
		PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation("Branch",
				new IRepositoryResource[] { branchTagResource }, SVNUtility.getProposedBranches(TestUtil.getLocation()),
				"test branch", false);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		op.add(new PreparedBranchTagOperation("Tag", new IRepositoryResource[] { branchTagResource },
				SVNUtility.getProposedTags(TestUtil.getLocation()), "test branch", false));
		return op;
	}

	public IActionOperation createCreateRemoteFolderOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource parent = storage.asRepositoryResource(TestUtil.getFirstProject().getFolder("src"));
		return new CreateFolderOperation(parent, "testFolder", "");
	}

	public IActionOperation createDeleteRemoteResourceOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();

		IProject prj = TestUtil.getFirstProject();
		IRepositoryResource remote1 = storage.asRepositoryResource(prj.getFile("testProject.xml"));
		IRepositoryResource remote2 = storage.asRepositoryResource(prj.getFolder("src/testFolder2"));

		prj = TestUtil.getSecondProject();
		IRepositoryResource remote3 = storage.asRepositoryResource(prj.getFile("bumprev.sh"));

		return new DeleteResourcesOperation(new IRepositoryResource[] { remote1, remote2, remote3 }, "test delete");
	}

	public IActionOperation createGetFileContentOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		return new GetFileContentOperation(
				storage.asRepositoryResource(TestUtil.getFirstProject().getFile("maven.xml")));
	}

	public IActionOperation createGetLogMessagesOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		GetLogMessagesOperation mainOp = new GetLogMessagesOperation(
				storage.asRepositoryResource(TestUtil.getFirstProject()));
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		op.add(new GetLogMessagesOperation(storage.asRepositoryResource(TestUtil.getSecondProject())));
		return op;
	}

	public IActionOperation createDiscardRepositoryOperation() {
		return new DiscardRepositoryLocationsOperation(new IRepositoryLocation[] { TestUtil.getLocation() });
	}

	public IActionOperation createGetResourceAnnotationOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		return new GetResourceAnnotationOperation(
				storage.asRepositoryResource(TestUtil.getFirstProject().getFile("maven.xml")),
				new SVNRevisionRange(SVNRevision.fromNumber(0), SVNRevision.HEAD));
	}

	public IActionOperation createRenameRemoteResourceOperation() {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource forRename = storage
				.asRepositoryResource(TestUtil.getFirstProject().getFolder("src/testFolder"));
		return new RenameResourceOperation(forRename, "testFolder2", "RenameRemoteResourceOperation Test");
	}
}
