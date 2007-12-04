/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties.bugtraq;

import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.ui.properties.bugtraq.IssueList.Issue;

/**
 * The model which represents bugtraq properties 
 * 
 * @author Sergiy Logvin
 */
public class BugtraqModel {
	
	public static final String BUG_ID = "%BUGID%";
	
	protected String url;
	protected String label;
	protected String message;
	protected String[] logregex;
	protected boolean warnIfNoIssue;
	protected boolean append = true;
	protected boolean number = true;
	
	public boolean isAppend() {
		return this.append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public String getLabel() {
		return this.label == null ? "Bug-ID:" : this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message == null || message.trim().length() == 0 ? null : message;
	}

	public boolean isNumber() {
		return this.number;
	}

	public void setNumber(boolean number) {
		this.number = number;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isWarnIfNoIssue() {
		return this.warnIfNoIssue;
	}

	public void setWarnIfNoIssue(boolean warnifnoissue) {
		this.warnIfNoIssue = warnifnoissue;
	}

	public String[] getLogregex() {
		return this.logregex;
	}

	public void setLogregex(String logregex) {
		this.logregex = logregex.split("\r\n|\r|\n");
	}
	
	public boolean isDoubleLogRegexp() {
		if (logregex != null) {
			return (this.logregex.length == 2);
		}
		return false;
	}
	
	public String getResultingURL(Issue issue) {
		if (this.url != null && issue != null && this.url.indexOf(BugtraqModel.BUG_ID) != -1) {
			return PatternProvider.replaceAll(this.url, BugtraqModel.BUG_ID, issue.getURL());
		}
		return null;
	}

}
