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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
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
	protected boolean keepLocal;

	public DeleteResourceOperation(IResource resource) {
		this(new IResource[]{resource}, false);
	}
	
	public DeleteResourceOperation(IResource resource, boolean keepLocal) {
		this(new IResource[]{resource}, keepLocal);
	}
	
	public DeleteResourceOperation(IResource[] resources) {
		this(resources, false);
	}
	
	public DeleteResourceOperation(IResource[] resources, boolean keepLocal) {
		super("Operation.DeleteLocal");
		this.resources = resources;
		this.keepLocal = keepLocal;
	}

	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule[] rules = new ISchedulingRule[this.resources.length];
		for (int i = 0; i < this.resources.length; i++) {
			rules[i] = SVNResourceRuleFactory.INSTANCE.deleteRule(this.resources[i]);
		}
		return new MultiRule(rules);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		// Clean up obstructed and new resources
		ArrayList<IResource> resourcesList = new ArrayList<IResource>(Arrays.asList(this.resources));
		this.cleanupResourcesList(resourcesList, DeleteResourceOperation.SF_OBSTRUCTED_OR_NEW);
		if (resourcesList.size() == 0) {
			return;
		}
		
		// Process resources by project basis
		IResource[] allResources = resourcesList.toArray(new IResource[resourcesList.size()]);
		Map<?, ?> project2Resources = SVNUtility.splitWorkingCopies(allResources);
		for (Iterator<?> it = project2Resources.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation((IResource)entry.getKey());
			IResource[] resources = ((List<?>)entry.getValue()).toArray(new IResource[((List<?>)entry.getValue()).size()]);
			String[] wcPaths = new String[resources.length];
			String printedPath = "";
			for (int i = 0; i < resources.length; i++) {
				wcPaths[i] = FileUtility.getWorkingCopyPath(resources[i]);
				printedPath += "\"" + FileUtility.normalizePath(wcPaths[i]) + ((i < resources.length - 1) ? " " : "") + "\"";
			}
			ISVNConnector proxy = location.acquireSVNProxy();
			try {
				//this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn delete " + printedPath + " --force\n");
				long options = ISVNConnector.Options.FORCE;
				if (this.keepLocal && CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x) {
					options |= ISVNConnector.Options.KEEP_LOCAL;
				}
				proxy.remove(wcPaths, "", options, null, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
			    location.releaseSVNProxy(proxy);
			}
		}
	}
	
	protected void cleanupResourcesList(List<IResource> resources, IStateFilter filter) {
		for (Iterator<IResource> it = resources.iterator(); it.hasNext();) {
			IResource resource = it.next();
			String wcPath = FileUtility.getWorkingCopyPath(resource);
			if (filter.accept(SVNRemoteStorage.instance().asLocalResourceAccessible(resource))) {
				it.remove();
				FileUtility.deleteRecursive(new File(wcPath));
			}
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		Object[] wcPaths = new String[this.resources.length];
		for (int i = 0; i < this.resources.length; i++) {
			wcPaths[i] = FileUtility.getWorkingCopyPath(this.resources[i]);
		}
		return MessageFormat.format(super.getShortErrorMessage(t), wcPaths);
	}
	
	protected static final IStateFilter SF_OBSTRUCTED_OR_NEW = new IStateFilter.AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_OBSTRUCTED.accept(resource, state, mask) || IStateFilter.SF_NEW.accept(resource, state, mask);
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_OBSTRUCTED.allowsRecursion(resource, state, mask) || IStateFilter.SF_NEW.allowsRecursion(resource, state, mask);
		}
	};
	
}
