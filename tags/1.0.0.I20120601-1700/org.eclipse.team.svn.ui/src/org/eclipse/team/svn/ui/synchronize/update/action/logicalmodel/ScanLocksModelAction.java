/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.lock.LocksView;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.ScanLocksAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Scan locks logical model action implementation for Synchronize View
 * 
 * @author Igor Burilo
 */
public class ScanLocksModelAction extends AbstractSynchronizeLogicalModelAction {

	public ScanLocksModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {						
				IResource resource = this.getSelectedResource();
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (local != null) {
					return IStateFilter.SF_ONREPOSITORY.accept(local);	
				}				
			}	
		}
	    return false;
	}
	
	protected IActionOperation getOperation() {
		IResource resource = this.getSelectedResource();				
		IWorkbenchPage page = UIMonitorUtility.getActivePage();
		if (page != null) { 
			try {
				LocksView view = (LocksView) page.showView(LocksView.VIEW_ID);
				if (view != null) {
					view.setResourceWithoutActionExecution(resource);
					IActionOperation op = view.getUpdateViewOperation();
					return op;		
				}	
			} catch (PartInitException pe) {
				LoggedOperation.reportError(ScanLocksAction.class.getName(), pe);
			}		
		}
		return null;
	}

}
