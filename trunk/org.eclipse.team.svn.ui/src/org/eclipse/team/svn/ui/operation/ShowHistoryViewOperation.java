/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show history view operation
 * 
 * @author Alexander Gurov
 */
public class ShowHistoryViewOperation extends AbstractNonLockingOperation {
	protected IRepositoryResource remote;
	protected IResource local;
	protected int options;
	protected int mask;

	public ShowHistoryViewOperation(IResource local, int mask, int options) {
		super("Operation.ShowHistory");
		this.mask = mask;
		this.options = options;
		this.local = local;
	}

	public ShowHistoryViewOperation(IRepositoryResource remote, int mask, int options) {
		super("Operation.ShowHistory");
		this.mask = mask;
		this.options = options;
		this.remote = remote;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = UIMonitorUtility.getActivePage();
				
				if (page != null) {
					try {
						IHistoryView historyView = (IHistoryView)page.showView(SVNHistoryPage.VIEW_ID);
						if (historyView != null) {
							SVNHistoryPage hPage = (SVNHistoryPage)historyView.showHistoryFor(ShowHistoryViewOperation.this.local != null ? (Object)ShowHistoryViewOperation.this.local : ShowHistoryViewOperation.this.remote);
							if (hPage != null) {
								hPage.setOptions(ShowHistoryViewOperation.this.mask, ShowHistoryViewOperation.this.options);
							}
						}
					}
					catch (PartInitException ex) {
						ShowHistoryViewOperation.this.reportError(ex);
					}
				}
			}
		});
	}

}
