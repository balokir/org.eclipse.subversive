/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.operation.local.change.visitors.RestorePropertiesVisitor;

/**
 * Restores svn:externals to its original values
 * 
 * @author Alexander Gurov
 */
public class RestoreExternalsOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor {
	protected FreezeExternalsOperation freezeOp;

	public RestoreExternalsOperation(FreezeExternalsOperation freezeOp) {
		super("Operation.RestoreExternals", freezeOp);
		this.freezeOp = freezeOp;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final RestorePropertiesVisitor visitor = new RestorePropertiesVisitor();
		
		for (Iterator it = this.freezeOp.getChanges().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			final ResourceChange change = (ResourceChange)it.next();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					change.traverse(visitor, IResource.DEPTH_ZERO, RestoreExternalsOperation.this, monitor);
				}
			}, monitor, this.freezeOp.getChanges().size());
		}
	}

	public void doOperation(IActionOperation op, IProgressMonitor monitor) {
    	this.reportStatus(op.run(monitor).getStatus());
	}

}
