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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RemoteStatusOperation;

/**
 * RemoteStatusOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class RemoteStatusOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
		return new RemoteStatusOperation(new IResource[] {this.getFirstProject(), this.getSecondProject()});
	}

}
