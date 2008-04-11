/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.compare.PropertyCompareInput;
import org.eclipse.team.svn.ui.compare.ThreeWayPropertyCompareInput;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * TODO insert type description here
 * 
 * @author Alexei Goncharov
 */
public class ComparePropertiesAction extends AbstractSynchronizeModelAction {

	public ComparePropertiesAction(String text,
			ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource local = this.getSelectedResource();
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(local);
		long remoteRev = -1;
		try {
			remoteRev = remote.getRevision();
		}
		catch (Exception ex) {}
		ILocalResource baseResource = SVNRemoteStorage.instance().asLocalResource(local);
		PropertyCompareInput input = new ThreeWayPropertyCompareInput(new CompareConfiguration(),
				new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(local), null, SVNRevision.WORKING),
				new SVNEntryRevisionReference(remote.getUrl(), SVNRevision.fromNumber(remoteRev), SVNRevision.fromNumber(remoteRev)),
				new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(local), null, SVNRevision.BASE),
				remote.getRepositoryLocation(),
				baseResource.getRevision());
		CompareUI.openCompareEditor(input);
		return null;
	}

}
