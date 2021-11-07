package org.eclipse.team.svn.tests.workflow.repository;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.core.misc.TestUtil;

/**
 * This class manages a {@link IRepositoryLocation} for a
 * {@link SVNRemoteStorage}.
 * 
 * @author Nicolas Peifer
 */
public class RemoteTestRepositoryManager extends FileTestRepositoryManager {

	public RemoteTestRepositoryManager() {
		this.svnStorage = SVNRemoteStorage.instance();
	}

	@Override
	public synchronized void createRepository() throws Exception {
		super.createRepository();
		initProject(TestUtil.getFirstProject());
		initProject(TestUtil.getSecondProject());
	}

	private void initProject(IProject project) throws CoreException {
		project.create(null);
		project.open(null);
		FileUtility.removeSVNMetaInformation(project, new NullProgressMonitor());
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		SVNRemoteStorage.instance().refreshLocalResources(new IResource[] { project }, IResource.DEPTH_INFINITE);
	}

}
