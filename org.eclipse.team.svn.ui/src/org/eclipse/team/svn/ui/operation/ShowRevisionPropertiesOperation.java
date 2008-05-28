/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.properties.RevPropertiesView;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Show revision properties operation
 * 
 * @author Alexei Goncharov
 */
public class ShowRevisionPropertiesOperation extends AbstractActionOperation {

	protected IRepositoryLocation location;
	protected SVNRevision revision;
	protected IWorkbenchPage page;
	
	public ShowRevisionPropertiesOperation(IWorkbenchPage page, IRepositoryLocation location, SVNRevision revision) {
		super("Operation.ShowRevProperties");
		this.page = page;
		this.location = location;
		this.revision = revision;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ShowRevisionPropertiesOperation.this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws PartInitException {
						RevPropertiesView view = (RevPropertiesView)ShowRevisionPropertiesOperation.this.page.showView(RevPropertiesView.VIEW_ID);
						view.setLocationAndRevision(ShowRevisionPropertiesOperation.this.location, ShowRevisionPropertiesOperation.this.revision);
					}
				}, monitor, 1);
				
			}
		});
	}

}
