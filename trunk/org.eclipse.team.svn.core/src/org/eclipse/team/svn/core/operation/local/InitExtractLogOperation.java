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

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * Performs initialization of extract operations log file.
 * 
 * @author Alexander Gurov
 */
public class InitExtractLogOperation extends AbstractActionOperation {
	public static final String COMPLETE_LOG_NAME = "/changes.log";
	public static final String DELETIONS_LOG_NAME = "/deletions.log";
	public static final String DELETIONS_TMP_LOG_NAME = "/deletions.log.tmp";
	
	protected String logPath;
	
	public InitExtractLogOperation(String logPath) {
		super("Operation.InitExtractLog");
		this.logPath = logPath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
		String date = formatter.format(new Date());
		InitExtractLogOperation.logToAll(this.logPath, "");
		InitExtractLogOperation.logToAll(this.logPath, date);
		InitExtractLogOperation.logToAll(this.logPath, "===============================================================================");
	}

	public static void logToAll(String logPath, String line) {
		InitExtractLogOperation.logTo(logPath + InitExtractLogOperation.COMPLETE_LOG_NAME, line);
	}

	public static void logToDeletions(String logPath, String line, boolean temporary) {
		InitExtractLogOperation.logTo(logPath + (temporary ? InitExtractLogOperation.DELETIONS_TMP_LOG_NAME : InitExtractLogOperation.DELETIONS_LOG_NAME), line);
	}

	private static void logTo(String logPath, String line) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(logPath, true);
			writer.write(line);
			writer.write(System.getProperty("line.separator"));
		}
		catch (IOException ex) {
			//ignore
		}
		finally {
			if (writer != null) {
				try {writer.close();} catch (Exception ex) {}
			}
		}
	}

}
