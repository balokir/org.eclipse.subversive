/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file;

import java.io.File;

import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.CheckoutAsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Checkout as operation test
 * 
 * @author Sergiy Logvin
 */
public class CheckoutAsOperationTest extends AbstractOperationTestCase {
	
	protected IActionOperation getOperation() {
		IRepositoryResource from = this.getLocation().asRepositoryContainer(SVNUtility.getProposedTrunkLocation(this.getLocation()) + "/" + this.getFirstFolder().getName(), false);
		CompositeOperation composite = new CompositeOperation("Checkout", SVNMessages.class);
		for (int i = 0; i < 10; i++) {
			File to = new File(this.getFirstFolder().getPath() + "_checkout_" + i);
			CheckoutAsOperation op = new CheckoutAsOperation(to, from, Depth.INFINITY, false, true);
			composite.add(op);
		}
		return composite; 
	}

}
