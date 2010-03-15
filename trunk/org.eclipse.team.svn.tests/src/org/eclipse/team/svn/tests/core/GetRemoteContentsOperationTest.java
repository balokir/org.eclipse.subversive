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

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetRemoteContentsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * GetRemoteContentsOperation test
 *
 * @author Sergiy Logvin
 */
public abstract class GetRemoteContentsOperationTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
	    SVNRemoteStorage storage = SVNRemoteStorage.instance();
	    IResource local = this.getFirstProject().getFile("maven.xml");
	    IRepositoryResource remote = storage.asRepositoryResource(this.getFirstProject().getFile("maven.xml"));
	    HashMap<String, String> remote2local = new HashMap<String, String>();
	    remote2local.put(SVNUtility.encodeURL(remote.getUrl()), FileUtility.getWorkingCopyPath(local));
		return new GetRemoteContentsOperation(new IResource[] {local}, new IRepositoryResource[] {remote}, remote2local, true);
	}

}
