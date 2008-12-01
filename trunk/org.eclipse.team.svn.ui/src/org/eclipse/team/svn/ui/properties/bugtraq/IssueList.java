/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *    Jens Scheidtmann - butraq:logregex property display disgresses from specification (bug 243678)
 *    Alexei Goncharov (Polarion Software) - URL decoration with bugtraq properties does not work properly (bug 252563)
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties.bugtraq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueList extends LinkList {
	public IssueList() {
		super();
	}

	/**
	 * Parse message to determine position of links to Bugtracking System
	 * 
	 * <p>
	 * From the specification in the TortoiseSVN manual:
	 * 
	 * <blockquote>
	 * <p>
	 * <b>bugtraq:message</b>
	 * <p>
	 * This property activates the bug tracking system in Input field mode. If this property is set, then TortoiseSVN
	 * will prompt you to enter an issue number when you commit your changes. It's used to add a line at the end of the
	 * log message. It must contain %BUGID%, which is replaced with the issue number on commit. This ensures that your
	 * commit log contains a reference to the issue number which is always in a consistent format and can be parsed by
	 * your bug tracking tool to associate the issue number with a particular commit. As an example you might use Issue :
	 * %BUGID%, but this depends on your Tool.
	 * 
	 * <p>
	 * <b>bugtraq:number</b>
	 * <p>
	 * If set to true only numbers are allowed in the issue-number text field. An exception is the comma, so you can
	 * comma separate several numbers. Valid values are true/false. If not defined, true is assumed.
	 * 
	 * <p>
	 * <b>bugtraq:logregex</b> This property activates the bug tracking system in Regex mode. It contains either a
	 * single regular expressions, or two regular expressions separated by a newline.
	 * <p>
	 * If two expressions are set, then the first expression is used as a pre-filter to find expressions which contain
	 * bug IDs. The second expression then extracts the bare bug IDs from the result of the first regex.
	 * <p>
	 * [...]
	 * <p>
	 * If only one expression is set, then the bare bug IDs must be matched in the groups of the regex string. Example:
	 * [Ii]ssue(?:s)? #?(\d+)
	 * 
	 * <p>
	 * <b>If both the bugtraq:message and bugtraq:logregex properties are set, logregex takes precedence.</b>
	 * </blockquote>
	 * 
	 * @param message,
	 *            log message text, which may contain links to the bugtracking system.
	 * @param model,
	 *            derived from <code>bugtraq</code> properties
	 */
	public void parseMessage(String message, BugtraqModel model) {
		String prefix = ""; //$NON-NLS-1$
		String suffix = ""; //$NON-NLS-1$

		String issueRegex = ".*"; //$NON-NLS-1$
		String innerRegExp = null;

		if (model.getLogregex() != null) {
			issueRegex = model.getLogregex()[0];
			if (model.isDoubleLogRegexp()) {
				innerRegExp = model.getLogregex()[1];
			}
		}
		else if (model.getMessage() != null) {
			String template = model.getMessage();
			prefix = getTemplatePrefix(template);
			suffix = getTemplateSuffix(template);
			if (model.isNumber()) {
				issueRegex = "[0-9]+(?:,[0-9]+)*"; //$NON-NLS-1$
				innerRegExp = "[0-9]+"; //$NON-NLS-1$
			}
		}
		else {
			return;
		}

		prefix = Pattern.quote(prefix);
		suffix = Pattern.quote(suffix);
		String regex = prefix + issueRegex + suffix;
		Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(message);
		while (matcher.find()) {
			if (innerRegExp == null) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					this.links.add(new LinkPlacement(matcher.start(i), matcher.end(i), message));
				}
			}
			else {
				String group = matcher.group();
				Matcher entryMatcher = Pattern.compile(innerRegExp).matcher(group);
				while (entryMatcher.find()) {
					this.links.add(new LinkPlacement(matcher.start() + entryMatcher.start(), matcher.start() + entryMatcher.end(), message));
				}
			}
		}
	}

	protected String getTemplatePrefix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);

		String prefix = ""; //$NON-NLS-1$
		if (indexOfIssue > 0) {
			prefix = this.maskRegExpEntries(template.substring(0, indexOfIssue));
		}
		return prefix;
	}

	protected String getTemplateSuffix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);

		String suffix = ""; //$NON-NLS-1$
		if (indexOfIssue != -1) {
			int indexOfSuffix = indexOfIssue + BugtraqModel.BUG_ID.length();
			if (indexOfSuffix < template.length()) {
				suffix = this.maskRegExpEntries(template.substring(indexOfSuffix));
			}
		}
		return suffix;
	}

	protected String maskRegExpEntries(String original) {
		String retVal = ""; //$NON-NLS-1$
		for (int i = 0; i < original.length(); i++) {
			if ("*\\/:.,?^&+()|".indexOf(original.charAt(i)) != -1) { //$NON-NLS-1$
				retVal = retVal + "\\" + original.charAt(i); //$NON-NLS-1$
			}
			else if (original.charAt(i) == '\n') {
				retVal = retVal + "(?:\r|\n|\r\n)"; //$NON-NLS-1$
			}
			else if (original.charAt(i) == '\r') {
				if ((i + 1) < original.length() && original.charAt(i + 1) == '\n') {
					i++;
				}
				retVal = retVal + "(?:\r|\n|\r\n)"; //$NON-NLS-1$
			}
			else {
				retVal = retVal + original.charAt(i);
			}
		}
		return retVal;
	}

}
