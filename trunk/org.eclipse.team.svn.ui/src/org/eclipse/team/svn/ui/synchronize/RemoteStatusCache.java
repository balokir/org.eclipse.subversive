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

package org.eclipse.team.svn.ui.synchronize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.variants.ResourceVariantByteStore;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Remote status cache implementation
 * 
 * @author Alexander Gurov
 */
public class RemoteStatusCache extends ResourceVariantByteStore {
	protected Map<IPath, byte[]> resourceStateMap;
	protected Map<IPath, Set<IResource>> resourceChildrenMap;

	public interface ICacheVisitor {
		public void visit(IPath path, byte []data);
	}
	
	public RemoteStatusCache() {
		this.resourceStateMap = new HashMap<IPath, byte[]>();
		this.resourceChildrenMap = new HashMap<IPath, Set<IResource>>();
	}

	public synchronized boolean containsData() {
		return this.resourceStateMap.size() > 0;
	}
	
	public void dispose() {

	}
	
	public synchronized void clearAll() {
		this.resourceChildrenMap.clear();
		this.resourceStateMap.clear();
	}

	public synchronized byte []getBytes(IResource resource) {
		return this.resourceStateMap.get(resource.getFullPath());
	}

	public synchronized boolean setBytes(IResource resource, byte []bytes) {
		byte []old = this.resourceStateMap.put(resource.getFullPath(), bytes);
		IPath parentPath = resource.getParent().getFullPath();
		Set<IResource> members = this.resourceChildrenMap.get(parentPath);
		if (members == null) {
			this.resourceChildrenMap.put(parentPath, members = new HashSet<IResource>());
		}
		members.add(resource);
		return !this.equals(old, bytes);
	}

	public synchronized boolean flushBytes(IResource resource, int depth) {
		final HashSet<IPath> removedSet = new HashSet<IPath>();
		boolean retVal = this.resourceStateMap.remove(resource.getFullPath()) != null;
		this.traverse(resource, depth, new ICacheVisitor() {
			public void visit(IPath path, byte[] data) {
				removedSet.add(path);
			}
		});
		for (IPath current : removedSet) {
			this.resourceStateMap.remove(current);
			this.resourceChildrenMap.remove(current);
		}
		Set<IResource> members = this.resourceChildrenMap.get(resource.getParent().getFullPath());
		if (members != null) {
			members.remove(resource);
		}
		return retVal;
	}

	public synchronized boolean deleteBytes(IResource resource) {
		return this.flushBytes(resource, IResource.DEPTH_ZERO);
	}

	public synchronized IResource []members(IResource resource) {
		Set<?> members = this.resourceChildrenMap.get(resource.getFullPath());
		return members == null ? new IResource[0] : members.toArray(new IResource[members.size()]);
	}

	public synchronized IResource []allMembers(IResource resource) {
		if (!(resource instanceof IContainer)) {
    		return FileUtility.NO_CHILDREN;
		}
		List<IResource> members = new ArrayList<IResource>(Arrays.asList(this.members(resource)));
		if (RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID) != null) {
			IContainer container = (IContainer)resource;
			GetAllResourcesOperation op = new GetAllResourcesOperation(container);
			ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
			members.addAll(Arrays.asList(op.getChildren()));
		}
		return members.toArray(new IResource[members.size()]);
	}

	public synchronized void traverse(IResource []resources, int depth, ICacheVisitor visitor) {
		for (int i = 0; i < resources.length; i++) {
			this.traverse(resources[i], depth, visitor);
		}
	}
	
	protected void traverse(IResource resource, int depth, ICacheVisitor visitor) {
		IPath base = resource.getFullPath();
	    for (Map.Entry<IPath, byte[]> entry : this.resourceStateMap.entrySet()) {
	    	IPath current = entry.getKey();
	    	if (this.isChildOf(base, current, depth)) {
	    		visitor.visit(current, entry.getValue());
	    	}
	    }
	}
	
	protected boolean isChildOf(IPath base, IPath current, int depth) {
		if (base.isPrefixOf(current)) {
            int cachedSegmentsCount = current.segmentCount();
            int matchingSegmentsCount = base.matchingFirstSegments(current);
            int difference = cachedSegmentsCount - matchingSegmentsCount;
            if (difference >= 0 && depth == IResource.DEPTH_INFINITE ? true : depth >= difference) {
            	return true;
            }
		}
		return false;
	}
	
}
