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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;

/**
 * Unlock operation test
 *
 * @author Sergiy Logvin
 */
public abstract class UnlockOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    IResource remote1 = this.getFirstProject().getFile("maven.xml");
	    IResource remote2 = this.getSecondProject().getFile("bumprev.sh");
		return new UnlockOperation(new IResource[] {remote1, remote2});		
	}

}
