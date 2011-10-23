/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.core.utility;

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;

/**
 * LogEntry call-back implementation which includes merge info
 * 
 * @author Igor Burilo
 */
public class SVNLogEntryCallbackWithMergeInfo implements ISVNLogEntryCallback {
	
	protected Stack<SVNLogEntry> mergeTreeBuilder = new Stack<SVNLogEntry>();
	
	protected ArrayList<SVNLogEntry> entries = new ArrayList<SVNLogEntry>();
	
	public void next(SVNLogEntry log) {
		if (log.revision == SVNRevision.INVALID_REVISION_NUMBER) {
			if (!this.mergeTreeBuilder.isEmpty()) {
				log = this.mergeTreeBuilder.pop();						
				if (this.mergeTreeBuilder.isEmpty()) {					
					this.addEntry(log);
				}
			}															
			return;
		}
		
		if (!this.mergeTreeBuilder.isEmpty()) {
			this.addChildEntry(this.mergeTreeBuilder.peek(), log);
		}
		else if (!log.hasChildren()) {
			this.addEntry(log);
		}
		if (log.hasChildren()) {
			this.mergeTreeBuilder.push(log);
		}			
	}
	
	/*
	 * Can be overridden in sub classes
	 */
	protected void addEntry(SVNLogEntry entry) {
		this.entries.add(entry);
	}
	
	/*
	 * Can be overridden in sub classes
	 */
	protected void addChildEntry(SVNLogEntry parent, SVNLogEntry child) {
		parent.add(child);
	}
	
	public SVNLogEntry[] getEntries() {
		return this.entries.toArray(new SVNLogEntry[entries.size()]);
	}
}
