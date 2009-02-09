/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *    Igor Burilo - Bug 245509: Improve extract log
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.io.FileWriter;
import java.io.IOException;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.ULocale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;

/**
 * Performs initialization of extract operations log file.
 * 
 * @author Alexander Gurov
 */
public class InitExtractLogOperation extends AbstractActionOperation {
	public static final String COMPLETE_LOG_NAME = "/changes.log"; //$NON-NLS-1$
	
	protected HashMap<String, List<String>> extractParticipants;
	protected String logPath;
	
	public InitExtractLogOperation(String logPath) {
		super("Operation_InitExtractLog"); //$NON-NLS-1$
		this.logPath = logPath;
		this.extractParticipants = new HashMap<String, List<String>>();
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, ULocale.getDefault());
		String date = formatter.format(new Date());
		this.logImpl(""); //$NON-NLS-1$
		this.logImpl(date);
		this.logImpl("==============================================================================="); //$NON-NLS-1$
	}

	public void log(String participant, String status) {
		String toPut = status.equals(IStateFilter.ST_NEW) ? IStateFilter.ST_ADDED : (status.equals(IStateFilter.ST_REPLACED) ? IStateFilter.ST_MODIFIED : status);
		if (this.extractParticipants.get(toPut) == null) {
			this.extractParticipants.put(toPut, new ArrayList<String>());
		}
		this.extractParticipants.get(toPut).add(participant);
	}
	
	public void flushLog() {
		HashMap<String, List<String>> sortedParticipants = new HashMap<String, List<String>>();
		for (String status : this.extractParticipants.keySet()) {
			String [] participants = this.extractParticipants.get(status).toArray(new String [0]);
			Arrays.sort(participants);
			ArrayList<String> participantsToLog = new ArrayList<String>();
			for (int i = 0; i < participants.length; i++) {
				if (status.equals(IStateFilter.ST_DELETED)) {
					boolean parentIsAlreadyLogged = false;
					for (String logged : participantsToLog) {
						if (participants[i].startsWith(logged + "\\")) { //$NON-NLS-1$
							parentIsAlreadyLogged = true;
							break;
						}
					}
					if (!parentIsAlreadyLogged) {
						participantsToLog.add(participants[i]);
					}
				}
				else if (i + 1 >= participants.length || !participants[i + 1].startsWith(participants[i] + "\\")) { //$NON-NLS-1$
					participantsToLog.add(participants[i]);
				}
			}
			sortedParticipants.put(status, participantsToLog);
		}
		
		//Sorting statuses
		List<String> statusesList = Arrays.asList(sortedParticipants.keySet().toArray(new String [this.extractParticipants.keySet().size()]));
		Collections.sort(statusesList, new Comparator<String>() {

			public int compare(String o1, String o2) {
				if (o1.equals(IStateFilter.ST_MODIFIED))
				{
					return -1;
				}
				if (o2.equals(IStateFilter.ST_MODIFIED))
				{
					return 1;
				}
				if (o1.equals(IStateFilter.ST_ADDED))
				{
					return -1;
				}
				if (o2.equals(IStateFilter.ST_ADDED))
				{
					return 1;
				}
				if (o1.equals(IStateFilter.ST_NEW))
				{
					return -1;
				}
				if (o2.equals(IStateFilter.ST_NEW))
				{
					return 1;
				}
				return 0;
			}
			
		});
		
		for (String status : statusesList) {
			for (String participant : sortedParticipants.get(status)) {
				this.logImpl(SVNMessages.getString("Console_Status_" + status) + " " + participant); //$NON-NLS-1$ //$NON-NLS-2$
			}
			this.logImpl(""); //$NON-NLS-1$
		}
		this.extractParticipants.clear();
	}

	private void logImpl(String line) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.logPath + InitExtractLogOperation.COMPLETE_LOG_NAME, true);
			writer.write(line);
			writer.write(System.getProperty("line.separator")); //$NON-NLS-1$
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
