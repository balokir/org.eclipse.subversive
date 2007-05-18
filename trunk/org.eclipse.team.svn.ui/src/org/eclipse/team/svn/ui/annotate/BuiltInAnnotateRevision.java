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

package org.eclipse.team.svn.ui.annotate;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Built-in annotate revision model 
 * 
 * @author Alexander Gurov
 */
public class BuiltInAnnotateRevision extends Revision {
	public static final int END_LINE = -1;
	protected String id;
	protected String author;
	private int startLine;
	private int stopLine;
	private String info;
	protected RGB color;
	protected LogMessage msg;
	
	public BuiltInAnnotateRevision(String id, String author, RGB color) {
		this.id = id;
		this.color = color;
		this.author = author;
		this.startLine = this.stopLine = BuiltInAnnotateRevision.END_LINE;
	}
	
	public void setLogMessage(LogMessage msg) {
		this.msg = msg;
	}
	
	public String getId() {
		return this.id;
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

	public RGB getColor() {
		return this.color;
	}

	public Object getHoverInfo() {
		if (this.info == null) {
			this.info = "<b>" + SVNTeamUIPlugin.instance().getResource("BuiltInAnnotateRevision.Revision") + " </b>" + this.getId() + "<br>";
			if (this.author != null) {
				this.info += "<b>" + SVNTeamUIPlugin.instance().getResource("BuiltInAnnotateRevision.Author") + " </b>" + this.author + "<br>";
			}
			if (this.getDate() != null) {
				DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
				this.info += "<b>" + SVNTeamUIPlugin.instance().getResource("BuiltInAnnotateRevision.Date") + " </b>" + dateTimeFormat.format(this.getDate()) + "<br>";
			}
			String message = this.msg == null ? null : this.msg.message;
			if (message != null && message.length() > 0) {
				this.info += "<b>" + SVNTeamUIPlugin.instance().getResource("BuiltInAnnotateRevision.Message") + "</b><br>" + this.msg.message;
			}
		}
		return this.info;
	}

	public Date getDate() {
		Date date = this.msg == null || this.msg.date == 0 ? null : new Date(this.msg.date);
		return date == null ? new Date(0) : date;
	}

}