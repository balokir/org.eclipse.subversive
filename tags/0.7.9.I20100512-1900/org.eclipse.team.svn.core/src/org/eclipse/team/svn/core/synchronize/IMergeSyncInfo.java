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

import org.eclipse.team.svn.core.resource.IResourceChange;

/**
 * Provide base and remote resource changes for Merge view statuses
 * 
 * As we can construct two types of statuses: MergeSyncInfo and UpdateSyncInfoForMerge
 * (see MergeSubscriber#getSVNSyncInfo),
 * this interface allows to return in the same manner base and remote resources
 * despite of what merge sync info object we use.
 * 
 *  For more details why we don't use Team API for getting these resources
 *  see UpdateSyncInfoForMerge
 * 
 * @author Igor Burilo
 */
public interface IMergeSyncInfo {
	
	IResourceChange getBaseResource();
	IResourceChange getRemoteResource();	
}
