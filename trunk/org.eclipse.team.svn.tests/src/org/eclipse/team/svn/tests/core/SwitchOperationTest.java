/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SwitchOperation test
 *
 * @author Elena Matokhina
 */
public abstract class SwitchOperationTest extends AbstractOperationTestCase {
    protected IActionOperation getOperation() {
        IResource project = this.getFirstProject();
        IRepositoryResource switchDestination = this.getLocation().asRepositoryContainer(SVNUtility.getProposedBranchesLocation(this.getLocation()) + "/" + project.getName(), false);
        return new SwitchOperation(new IResource[] {project}, new IRepositoryResource[] {switchDestination});
	}
   
}
