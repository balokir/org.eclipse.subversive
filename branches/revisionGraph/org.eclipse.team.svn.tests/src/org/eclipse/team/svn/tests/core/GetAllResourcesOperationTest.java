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

import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;

/**
 * GetAllResourcesOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class GetAllResourcesOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    GetAllResourcesOperation mainOp = new GetAllResourcesOperation(this.getFirstProject());
	    CompositeOperation op = new CompositeOperation(mainOp.getId());
	    op.add(mainOp);
	    op.add(new GetAllResourcesOperation(this.getSecondProject()));
		return op;
	}

}
