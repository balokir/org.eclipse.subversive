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

package org.eclipse.team.svn.ui.synchronize.merge.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.operation.LocalShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show annotation action
 * 
 * @author Alexander Gurov
 */
public class ShowAnnotationAction extends AbstractSynchronizeModelAction {

	public ShowAnnotationAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof SyncInfoModelElement)) {
		    return false;
		}
		SyncInfoModelElement element = (SyncInfoModelElement)selection.getFirstElement();
		MergeSyncInfo sync = (MergeSyncInfo)element.getSyncInfo();
	    ILocalResource local = ((RemoteResourceVariant)sync.getRemote()).getResource();
		return local instanceof ILocalFile;
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		return new AbstractNonLockingOperation("Operation.MShowAnnotation") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				operation.getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
					    MergeSyncInfo info = (MergeSyncInfo)operation.getSVNSyncInfo();
					    ILocalResource local = ((RemoteResourceVariant)info.getRemote()).getResource();
						IWorkbenchPage page = operation.getPart().getSite().getPage();
						UIMonitorUtility.doTaskBusyDefault(
								local instanceof IResourceChange ? 
								(IActionOperation)new RemoteShowAnnotationOperation(((IResourceChange)local).getOriginator(), page) :
								new LocalShowAnnotationOperation(local.getResource(), page));
					}
				});
			}
		};
	}

}
