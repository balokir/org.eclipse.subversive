/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.refactor;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Remove Eclipse resource from working copy
 * 
 * @author Alexander Gurov
 */
public class DeleteResourceOperation extends AbstractActionOperation {
	protected IResource[] resources;

	public DeleteResourceOperation(IResource resource) {
		this(new IResource[]{resource});
	}
	
	public DeleteResourceOperation(IResource[] resources) {
		super("Operation.DeleteLocal");
		this.resources = resources;
	}

	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule[] rules = new ISchedulingRule[this.resources.length];
		for (int i = 0; i < this.resources.length; i++) {
			rules[i] = SVNResourceRuleFactory.INSTANCE.deleteRule(this.resources[i]);
		}
		return new MultiRule(rules);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		
		// Clean up obstructed and new resources
		ArrayList resourcesList = new ArrayList(Arrays.asList(this.resources));
		this.cleanupResourcesList(resourcesList, DeleteResourceOperation.SF_OBSTRUCTED_OR_NEW, storage);
		if (resourcesList.size() == 0) {
			return;
		}
		
		// Process resources by project basis
		IResource[] allResources = (IResource[])resourcesList.toArray(new IResource[resourcesList.size()]);
		Map project2Resources = SVNUtility.splitWorkingCopies(allResources);
		for (Iterator it = project2Resources.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			IRepositoryLocation location = storage.getRepositoryLocation((IResource)entry.getKey());
			IResource[] resources = (IResource[])((List)entry.getValue()).toArray(new IResource[((List)entry.getValue()).size()]);
			String[] wcPaths = new String[resources.length];
			String printedPath = "";
			for (int i = 0; i < resources.length; i++) {
				wcPaths[i] = FileUtility.getWorkingCopyPath(resources[i]);
				printedPath += "\"" + FileUtility.normalizePath(wcPaths[i]) + ((i < resources.length - 1) ? " " : "") + "\"";
			}
			ISVNClientWrapper proxy = location.acquireSVNProxy();
			try {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn delete " + printedPath + " --force\n");
				proxy.remove(wcPaths, "", true, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
			    location.releaseSVNProxy(proxy);
			}
		}
	}
	
	protected void cleanupResourcesList(List resources, IStateFilter filter, IRemoteStorage storage) {
		for (Iterator it = resources.iterator(); it.hasNext();) {
			IResource resource = (IResource)it.next();
			ILocalResource local = storage.asLocalResource(resource);
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			if (local != null && filter.accept(resource, local.getStatus(), local.getChangeMask())) {
				it.remove();
				FileUtility.deleteRecursive(new File(wcPath));
			}
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		String[] wcPaths = new String[resources.length];
		for (int i = 0; i < resources.length; i++) {
			wcPaths[i] = FileUtility.getWorkingCopyPath(resources[i]);
		}
		return MessageFormat.format(super.getShortErrorMessage(t), wcPaths);
	}
	
	protected static final IStateFilter SF_OBSTRUCTED_OR_NEW = new IStateFilter() {

		public boolean accept(IResource resource, String state, int mask) {
			return IStateFilter.SF_OBSTRUCTED.accept(resource, state, mask) ||
					IStateFilter.SF_NEW.accept(resource, state, mask);
		}

		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_OBSTRUCTED.allowsRecursion(resource, state, mask) ||
					IStateFilter.SF_NEW.allowsRecursion(resource, state, mask);
		}
		
	};
	
}
