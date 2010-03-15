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

package org.eclipse.team.svn.tests.core;

import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * CheckoutOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class CheckoutOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    IRepositoryResource trunk = SVNUtility.getProposedTrunk(this.getLocation());
	    CheckoutAsOperation mainOp = new CheckoutAsOperation(this.getFirstProject().getName(), trunk.asRepositoryContainer(this.getFirstProject().getName(), false), Depth.INFINITY, true);
	    CompositeOperation op = new CompositeOperation(mainOp.getId());
	    op.add(mainOp);
	    op.add(new CheckoutAsOperation(this.getSecondProject().getName(), trunk.asRepositoryContainer(this.getSecondProject().getName(), false), Depth.INFINITY, true));
		return op;
	}
	
}
