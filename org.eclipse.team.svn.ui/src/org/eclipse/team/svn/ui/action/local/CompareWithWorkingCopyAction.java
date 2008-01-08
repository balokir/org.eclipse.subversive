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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;

/**
 * Compare menu "compare with base revision" action implementation
 * 
 * @author Alexander Gurov
 */
public class CompareWithWorkingCopyAction extends AbstractWorkingCopyAction {

	public CompareWithWorkingCopyAction() {
		super();
	}

	public void runImpl(IAction action) {
		IResource local = this.getSelectedResources()[0];
		ILocalResource wcInfo = SVNRemoteStorage.instance().asLocalResource(local);
		if (wcInfo != null) {
			IRepositoryResource ancestor = wcInfo.isCopied() ? SVNUtility.getCopiedFrom(local) : SVNRemoteStorage.instance().asRepositoryResource(local);
			IRepositoryResource remote = SVNUtility.copyOf(ancestor);
			remote.setSelectedRevision(SVNRevision.BASE);
			this.runScheduled(new CompareResourcesOperation(local, ancestor, remote));
		}
	}

	public boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(CompareWithWorkingCopyAction.COMPARE_FILTER);
	}
	
	public static final IStateFilter COMPARE_FILTER = new IStateFilter() {
		public boolean accept(IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask) | (mask & ILocalResource.IS_COPIED) != 0;
		}
		
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
	};

}
