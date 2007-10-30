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

package org.eclipse.team.svn.ui.debugmail;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;

/**
 * Problem listener implementation
 * 
 * @author Alexander Gurov
 */
public class ProblemListener implements ILogListener {
	protected static PluginIDVisitor idVisitor = new PluginIDVisitor();
	protected static StackTraceVisitor stackVisitor = new StackTraceVisitor();
	
	public ProblemListener() {
		super();
	}

	public void logging(IStatus status, String plugin) {
		// our problems should be handled in the UILoggedOperation in order to suppress two sequential dialogs 
		Reporter.IStatusVisitor visitor = Reporter.checkStatus(status, ProblemListener.idVisitor) ? (Reporter.IStatusVisitor)null : ProblemListener.stackVisitor;
		if (visitor != null && Reporter.checkStatus(status, visitor)) {
			this.sendReport(status, plugin);
		}
	}
	
	protected void sendReport(IStatus status, String plugin) {
		UILoggedOperation.showError(plugin, "", status, true);
	}
	
}
