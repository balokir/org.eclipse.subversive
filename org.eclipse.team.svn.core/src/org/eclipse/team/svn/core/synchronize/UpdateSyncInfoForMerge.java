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

import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;

/**
 * Sync info implementation used in Merge view
 * 
 * It's used to present sync info for resources which were affected by
 * merge operation, e.g. changed, conflicted resources etc.
 * 
 * Note that in constructor we pass to second parameter
 * (for IResourceChange remote) null value, this parameter presents incoming change.
 * Why this is done: as resource after merge operation will already contain incoming changes,
 * we should show that there are no incoming changes for this resource and so 
 * we provide null. But we need to store somehow its base and remote resources
 * which were used in merge operation. These base and remote resources can be further used in
 * some remote operations, e.g. get remote content, show remote history etc.  
 * 
 * As we pass 'null' to second parameter, we can't use Team API for getting remote resource
 * and so this class implements IMergeSyncInfo.
 * 
 * Team API approach for getting Subversive's remote resource (can't be used here):
 *  SyncInfo syncInfo = ...;
 *  ResourceVariant resourceVariant = (ResourceVariant) syncInfo.getRemote();
 *  ILocalResource local = resourceVariant.getResource();
 *  if (local instanceof IResourceChange) {
 *  	IResourceChange resourceChange = (IResourceChange) local;
 *  	...
 *  }
 * 
 * @author Igor Burilo
 */
public class UpdateSyncInfoForMerge extends UpdateSyncInfo implements IMergeSyncInfo {

	protected IResourceChange baseStatus;
	protected IResourceChange remoteStatus;
	
	public UpdateSyncInfoForMerge(ILocalResource local, IResourceChange baseStatus, IResourceChange remoteStatus, IResourceVariantComparator comparator) {
		super(local, null, comparator);
		this.baseStatus = baseStatus;
		this.remoteStatus = remoteStatus;
	}
	
	public IResourceChange getBaseResource() {
		return this.baseStatus;
	}
	
	public IResourceChange getRemoteResource() {
		return this.remoteStatus;
	}	
	
}
