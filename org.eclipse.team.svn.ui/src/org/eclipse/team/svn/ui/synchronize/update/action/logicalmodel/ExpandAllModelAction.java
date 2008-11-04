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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.AbstractModelToolbarAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Expand All logical model action
 * 
 * @author Igor Burilo
 */
public class ExpandAllModelAction extends AbstractModelToolbarAction {

	public ExpandAllModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected IActionOperation getOperation() {
		return new AbstractActionOperation("Operation.UExpandAll") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						Viewer viewer = ExpandAllModelAction.this.getConfiguration().getPage().getViewer();
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
