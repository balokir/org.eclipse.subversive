/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * AddToSVNIgnoreOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class AddToSVNIgnoreOperationTest extends AbstractOperationTestCase {
    protected IActionOperation getOperation() {
        try {
            FileUtility.copyFile(this.getFirstProject().getFolder("src").getLocation().toFile(), this.getSecondProject().getFile("bumprev.sh").getLocation().toFile(), new NullProgressMonitor());
        } catch (Exception e) {            
            e.printStackTrace();
        }        
        IResource[] ignoreResource = new IResource[] {this.getFirstProject().getFile("src/bumprev.sh")};
        return new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "");
    }

}
