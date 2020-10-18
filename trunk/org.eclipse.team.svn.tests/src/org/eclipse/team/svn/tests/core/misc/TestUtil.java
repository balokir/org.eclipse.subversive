package org.eclipse.team.svn.tests.core.misc;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.tests.TestPlugin;

public class TestUtil {
	static {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		nameOfProject1 = bundle.getString("Project1.Name");
		nameOfProject2 = bundle.getString("Project2.Name");
	}

	public static String nameOfProject1;
	public static String nameOfProject2;

	private static Object lockObject = new Object();

	public static IProject getFirstProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(nameOfProject1);
	}

	public static IProject getSecondProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(nameOfProject2);
	}

	public static File getFirstProjectFolder() {
		return toFile(nameOfProject1);
	}

	public static File getSecondProjectFolder() {
		return toFile(nameOfProject2);
	}

	private static File toFile(String projectName) {
		return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getPath() + "/" + projectName);
	}

	public static void refreshProjects() {
		final boolean[] refreshDone = new boolean[1];
		IResourceStatesListener listener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				synchronized (lockObject) {
					refreshDone[0] = true;
					lockObject.notify();
				}
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, listener);
		try {
			if (!TestUtil.getFirstProject().isSynchronized(IResource.DEPTH_INFINITE)
					|| !TestUtil.getSecondProject().isSynchronized(IResource.DEPTH_INFINITE)) {
				TestUtil.getFirstProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				TestUtil.getSecondProject().refreshLocal(IResource.DEPTH_INFINITE, null);

				synchronized (lockObject) {
					if (!refreshDone[0]) {
						lockObject.wait(120000);
					}
					if (!refreshDone[0]) {
						throw new RuntimeException("No refresh event is generated !");
					}
				}
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, listener);
		}
	}

	public static File[] getWorkspaceFiles() {
		return TestUtil.getFirstProjectFolder().getParentFile().listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.getName().equals(".metadata");
			}
		});
	}

	public static File[] getBothFolders() {
		return new File[] { TestUtil.getFirstProjectFolder(), TestUtil.getSecondProjectFolder() };
	}

	public static File[] getListFilesRecursive() {
		List<File> allFiles = new ArrayList<File>();
		getFilesRecursiveImpl(getWorkspaceFiles(), allFiles);
		return allFiles.toArray(new File[allFiles.size()]);
	}

	public static void getFilesRecursiveImpl(File[] roots, List<File> allFiles) {
		for (int i = 0; i < roots.length; i++) {
			allFiles.add(roots[i]);
			if (roots[i].isDirectory()) {
				getFilesRecursiveImpl(roots[i].listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return !pathname.getName().equals(SVNUtility.getSVNFolderName());
					}
				}), allFiles);
			}
		}
	}

	public static boolean isSVNInternals(File file) {
		if (SVNUtility.getSVNFolderName().equals(file.getName())) {
			return true;
		}
		File parent = file.getParentFile();
		return parent == null ? false : isSVNInternals(parent);
	}

	public static IRepositoryLocation getRepositoryLocation() {
		return SVNRemoteStorage.instance().getRepositoryLocations()[0];
	}
}
