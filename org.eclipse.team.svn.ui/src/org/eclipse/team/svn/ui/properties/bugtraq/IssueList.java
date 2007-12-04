/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties.bugtraq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Issue list
 * 
 * @author Sergiy Logvin
 */
public class IssueList {
	protected ArrayList issues = new ArrayList();

	public IssueList() {
		super();
	}
	
	public List getIssues() {
		return this.issues;
	}

	public String getTemplatePrefix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);

		String prefix = "";
		if (indexOfIssue > 0) {
			prefix = this.maskRegExpEntries(template.substring(0, indexOfIssue));
		}
		return prefix;
	}

	public String getTemplateSuffix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);
		
		String suffix = "";
		if (indexOfIssue != -1) {
			int indexOfSuffix = indexOfIssue + BugtraqModel.BUG_ID.length();
			if (indexOfSuffix < template.length()) {
				suffix = this.maskRegExpEntries(template.substring(indexOfSuffix));
			}
		}
		return suffix;
	}
	
	public void parseMessage(String message, BugtraqModel model) {
		final String template = model.getMessage();
		this.issues.clear();
		if (template == null) {
			return;
		}

		final String prefix = getTemplatePrefix(template);
		final String sufix = getTemplateSuffix(template);
		
		int bugIdIndex = template.indexOf(BugtraqModel.BUG_ID);
		
		if (bugIdIndex != -1) {
			String issueRegex = model.isNumber() ? "[0-9]+(?:,[0-9]+)*" : model.getLogregex()[0];
			if (issueRegex == null) {
				issueRegex = "*";
			}
			String regex;
			if (model.isDoubleLogRegexp()) {
				regex = issueRegex;
			}
			else {
				regex = prefix + issueRegex + sufix;
			}
			
			Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(message);
			if (matcher.find()) {
				String group = matcher.group();
				String innerRegExp = model.isNumber() ? "[0-9]+" : (model.isDoubleLogRegexp() ? model.getLogregex()[1] : issueRegex);
				Matcher entryMatcher = Pattern.compile(innerRegExp).matcher(group);
				int start = 0;
				while (entryMatcher.find(start)) {
					this.issues.add(new Issue(matcher.start() + entryMatcher.start(), matcher.start() + entryMatcher.end(), message));
					start = entryMatcher.end();
				}
			} 
		}		
	}

	public boolean isIssueAt(int offset) {
		for (Iterator iter = this.issues.iterator(); iter.hasNext();) {
			Issue issue = (Issue) iter.next();
			if (issue.existAtOffset(offset)) {
				return true;
			}
		}
		return false;
	}
	
	public Issue getIssueAt(int offset) {
		for (Iterator iter = this.issues.iterator(); iter.hasNext();) {
			Issue issue = (Issue)iter.next();
			if (issue.existAtOffset(offset)) {
				return issue;
			}
		}
		return null;
	}
	
	public class Issue {
		protected int start;
		protected int end;
		protected String issue;
		
		public Issue(int start, int end, String message) {
			this.issue =  message.substring(start, end);
			this.start = start;
			this.end = end;
		}	
		protected boolean existAtOffset(int offset) {
			return (this.start <= offset) && (offset < this.end);
		}
		public int getStart() {
			return this.start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return this.end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		public String getURL() {
			return this.issue;
		}
	}

	protected String maskRegExpEntries(String original) {
		String retVal = "";
		for (int i = 0; i < original.length(); i++) {
			if ("*\\/:.,?^&+()|".indexOf(original.charAt(i)) != -1) {
				retVal = retVal + "\\" + original.charAt(i);
			}
			else if (original.charAt(i) == '\n') {
				retVal = retVal + "(?:\r|\n|\r\n)";
			}
			else if (original.charAt(i) == '\r') {
				if ((i + 1) < original.length() && original.charAt(i + 1) == '\n')
				{
					i++;
				}
				retVal = retVal + "(?:\r|\n|\r\n)";
			}
			else {
				retVal = retVal + original.charAt(i);
			}
		}
		return retVal;
	}

}
