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

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetResourceAnnotationOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * GetResourceAnnotationOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class GetResourceAnnotationOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    SVNRemoteStorage storage = SVNRemoteStorage.instance();
		return new GetResourceAnnotationOperation(storage.asRepositoryResource(this.getFirstProject().getFile("maven.xml")), new SVNRevisionRange(SVNRevision.fromNumber(0), SVNRevision.HEAD));
	}

}
