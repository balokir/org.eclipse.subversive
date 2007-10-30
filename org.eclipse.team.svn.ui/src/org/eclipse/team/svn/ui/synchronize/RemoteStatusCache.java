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

package org.eclipse.team.svn.ui.synchronize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	protected Map resourceStateMap;
	protected Map resourceChildrenMap;

	public interface ICacheVisitor {
		public void visit(IPath path, byte []data);
	}
	
	public RemoteStatusCache() {
		this.resourceStateMap = new HashMap();
		this.resourceChildrenMap = new HashMap();
	}

	public synchronized boolean containsData() {
		return this.resourceStateMap.size() > 0;
	}
	
	public void dispose() {

	}

	public synchronized byte []getBytes(IResource resource) {
		return (byte [])this.resourceStateMap.get(resource.getFullPath());
	}

	public synchronized boolean setBytes(IResource resource, byte []bytes) {
		byte []old = (byte [])this.resourceStateMap.put(resource.getFullPath(), bytes);
		IPath parentPath = resource.getParent().getFullPath();
		Set members = (Set)this.resourceChildrenMap.get(parentPath);
		if (members == null) {
			this.resourceChildrenMap.put(parentPath, members = new HashSet());
		}
		members.add(resource);
		return !this.equals(old, bytes);
	}

	public synchronized boolean flushBytes(IResource resource, int depth) {
		final HashSet removedSet = new HashSet();
		boolean retVal = this.resourceStateMap.remove(resource.getFullPath()) != null;
		this.traverse(resource, depth, new ICacheVisitor() {
			public void visit(IPath path, byte[] data) {
				removedSet.add(path);
			}
		});
		for (Iterator it = removedSet.iterator(); it.hasNext(); ) {
			IPath current = (IPath)it.next();
			this.resourceStateMap.remove(current);
			this.resourceChildrenMap.remove(current);
		}
		Set members = (Set)this.resourceChildrenMap.get(resource.getParent().getFullPath());
		if (members != null) {
			members.remove(resource);
		}
		return retVal;
	}

	public synchronized boolean deleteBytes(IResource resource) {
		return this.flushBytes(resource, IResource.DEPTH_ZERO);
	}

	public synchronized IResource []members(IResource resource) {
		Set members = (Set)this.resourceChildrenMap.get(resource.getFullPath());
		return members == null ? new IResource[0] : (IResource [])members.toArray(new IResource[members.size()]);
	}

	public synchronized IResource []allMembers(IResource resource) {
		if (!(resource instanceof IContainer)) {
    		return FileUtility.NO_CHILDREN;
		}
		List members = new ArrayList(Arrays.asList(this.members(resource)));
		if (RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID) != null) {
			IContainer container = (IContainer)resource;
			GetAllResourcesOperation op = new GetAllResourcesOperation(container);
			ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
			members.addAll(Arrays.asList(op.getChildren()));
		}
		return (IResource [])members.toArray(new IResource[members.size()]);
	}

	public synchronized void traverse(IResource []resources, int depth, ICacheVisitor visitor) {
		for (int i = 0; i < resources.length; i++) {
			this.traverse(resources[i], depth, visitor);
		}
	}
	
	protected void traverse(IResource resource, int depth, ICacheVisitor visitor) {
		IPath base = resource.getFullPath();
	    for (Iterator it = this.resourceStateMap.entrySet().iterator(); it.hasNext(); ) {
	    	Map.Entry entry = (Map.Entry)it.next();
	    	IPath current = (IPath)entry.getKey();
	    	if (this.isChildOf(base, current, depth)) {
	    		visitor.visit(current, (byte [])entry.getValue());
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
