/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Locate Eclipse IDE projects on the repository
 * 
 * @author Alexander Gurov
 */
public class LocateProjectsOperation extends AbstractRepositoryOperation implements IRepositoryResourceProvider {
	public static final int LEVEL_ALL = -1;
	
	protected IRepositoryResource []foundProjects;
	protected ILocateFilter filter;
	protected int levelLimitation;

	public interface ILocateFilter {
		public boolean isProject(IRepositoryResource remote, IRepositoryResource []children);
	}
	
	public LocateProjectsOperation(IRepositoryResource []startFrom, ILocateFilter filter) {
		this(startFrom, filter, LocateProjectsOperation.LEVEL_ALL);
	}
	
	public LocateProjectsOperation(IRepositoryResource []startFrom, ILocateFilter filter, int limitation) {
		super("Operation_LocateProjects", startFrom); //$NON-NLS-1$
		this.levelLimitation = limitation;
		this.filter = filter;
	}

	public IRepositoryResource[] getRepositoryResources() {
		return this.foundProjects;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []baseFolders = SVNUtility.shrinkChildNodes(this.operableData());
		for (int i = 0; i < baseFolders.length; i++) {
			SVNRevision selectedRevision = baseFolders[i].getSelectedRevision();
			SVNRevision pegRevision = baseFolders[i].getPegRevision();
			baseFolders[i] = baseFolders[i].getRepositoryLocation().asRepositoryContainer(baseFolders[i].getUrl(), false);			
			baseFolders[i].setSelectedRevision(selectedRevision);
			baseFolders[i].setPegRevision(pegRevision);
		}
		ArrayList<IRepositoryResource> found = new ArrayList<IRepositoryResource>();
		this.findProjects(monitor, found, baseFolders, 0);
		this.foundProjects = found.toArray(new IRepositoryResource[found.size()]);
	}
	
	protected void findProjects(final IProgressMonitor monitor, final List<IRepositoryResource> found, IRepositoryResource []baseFolders, final int level) throws Exception {
		for (int i = 0; i < baseFolders.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource current = baseFolders[i];
			if (this.isCheckEnabled(level, current)) {
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						if (current instanceof IRepositoryContainer) {
							String message = LocateProjectsOperation.this.getOperationResource("Scanning"); //$NON-NLS-1$
							ProgressMonitorUtility.setTaskInfo(monitor, LocateProjectsOperation.this, BaseMessages.format(message, new Object[] {current.getUrl()}));
							IRepositoryResource []children = ((IRepositoryContainer)current).getChildren();
							if (LocateProjectsOperation.this.filter.isProject(current, children)) {
								found.add(current);
							}
							else if (LocateProjectsOperation.this.isRecursionEnabled(level)) {
								LocateProjectsOperation.this.findProjects(monitor, found, children, level + 1);
							}
						}
					}
				}, monitor, baseFolders.length);
			}
		}
	}
	
	protected boolean isCheckEnabled(int level, IRepositoryResource current) {
		if (level > 0) {
			IRepositoryLocation location = current.getRepositoryLocation();
			if (location.isStructureEnabled() &&
				(current.getName().equals(location.getBranchesLocation()) ||
				current.getName().equals(location.getTagsLocation()))) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean isRecursionEnabled(int level) {
		return 
			LocateProjectsOperation.this.levelLimitation <= LocateProjectsOperation.LEVEL_ALL || 
			level < LocateProjectsOperation.this.levelLimitation;
	}
	
}
