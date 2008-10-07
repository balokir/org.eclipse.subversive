/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Rene Link - [patch] NPE in Interactive Merge UI
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.merge;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;

/**
 * Merge sync info: ignores outgoing changes
 * 
 * @author Alexander Gurov
 */
public class MergeSyncInfo extends AbstractSVNSyncInfo {
	public MergeSyncInfo(ILocalResource local, IResourceChange base, IResourceChange remote, IResourceVariantComparator comparator) {
		super(local, base == null ? AbstractSVNSyncInfo.makeBaseVariant(local) : AbstractSVNSyncInfo.makeRemoteVariant(local, base), AbstractSVNSyncInfo.makeRemoteVariant(local, remote), comparator);
	}

	protected int calculateKind() throws TeamException {
		String localKind = this.local == null ? IStateFilter.ST_NOTEXISTS : this.local.getStatus();
		int localMask = this.local == null ? 0 : this.local.getChangeMask();
		String remoteKind = this.getRemote() == null ? (this.isNonVersioned(localKind, localMask) ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NORMAL) : ((ResourceVariant) this
				.getRemote()).getStatus();
		int remoteMask = this.getRemote() == null ? 0 : ((ResourceVariant) this.getRemote()).getResource().getChangeMask();

		if (this.isLinked(localKind, localMask)) {
        	// Corresponding resource at remote site can produce a change
			if (this.isAdded(remoteKind, remoteMask)) {
				return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
			}
			if (this.isModified(remoteKind, remoteMask) || this.isReplaced(remoteKind, remoteMask)) {
				return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
			}
			if (this.isDeleted(remoteKind, remoteMask)) {
				return SyncInfo.CONFLICTING | SyncInfo.DELETION;
			}
			return SyncInfo.IN_SYNC;
		}

		if (this.isAdded(remoteKind, remoteMask)) {
			if (this.isNotExists(localKind, localMask) || this.isDeleted(localKind, localMask)) {
				return SyncInfo.INCOMING | SyncInfo.ADDITION;
			}
			return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
		}
		if (this.isConflicted(remoteKind, remoteMask)) {
			return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
		}
		if (this.isModified(remoteKind, remoteMask)) {
			if (this.isNotExists(localKind, localMask)) {
				return SyncInfo.INCOMING | SyncInfo.ADDITION;
			}
			if (this.isDeleted(localKind, localMask)) {
				return SyncInfo.CONFLICTING | SyncInfo.DELETION;
			}
			return SyncInfo.INCOMING | SyncInfo.CHANGE;
		}
		if (this.isDeleted(remoteKind, remoteMask)) {
			if (this.isNotExists(localKind, localMask) || this.isDeleted(localKind, localMask)) {
				return SyncInfo.IN_SYNC;
			}
			if (this.isNotModified(localKind, localMask)) {
				return SyncInfo.INCOMING | SyncInfo.DELETION;
			}
			return SyncInfo.CONFLICTING | SyncInfo.DELETION;
		}

		// if (this.isNotModified(remoteKind)) {...
		return SyncInfo.IN_SYNC;
	}

}
