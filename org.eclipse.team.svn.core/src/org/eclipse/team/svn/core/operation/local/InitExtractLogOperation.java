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

package org.eclipse.team.svn.core.operation.local;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.remote.ExtractToOperationRemote;

/**
 * Performs initialization of extract operations log file.
 * 
 * @author Alexander Gurov
 */
public class InitExtractLogOperation extends AbstractActionOperation {
	protected String logPath;
	
	public InitExtractLogOperation(String logPath) {
		super("Operation.InitExtractLog");
		this.logPath = logPath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
		String date = formatter.format(new Date());
		ExtractToOperationRemote.logToDeletions(this.logPath, "", true);
		ExtractToOperationRemote.logToDeletions(this.logPath, date, true);
		ExtractToOperationRemote.logToDeletions(this.logPath, "===============================================================================", true);
		ExtractToOperationRemote.logToAll(this.logPath, "", true);
		ExtractToOperationRemote.logToAll(this.logPath, date, true);
		ExtractToOperationRemote.logToAll(this.logPath, "===============================================================================", true);
	}

}
