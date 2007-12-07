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

package org.eclipse.team.svn.ui.action;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.ICancellableOperationWrapper;
import org.eclipse.team.svn.ui.utility.IOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This class implements operation running policies
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNTeamAction extends TeamAction {
	// copy paste in order to fix problems with Eclipse 3.0.x->3.1.x->3.2 API changes
	private IWorkbenchWindow window;
	private Shell shell;
	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				AbstractSVNTeamAction.this.checkSelection((IStructuredSelection)selection);
			}
		}
	};

	public AbstractSVNTeamAction() {
		super();
	}

	public final void run(final IAction action) {
		ProgressMonitorUtility.doTaskExternal(new AbstractActionOperation("Operation.CallMenuAction") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (AbstractSVNTeamAction.this.isEnabled()) {
					AbstractSVNTeamAction.this.runImpl(action);
				}
			}
		}, new NullProgressMonitor());
	}
	
	public abstract boolean isEnabled();
	public abstract void runImpl(IAction action);
	
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// compatibility with 3.3
	}
	
	protected ICancellableOperationWrapper runBusy(IActionOperation operation) {
		return UIMonitorUtility.doTaskBusy(operation, this.getOperationWrapperFactory());
	}
	
	protected ICancellableOperationWrapper runNow(IActionOperation operation, boolean cancellable) {
		return UIMonitorUtility.doTaskNow(this.getShell(), operation, cancellable, this.getOperationWrapperFactory());
	}
	
	protected ICancellableOperationWrapper runScheduled(IActionOperation operation) {
		return UIMonitorUtility.doTaskScheduled(this.getTargetPart(), operation, this.getOperationWrapperFactory());
	}
	
	protected IOperationWrapperFactory getOperationWrapperFactory() {
		return new DefaultOperationWrapperFactory();
	}

	protected void handleException(Exception ex) {
		this.handle(ex, SVNTeamUIPlugin.instance().getResource("Error.ActionFailed"), SVNTeamUIPlugin.instance().getResource("Error.ActionFailed.Message"));
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
				action.setEnabled(false);
				return;
			}
			
			this.checkSelection((IStructuredSelection)selection);
			
			super.selectionChanged(action, selection);
		}
		catch (Throwable ex) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.MenuEnablement"), ex);
		}
	}
	
	public Object []getSelectedResources(Class c) {
		// This method is created in order to provide fix for Eclipse 3.1.0 where identical method is removed from TeamAction 
		return TeamAction.getSelectedAdaptables(this.getSelection(), c);
	}
	
	protected Shell getShell() {
		return this.shell != null ? this.shell : super.getShell();
	}
	
	public IWorkbenchWindow getWindow() {
		return this.window;
	}
	
	public void init(IWorkbenchWindow window) {
		this.window = window;
		this.shell = this.window.getShell();	
		this.window.getSelectionService().addPostSelectionListener(this.selectionListener);
	}
	
	public void dispose() {
		if (this.window != null) {
			this.window.getSelectionService().removePostSelectionListener(this.selectionListener);
		}
		super.dispose();
	}
	
	protected abstract void checkSelection(IStructuredSelection selection);
	protected abstract IStructuredSelection getSelection();
	
}
