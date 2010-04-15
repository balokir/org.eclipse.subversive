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
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * GetLogMessagesOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class GetLogMessagesOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    SVNRemoteStorage storage = SVNRemoteStorage.instance();
	    GetLogMessagesOperation mainOp = new GetLogMessagesOperation(storage.asRepositoryResource(this.getFirstProject()));
	    CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
	    op.add(mainOp);
	    op.add(new GetLogMessagesOperation(storage.asRepositoryResource(this.getSecondProject())));
		return op;
	}

}
