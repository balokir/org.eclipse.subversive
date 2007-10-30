/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.SwitchOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Switch operation test
 * 
 * @author Sergiy Logvin
 */
public class SwitchOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		IRepositoryResource switchDestination = this.getLocation().asRepositoryContainer(SVNUtility.getProposedBranchesLocation(this.getLocation()) + "/" + this.getFirstFolder().getName(), false);
		return new SwitchOperation(this.getFirstFolder(), switchDestination);
	}

}
