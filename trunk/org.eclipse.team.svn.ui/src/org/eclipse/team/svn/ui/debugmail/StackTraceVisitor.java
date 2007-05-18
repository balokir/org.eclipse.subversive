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

import org.eclipse.core.runtime.IStatus;

/**
 * Stack trace-based status visitor
 * 
 * @author Alexander Gurov
 */
public class StackTraceVisitor implements Reporter.IStatusVisitor {
	public boolean visit(IStatus status) {
		String output = Reporter.getOutput(status);
		return 
			output.indexOf(".polarion.") != -1 && 
			output.indexOf("Could not instantiate provider") == -1 &&
			output.indexOf("Java Model Exception: Java Model Status [") == -1 &&
			output.indexOf("org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation.openEditor") == -1;
	}
}
