/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *******************************************************************************/

package org.eclipse.team.svn.ui.annotate;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.DateFormatter;

/**
 * Built-in annotate revision model 
 * 
 * @author Alexander Gurov
 */
public class BuiltInAnnotateRevision extends Revision {
	public static final int END_LINE = -1;
	
	protected class MergeInfo {
		public final String line;
		public final String reference;
		public final long date;
		public final String author;
		
		public MergeInfo(String line, String reference, long date, String author) {
			this.line = line;
			this.reference = reference;
			this.date = date;
			this.author = author;
		}
	}
	
	protected String id;
	protected String author;
	private int startLine;
	private int stopLine;
	protected RGB color;
	protected SVNLogEntry msg;
	
	protected ArrayList<MergeInfo> mergeInfoList;
	
	public BuiltInAnnotateRevision(String id, String author, RGB color) {
		this.id = id;
		this.color = color;
		this.author = author;
		this.startLine = this.stopLine = BuiltInAnnotateRevision.END_LINE;
	}
	
	public void setLogMessage(SVNLogEntry msg) {
		this.msg = msg;
	}
	
	public long getRevision() {
		return Long.parseLong(this.id);
	}
	
	public String getId() {
		return this.mergeInfoList != null ? this.id + "+" : this.id;
	}
	
	public void addLine(int line) {
		if (this.startLine == BuiltInAnnotateRevision.END_LINE) {
			this.startLine = this.stopLine = line;
		}
		else if (line == BuiltInAnnotateRevision.END_LINE) {
			this.addRange(new LineRange(this.startLine - 1, this.stopLine - this.startLine + 1));
		}
		else if (line - this.stopLine == 1) {
			this.stopLine = line;
		}
		else {
			this.addRange(new LineRange(this.startLine - 1, this.stopLine - this.startLine + 1));
			this.startLine = this.stopLine = line;
		}
	}
	
	public void addMergeInfo(int line, long mergedRevision, long mergedDate, String mergedAuthor, String mergedPath) {
		// Merged lines:
		// XX with line from path@rev made by ZZZ at YYY
		if (this.mergeInfoList == null) {
			this.mergeInfoList = new ArrayList<MergeInfo>();
		}
		String reference = mergedPath != null ? mergedPath + "@" + mergedRevision : String.valueOf(mergedRevision);
		this.mergeInfoList.add(new MergeInfo(String.valueOf(line), reference, mergedDate, mergedAuthor));
	}

	public RGB getColor() {
		return this.color;
	}

	public Object getHoverInfo() {
		String info = "<b>" + SVNUIMessages.BuiltInAnnotateRevision_Revision + " </b>" + this.id + "<br>";
		if (this.author != null) {
			info += "<b>" + SVNUIMessages.BuiltInAnnotateRevision_Author + " </b>" + this.author;
		}
		if (this.getDateImpl() != null) {
			info += "<br><b>" + SVNUIMessages.BuiltInAnnotateRevision_Date + " </b>" + DateFormatter.formatDate(this.getDate());
		}
		String message = this.msg == null ? null : this.msg.message;
		if (message != null && message.length() > 0) {
			info += "<br><b>" + SVNUIMessages.BuiltInAnnotateRevision_Message + "</b><br>" + this.msg.message;
		}
		if (this.mergeInfoList != null) {
			info += "<br>";
			for (MergeInfo mergeInfo : this.mergeInfoList) {
				info += "<br>" + SVNUIMessages.format(SVNUIMessages.BuiltInAnnotateRevision_MergedWith, new String[] {mergeInfo.line, mergeInfo.reference, mergeInfo.author, DateFormatter.formatDate(new Date(mergeInfo.date))});
			}
		}
		return info;
	}

	public Date getDate() {
		Date date = this.getDateImpl();
		return date == null ? new Date(0) : date;
	}

	protected Date getDateImpl() {
		return this.msg == null || this.msg.date == 0 ? null : new Date(this.msg.date);
	}

	public String getAuthor() {
		String author = this.author == null ? SVNMessages.SVNInfo_NoAuthor : this.author;
		return author + " "; // Eclipse IDE does not separate line numbers and author names 
	}
	
}
