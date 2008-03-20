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
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.history.ISVNHistoryView;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Show history view operation
 * 
 * @author Alexander Gurov
 */
public class ShowHistoryViewOperation extends AbstractActionOperation {
	protected IRepositoryResource remote;
	protected IRepositoryResourceProvider provider;
	protected IResource compareWith;
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

	public ShowHistoryViewOperation(IRepositoryResourceProvider provider, int mask, int options) {
		super("Operation.ShowHistory");
		this.mask = mask;
		this.options = options;
		this.provider = provider;
	}

	public ShowHistoryViewOperation(IResource compareWith, IRepositoryResource remote, int mask, int options) {
		super("Operation.ShowHistory");
		this.mask = mask | ISVNHistoryView.COMPARE_MODE;
		this.options = options | ISVNHistoryView.COMPARE_MODE;
		this.remote = remote;
		this.compareWith = compareWith;
	}

	public int getOperationWeight() {
		return 0;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (this.provider != null) {
			this.remote = this.provider.getRepositoryResources()[0];
		}
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = UIMonitorUtility.getActivePage();
				
				if (page != null) {
					try {
						IHistoryView historyView = (IHistoryView)page.showView(IHistoryView.VIEW_ID);
						if (historyView != null) {
							SVNHistoryPage hPage = (SVNHistoryPage)historyView.showHistoryFor(ShowHistoryViewOperation.this.local != null ? (Object)ShowHistoryViewOperation.this.local : ShowHistoryViewOperation.this.remote);
							if (hPage != null) {
								hPage.setOptions(ShowHistoryViewOperation.this.mask, ShowHistoryViewOperation.this.options);
								hPage.setCompareWith(ShowHistoryViewOperation.this.compareWith);
								if (ShowHistoryViewOperation.this.local != null && !ShowHistoryViewOperation.this.local.equals(hPage.getResource())) {
									// view is disconnected
									hPage.showHistory(ShowHistoryViewOperation.this.local);
								}
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
