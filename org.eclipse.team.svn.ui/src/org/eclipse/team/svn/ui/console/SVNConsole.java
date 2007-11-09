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

package org.eclipse.team.svn.ui.console;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * SVN Console implementation
 * 
 * @author Alexander Gurov
 */
public class SVNConsole extends MessageConsole implements IPropertyChangeListener {
	public static final String SVN_CONSOLE_TYPE = "org.eclipse.team.svn.ui.console.SVNConsole";
	
	protected MessageConsoleStream cmdStream;
	protected MessageConsoleStream okStream;
	protected MessageConsoleStream warningStream;
	protected MessageConsoleStream errorStream;
	
	protected int autoshow;
	protected boolean enabled;
	
	public SVNConsole() {
		super(SVNTeamUIPlugin.instance().getResource("SVNConsole.Name"), SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/console.gif"));
		
		super.setType(SVNConsole.SVN_CONSOLE_TYPE);
		
		super.init();
		
		this.setTabWidth(4);
		
		this.cmdStream = this.newMessageStream();
		this.okStream = this.newMessageStream();
		this.warningStream = this.newMessageStream();
		this.errorStream = this.newMessageStream();
		
		this.loadPreferences();
		
		JFaceResources.getFontRegistry().addListener(this);
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	public IConsoleStream getConsoleStream() {
		return new SVNConsoleStream();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith(SVNTeamPreferences.CONSOLE_BASE)) {
			this.loadPreferences();
		}
	}
	
	public void shutdown() {
		super.dispose();
		
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getFontRegistry().removeListener(this);
		
		this.enabled = false;
		
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {this});
		
		Color tmp1 = this.cmdStream.getColor();
		Color tmp2 = this.okStream.getColor();
		Color tmp3 = this.warningStream.getColor();
		Color tmp4 = this.errorStream.getColor();
		
		// unsupported in Eclipse IDE 3.0
		try {this.cmdStream.close();} catch (Exception ex) {}
		try {this.okStream.close();} catch (Exception ex) {}
		try {this.warningStream.close();} catch (Exception ex) {}
		try {this.errorStream.close();} catch (Exception ex) {}
		
		if (tmp1 != null) {
			tmp1.dispose();
		}
		if (tmp1 != null) {
			tmp2.dispose();
		}
		if (tmp1 != null) {
			tmp3.dispose();
		}
		if (tmp1 != null) {
			tmp4.dispose();
		}
	}
	
	protected void init() {

	}
	
	protected void dispose() {

	}
	
	protected void loadPreferences() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		this.enabled = SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_ENABLED_NAME);
		
		Color tmp = this.cmdStream.getColor();
		this.cmdStream.setColor(SVNTeamPreferences.getConsoleColor(store, SVNTeamPreferences.CONSOLE_CMD_COLOR_NAME));
		if (tmp != null && !tmp.equals(this.cmdStream.getColor())) {
			tmp.dispose();
		}
		tmp = this.okStream.getColor();
		this.okStream.setColor(SVNTeamPreferences.getConsoleColor(store, SVNTeamPreferences.CONSOLE_OK_COLOR_NAME));
		if (tmp != null && !tmp.equals(this.okStream.getColor())) {
			tmp.dispose();
		}
		tmp = this.warningStream.getColor();
		this.warningStream.setColor(SVNTeamPreferences.getConsoleColor(store, SVNTeamPreferences.CONSOLE_WRN_COLOR_NAME));
		if (tmp != null && !tmp.equals(this.warningStream.getColor())) {
			tmp.dispose();
		}
		tmp = this.errorStream.getColor();
		this.errorStream.setColor(SVNTeamPreferences.getConsoleColor(store, SVNTeamPreferences.CONSOLE_ERR_COLOR_NAME));
		if (tmp != null && !tmp.equals(this.errorStream.getColor())) {
			tmp.dispose();
		}

		if (SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_WRAP_ENABLED_NAME)) {
			this.setConsoleWidth(SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_WRAP_WIDTH_NAME));
		}
		else {
			this.setConsoleWidth(-1); 
		}
		
		this.autoshow = SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NAME);

		if (SVNTeamPreferences.getConsoleBoolean(store, SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_NAME)) {
			int limit = SVNTeamPreferences.getConsoleInt(store, SVNTeamPreferences.CONSOLE_LIMIT_VALUE_NAME);
			this.setWaterMarks(1000 < limit ? 1000 : limit - 1, limit);
		}
		else {
			this.setWaterMarks(-1, 0);
		}
		
		UIMonitorUtility.getDisplay().asyncExec(new Runnable() {
			public void run() {
				SVNConsole.this.setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_FONT_NAME)));
			}
		});
		
		if (this.enabled) {
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {this});
		}
		else {
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {this});
		}
	}

	protected class SVNConsoleStream implements IConsoleStream {
		protected long start;
		protected String buffer;
		protected boolean outputStarted;
		protected boolean hasError;
		protected boolean hasWarning;
		protected boolean activated;
		protected boolean cancelled;
		
		public SVNConsoleStream() {

		}

		public void markEnd() {
			if (this.outputStarted) {
				this.write(IConsoleStream.LEVEL_CMD, "*** ");
				if (this.hasError) {
					this.write(IConsoleStream.LEVEL_ERROR, SVNTeamUIPlugin.instance().getResource("SVNConsole.Error"));
				}
				else if (this.hasWarning) {
					this.write(IConsoleStream.LEVEL_WARNING, SVNTeamUIPlugin.instance().getResource("SVNConsole.Warning"));
				}
				else {
					this.write(IConsoleStream.LEVEL_CMD, this.cancelled ? SVNTeamUIPlugin.instance().getResource("SVNConsole.Cancelled") : SVNTeamUIPlugin.instance().getResource("SVNConsole.Ok"));
				}
				this.write(IConsoleStream.LEVEL_CMD, " " + MessageFormat.format(SVNTeamUIPlugin.instance().getResource("SVNConsole.Took"), new String[] {new SimpleDateFormat("mm:ss.SSS").format(new Date(System.currentTimeMillis() - this.start))}) + "\n\n");
			}
		}

		public void markStart(String data) {
			this.start = System.currentTimeMillis();
			this.buffer = data;
		}
		
		public void doComplexWrite(Runnable runnable) {
			this.flushBuffer();
			if (SVNConsole.this.enabled) {
				runnable.run();
			}
		}

		public void write(int severity, String data) {
			this.flushBuffer();
			if (SVNConsole.this.enabled) {
				if (!this.activated &&
					(SVNConsole.this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ALWAYS ||
					SVNConsole.this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_ERROR && 
					severity == IConsoleStream.LEVEL_ERROR ||
					SVNConsole.this.autoshow == SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_WARNING_ERROR && 
					(severity == IConsoleStream.LEVEL_ERROR || severity == IConsoleStream.LEVEL_WARNING))) {
					this.activated = true;
					ConsolePlugin.getDefault().getConsoleManager().showConsoleView(SVNConsole.this);
				}
				switch (severity) {
					case IConsoleStream.LEVEL_CMD: {
						this.print(SVNConsole.this.cmdStream, data);
						break;
					}
					case IConsoleStream.LEVEL_OK: {
						this.print(SVNConsole.this.okStream, data);
						break;
					}
					case IConsoleStream.LEVEL_WARNING: {
						this.hasWarning = true;
						this.print(SVNConsole.this.warningStream, data);
						break;
					}
					case IConsoleStream.LEVEL_ERROR: 
					default: {
						this.hasError = true;
						this.print(SVNConsole.this.errorStream, data);
						break;
					}
				}
			}
		}

		public void markCancelled() {
			this.cancelled = true;
		}
		
		protected void print(final MessageConsoleStream stream, final String data) {
			// workaround for the Eclipse issue #136943 
			if (UIMonitorUtility.getDisplay().getThread() == Thread.currentThread()) {
				ProgressMonitorUtility.doTaskScheduledDefault(new AbstractNonLockingOperation("Operation.WriteToConsoleResources") {
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						stream.print(data);
					}
				}, true);
			}
			else {
				stream.print(data);
			}
		}
		
		protected void flushBuffer() {
			this.outputStarted = true;
			if (this.buffer != null) {
				String tmp = this.buffer;
				this.buffer = null;
				this.write(IConsoleStream.LEVEL_CMD, "*** ");
				this.write(IConsoleStream.LEVEL_CMD, tmp);
				this.write(IConsoleStream.LEVEL_CMD, "\n");
			}
		}
		
	}
	
}
