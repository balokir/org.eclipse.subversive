/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Extract selected resources to location
 * Used from synchronize view ExpandTo action
 * 
 * @author Alexei Goncharov
 */
public class ExtractToOperationLocal extends AbstractActionOperation {

	private IResource [] outgoingResources;
	private ArrayList<IResource> allResources;
	private String path;
	private boolean delitionAllowed;
	
	public ExtractToOperationLocal(IResource [] outgoingResources, IResource [] allSelected, String path, boolean delitionAllowed) {
		super("Expand To Operation");
		this.outgoingResources = outgoingResources;
		this.allResources = new ArrayList<IResource>();
		for (int i = 0; i < allSelected.length; i++) {
			this.allResources.add(allSelected[i]);
		}
		this.path = path;
		this.delitionAllowed = delitionAllowed;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		HashSet<IResource> operable = new HashSet<IResource>();
		HashMap<IProject, Integer> firstNeededSegments = new HashMap<IProject, Integer>();
		IResource [] parents = FileUtility.getParents(this.outgoingResources, false);
		for (int i = 0; i < parents.length; i++) {
			if (allResources.contains(parents[i])) {
				operable.add(parents[i]);
				int preSegmentsCount = parents[i].getFullPath().segmentCount() - 1;
				Integer firstNeededSegment = firstNeededSegments.get(parents[i].getProject());
				if (firstNeededSegment == null || preSegmentsCount < firstNeededSegment) {
					firstNeededSegments.put(parents[i].getProject(), preSegmentsCount);
				}
			}
		}
		for (int i = 0; i < this.outgoingResources.length; i++) {
			operable.add(this.outgoingResources[i]);
			int preSegmentsCount = this.outgoingResources[i].getFullPath().segmentCount() - 1;
			Integer firstNeededSegment = firstNeededSegments.get(this.outgoingResources[i].getProject());
			if (firstNeededSegment == null || preSegmentsCount < firstNeededSegment) {
				firstNeededSegments.put(this.outgoingResources[i].getProject(), preSegmentsCount);
			}
		}
		this.outgoingResources = operable.toArray(new IResource[0]);
		for (int i = 0; i < this.outgoingResources.length; i++) {
			IPath resourcePath = this.outgoingResources[i].getFullPath();
			File what = new File(FileUtility.getWorkingCopyPath(this.outgoingResources[i]));
			if (what.isDirectory()) {
				String toOperate = "";
				int firstNeededSegment = firstNeededSegments.get(this.outgoingResources[i].getProject());
				for (int j = firstNeededSegment; j < resourcePath.segmentCount(); j++) {
					toOperate = toOperate + "\\" + resourcePath.segment(j);
				}
				File fileToOperate = new File(this.path + toOperate);
				if (IStateFilter.SF_DELETED.accept(SVNRemoteStorage.instance().asLocalResource(outgoingResources[i]))) {
					if (fileToOperate.exists() && this.delitionAllowed) {
						fileToOperate.delete();
					}
				}
				else {
					fileToOperate.mkdirs();
				}
			}
			else {
				String pathToOperate = this.path;
				File to = new File(pathToOperate);
				for (int j = 0; j < resourcePath.segmentCount() - 1; j++) {
					pathToOperate = pathToOperate + "\\" + resourcePath.segment(j);
					to = new File(pathToOperate);
					if (!to.exists()) {
						pathToOperate = this.path;
					}
				}
				if (IStateFilter.SF_DELETED.accept(SVNRemoteStorage.instance().asLocalResource(outgoingResources[i]))) {
					to = new File(pathToOperate + "\\" + this.outgoingResources[i].getName());
					if (to.exists() && this.delitionAllowed) {
						to.delete();
					}
				}
				else {
					to = new File(pathToOperate);
					FileUtility.copyAll(to, new File(FileUtility.getWorkingCopyPath(this.outgoingResources[i])), monitor);
				}
			}
		}
	}

}
