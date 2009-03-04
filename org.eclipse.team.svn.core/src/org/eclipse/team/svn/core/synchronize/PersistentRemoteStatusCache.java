/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.synchronize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.PersistantResourceVariantByteStore;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Persistent remote status cache implementation
 * 
 * @author Igor Burilo
 */
public class PersistentRemoteStatusCache extends PersistantResourceVariantByteStore implements IRemoteStatusCache {
	
	public PersistentRemoteStatusCache(QualifiedName qualifiedName) {
		super(qualifiedName);
	}
	
	public synchronized boolean containsData() throws TeamException {
		boolean containsData = false;
		IResource[] roots = this.roots();
		for (IResource root : roots) {
			if (this.getBytes(root) != null) {
				containsData = true;
				break;
			}	
		}				
		return containsData;
	}
	
	public synchronized void clearAll() throws TeamException {
		IResource[] resources = this.roots();		
		for (IResource resource : resources) {		
		    this.flushBytes(resource, IResource.DEPTH_INFINITE);		    
		}
	}
	
	public synchronized IResource []allMembers(IResource resource) throws TeamException {
		if (!(resource instanceof IContainer)) {
    		return FileUtility.NO_CHILDREN;
		}
		Set<IResource> members = new HashSet<IResource>(Arrays.asList(this.members(resource)));
		if (RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID) != null) {
			IContainer container = (IContainer)resource;
			GetAllResourcesOperation op = new GetAllResourcesOperation(container);
			ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
			members.addAll(Arrays.asList(op.getChildren()));
		}
		return members.toArray(new IResource[members.size()]);
	}
	
	public synchronized void traverse(IResource []resources, int depth, ICacheVisitor visitor) throws TeamException {
		for (int i = 0; i < resources.length; i++) {
			this.traverse(resources[i], depth, visitor);
		}
	}
	
	protected void traverse(IResource resource, int depth, ICacheVisitor visitor) throws TeamException {
		IPath base = resource.getFullPath();
		
		IResource[] resources = this.getAllMembers();
		for (IResource res : resources) {
			if (this.isChildOf(base, res.getFullPath(), depth)) {
				try {
					visitor.visit(res.getFullPath(), this.getBytes(res));
	    		} catch (TeamException e) {
	    			LoggedOperation.reportError(this.getClass().getName(), e);
	    		}
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
	
	protected IResource[] getAllMembers() throws TeamException {
		List<IResource> res = new ArrayList<IResource>();		
						
		IResource[] roots = this.roots();		
		LinkedList<IResource> queue = new LinkedList<IResource>();
		for (IResource root : roots) {
			queue.add(root);										
		}		
				
		IResource resource = null;
		while ((resource = queue.poll()) != null) {
			if (this.getBytes(resource) != null) {
				res.add(resource);
				
				IResource[] members = this.members(resource);
				for (IResource member : members) {
					queue.add(member);										
				}
			}						
		}
		return res.toArray(new IResource[0]);
	}
	
	protected IResource[] roots() {	
		return UpdateSubscriber.instance().roots();		
	}
	
}
