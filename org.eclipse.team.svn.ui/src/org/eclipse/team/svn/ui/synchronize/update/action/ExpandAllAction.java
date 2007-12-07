/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Expand All Action
 * 
 * @author Sergiy Logvin
 */
public class ExpandAllAction extends AbstractSynchronizeModelAction {
	
	protected ISynchronizePageConfiguration pageConfiguration;
	
	public ExpandAllAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.pageConfiguration = configuration;
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		return new AbstractActionOperation("Operation.UExpandAll") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				operation.getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						Viewer viewer = ExpandAllAction.this.pageConfiguration.getPage().getViewer();
						if (viewer == null || viewer.getControl().isDisposed() || !(viewer instanceof AbstractTreeViewer)) {
							return;
						}
						viewer.getControl().setRedraw(false);		
						((AbstractTreeViewer)viewer).expandAll();
						viewer.getControl().setRedraw(true);
					}
				});
			}
		};
	}

}
