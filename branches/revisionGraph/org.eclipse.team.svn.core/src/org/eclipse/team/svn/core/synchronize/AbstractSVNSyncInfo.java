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

import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
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

	public AbstractSVNSyncInfo(ILocalResource local, IResourceChange remote, IResourceVariantComparator comparator) {
		this(local, AbstractSVNSyncInfo.makeBaseVariant(local), AbstractSVNSyncInfo.makeRemoteVariant(local, remote), comparator);
	}
	
	protected AbstractSVNSyncInfo(ILocalResource local, IResourceVariant base, IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local.getResource(), base, remote, comparator);
		this.local = local;
	}
	
	public ILocalResource getLocalResource() {
		return this.local;
	}
	
    protected boolean isLinked(String kind, int mask) {
        return IStateFilter.SF_LINKED.accept(this.getLocal(), kind, mask);
    }

    protected boolean isReplaced(String kind, int mask) {
        return IStateFilter.SF_PREREPLACEDREPLACED.accept(this.getLocal(), kind, mask);
    }

    protected boolean isDeleted(String kind, int mask) {
        return IStateFilter.SF_DELETED.accept(this.getLocal(), kind, mask);
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
        return IStateFilter.SF_UNVERSIONED.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isNotExists(String kind, int mask) {
        return IStateFilter.SF_NOTEXISTS.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isIgnored(String kind, int mask) {
        return IStateFilter.SF_IGNORED.accept(this.getLocal(), kind, mask);
    }
    
    protected boolean isAdded(String kind, int mask) {
        return IStateFilter.SF_ADDED.accept(this.getLocal(), kind, mask);
    }

    protected static IResourceVariant makeBaseVariant(ILocalResource local) {
		if (local == null) {
			return null;
		}
		return (local instanceof ILocalFolder) ? (IResourceVariant)new BaseFolderVariant(local) : new BaseFileVariant(local);
	}
	
    protected static IResourceVariant makeRemoteVariant(ILocalResource local, ILocalResource remote) {
		if (remote == null) {
			return (local instanceof ILocalFolder) ? (IResourceVariant)new VirtualRemoteFolderVariant(local) : new VirtualRemoteFileVariant(local);
		}
		return (remote instanceof ILocalFolder) ? (IResourceVariant)new RemoteFolderVariant(remote) : new RemoteFileVariant(remote);
	}
	
}
