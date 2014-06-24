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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IPostCommitErrorsProvider;
import org.eclipse.team.svn.core.operation.SVNPostCommitError;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.ShowPostCommitErrorsDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * This operation provide ability to notify user about unresolved conflict situation in time of update/merge
 * 
 * @author Alexander Gurov
 */
public class ShowPostCommitErrorsOperation extends AbstractActionOperation {
    protected IPostCommitErrorsProvider provider;

    public ShowPostCommitErrorsOperation(IPostCommitErrorsProvider provider) {
        super("Operation_ShowPostCommitErrors", SVNUIMessages.class); //$NON-NLS-1$
        this.provider = provider;
    }
    
    public int getOperationWeight() {
		return 0;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
    	SVNPostCommitError []errors = this.provider.getPostCommitErrors();
        if (errors != null) {
        	String tCompleteMessage = null;
        	for (SVNPostCommitError error : errors) {
        		tCompleteMessage = tCompleteMessage == null ? error.message : (tCompleteMessage + "\n\n" + error.message); //$NON-NLS-1$
        	}
        	if (tCompleteMessage != null) {
            	final String completeMessage = tCompleteMessage;
                UIMonitorUtility.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        new ShowPostCommitErrorsDialog(UIMonitorUtility.getShell(), completeMessage).open();
                    }
                });
        	}
        }
    }

}
