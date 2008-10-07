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
import org.eclipse.team.svn.core.operation.local.IUnresolvedConflictDetector;
import org.eclipse.team.svn.ui.dialog.NotifyUnresolvedConflictDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * This operation provide ability to notify user about unresolved conflict situation in time of update/merge
 * 
 * @author Alexander Gurov
 */
public class NotifyUnresolvedConflictOperation extends AbstractActionOperation {
    protected IUnresolvedConflictDetector sign;

    public NotifyUnresolvedConflictOperation(IUnresolvedConflictDetector sign) {
        super("Operation.NotifyConflicts");
        this.sign = sign;
    }
    
    public int getOperationWeight() {
		return 0;
	}

    protected void runImpl(IProgressMonitor monitor) throws Exception {
        if (this.sign.hasUnresolvedConflicts()) {
            UIMonitorUtility.getDisplay().syncExec(new Runnable() {
                public void run() {
                    new NotifyUnresolvedConflictDialog(UIMonitorUtility.getShell(), NotifyUnresolvedConflictOperation.this.sign.getMessage()).open();
                }
            });
        }
    }

}
