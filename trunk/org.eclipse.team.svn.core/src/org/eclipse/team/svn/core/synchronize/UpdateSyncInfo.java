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

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;

/**
 * Concrete SyncInfo implementation
 * 
 * @author Alexander Gurov
 */
public class UpdateSyncInfo extends AbstractSVNSyncInfo {
	
	public UpdateSyncInfo(ILocalResource local, IResourceChange remote, IResourceVariantComparator comparator) {
		super(local, remote, comparator);
	}
	
    protected int calculateKind() throws TeamException {
        String localKind = this.local == null ? IStateFilter.ST_NOTEXISTS : this.local.getStatus();
        int localMask = this.local == null ? 0 : this.local.getChangeMask();
        String remoteKind = 
        	this.remoteStatus == null ? 
        	(this.isNonVersioned(localKind, localMask) ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NORMAL) : 
        		this.remoteStatus.getStatus();
        int remoteMask = this.remoteStatus == null ? 0 : this.remoteStatus.getChangeMask();
        	
        if (this.isLinked(localKind, localMask)) {
        	// Corresponding resource can be added at remote site
			if (this.isAdded(remoteKind, remoteMask)) {
				this.localKind = SyncInfo.OUTGOING | SyncInfo.ADDITION;
				this.remoteKind = SyncInfo.INCOMING | SyncInfo.ADDITION;
				return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
			}
    		return SyncInfo.IN_SYNC;
        }
               
        if (this.isTreeConflicted(localKind, localMask)) {
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.CHANGE;
        	return SyncInfo.CONFLICTING | SyncInfo.CHANGE;        	
        }
//		if (this.isTreeConflicted(remoteKind, remoteMask)) {
//			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
//			this.remoteKind = SyncInfo.INCOMING | SyncInfo.CHANGE;
//			return SyncInfo.CONFLICTING | SyncInfo.CHANGE;        	
//		}
        
        if (this.isReplaced(remoteKind, remoteMask)) {
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.CHANGE;
            if (this.isNotModified(localKind, localMask)) {
            	return SyncInfo.INCOMING | SyncInfo.CHANGE;
            }
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
    		return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
        }
    	if (this.isAdded(remoteKind, remoteMask)) {
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.ADDITION;
    		if (this.isNotExists(localKind, localMask)) {
        		return SyncInfo.INCOMING | SyncInfo.ADDITION;
    		}
			this.localKind = SyncInfo.OUTGOING | SyncInfo.ADDITION;
    		return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
    	}
    	if (this.isModified(remoteKind, remoteMask)) {
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.CHANGE;
            if (this.isNotModified(localKind, localMask)) {
            	return SyncInfo.INCOMING | SyncInfo.CHANGE;
            }
        	if (this.isDeleted(localKind, localMask)) {
    			this.localKind = SyncInfo.OUTGOING | SyncInfo.DELETION;
        		return SyncInfo.CONFLICTING | SyncInfo.DELETION;
        	}
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
    		return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
    	}
    	if (this.isDeleted(remoteKind, remoteMask)) {
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.DELETION;
            if (this.isNotModified(localKind, localMask)) {
        		return SyncInfo.INCOMING | SyncInfo.DELETION;
            }
        	if (this.isDeleted(localKind, localMask)) {
    			this.localKind = SyncInfo.OUTGOING | SyncInfo.DELETION;
        		return SyncInfo.CONFLICTING | SyncInfo.DELETION;
        	}
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
    		return SyncInfo.CONFLICTING | SyncInfo.DELETION;
    	}
    	if (this.isNonVersioned(remoteKind, remoteMask)) {
        	if (this.isIgnored(localKind, localMask) || this.isNotExists(localKind, localMask)) {
	    		return SyncInfo.IN_SYNC;
	    	}
			this.localKind = SyncInfo.OUTGOING | SyncInfo.ADDITION;
        	return SyncInfo.OUTGOING | SyncInfo.ADDITION;
        }
        
    	//if (this.isNotModified(remoteKind)) {...
    	if (this.isConflicted(localKind, localMask)) {
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
			this.remoteKind = SyncInfo.INCOMING | SyncInfo.CHANGE;
        	return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
    	}
    	if (this.isReplaced(localKind, localMask) || this.isModified(localKind, localMask)) {
			this.localKind = SyncInfo.OUTGOING | SyncInfo.CHANGE;
        	return SyncInfo.OUTGOING | SyncInfo.CHANGE;
    	}
    	if (this.isDeleted(localKind, localMask)) {
			this.localKind = SyncInfo.OUTGOING | SyncInfo.DELETION;
    		return SyncInfo.OUTGOING | SyncInfo.DELETION;
    	}
    	if (this.isAdded(localKind, localMask)) {
			this.localKind = SyncInfo.OUTGOING | SyncInfo.ADDITION;
    		return SyncInfo.OUTGOING | SyncInfo.ADDITION;
    	}
    	
    	//if (this.isNotModified(localKind)) {...
		return SyncInfo.IN_SYNC;
    }
    
}
