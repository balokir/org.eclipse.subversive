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

package org.eclipse.team.svn.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.eclipse.core.runtime.Platform;

/**
 * Implementation of the SVN task for Ant
 * 
 * @author Alexander Gurov
 */
public class SVNTask extends Task {
	public static final String CMD_EXPORT = "export";
	public static final String CMD_CAT = "cat";
	
	protected String command;
	protected String url;
	protected String pegRev;
	protected String rev;
	protected String dest;
	protected String username;
	protected String password;
	protected boolean force;
	
	protected String SPACE_WRAP_CHAR = Platform.OS_WIN32.equals(Platform.getOS()) ? "\"" : "'";
	
	public void execute() throws BuildException {
		String cmdLine = null;
		FileOutputStream oStream = null;
		if (SVNTask.CMD_CAT.equals(this.command)) {
			cmdLine = "svn cat " + this.SPACE_WRAP_CHAR + this.url;
			if (this.pegRev != null) {
				cmdLine += "@" + this.pegRev;
			}
			cmdLine += this.SPACE_WRAP_CHAR;
			if (this.rev != null) {
				cmdLine += " -r " + this.rev;
			}
			cmdLine += this.getCreadentialsPart();
			cmdLine += " --non-interactive";
			
			File folder = new File(this.dest).getParentFile();
			folder.mkdirs();
			
			try {
				oStream = new FileOutputStream(this.dest);
			}
			catch (FileNotFoundException e) {
				throw new BuildException(e);
			}
		}
		else if (SVNTask.CMD_EXPORT.equals(this.command)) {
			cmdLine = "svn export ";
					    
			if (this.force) {
				cmdLine += " --force ";
			}
			
			if (this.rev != null) {
				cmdLine += " -r " + this.rev;
			}
			
			cmdLine += " " + this.SPACE_WRAP_CHAR + this.url;
			if (this.pegRev != null) {
				cmdLine += "@" + this.pegRev;
			}
			cmdLine += this.SPACE_WRAP_CHAR;
			cmdLine += " " + this.SPACE_WRAP_CHAR + this.dest + this.SPACE_WRAP_CHAR;
			cmdLine += this.getCreadentialsPart();
			cmdLine += " -q --non-interactive";
		}
		
		if (cmdLine != null) {
			try {
				Execute exe = new Execute(new PumpStreamHandler(oStream == null ? System.out : oStream, System.err));
				exe.setAntRun(this.getProject());
				exe.setCommandline(new Commandline(cmdLine).getCommandline());
				exe.execute();
			}
			catch (IOException e) {
				throw new BuildException(e);
			}
			finally {
				if (oStream != null) {
					try {oStream.close();} catch (IOException ex) {}
				}
			}
		}
	}

	public String getCommand() {
		return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public boolean getForce() {
		return this.force;
	}
	
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPegRev() {
		return this.pegRev;
	}

	public void setPegRev(String pegRev) {
		this.pegRev = pegRev;
	}

	public String getRev() {
		return this.rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getDest() {
		return this.dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	protected String getCreadentialsPart() {
		String cmdLine = " --no-auth-cache";
		if (this.username != null && this.username.length() != 0) {
			cmdLine += " --username " + this.SPACE_WRAP_CHAR + this.username + this.SPACE_WRAP_CHAR;
		}
		if (this.password != null && this.password.length() != 0) {
			cmdLine += " --password " + this.SPACE_WRAP_CHAR + this.password + this.SPACE_WRAP_CHAR;
		}
		return cmdLine;
	}
	
}
