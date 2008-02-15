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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.operation.LocalShowAnnotationOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show annotation action implementation
 * 
 * @author Alexander Gurov
 */
public class ShowOutgoingAnnotationAction extends AbstractSynchronizeModelAction {

	public ShowOutgoingAnnotationAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(element.getResource());
			// null for change set nodes
			return local instanceof ILocalFile && IStateFilter.SF_ONREPOSITORY.accept(local);
		}
	    return false;
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		return new AbstractActionOperation("Operation.UShowAnnotation") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				operation.getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
					    IResource resource = ShowOutgoingAnnotationAction.this.getSelectedResource();
						UIMonitorUtility.doTaskBusyDefault(new LocalShowAnnotationOperation(resource, operation.getPart().getSite().getPage()));
					}
				});
			}
		};
	}

}
