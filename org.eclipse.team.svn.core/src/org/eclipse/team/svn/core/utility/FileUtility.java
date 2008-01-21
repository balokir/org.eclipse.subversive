/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Common file processing functions
 * 
 * @author Alexander Gurov
 */
public final class FileUtility {
	public static final IResource []NO_CHILDREN = new IResource[0];
	
	public static IResource selectOneOf(IResource []scope, IResource []set) {
		for (int i = 0; i < set.length; i++) {
			if (FileUtility.relatesTo(scope, set[i])) {
				return set[i];
			}
		}
		return null;
	}
	
	public static boolean relatesTo(IResource []set, IResource resource) {
		for (int i = 0; i < set.length; i++) {
			if (FileUtility.relatesTo(set[i], resource)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean relatesTo(IResource set, IResource resource) {
		return set.equals(resource) ? true : (resource == null ? false : FileUtility.relatesTo(set, resource.getParent()));
	}
	
	public static String getResource(ResourceBundle bundle, String key) {
		if (key == null) {
			return null;
		}
		if (bundle == null) {
			return key;
		}
		String retVal = FileUtility.getResourceImpl(bundle, key);
		if (retVal != null) {
			if (key.indexOf("Error") != -1) {
				String id = FileUtility.getResourceImpl(bundle, key + ".Id");
				if (id != null) {
					retVal = id + ": " + retVal;
				}
			}
			return retVal;
		}
		return key;
	}
	
	public static String getWorkingCopyPath(IResource resource) {
		return FileUtility.getResourcePath(resource).toString();
	}
	
	public static IPath getResourcePath(IResource resource) {
		IPath location = resource.getLocation();
		if (location == null) {
			String errMessage = SVNTeamPlugin.instance().getResource("Error.InaccessibleResource", new String[] {resource.getFullPath().toString()});
			throw new UnreportableException(errMessage);
		}
		return location;
	}
	
	public static Map getEnvironmentVariables() {
		try {
			Method getenv = System.class.getMethod("getenv", (Class [])null);
			return (Map)getenv.invoke(null, (Object [])null);
		}
		catch (Exception ex) {
			try {
				boolean isWindows = FileUtility.isWindows();
				Process p = isWindows ? Runtime.getRuntime().exec("cmd.exe /c set") : Runtime.getRuntime().exec("env");
	            
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            String varLine;
				HashMap retVal = new HashMap();
	            while ((varLine = br.readLine()) != null) {
	                int idx = varLine.indexOf('=');
	                if (idx != -1) {
		                String name = varLine.substring(0, idx);
		                retVal.put(isWindows ? name.toUpperCase() : name, varLine.substring(idx + 1));
	                }
	                else if (varLine.length() > 0) {
	                	retVal.put(varLine, "");
	                }
	            }
				return retVal;
			}
			catch (IOException ex1) {
				return Collections.EMPTY_MAP;
			}
		}
	}
	
	public static String normalizePath(String path) {
		return FileUtility.isWindows() ? path.replace('/', '\\') : path.replace('\\', '/');
	}
	
	public static boolean isWindows() {
		return FileUtility.getOSName().indexOf("windows") != -1;
	}
	
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}
	
	public static boolean isCaseInsensitiveOS() {
		return !(Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new java.io.File("a").compareTo(new java.io.File("A")) != 0);
	}
	
    public static boolean isLinked(IResource resource) {
    	// Eclipse 3.2 and higher
        return resource.isLinked(IResource.CHECK_ANCESTORS);
    }
    
	public static String []asPathArray(IResource []resources) {
	    String []retVal = new String[resources.length];
		for (int i = 0; i < resources.length; i++) {
			retVal[i] = FileUtility.normalizePath(FileUtility.getWorkingCopyPath(resources[i]));
		}
		return retVal;
	}
	
	public static String []asPathArray(File []files) {
	    String []retVal = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			retVal[i] = FileUtility.normalizePath(files[i].getAbsolutePath());
		}
		return retVal;
	}
	
	public static String getResourceParent(IRepositoryResource resource) {
		String parent = "";
		String url = resource.getUrl();
		String rootUrl = resource.getRoot().getUrl();
		if (url.equals(rootUrl)) {
			return "";
		}
		parent = url.substring(rootUrl.length(), url.length() - resource.getName().length() - 1);
		return parent;
	}
	
	public static String formatResourceName(String projectName) {
		// remove invalid characters when repository root was specified
		return PatternProvider.replaceAll(projectName, "([\\/:])+", ".");
	}
	
	public static String formatPath(String path) {
		return PatternProvider.replaceAll(path, "\\\\", "/");
	}
	
	public static String getUsernameParam(String username) {
		return username == null || username.trim().length() == 0 ? "" : " --username \"" + username + "\"";
	}
	
	public static String flattenText(String text) {
		StringBuffer flat = new StringBuffer(text.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (currentChar == '\r' || currentChar == '\n') {
				if (!skipAdjacentLineSeparator)
					flat.append("/"); 
				skipAdjacentLineSeparator = true;
			} else {
				flat.append(currentChar);
				skipAdjacentLineSeparator = false;
			}
		}
		return flat.toString().replace('\t', ' ');
	}
	
	public static int getMaxStringLength(String[] strings) {
		int result = 0;
		for (int i = 0; i < strings.length; i++) {
			result = Math.max(result, strings[i].length());
		}
		return result;
	}
	
	public static String formatMultilineText(String text) {
		if (text.length() > 0 && text.substring(0, 1).matches("(\\s)+")) {
			text = text.replaceFirst("(\\s)+", "");
		}
		if (text.length() == 0) {
			return "";
		}
		text = text.replace('\t', ' ');
		int idx = text.indexOf('\n');
		int idx1 = text.indexOf('\r');
		if (idx == -1) {
			idx = idx1;
		}
		idx = idx < idx1 || idx1 == -1 ? idx : idx1;
		if (idx != -1) {
			if (text.substring(idx).trim().length() != 0) {
				return text.substring(0, idx) + "...";
			}
			return text.substring(0, idx);
		}
		return text;
	}
	
	public static String[] decodeStringToArray(String encodedString) {
        String []valuesArray = new String[] {};
        if (encodedString != null && encodedString.length() > 0) {
            valuesArray = encodedString.split(";");
            for (int i = 0; i < valuesArray.length; i++) {
            	valuesArray[i] = new String(Base64.decode(valuesArray[i].getBytes()));
            }
        }
        return valuesArray;
    }
    
    public static String encodeArrayToString(String []valuesArray) {
        String result = "";
        for (int i = 0; i < valuesArray.length; i++) {
            String str = new String(Base64.encode(valuesArray[i].getBytes()));
            result += result.length() == 0 ? str : (";" + str);
		}
        return result;
    }
	
	public static void visitNodes(IResource resource, IResourceVisitor visitor, int depth) throws CoreException {
		boolean stepInside = visitor.visit(resource);
		if (stepInside &&
			resource instanceof IContainer && 
			depth != IResource.DEPTH_ZERO && 
			resource.isAccessible()) {
			
			IContainer container = (IContainer)resource;
			IResource []children = FileUtility.resourceMembers(container, true);
			int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
			for (int i = 0; i < children.length; i++) {
				FileUtility.visitNodes(children[i], visitor, nextDepth);
			}
		}
	}
	
	public static boolean checkForResourcesPresenceRecursive(IResource []roots, IStateFilter filter) {
		return FileUtility.checkForResourcesPresence(roots, filter, IResource.DEPTH_INFINITE);
	}
	
	public static boolean checkForResourcesPresence(IResource []roots, IStateFilter filter, int depth) {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		
		ArrayList recursiveCheck = null;
		int nextDepth = IResource.DEPTH_ZERO;
		if (depth != IResource.DEPTH_ZERO) {
			recursiveCheck = new ArrayList();
			nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		}
		
		// first check all resources that are already accessible (performance optimizations)
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].isTeamPrivateMember()) {//FileUtility.isSVNInternals(roots[i])
				continue;
			}
			
			ILocalResource local = storage.asLocalResource(roots[i]);
			if (local != null) {
				if (filter.accept(local)) {
					return true;
				}
				else if (roots[i] instanceof IContainer && depth != IResource.DEPTH_ZERO && filter.allowsRecursion(local)) {
					recursiveCheck.add(roots[i]);
				}
			}
		}
		
		// no resources accepted, check recursively (performance optimizations)
		if (depth != IResource.DEPTH_ZERO) {
			for (Iterator it = recursiveCheck.iterator(); it.hasNext(); ) {
				IContainer local = (IContainer)it.next();
				if (FileUtility.checkForResourcesPresence(FileUtility.getAllMembers(local), filter, nextDepth)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter) {
		return FileUtility.getResourcesRecursive(roots, filter, IResource.DEPTH_INFINITE);
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter, int depth) {
		return FileUtility.getResourcesRecursive(roots, filter, depth, null, null);
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter, int depth, IActionOperation calledFrom, IProgressMonitor monitor) {
		Set resources = new HashSet();
		FileUtility.addChildren(resources, roots, filter, depth, calledFrom, monitor);
		return (IResource [])resources.toArray(new IResource[resources.size()]);
	}
	
	public static IResource []addOperableParents(IResource []resources, IStateFilter stateFilter) {
		return FileUtility.addOperableParents(resources, stateFilter, false);		
	}
	
	public static IResource []addOperableParents(IResource []resources, IStateFilter stateFilter, boolean through) {
		HashSet tmp = new HashSet(Arrays.asList(resources));
		tmp.addAll(Arrays.asList(FileUtility.getOperableParents(resources, stateFilter, through)));
		return (IResource [])tmp.toArray(new IResource[tmp.size()]);
	}
	
	public static IResource []getOperableParents(IResource []resources, IStateFilter stateFilter) {
		return FileUtility.getOperableParents(resources, stateFilter, false);
	}
	
	public static IResource []getOperableParents(IResource []resources, IStateFilter stateFilter, boolean through) {
		HashSet tmp = new HashSet();
		IResource []parents = FileUtility.getParents(resources, true);
		if (!through) {
			FileUtility.reorder(parents, false);
		}
		for (int i = 0; i < parents.length; i++) {
			ILocalResource parent = SVNRemoteStorage.instance().asLocalResource(parents[i]);
			if (parent != null && stateFilter.accept(parent)) {
				tmp.add(parents[i]);
			}
			else if (!through) {
				IPath current = parents[i].getFullPath();
				while (i < parents.length) {
					if (parents[i].getFullPath().isPrefixOf(current)) {
						i++;
					}
					else {
						i--;
						break;
					}
				}
			}
		}
		return (IResource [])tmp.toArray(new IResource[tmp.size()]);
	}
	
	public static IResource []getParents(IResource []resources, boolean excludeIncoming) {
		HashSet parents = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource parent = resources[i];
			while ((parent = parent.getParent()) != null && !(parent instanceof IWorkspaceRoot)) {
				parents.add(parent);
			}
			if (parents == resources[i]) {
				parents.add(parent);
			}
		}
		if (excludeIncoming) {
			parents.removeAll(Arrays.asList(resources));
		}
		return (IResource [])parents.toArray(new IResource[parents.size()]);
	}
	
	public static boolean isSVNInternals(IResource resource) {
		if (SVNUtility.getSVNFolderName().equals(resource.getName())) {
			return true;
		}
		IResource parent = resource.getParent();
		return parent == null ? false : FileUtility.isSVNInternals(parent);
	}
	
	public static void findAndMarkSVNInternals(IResource node, boolean isTeamPrivate) throws CoreException {
		if (node instanceof IContainer && !FileUtility.isLinked(node)) {
			if (SVNUtility.getSVNFolderName().equals(node.getName()) && node.isTeamPrivateMember() != isTeamPrivate) {
			    FileUtility.markSVNInternalsTree(node, isTeamPrivate);
			}
			else {
				IResource []children = FileUtility.resourceMembers((IContainer)node, false);
				for (int i = 0; i < children.length; i++) {
					FileUtility.findAndMarkSVNInternals(children[i], isTeamPrivate);
				}
			}
		}
	}
	
	public static boolean deleteRecursive(File node) {
		return FileUtility.deleteRecursive(node, null);
	}
	
	public static boolean deleteRecursive(File node, IProgressMonitor monitor) {
		if (node.isDirectory()) {
			File []files = node.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length && (monitor == null || !monitor.isCanceled()); i++) {
					FileUtility.deleteRecursive(files[i], monitor);
				}
			}
		}
		return node.delete();
	}
	
	public static void copyAll(File to, File what, IProgressMonitor monitor) throws Exception {
		FileUtility.copyAll(to, what, false, monitor);
	}
	
	public static final int COPY_NO_OPTIONS = 0x00;
	public static final int COPY_IGNORE_EXISTING_FOLDERS = 0x01;
	public static final int COPY_OVERRIDE_EXISTING_FILES = 0x02;
	
	
	public static void copyAll(File to, File what, boolean ignoreExistingFolders, IProgressMonitor monitor) throws Exception {
		FileUtility.copyAll(to, what, ignoreExistingFolders ? FileUtility.COPY_IGNORE_EXISTING_FOLDERS : FileUtility.COPY_NO_OPTIONS, null, monitor);
	}
	
	public static void copyAll(File to, File what, int options, FileFilter filter, IProgressMonitor monitor) throws Exception {
		if (what.isDirectory()) {
			to = new File(to.getAbsolutePath() + "/" + what.getName());
			if (monitor.isCanceled()) {
				return;
			}
			if (!to.mkdirs() && ((options & FileUtility.COPY_IGNORE_EXISTING_FOLDERS) == 0)) {
				String errMessage = SVNTeamPlugin.instance().getResource("Error.CreateDirectory", new String[] {to.getAbsolutePath()});
				throw new Exception(errMessage);
			}
			File []files = what.listFiles(filter);
			if (files != null) {
				for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
					FileUtility.copyAll(to, files[i], options, filter, monitor);
				}
			}
		}
		else {
			FileUtility.copyFile(to, what, options, monitor);
		}
	}
	
	public static void copyFile(File to, File what, IProgressMonitor monitor) throws Exception {
		FileUtility.copyFile(to, what, FileUtility.COPY_OVERRIDE_EXISTING_FILES, monitor);
	}
	
	public static void copyFile(File to, File what, int options, IProgressMonitor monitor) throws Exception {
		if (to.exists() && to.isDirectory()) {
			to = new File(to.getAbsolutePath() + "/" + what.getName());
		}
		if ((!to.exists() || (options & FileUtility.COPY_OVERRIDE_EXISTING_FILES) != 0) && !monitor.isCanceled()) {
			FileOutputStream output = null;
			FileInputStream input = null;
			try {
				output = new FileOutputStream(to);
				input = new FileInputStream(what);
				byte []buf = new byte[2048];
				int loaded = 0;
				while ((loaded = input.read(buf)) > 0 && !monitor.isCanceled()) {
					output.write(buf, 0, loaded);
				}
			}
			finally {
				if (output != null) {
					try {output.close();} catch (Exception ex) {}
				}
				if (input != null) {
					try {input.close();} catch (Exception ex) {}
				}
			}
		}
	}
	
	public static void removeSVNMetaInformation(IResource root, IProgressMonitor monitor) throws CoreException {
		final List toRemove = new ArrayList();
		
		root.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (SVNUtility.getSVNFolderName().equals(resource.getName()) && !FileUtility.isLinked(resource)) {
					toRemove.add(resource);
					return false;
				}
				return true;
			}
		}, 
		IResource.DEPTH_INFINITE, 
		IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		
		for (Iterator it = toRemove.iterator(); it.hasNext(); ) {
			IResource resource = (IResource)it.next();
			FileUtility.deleteRecursive(new File(resource.getLocation().toString()));
		}
	}
	
	public static boolean alreadyOnSVN(IResource root) {
		return SVNUtility.getSVNInfoForNotConnected(root) != null;
	}
	
	public static boolean isConnected(IResource resource) {
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
		return provider != null && provider instanceof IConnectedProjectInformation;
	}
	
	public static IResource []getPathNodes(IResource resource) {
		return FileUtility.getPathNodes(new IResource[] {resource});
	}
	
	public static IResource []getPathNodes(IResource []resources) {
		List tmp = Arrays.asList(resources);
		Set modifiedRoots = new HashSet();
		IWorkspaceRoot wRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		for (int i = 0; i < resources.length; i++) {
			IResource root = resources[i];
			while ((root = root.getParent()) != wRoot) {
				if (!tmp.contains(root)) {
					modifiedRoots.add(root);
				}
				else {
					break;
				}
			}
		}
		
		return (IResource [])modifiedRoots.toArray(new IResource[modifiedRoots.size()]);
	}
	
	public static void reorder(IResource []resources, final boolean parent2Child) {
		Arrays.sort(resources, new Comparator() {
			public int compare(Object o1, Object o2) {
				String first = ((IResource)o1).getFullPath().toString();
				String second = ((IResource)o2).getFullPath().toString();
				return parent2Child ? first.compareTo(second) : second.compareTo(first);
			}
		});
	}
	
	public static void reorder(File []files, final boolean parent2Child) {
		Arrays.sort(files, new Comparator() {
			public int compare(Object o1, Object o2) {
				String first = ((File)o1).getAbsolutePath();
				String second = ((File)o2).getAbsolutePath();
				return parent2Child ? first.compareTo(second) : second.compareTo(first);
			}
		});
	}
	
	public static IResource []shrinkChildNodes(IResource []resources) {
		HashSet tRoots = new HashSet(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			if (FileUtility.hasRoots(tRoots, resources[i])) {
				tRoots.remove(resources[i]);
			}
		}
		return (IResource [])tRoots.toArray(new IResource[tRoots.size()]);
	}
	
	public static File []shrinkChildNodes(File []files, boolean skipFiles) {
		HashSet tRoots = new HashSet(Arrays.asList(files));
		for (int i = 0; i < files.length; i++) {
			if (skipFiles && files[i].isFile()) {
				continue;
			}
			if (FileUtility.hasRoots(tRoots, files[i])) {
				tRoots.remove(files[i]);
			}
		}
		return (File [])tRoots.toArray(new File[tRoots.size()]);
	}
	
	public static IResource []resourceMembers(IContainer node, boolean includePhantoms) throws CoreException {
		try {
			return node.members(includePhantoms);
		}
		catch (CoreException ex) {
			// if project asynchronously closed then skip node
			//	checked only in case of exception and not before members() due to non-transactional nature of isAccessible()/members() methods pair
			if (node.isAccessible()) {
				throw ex;
			}
		}
		return new IResource[0];
	}
	
	public static String getNamesListAsString(Object []resources) {
		String resourcesNames = "";
		String name = "";
		for (int i = 0; i < resources.length; i++) {
			if (i == 4) {
				resourcesNames += "...";
				break;
			}
			if (resources[i] instanceof IRepositoryResource) {
				name = ((IRepositoryResource)resources[i]).getName();
			}
			else if (resources[i] instanceof IResource) {
				name = ((IResource)resources[i]).getName();
			}
			else {
				name = resources[i].toString();
			}
			resourcesNames += (i == 0 ? "'" : ", '") + name + "'";
		}
		
		return resourcesNames;
	}
	
	public static boolean hasNature(IResource resource, String natureId) throws CoreException {
		IProject project = resource.getProject();
		if (project == null) {
			return false;
		}
		String []natureIds = project.getDescription().getNatureIds();
		for (int i = 0; i < natureIds.length; i++) {
			if (natureId.equals(natureIds[i])) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasRoots(Set roots, IResource node) {
		while ((node = node.getParent()) != null) {
			if (roots.contains(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasRoots(Set roots, File node) {
		while ((node = node.getParentFile()) != null) {
			if (roots.contains(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static void markSVNInternalsTree(IResource node, boolean isTeamPrivate) throws CoreException {
		if (node instanceof IContainer) {
			IResource []children = FileUtility.resourceMembers((IContainer)node, false);
			for (int i = 0; i < children.length; i++) {
				FileUtility.markSVNInternalsTree(children[i], isTeamPrivate);
			}
		}
		node.setTeamPrivateMember(isTeamPrivate);
	}
	
	private static void addChildren(Set resources, IResource []roots, IStateFilter filter, int depth, IActionOperation calledFrom, IProgressMonitor monitor) {
		int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		IRemoteStorage storage = SVNRemoteStorage.instance();
		for (int i = 0; i < roots.length && (monitor == null || !monitor.isCanceled()); i++) {
			if (roots[i].isTeamPrivateMember()) {//FileUtility.isSVNInternals(roots[i])
				continue;
			}
			
			if (monitor != null) {
				String path = roots[i].getFullPath().toString();
				if (calledFrom != null) {
					ProgressMonitorUtility.setTaskInfo(monitor, calledFrom, path);
				}
				else {
					monitor.subTask(path);
				}
				ProgressMonitorUtility.progress(monitor, 1, IProgressMonitor.UNKNOWN);
			}
			
			ILocalResource local = storage.asLocalResource(roots[i]);
			if (local == null) {
				continue;
			}
			
			if (filter.accept(local)) {
				resources.add(roots[i]);
			}
			
			if (roots[i] instanceof IContainer && depth != IResource.DEPTH_ZERO && filter.allowsRecursion(local)) {
				FileUtility.addChildren(resources, FileUtility.getAllMembers((IContainer)roots[i]), filter, nextDepth, calledFrom, monitor);
			}
		}
	}
	
	private static IResource []getAllMembers(IContainer root) {
		GetAllResourcesOperation op = new GetAllResourcesOperation(root);
		ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
		return op.getChildren();
	}
	
	private static String getResourceImpl(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		}
		catch (MissingResourceException ex) {
			return null;
		}
	}
	
	private FileUtility() {
	}

}
