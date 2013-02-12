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

package org.eclipse.team.svn.ui.synchronize.action.logicalmodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.annotate.BuiltInAnnotate;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show annotation logical model action implementation
 * 
 * @author Igor Burilo
 */
public class ShowOutgoingAnnotationModelAction extends AbstractSynchronizeLogicalModelAction {

	public ShowOutgoingAnnotationModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {						
				IResource resource = this.getSelectedResource();
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				// null for change set nodes
				return local instanceof ILocalFile && IStateFilter.SF_ONREPOSITORY.accept(local);
			}	
		}
	    return false;
	}
	
	protected IActionOperation getOperation() {
		return new BuiltInAnnotate().getAnnotateOperation(UIMonitorUtility.getActivePage(), (IFile)this.getSelectedResource(), UIMonitorUtility.getShell());
	}

}
