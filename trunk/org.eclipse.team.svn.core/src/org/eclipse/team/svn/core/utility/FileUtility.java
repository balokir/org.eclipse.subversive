/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Common file processing functions
 * 
 * @author Alexander Gurov
 */
public final class FileUtility {
	public static final IResource []NO_CHILDREN = new IResource[0];
	
	public static int getMIMEType(IResource resource) {
		int type = Team.getFileContentManager().getTypeForExtension(resource.getFileExtension() == null ? "" : resource.getFileExtension()); //$NON-NLS-1$
		if (type == Team.UNKNOWN) {
			type = Team.getFileContentManager().getTypeForName(resource.getName());
		}
		return type;
	}
	
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
	
	/**
	 * Returns resource from bundle
	 * 
	 * You shouldn't use this method if you follow to new Eclipse's resource bundles approach 
	 * 
	 * @param bundle
	 * @param key
	 * @return
	 */
	public static String getResource(ResourceBundle bundle, String key) {
		if (key == null) {
			return null;
		}
		if (bundle == null) {
			return key;
		}
		String retVal = FileUtility.getResourceImpl(bundle, key);
		if (retVal != null) {
			if (key.indexOf("Error") != -1) { //$NON-NLS-1$
				String id = FileUtility.getResourceImpl(bundle, key + ".Id"); //$NON-NLS-1$
				if (id != null) {
					retVal = id + ": " + retVal; //$NON-NLS-1$
				}
			}
			return retVal;
		}
		return key;
	}
	
	private static String getResourceImpl(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		}
		catch (MissingResourceException ex) {
			return null;
		}
	}
	
	public static String getWorkingCopyPath(IResource resource) {
		return FileUtility.getResourcePath(resource).toString();
	}
	
	public static IPath getResourcePath(IResource resource) {
		IPath location = resource.getLocation();
		if (location == null) {
			String errMessage = SVNMessages.formatErrorString("Error_InaccessibleResource_2", new String[] {resource.getFullPath().toString()}); //$NON-NLS-1$
			throw new UnreportableException(errMessage);
		}
		return location;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getEnvironmentVariables() {
		try {
			Method getenv = System.class.getMethod("getenv", (Class [])null); //$NON-NLS-1$
			return (Map<String, String>)getenv.invoke(null, (Object [])null);
		}
		catch (Exception ex) {
			try {
				boolean isWindows = FileUtility.isWindows();
				Process p = isWindows ? Runtime.getRuntime().exec("cmd.exe /c set") : Runtime.getRuntime().exec("env"); //$NON-NLS-1$ //$NON-NLS-2$
	            
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            String varLine;
				HashMap<String, String> retVal = new HashMap<String, String>();
	            while ((varLine = br.readLine()) != null) {
	                int idx = varLine.indexOf('=');
	                if (idx != -1) {
		                String name = varLine.substring(0, idx);
		                retVal.put(isWindows ? name.toUpperCase() : name, varLine.substring(idx + 1));
	                }
	                else if (varLine.length() > 0) {
	                	retVal.put(varLine, ""); //$NON-NLS-1$
	                }
	            }
				return retVal;
			}
			catch (IOException ex1) {
				return Collections.emptyMap();
			}
		}
	}
	
	public static String normalizePath(String path) {
		return FileUtility.isWindows() ? path.replace('/', '\\') : path.replace('\\', '/');
	}
	
	public static boolean isWindows() {
		return FileUtility.getOSName().indexOf("windows") != -1; //$NON-NLS-1$
	}
	
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
	}
	
	public static boolean isCaseInsensitiveOS() {
		return !(Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new java.io.File("a").compareTo(new java.io.File("A")) != 0); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
    public static boolean isLinked(IResource resource) {
    	// Eclipse 3.2 and higher
        return resource.isLinked(IResource.CHECK_ANCESTORS);
    }
    
    /*
     * If we can't get project location we consider it as a remote one, e.g.
     * if project is not stored in the local file system
     */
    public static boolean isRemoteProject(IProject project) {
    	return project.getLocation() == null;
    }
    
    /*
     * If the resource is a derived, team private or linked resource, it is ignored
     */
    public static boolean isIgnored(IResource resource) {
    	return resource.isDerived() || resource.isTeamPrivateMember() || FileUtility.isLinked(resource); 
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
	
	public static String formatResourceName(String projectName) {
		// remove invalid characters when repository root was specified
		return projectName == null ? null : PatternProvider.replaceAll(projectName, "([\\/:])+", "."); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String formatPath(String path) {
		return PatternProvider.replaceAll(path, "\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String getUsernameParam(String username) {
		return username == null || username.trim().length() == 0 ? "" : " --username \"" + username + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public static String flattenText(String text) {
		StringBuffer flat = new StringBuffer(text.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (currentChar == '\r' || currentChar == '\n') {
				if (!skipAdjacentLineSeparator)
					flat.append("/");  //$NON-NLS-1$
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
		if (text.length() > 0 && text.substring(0, 1).matches("(\\s)+")) { //$NON-NLS-1$
			text = text.replaceFirst("(\\s)+", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (text.length() == 0) {
			return ""; //$NON-NLS-1$
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
				return text.substring(0, idx) + "..."; //$NON-NLS-1$
			}
			return text.substring(0, idx);
		}
		return text;
	}
	
	public static String[] decodeStringToArray(String encodedString) {
        String[] valuesArray = new String[] {};
        if (encodedString != null && encodedString.length() > 0) {
            String[] array = encodedString.split(";"); //$NON-NLS-1$
            for (int i = 0; i < array.length; i++) {
            	array[i] = new String(Base64.decode(array[i].getBytes()));
            }
            //include trailing empty string
            if (encodedString.endsWith(";")) { //$NON-NLS-1$
            	valuesArray = new String[array.length + 1];
            	System.arraycopy(array, 0, valuesArray, 0, array.length);
            	valuesArray[valuesArray.length -1] = ""; //$NON-NLS-1$
            } else {
            	valuesArray = array;
            }
        }
        return valuesArray;
    }
    
    public static String encodeArrayToString(String []valuesArray) {
        String result = ""; //$NON-NLS-1$
        for (int i = 0; i < valuesArray.length; i++) {
            String str = new String(Base64.encode(valuesArray[i].getBytes()));
            result += i == 0 ? str : (";" + str); //$NON-NLS-1$
		}
        return result;
    }
	
	public static void visitNodes(IResource resource, IResourceVisitor visitor, int depth) throws Exception {
		FileUtility.visitNodes(resource, visitor, depth, true);
	}
	
	public static void visitNodes(IResource resource, IResourceVisitor visitor, int depth, boolean useCache) throws Exception {
		FileUtility.visitNodes(resource, visitor, depth, useCache, false);
	}
	
	public static void visitNodes(IResource resource, IResourceVisitor visitor, int depth, boolean useCache, boolean useAlphabeticalOrder) throws Exception {
		//don't visit ignored resources
		if (FileUtility.isIgnored(resource)) {
			return;
		}
		
		boolean stepInside = visitor.visit(resource);
		if (stepInside &&
			resource instanceof IContainer && 
			depth != IResource.DEPTH_ZERO && 
			resource.isAccessible()) {
			
			IContainer container = (IContainer)resource;
			IResource []children = useCache ? SVNRemoteStorage.instance().getRegisteredChildren(container) : FileUtility.resourceMembers(container, true);
			if (useAlphabeticalOrder) {
				FileUtility.reorder(children, true);
			}
			int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
			for (int i = 0; i < children.length; i++) {
				FileUtility.visitNodes(children[i], visitor, nextDepth, useCache, useAlphabeticalOrder);
			}
		}
	}
	
	public static boolean checkForResourcesPresenceRecursive(IResource []roots, IStateFilter filter) {
		return FileUtility.checkForResourcesPresence(roots, filter, IResource.DEPTH_INFINITE);
	}
	
	public static boolean checkForResourcesPresence(IResource []roots, IStateFilter filter, int depth) {
		ArrayList<IResource> recursiveCheck = null;
		int nextDepth = IResource.DEPTH_ZERO;
		if (depth != IResource.DEPTH_ZERO) {
			recursiveCheck = new ArrayList<IResource>();
			nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		}
		
		// first check all resources that are already accessible (performance optimizations)
		for (int i = 0; i < roots.length; i++) {
			//don't check ignored resources
			if (FileUtility.isIgnored(roots[i])) {//FileUtility.isSVNInternals(roots[i])
				continue;
			}
			
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(roots[i]);
			if (filter.accept(local)) {
				return true;
			}
			else if (roots[i] instanceof IContainer && depth != IResource.DEPTH_ZERO && filter.allowsRecursion(local)) {
				recursiveCheck.add(roots[i]);
			}
		}
		
		// no resources accepted, check recursively (performance optimizations)
		if (depth != IResource.DEPTH_ZERO) {
			for (Iterator<IResource> it = recursiveCheck.iterator(); it.hasNext(); ) {
				IContainer local = (IContainer)it.next();
				if (FileUtility.checkForResourcesPresence(FileUtility.getAllMembers(local), filter, nextDepth)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static IResource []filterResources(IResource []resources, IStateFilter filter) {
		return FileUtility.filterResources(resources, filter, IResource.DEPTH_INFINITE);
	}
	
	public static IResource []filterResources(IResource []resources, IStateFilter filter, int depth) {
		HashSet<IResource> retVal = new HashSet<IResource>(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			if (!FileUtility.checkForResourcesPresence(new IResource[] {resources[i]}, filter, depth)) {
				retVal.remove(resources[i]);
			}
		}
		return retVal.toArray(new IResource[retVal.size()]);
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter) {
		return FileUtility.getResourcesRecursive(roots, filter, IResource.DEPTH_INFINITE);
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter, int depth) {
		return FileUtility.getResourcesRecursive(roots, filter, depth, null, null);
	}
	
	public static IResource []getResourcesRecursive(IResource []roots, IStateFilter filter, int depth, IActionOperation calledFrom, IProgressMonitor monitor) {
		Set<IResource> resources = new HashSet<IResource>();
		FileUtility.addChildren(resources, roots, filter, depth, calledFrom, monitor);
		return resources.toArray(new IResource[resources.size()]);
	}
	
	public static IResource []addOperableParents(IResource []resources, IStateFilter stateFilter) {
		return FileUtility.addOperableParents(resources, stateFilter, false);		
	}
	
	public static IResource []addOperableParents(IResource []resources, IStateFilter stateFilter, boolean through) {
		HashSet<IResource> tmp = new HashSet<IResource>(Arrays.asList(resources));
		tmp.addAll(Arrays.asList(FileUtility.getOperableParents(resources, stateFilter, through)));
		return tmp.toArray(new IResource[tmp.size()]);
	}
	
	public static IResource []getOperableParents(IResource []resources, IStateFilter stateFilter) {
		return FileUtility.getOperableParents(resources, stateFilter, false);
	}
	
	/**
	 * @param through		Determines whether to proceed higher parents if one of its child isn't accepted by filter
	 * 						if 'through' is true, then proceed always all parents
	 * 						if 'through' is false, then don't proceed higher parents if of their child isn't accepted by filter
	 */
	public static IResource []getOperableParents(IResource []resources, IStateFilter stateFilter, boolean through) {
		HashSet<IResource> tmp = new HashSet<IResource>();
		IResource []parents = FileUtility.getParents(resources, true);
		if (!through) {
			FileUtility.reorder(parents, false);
		}
		for (int i = 0; i < parents.length; i++) {
			ILocalResource parent = SVNRemoteStorage.instance().asLocalResource(parents[i]);
			if (stateFilter.accept(parent)) {
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
		return tmp.toArray(new IResource[tmp.size()]);
	}
		
	public static IResource []getParents(IResource []resources, boolean excludeIncoming) {
		HashSet<IResource> parents = new HashSet<IResource>();
		for (int i = 0; i < resources.length; i++) {
			IResource parent = resources[i];
			if (parent.getType() == IResource.PROJECT) {
				parents.add(parent);
			}
			else {
				while ((parent = parent.getParent()) != null && !(parent instanceof IWorkspaceRoot)) {
					parents.add(parent);
				}
			}
		}
		if (excludeIncoming) {
			for (int i = 0; i < resources.length; i++) {
				if (resources[i].getType() != IResource.PROJECT) {
					parents.remove(resources[i]);
				}
			}
		}
		return parents.toArray(new IResource[parents.size()]);
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
			to = new File(to.getAbsolutePath() + "/" + what.getName()); //$NON-NLS-1$
			if (monitor.isCanceled()) {
				return;
			}
			if (!to.mkdirs() && ((options & FileUtility.COPY_IGNORE_EXISTING_FOLDERS) == 0)) {
				String errMessage = SVNMessages.formatErrorString("Error_CreateDirectory", new String[] {to.getAbsolutePath()}); //$NON-NLS-1$
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
	
	public static boolean copyFile(File to, File what, IProgressMonitor monitor) throws Exception {
		return FileUtility.copyFile(to, what, FileUtility.COPY_OVERRIDE_EXISTING_FILES, monitor);
	}
	
	public static boolean copyFile(File to, File what, int options, IProgressMonitor monitor) throws Exception {
		if (!what.exists()) {
			return false;
		}
		if (to.exists() && to.isDirectory()) {
			to = new File(to.getAbsolutePath() + "/" + what.getName()); //$NON-NLS-1$
		}
		if ((!to.exists() || (options & FileUtility.COPY_OVERRIDE_EXISTING_FILES) != 0) && !monitor.isCanceled()) {
			File parent = to.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
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
				return true;
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
		return false;
	}
	
	public static void removeSVNMetaInformation(IResource root, IProgressMonitor monitor) throws CoreException {
		final List<IResource> toRemove = new ArrayList<IResource>();
		
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
		
		for (Iterator<IResource> it = toRemove.iterator(); it.hasNext(); ) {
			IResource resource = it.next();
			FileUtility.deleteRecursive(new File(resource.getLocation().toString()));
		}
	}
	
	public static boolean alreadyOnSVN(IResource root) {
		return SVNUtility.getSVNInfoForNotConnected(root) != null;
	}
	
	public static boolean isConnected(IResource resource) {
		if (resource.getProject() != null) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
			return provider != null && provider instanceof IConnectedProjectInformation;	
		}
		return false;
	}
	
	public static IResource []getPathNodes(IResource resource) {
		return FileUtility.getPathNodes(new IResource[] {resource});
	}
	
	public static IResource []getPathNodes(IResource []resources) {
		Set<IResource> tmp = new HashSet<IResource>(Arrays.asList(resources));
		Set<IResource> modifiedRoots = new HashSet<IResource>();
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
		
		return modifiedRoots.toArray(new IResource[modifiedRoots.size()]);
	}
	
	public static void reorder(IResource []resources, final boolean parent2Child) {
		Arrays.sort(resources, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				String first = ((IResource)o1).getFullPath().toString();
				String second = ((IResource)o2).getFullPath().toString();
				return parent2Child ? first.compareTo(second) : second.compareTo(first);
			}
		});
	}
	
	public static void reorder(File []files, final boolean parent2Child) {
		Arrays.sort(files, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				String first = ((File)o1).getAbsolutePath();
				String second = ((File)o2).getAbsolutePath();
				return parent2Child ? first.compareTo(second) : second.compareTo(first);
			}
		});
	}
	
	/*
	 * Take into account that there can be externals in resources: externals should not be
	 * shrinked with not externals.
	 */	
	public static IResource []shrinkChildNodesWithSwitched(IResource []resources) {
		Set<IResource> resourcesSet = new HashSet<IResource>();
		Set<IResource> switchedResourcesSet = new HashSet<IResource>();		
		for (IResource resource : resources) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			if ((local.getChangeMask() & ILocalResource.IS_SWITCHED) == 0) {
				resourcesSet.add(resource);
			} else {
				switchedResourcesSet.add(resource);
			}
		}
		
		HashSet<IResource> tRoots = new HashSet<IResource>(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			Set<IResource> roots = resourcesSet.contains(resources[i]) ? resourcesSet : switchedResourcesSet;	
			if (!roots.isEmpty() && FileUtility.hasRoots(roots, resources[i])) {
				tRoots.remove(resources[i]);
			}
		}
		return tRoots.toArray(new IResource[tRoots.size()]);		
	}
		
	public static IResource []shrinkChildNodes(IResource []resources) {
		HashSet<IResource> tRoots = new HashSet<IResource>(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			if (FileUtility.hasRoots(tRoots, resources[i])) {
				tRoots.remove(resources[i]);
			}
		}
		return tRoots.toArray(new IResource[tRoots.size()]);
	}
	
	public static File []shrinkChildNodes(File []files, boolean skipFiles) {
		HashSet<File> tRoots = new HashSet<File>(Arrays.asList(files));
		for (int i = 0; i < files.length; i++) {
			if (skipFiles && files[i].isFile()) {
				continue;
			}
			if (FileUtility.hasRoots(tRoots, files[i])) {
				tRoots.remove(files[i]);
			}
		}
		return tRoots.toArray(new File[tRoots.size()]);
	}
	
	public static IResource []resourceMembers(IContainer node, boolean includePhantoms) throws CoreException {
		if (node.isAccessible()) {
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
		}
		return new IResource[0];
	}
	
	public static String getNamesListAsString(Object []resources) {
		String resourcesNames = ""; //$NON-NLS-1$
		String name = ""; //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			if (i == 4) {
				resourcesNames += "..."; //$NON-NLS-1$
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
			resourcesNames += (i == 0 ? "'" : ", '") + name + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	
	private static boolean hasRoots(Set<IResource> roots, IResource node) {
		while ((node = node.getParent()) != null) {
			if (roots.contains(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasRoots(Set<File> roots, File node) {
		while ((node = node.getParentFile()) != null) {
			if (roots.contains(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static void markSVNInternalsTree(IResource node, boolean isTeamPrivate) throws CoreException {
		if (node.exists()) {
			if (node instanceof IContainer) {
				IResource []children = FileUtility.resourceMembers((IContainer)node, false);
				for (int i = 0; i < children.length; i++) {
					FileUtility.markSVNInternalsTree(children[i], isTeamPrivate);
				}
			}
			node.setTeamPrivateMember(isTeamPrivate);
		}
	}
	
	private static void addChildren(Set<IResource> resources, IResource []roots, IStateFilter filter, int depth, IActionOperation calledFrom, IProgressMonitor monitor) {
		if (roots == null || roots.length == 0) {
			return;
		}
		int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		for (int i = 0; i < roots.length && (monitor == null || !monitor.isCanceled()); i++) {
			//don't process ignored resources
			if (FileUtility.isIgnored(roots[i])) {//FileUtility.isSVNInternals(roots[i])
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
			
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(roots[i]);
			
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
	
	private FileUtility() {
	}

}
