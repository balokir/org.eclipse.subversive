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
			prefix = template.substring(0, indexOfIssue);
		}
		return prefix;
	}

	public String getTemplateSuffix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);
		
		String suffix = "";
		if (indexOfIssue != -1) {
			final int indexOfSuffix = indexOfIssue + BugtraqModel.BUG_ID.length();
			if (indexOfSuffix < template.length()) {
				suffix = template.substring(indexOfSuffix);
			}
		}
		return suffix;
	}

	public void parseMessage(final String message, BugtraqModel model) {
		final String template = model.getMessage();
		this.issues.clear();
		if (template == null) {
			return;
		}

		final String prefix = getTemplatePrefix(template);
		final String sufix = getTemplateSuffix(template);
		
		int bugIdIndex = template.indexOf(BugtraqModel.BUG_ID);
		
		if (bugIdIndex != -1) {
			String issueRegex = model.isNumber() ? "[0-9]+(,?[0-9]+)*" : model.getLogregex();
			if (issueRegex == null) {
				issueRegex = "*";
			}
			
			String regex = prefix + issueRegex + sufix;
			
			Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(message);
			if (matcher.find()) {
				final String issuesPattern = matcher.group(0);
				int startAllIssues = matcher.start(0);
				boolean first = true;
				for (int i = prefix.length(); i < issuesPattern.length(); i++) {				
					if (issuesPattern.substring(i, i + 1).equals(",") || first) {
						if (issuesPattern.substring(i, i + 1).equals(",")){
							//skip first ","
							first = true;
							continue;
						}
						first = false;
						int start = i;
						i++;
						int end = i;
						boolean found = false;
						for (; (i + 1) < issuesPattern.length() && !issuesPattern.substring(i + 1, i + 2).equals(","); i++) {
							end++;
							found = true;
						}
						if (found == true) {
							end++; //to point on exclusive index of character after the end
							this.issues.add(new Issue(startAllIssues + start, startAllIssues + end, message));
						}
					}
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

}
