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

package org.eclipse.team.svn.core.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.variant.BaseFileVariant;
import org.eclipse.team.svn.core.synchronize.variant.BaseFolderVariant;
import org.eclipse.team.svn.core.synchronize.variant.RemoteFileVariant;
import org.eclipse.team.svn.core.synchronize.variant.RemoteFolderVariant;
import org.eclipse.team.svn.core.synchronize.variant.VirtualRemoteFileVariant;
import org.eclipse.team.svn.core.synchronize.variant.VirtualRemoteFolderVariant;

/**
 * Abstract SVN SyncInfo implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNSyncInfo extends SyncInfo {

	protected ILocalResource local;

	//If there are no remote changes it's null
	protected IResourceChange remoteStatus;	
	
	public AbstractSVNSyncInfo(ILocalResource local, IResourceChange remote, IResourceVariantComparator comparator) {						
		this(local, AbstractSVNSyncInfo.makeBaseVariant(local), AbstractSVNSyncInfo.makeRemoteVariant(local, remote), comparator, remote);
	}
	
	protected AbstractSVNSyncInfo(ILocalResource local, IResourceVariant base, IResourceVariant remote, IResourceVariantComparator comparator, IResourceChange remoteStatus) {
		super(local.getResource(), base, remote, comparator);
		this.local = local;
		this.remoteStatus = remoteStatus;		
	}
	
	public ILocalResource getLocalResource() {
		return this.local;
	}
	
	public IResourceChange getRemoteResource() {
		return this.remoteStatus;
	}
	
	/**
	 * If resource is deleted on repository or there are no remote changes 
	 * then remote resource variant, see {@link SyncInfo#getRemote()},
	 * will be null. So we can't use SyncInfo#getRemote() for all our status checks because when it returns null
	 * we can't know whether resource has no remote changes or it's deleted. In order not to complicate too much
	 * our status verification code we return out custom remote resource which if there are no remote changes is equal
	 * to local resource from which we can easily determine status. 
	 */
	public ILocalResource getRemoteChangeResource() {
		return this.remoteStatus != null ? this.remoteStatus : this.local;
	}
	
	public ILocalResource getBaseChangeResource() {
		return this.local;
	}
	
    protected boolean isLinked(String kind, int mask) {
        return IStateFilter.SF_LINKED.accept(this.getLocal(), kind, mask);
    }

    protected boolean isReplaced(String kind, int mask) {
    	return AbstractSVNSyncInfo.isReplaced(this.getLocal(), kind, mask);
    }
    
    protected static boolean isReplaced(IResource resource, String kind, int mask) {
        return IStateFilter.SF_PREREPLACEDREPLACED.accept(resource, kind, mask);
    }

    protected boolean isDeleted(String kind, int mask) {
    	return AbstractSVNSyncInfo.isDeleted(this.getLocal(), kind, mask);
    }
    
    protected static boolean isDeleted(IResource resource, String kind, int mask) {
        return IStateFilter.SF_DELETED.accept(resource, kind, mask);
    }

    protected boolean isModified(String kind, int mask) {
        return IStateFilter.SF_MODIFIED.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isConflicted(String kind, int mask) {
        return IStateFilter.SF_CONFLICTING.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isTreeConflicted(String kind, int mask) {    	
        return IStateFilter.SF_TREE_CONFLICTING.accept(this.getLocal(), kind, mask);
    }    
    
    protected boolean isNotModified(String kind, int mask) {
        return IStateFilter.SF_NOTMODIFIED.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isNonVersioned(String kind, int mask) {
    	return AbstractSVNSyncInfo.isNonVersioned(this.getLocal(), kind, mask);
    }    
    
    protected static boolean isNonVersioned(IResource resource, String kind, int mask) {
        return IStateFilter.SF_UNVERSIONED.accept(resource, kind, mask);
    }        
    
    protected boolean isNotExists(String kind, int mask) {
    	return AbstractSVNSyncInfo.isNotExists(this.getLocal(), kind, mask);
    }
        
    protected static boolean isNotExists(IResource resource, String kind, int mask) {
        return IStateFilter.SF_NOTEXISTS.accept(resource, kind, mask);
    }
    
    protected boolean isIgnored(String kind, int mask) {
    	return AbstractSVNSyncInfo.isIgnored(this.getLocal(), kind, mask);
    }
    
    protected static boolean isIgnored(IResource resource, String kind, int mask) {
        return IStateFilter.SF_IGNORED.accept(resource, kind, mask);
    }
    
    protected boolean isAdded(String kind, int mask) {    
    	return AbstractSVNSyncInfo.isAdded(this.getLocal(), kind, mask);
    }
    
    protected static boolean isAdded(IResource resource, String kind, int mask) {
        return IStateFilter.SF_ADDED.accept(resource, kind, mask);
    }

    /**
     * If resource doesn't exist locally, return null
     */
    protected static IResourceVariant makeBaseVariant(ILocalResource local) {
		if (local == null) {
			return null;
		}
				
		if (local.getRevision() == SVNRevision.INVALID_REVISION_NUMBER) {			
			return null;
		}
				
		return (local instanceof ILocalFolder) ? (IResourceVariant)new BaseFolderVariant(local) : new BaseFileVariant(local);
	}
	
    /**
     * If resource doesn't exist in repository, return null       
     */
    protected static IResourceVariant makeRemoteVariant(ILocalResource local, ILocalResource remote) {    	 	    	    	
    	String localKind = local == null ? IStateFilter.ST_NOTEXISTS : local.getStatus();
        int localMask = local == null ? 0 : local.getChangeMask();
              
        IResource resource = local.getResource();
        
        String remoteKind = 
        	remote == null ? 
        	(isNonVersioned(resource, localKind, localMask) ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NORMAL) :  remote.getStatus();
        int remoteMask = remote == null ? 0 : remote.getChangeMask();
        
        
        //remote: not_exist remotely
    	if (isNonVersioned(resource, remoteKind, remoteMask)) {
    		return null;
        }
            	
    	//deleted remotely
    	if (!isReplaced(resource, remoteKind, remoteMask) && isDeleted(resource, remoteKind, remoteMask)) {
       		return null;       		    		
    	}  
    	
    	//locally added and no remote status
    	if (remote == null && isAdded(resource, localKind, localMask)) {    	
        	return null;
    	}
	
     	if (remote == null) {
			return (local instanceof ILocalFolder) ? (IResourceVariant)new VirtualRemoteFolderVariant(local) : new VirtualRemoteFileVariant(local);
		}
		return (remote instanceof ILocalFolder) ? (IResourceVariant)new RemoteFolderVariant(remote) : new RemoteFileVariant(remote);
	}
	
}
