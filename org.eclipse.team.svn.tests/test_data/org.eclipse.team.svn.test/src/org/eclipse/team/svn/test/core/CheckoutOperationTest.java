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

package org.eclipse.team.svn.test.core;

import org.eclipse.team.svn.core.operation.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.common.IActionOperation;
import org.eclipse.team.svn.ui.SVNTeamProjectMapper;

/**
 * CheckoutOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class CheckoutOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new CheckoutAsOperation(new SVNTeamProjectMapper(), this.getProject().getName(), this.getLocation().getRoot());
	}
	
}
