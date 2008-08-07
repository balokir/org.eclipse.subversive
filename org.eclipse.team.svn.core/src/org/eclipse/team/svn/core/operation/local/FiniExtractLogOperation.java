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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * Performs finalization of extract operations log file.
 * 
 * @author Alexander Gurov
 */
public class FiniExtractLogOperation extends AbstractActionOperation {
	protected String logPath;
	
	public FiniExtractLogOperation(String logPath) {
		super("Operation.FiniExtractLog");
		this.logPath = logPath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
		String date = formatter.format(new Date());
		File copyFrom = new File(this.logPath + InitExtractLogOperation.DELETIONS_TMP_LOG_NAME);
		if (copyFrom.length() > 0) {
			InitExtractLogOperation.logToDeletions(this.logPath, "", false);
			InitExtractLogOperation.logToDeletions(this.logPath, date, false);
			InitExtractLogOperation.logToDeletions(this.logPath, "===============================================================================", false);
			
			FileInputStream input = new FileInputStream(copyFrom);
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(this.logPath + InitExtractLogOperation.DELETIONS_LOG_NAME, true);
				byte []buf = new byte[2048];
				int len;
				while ((len = input.read(buf)) > 0) {
					output.write(buf, 0, len);
				}
			}
			finally {
				if (output != null) {
					try {output.close();} catch (Exception ex) {}
				}
				try {input.close();} catch (Exception ex) {}
			}
		}
		copyFrom.delete();
	}
	
}
