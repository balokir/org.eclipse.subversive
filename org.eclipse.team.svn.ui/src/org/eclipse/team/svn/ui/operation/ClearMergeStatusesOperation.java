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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSubscriber;

/**
 * Clear merge statuses operation implementation
 * 
 * @author Alexander Gurov
 */
public class ClearMergeStatusesOperation extends AbstractWorkingCopyOperation {
	public ClearMergeStatusesOperation(IResource[] resources) {
		super("Operation.ClearMergeStatusesCache", resources);
	}

	public ClearMergeStatusesOperation(IResourceProvider provider) {
		super("Operation.ClearMergeStatusesCache", provider);
	}

	public ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	public int getOperationWeight() {
		return 0;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		MergeSubscriber.instance().clearRemoteStatuses(this.operableData());
	}

}
