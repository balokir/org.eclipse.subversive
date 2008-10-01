/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Operation for replacement of local resources with remote ones.
 * Deletes the resources, that are not got from remote source.
 * 
 * @author Alexei Goncharov
 */
public class ReplaceWithRemoteOperation extends AbstractActionOperation {

	protected IResource toReplace;
	protected IRepositoryResource remoteRoot;
	
	public ReplaceWithRemoteOperation(IResource toReplace, IRepositoryResource remoteResource) {
		super("Replace Operation");
		this.toReplace = toReplace;
		this.remoteRoot = remoteResource;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		//perform export into temp folder
		IRepositoryLocation location = this.remoteRoot.getRepositoryLocation();
		String toReplacePath = FileUtility.getWorkingCopyPath(this.toReplace);
		File f = File.createTempFile("svn", "");
		f.delete(); 
		String tempPath = toReplacePath.substring(0, toReplacePath.lastIndexOf("/") + 1) + f.getName() + "/"; 
		final ISVNConnector proxy = location.acquireSVNProxy();
		final String path = tempPath + this.remoteRoot.getName();
		final SVNEntryRevisionReference entryRef = SVNUtility.getEntryRevisionReference(this.remoteRoot);		
		try {
			proxy.doExport(entryRef, path, null, Depth.INFINITY, ISVNConnector.Options.FORCE, new SVNProgressMonitor(this, monitor, null));
			//perform replacement
			if (this.toReplace instanceof IFile) {
				FileUtility.copyFile(new File(toReplacePath), new File(path), monitor);
			}
			else {
				this.performReplacementRecursively(toReplacePath, path, proxy, monitor);
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
			FileUtility.deleteRecursive(new File(tempPath));
		}
	}
	
	protected void performReplacementRecursively(String pathForReplacement, String sourcePath, ISVNConnector connectorProxy, IProgressMonitor monitor) throws Exception {
		File dirToReplace = new File(pathForReplacement);
		File sourceDir = new File(sourcePath);
		ArrayList<String> toReplaceChildren = new ArrayList<String>();
		String [] children = dirToReplace.list();
		if (children != null) {
			toReplaceChildren.addAll(Arrays.asList(children));
		}
		children = sourceDir.list();
		ArrayList<String> sourceChildren = new ArrayList<String>();
		if (children != null) {
			sourceChildren.addAll(Arrays.asList(children));
		}
		ArrayList<String> pathsToDelete = new ArrayList<String>();
		for (String currentToReplace : toReplaceChildren) {
			if (!currentToReplace.equalsIgnoreCase(".svn") && !sourceChildren.contains(currentToReplace)) {
				pathsToDelete.add(pathForReplacement + "/" + currentToReplace);
			}
		}
		connectorProxy.remove(pathsToDelete.toArray(new String [0]), "", ISVNConnector.Options.FORCE, null, new SVNProgressMonitor(this, monitor, null));
		for (String currentFromSource : sourceChildren) {
			File toReplace =  new File(pathForReplacement + "/" + currentFromSource);
			File source = new File(sourcePath + "/" + currentFromSource);
			if (source.isDirectory()) {
				if (!toReplace.exists()) {
					toReplace.mkdir();
				}
				this.performReplacementRecursively(pathForReplacement + "/" + currentFromSource, sourcePath + "/" + currentFromSource, connectorProxy, monitor);
			}
			else {
				FileUtility.copyFile(toReplace, source, monitor);
			}
		}
	}
}
