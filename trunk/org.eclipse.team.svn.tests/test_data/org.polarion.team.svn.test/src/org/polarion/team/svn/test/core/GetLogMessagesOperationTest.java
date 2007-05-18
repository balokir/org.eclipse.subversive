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

import org.eclipse.team.svn.core.operation.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.common.IActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * GetLogMessagesOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class GetLogMessagesOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new GetLogMessagesOperation(SVNRemoteStorage.instance().asRepositoryResource(this.getProject()));
	}

}
