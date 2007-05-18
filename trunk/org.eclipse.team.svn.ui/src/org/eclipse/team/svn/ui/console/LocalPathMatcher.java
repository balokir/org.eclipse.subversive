/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/
package org.eclipse.team.svn.ui.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Listen to SVN console. Finds lines that contains local resources paths.
 * Adds hyperlinks to local resources. 
 *
 * @author Alexey Mikoyan
 *
 */
public class LocalPathMatcher implements IPatternMatchListenerDelegate {

	protected Pattern[] patterns = new Pattern[0];
	
	protected TextConsole console;
	
	public void createPatterns() {
		ArrayList patternList = new ArrayList();
		
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Added"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Modified"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Deleted"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Missing"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Replaced"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Merged"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Conflicted"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Status.Obstructed"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Added"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Deleted"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Modified"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Replaced"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Reverted"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Restored"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Locked"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Action.Unlocked"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.inapplicable"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.unknown"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.unchanged"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.missing"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.obstructed"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.changed"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.merged"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.conflicted"));
		this.addPattern(patternList, SVNTeamPlugin.instance().getResource("Console.Update.Status.conflicted_unresolved"));
		
		this.patterns = (Pattern[])patternList.toArray(new Pattern[patternList.size()]);
	}
	
	protected void addPattern(List patterns, String base) {
		if (base.length() == 0) {
			return;
		}
		String template = ".*\\s+";
		template += "\\Q" + base + "\\E";
		template += "\\s+([^\r\n]+)";
		try {
			patterns.add(Pattern.compile(template));
		}
		catch (PatternSyntaxException pse) {
		}
	}
	
	public void connect(TextConsole console) {
		this.console = console;
		this.createPatterns();
	}

	public void disconnect() {
		this.console = null;
	}

	public void matchFound(PatternMatchEvent event) {
		if (this.console == null) {
			return;
		}
		UIMonitorUtility.doTaskBusyDefault(new AddConsoleHyperlinkOperation(event));
	}
	
	protected class AddConsoleHyperlinkOperation extends AbstractNonLockingOperation {
		protected PatternMatchEvent event;
		
		public AddConsoleHyperlinkOperation(PatternMatchEvent event) {
			super("Operation.AddConsoleHyperlink");
			this.event = event;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			int offset = this.event.getOffset();
			int length = this.event.getLength();
			String path = LocalPathMatcher.this.console.getDocument().get(offset, length);
			if (path == null) {
				return;
			}
		    int start = 0;
			for (int i = 0; i < LocalPathMatcher.this.patterns.length; i++) {
	    	    Pattern pattern = LocalPathMatcher.this.patterns[i];
	    	    Matcher matcher = pattern.matcher(path);
	    	    while(matcher.find(start)) {
					length = matcher.end(1) - matcher.start(1);
					String link = path.substring(matcher.start(1), matcher.end(1));
					if (link != null) {
						LocalPathMatcher.this.console.addHyperlink(new LocalFileHyperlink(link), offset + matcher.start(1), length);							
						return;
					}
					start = matcher.end();
	    	    }
			}
		}
		
	}

}
