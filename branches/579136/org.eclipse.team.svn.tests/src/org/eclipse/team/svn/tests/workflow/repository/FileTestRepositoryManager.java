package org.eclipse.team.svn.tests.workflow.repository;

import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.operation.remote.CreateFolderOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.TestPlugin;

public class FileTestRepositoryManager implements TestRepositoryManager {
	protected IRepositoryLocation repositoryLocation;
	protected ISVNStorage svnStorage;
	protected ResourceBundle bundle = TestPlugin.instance().getResourceBundle();

	public FileTestRepositoryManager() {
		this.svnStorage = SVNFileStorage.instance();
	}

	@Override
	public synchronized void createRepository() throws Exception {
		// NIC compare with SVNTeamPlugin.start(..) and TestPlugin.start(..) and
		// TestUtil.setUpRemoteRepository()
		// this extended version was taken from TestWorkflow

		HashMap<String, Object> preferences = new HashMap<String, Object>();
		preferences.put(ISVNStorage.PREF_STATE_INFO_LOCATION, TestPlugin.instance().getStateLocation());
		svnStorage.initialize(preferences);

		this.repositoryLocation = svnStorage.newRepositoryLocation();
		this.repositoryLocation.setUrl(bundle.getString("Repository.URL"));
		this.repositoryLocation.setTrunkLocation(bundle.getString("Repository.Trunk"));
		this.repositoryLocation.setBranchesLocation(bundle.getString("Repository.Branches"));
		this.repositoryLocation.setTagsLocation(bundle.getString("Repository.Tags"));
		this.repositoryLocation.setStructureEnabled(true);
		this.repositoryLocation.setLabel(bundle.getString("Repository.Label"));
		this.repositoryLocation.setUsername(bundle.getString("Repository.Username"));
		this.repositoryLocation.setPassword(bundle.getString("Repository.Password"));
		this.repositoryLocation.setPasswordSaved("true".equals(bundle.getString("Repository.SavePassword")));

		svnStorage.addRepositoryLocation(this.repositoryLocation);
		this.repositoryLocation = svnStorage.getRepositoryLocation(this.repositoryLocation.getId());

		this.deleteRepositoryNode(SVNUtility.getProposedTrunk(this.repositoryLocation));
		this.deleteRepositoryNode(SVNUtility.getProposedBranches(this.repositoryLocation));
		this.deleteRepositoryNode(SVNUtility.getProposedTags(this.repositoryLocation));

		CreateFolderOperation op = new CreateFolderOperation(this.repositoryLocation.getRoot(),
				this.repositoryLocation.getTrunkLocation(), "create trunk");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.repositoryLocation.getRoot(), this.repositoryLocation.getBranchesLocation(),
				"create branches");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.repositoryLocation.getRoot(), this.repositoryLocation.getTagsLocation(),
				"createTags");
		op.run(new NullProgressMonitor());

	}

	@Override
	public synchronized void removeRepository() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		try {
			root.delete(true, true, null);
		} catch (Exception ex) {
		}

		try {
			this.cleanRepositoryNode(SVNUtility.getProposedTags(this.repositoryLocation));
		} catch (Exception ex) {
		}
		try {
			this.cleanRepositoryNode(SVNUtility.getProposedBranches(this.repositoryLocation));
		} catch (Exception ex) {
		}
		try {
			this.cleanRepositoryNode(SVNUtility.getProposedTrunk(this.repositoryLocation));
		} catch (Exception ex) {
		}

		ISVNStorage storage = svnStorage;
		IRepositoryLocation[] locations = storage.getRepositoryLocations();

		for (int i = 0; i < locations.length; i++) {
			locations[i].dispose();
			try {
				storage.removeRepositoryLocation(locations[i]);
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public IRepositoryLocation getRepositoryLocation() {
		return repositoryLocation;
	}

	private void deleteRepositoryNode(IRepositoryContainer node) throws Exception {
		if (node.exists()) {
			DeleteResourcesOperation op = new DeleteResourcesOperation(new IRepositoryResource[] { node },
					"test delete");
			op.run(new NullProgressMonitor());
		}
	}

	private void cleanRepositoryNode(IRepositoryContainer node) throws Exception {
		if (node.exists()) {
			IRepositoryResource[] children = node.getChildren();
			if (children != null && children.length > 0) {
				String[] toDelete = new String[children.length];
				for (int i = 0; i < children.length; i++) {
					toDelete[i] = SVNUtility.encodeURL(children[i].getUrl());
				}
				ISVNConnector proxy = this.repositoryLocation.acquireSVNProxy();
				try {
					proxy.removeRemote(toDelete, "Test Done", ISVNConnector.Options.FORCE, null,
							new SVNNullProgressMonitor());
				} finally {
					this.repositoryLocation.releaseSVNProxy(proxy);
				}
			}
		}
	}

}
