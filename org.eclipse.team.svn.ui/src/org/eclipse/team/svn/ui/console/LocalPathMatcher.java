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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Listen to SVN console. Finds lines that contains local resources paths.
 * Adds hyperlinks to local resources. 
 *
 * @author Alexey Mikoyan
 *
 */

public class LocalPathMatcher implements IPatternMatchListenerDelegate, IPropertyChangeListener {

	protected Pattern pattern;
	
	protected TextConsole console;
	protected boolean enabled;
	
	public void createPattern() {
		String regExp = "(?:[A-Z]\\:)?(?:[\\\\/][^\\\\/\\:\\?\\*\r\n\"]+)+";
		this.pattern = Pattern.compile(regExp);
	}
	
	public void connect(TextConsole console) {
		this.console = console;
		this.createPattern();
		this.loadPreferences();
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith(SVNTeamPreferences.CONSOLE_BASE)) {
			this.loadPreferences();
		}
	}
	
	public void disconnect() {
		this.console = null;
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this);
	}

	public void matchFound(PatternMatchEvent event) {
		if (this.console == null || !this.enabled) {
			return;
		}
		UIMonitorUtility.doTaskBusyDefault(new AddConsoleHyperlinkOperation(event));
	}
	
	protected void loadPreferences() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		this.enabled = SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_HYPERLINKS_ENABLED_NAME);
	}
	
	protected class AddConsoleHyperlinkOperation extends AbstractActionOperation {
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
			
	    	Matcher matcher = LocalPathMatcher.this.pattern.matcher(path);
	    	if (matcher.find(0)) {
				String link = matcher.group(matcher.groupCount()).trim();
				LocalPathMatcher.this.console.addHyperlink(new LocalFileHyperlink(link), offset + matcher.start(matcher.groupCount()), link.length());
	    	}
		}
		
	}

}
