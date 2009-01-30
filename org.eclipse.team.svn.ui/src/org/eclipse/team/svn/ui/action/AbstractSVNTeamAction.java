/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.ICancellableOperationWrapper;
import org.eclipse.team.svn.ui.utility.IOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

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

	/**
	 * Save all dirty editors in the workbench that are open on files that may
	 * be affected by this operation. Opens a dialog to prompt the user if
	 * <code>confirm</code> is true. Return true if successful. Return false
	 * if the user has canceled the command. Must be called from the UI thread.
	 * 
	 * @param confirm prompt the user if true
	 * @return boolean false if the operation was canceled.
	 */
	public final boolean saveAllEditors(boolean confirm) {
		return IDE.saveAllEditors(this.getOriginalSelectedResources(), confirm);
	}
	
	public abstract boolean isEnabled();
	public abstract void runImpl(IAction action);
	
	protected final void execute(final IAction action) throws InvocationTargetException, InterruptedException {
		ProgressMonitorUtility.doTaskExternal(new AbstractActionOperation("Operation_CallMenuAction") { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (AbstractSVNTeamAction.this.isEnabled()) {
					if (AbstractSVNTeamAction.this.needsToSaveDirtyEditors() && 
						!AbstractSVNTeamAction.this.saveAllEditors(AbstractSVNTeamAction.this.confirmSaveOfDirtyEditor())) {
						return;
					}
					AbstractSVNTeamAction.this.runImpl(action);
				}
			}
		}, new NullProgressMonitor());
	}
	
	/**
	 * Return whether dirty editor should be saved before this action is run.
	 * Default is <code>false</code>.
	 * 
	 * @return whether dirty editor should be saved before this action is run
	 */
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}

	/**
	 * Returns whether the user should be prompted to save dirty editors. The
	 * default is <code>true</code>.
	 * 
	 * @return whether the user should be prompted to save dirty editors
	 */
	protected boolean confirmSaveOfDirtyEditor() {
		return true;
	}
	
	protected ICancellableOperationWrapper runBusy(IActionOperation operation) {
		return UIMonitorUtility.doTaskBusy(operation, this.getOperationWrapperFactory());
	}
	
	protected ICancellableOperationWrapper runScheduled(IActionOperation operation) {
		return UIMonitorUtility.doTaskScheduled(this.getTargetPart(), operation, this.getOperationWrapperFactory());
	}
	
	protected IOperationWrapperFactory getOperationWrapperFactory() {
		return new DefaultOperationWrapperFactory();
	}

	protected void handleException(Exception ex) {
		this.handle(ex, SVNUIMessages.getErrorString("Error_ActionFailed"), SVNUIMessages.Error_ActionFailed_Message); //$NON-NLS-1$
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
				if (action != null) {
					action.setEnabled(false);
				}
				return;
			}
			
			this.checkSelection((IStructuredSelection)selection);
			
			super.selectionChanged(action, selection);
		}
		catch (Throwable ex) {
			LoggedOperation.reportError(SVNUIMessages.getErrorString("Error_MenuEnablement"), ex); //$NON-NLS-1$
		}
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */		
	protected IResource[] getSelectedResources() {	
		//filters out not valid resources   		
		List<IResource> res = new ArrayList<IResource>();
		IResource[] resources = super.getSelectedResources();
		for (IResource resource : resources) {
			if (!FileUtility.isIgnored(resource)) {
				res.add(resource);
			}
		} 				
		return res.toArray(new IResource[0]);
	}	
	
	protected IResource[] getOriginalSelectedResources() {		
		return super.getSelectedResources();		
	} 
	
	protected abstract void checkSelection(IStructuredSelection selection);
	protected abstract IStructuredSelection getSelection();
	
}
