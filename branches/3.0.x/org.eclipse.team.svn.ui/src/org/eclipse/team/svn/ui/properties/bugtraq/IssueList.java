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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.team.svn.core.operation.LoggedOperation;

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
		String modelMessage = null;

		if (model.getLogregex() != null) {
			issueRegex = model.getLogregex()[0];
			if (model.isDoubleLogRegexp()) {
				innerRegExp = model.getLogregex()[1];
			}
		}
		else if (model.getMessage() != null) {
			modelMessage = model.getMessage();
			prefix = this.getTemplatePrefix(modelMessage);
			suffix = this.getTemplateSuffix(modelMessage);
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
		
		//surround with brackets in order latter to capture bug's regexp
		String regex = prefix + "(" + issueRegex + ")" + suffix;	
		
		Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(message);
		while (matcher.find()) {
			if (innerRegExp == null) {
				//start from 2 as we also encoded issueRegex in brackets 
				for (int i = 2; i <= matcher.groupCount(); i++) {
					this.links.add(new LinkPlacement(matcher.start(i), matcher.end(i), message));
				}
			}
			else {	
				//search between prefix and sufix												
				if (matcher.groupCount() > 0) {
					//group 1 contains regexp with bug id
					String group = matcher.group(1);
					
					Matcher entryMatcher = Pattern.compile(innerRegExp).matcher(group);
					while (entryMatcher.find()) {
						String originalPrefix = modelMessage == null ? "" : this.getTemplatePrefix(modelMessage);
						int prefixLength = matcher.start() + originalPrefix.length();
						// FIXME generate debug report, since there is an error, check bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=471473
						if (prefixLength + entryMatcher.end() > message.length()) {
							LoggedOperation.reportError(
								prefix + "~~" + suffix + "~~" + issueRegex + "~~" + (innerRegExp == null ? "null" : innerRegExp) + "~~" + originalPrefix, 
								new StringIndexOutOfBoundsException(message));
							continue;
						}
						this.links.add(new LinkPlacement(prefixLength + entryMatcher.start(), prefixLength + entryMatcher.end(), message));
					}	
				}															
			}
		}
	}

	protected String getTemplatePrefix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);

		String prefix = ""; //$NON-NLS-1$
		if (indexOfIssue > 0) {
			//prefix = this.maskRegExpEntries(template.substring(0, indexOfIssue));
			prefix = template.substring(0, indexOfIssue);
		}
		return prefix;
	}

	protected String getTemplateSuffix(String template) {
		int indexOfIssue = template.indexOf(BugtraqModel.BUG_ID);

		String suffix = ""; //$NON-NLS-1$
		if (indexOfIssue != -1) {
			int indexOfSuffix = indexOfIssue + BugtraqModel.BUG_ID.length();
			if (indexOfSuffix < template.length()) {
				//suffix = this.maskRegExpEntries(template.substring(indexOfSuffix));
				suffix = template.substring(indexOfSuffix);
			}
		}
		return suffix;
	}
	
//	protected String maskRegExpEntries(String original) {
//		String retVal = ""; //$NON-NLS-1$
//		for (int i = 0; i < original.length(); i++) {
//			if ("*\\/:.,?^&+()|".indexOf(original.charAt(i)) != -1) { //$NON-NLS-1$
//				retVal = retVal + "\\" + original.charAt(i); //$NON-NLS-1$
//			}
//			else 
//			if (original.charAt(i) == '\n') {
//				retVal = retVal + "(?:\r|\n|\r\n)"; //$NON-NLS-1$
//			}
//			else if (original.charAt(i) == '\r') {
//				if ((i + 1) < original.length() && original.charAt(i + 1) == '\n') {
//					i++;
//				}
//				retVal = retVal + "(?:\r|\n|\r\n)"; //$NON-NLS-1$
//			}
//			else {
//				retVal = retVal + original.charAt(i);
//			}
//		}
//		return retVal;
//	} 
		
	public static void main(String[] args) 
	{
		/*
		 * Tests. In order to activate asserts add -enableassertions in VM arguments
		 */		
		BugtraqModel model = new BugtraqModel();
		model.setAppend(true);
		model.setLabel(null);
		//model.setLogregex("");
		//model.setMessage(messagePattern);
		model.setNumber(true);
		model.setUrl("http://site.com/bugs=%BUGID%");
		model.setWarnIfNoIssue(false);		
		
		String messagePattern = "";
		String message = "";
		IssueList issue = null;
		List<LinkPlacement> links = null;
			
		//My bug %BUGID%.
		 //Note to dot here				
		messagePattern = "My bug %BUGID%.";
		message = "\nMy bug 48.\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
		
		//My bug: %BUGID%
		 //Note to colon here				
		messagePattern = "My bug: %BUGID%";
		message = "\nMy bug: 48\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
		
		//My %BUGID%,and
		messagePattern = "My %BUGID%,and";
		message = "\nMy 48,and\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
		
		//My (%BUGID%)
		messagePattern = "My (%BUGID%)";
		message = "\nMy (48)\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
		
		//My f%BUGID%f
		messagePattern = "My f%BUGID%f";
		message = "\nMy f48f\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
		
		//My111 %BUGID%
		//note to numbers with prefix
		messagePattern = "My111 %BUGID%";
		message = "\nMy111 48\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));
			
		//you can comma separate several numbers
		messagePattern = "My111 %BUGID%";
		message = "\nMy111 48,49\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 2);
		assert ("48".equals(links.get(0).getURL()) || "49".equals(links.get(0).getURL()));
		assert ("48".equals(links.get(1).getURL()) || "49".equals(links.get(1).getURL()));						
		
		//Prefix contains \n and \r characters
		messagePattern = "My\n and \r prefix %BUGID%";
		message = "\nMy\n and \r prefix 48\n";
		model.setMessage(messagePattern);		
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));		
		 		
		//---- bugtraq:logregex
		
		String logregex = null;
		
		//[Ii]ssue(?:s)? #?(\d+)
		logregex = "[Ii]ssue(?:s)? #?(\\d+)";
		model.setLogregex(logregex);
		model.setMessage(null);
		message = "Issue #48";
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 1);
		assert ("48".equals(links.get(0).getURL()));		
		
		//set two expressions separated by new line
		//see TortoiseSVN's help for more details about this kind of message
		logregex = "[Ii]ssues?:?(\\s*(,|and)?\\s*#\\d+)+" +
			"\n" + 
			"\\d+";		
		model.setLogregex(logregex);
		model.setMessage(null);
		message = "This change resolves issues #23, #24 and #25";
		issue = new IssueList();
		issue.parseMessage(message, model);
		links = issue.getLinks();
		assert (links.size() == 3);
		String res = links.get(0).getURL();
		assert ("23".equals(res) || "24".equals(res) || "25".equals(res));
		res = links.get(1).getURL();
		assert ("23".equals(res) || "24".equals(res) || "25".equals(res));
		res = links.get(2).getURL();
		assert ("23".equals(res) || "24".equals(res) || "25".equals(res));		
	}
}
