/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.refactor.CopyResourceOperation;

/**
 * CopyLocalResourceOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class CopyLocalResourceOperationTest extends AbstractOperationTestCase {
    protected IActionOperation getOperation() {
        IResource source = this.getSecondProject().getFile("site.xml");
        File newFolder = new File(this.getSecondProject().getLocation().toString() + "/web");
        newFolder.mkdirs();
        this.refreshProjects();
        IResource destination = this.getSecondProject().getFile("web/site.xml");
        return new CopyResourceOperation(source, destination);
	}
    
}
