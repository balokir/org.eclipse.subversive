/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.client.ClientWrapperCancelException;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.AdvancedDialog;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation.OperationErrorInfo;
import org.eclipse.team.svn.ui.panel.reporting.ErrorCancelPanel;
import org.eclipse.team.svn.ui.repository.model.RepositoryError;

/**
 * Show repository error action implementation
 * 
 * @author Vladimir Bykov
 */
public class ShowBrowsingErrorAction extends AbstractRepositoryTeamAction {
	
	public ShowBrowsingErrorAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		Object selectedElement = this.getSelection().getFirstElement();
		OperationErrorInfo errorInfo =  UILoggedOperation.formatMessage(((RepositoryError)selectedElement).getErrorStatus(), true);
		ErrorCancelPanel panel;
        if (errorInfo.exception instanceof ClientWrapperCancelException || 
        	errorInfo.exception instanceof ActivityCancelledException ||
        	errorInfo.exception instanceof OperationCanceledException) {
        	panel = new ErrorCancelPanel(SVNTeamUIPlugin.instance().getResource("ShowBrowsingErrorAction.Dialog.Title"), errorInfo.simpleMessage, errorInfo.advancedMessage, false, null);
        } 
        else {
        	panel = new ErrorCancelPanel(SVNTeamUIPlugin.instance().getResource("ShowBrowsingErrorAction.Dialog.Title"), errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, false, null);
        }
		AdvancedDialog dialog = new AdvancedDialog(this.getShell(), panel);
		dialog.open();
	}

	public boolean isEnabled() {
		return true;
	}
	
}
