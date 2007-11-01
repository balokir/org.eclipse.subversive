/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNProgressMonitor;
import org.eclipse.team.svn.core.client.NotifyAction;
import org.eclipse.team.svn.core.client.NotifyStatus;
import org.eclipse.team.svn.core.client.Status.Kind;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Default implementation of the org.tmatesoft.svn.core.client.IProgressMonitor
 * 
 * @author Alexander Gurov
 */
public class SVNProgressMonitor implements ISVNProgressMonitor {
	protected org.eclipse.core.runtime.IProgressMonitor monitor;
	protected IActionOperation parent;
	protected IConsoleStream stream;
	protected IPath root;
	protected boolean enableConsoleOutput;

	public SVNProgressMonitor(IActionOperation parent, org.eclipse.core.runtime.IProgressMonitor monitor, IPath root) {
		this(parent, monitor, root, true);
	}
	
	public SVNProgressMonitor(IActionOperation parent, org.eclipse.core.runtime.IProgressMonitor monitor, IPath root, boolean enableConsoleOutput) {
		this.monitor = monitor;
		this.parent = parent;
		this.root = root;
		this.stream = parent.getConsoleStream();
		this.enableConsoleOutput = enableConsoleOutput;
	}

	public void progress(int current, int total, ItemState state) {
		if (state != null && state.path != null) {
//			TODO rework display path...
//			String info = root.toString();
//			info = state.path.length() > info.length() ? state.path.substring(info.length()) : state.path;
			ProgressMonitorUtility.setTaskInfo(this.monitor, this.parent, state.path);
			if (this.enableConsoleOutput) {
				SVNProgressMonitor.writeToConsole(this.stream, state.contentState, state.propState, state.action, state.path, state.revision);
			}
		}
		ProgressMonitorUtility.progress(this.monitor, current, total);
	}

	public boolean isActivityCancelled() {
		return this.monitor.isCanceled();
	}

	public static void writeToConsole(IConsoleStream stream, int contentState, int propState, int action, String path, long revision) {
		if (stream != null && path != null && path.length() > 0) {
			if (action == NotifyAction.UPDATE_COMPLETED || action == NotifyAction.STATUS_COMPLETED) {
				String message = SVNTeamPlugin.instance().getResource("Console.AtRevision");
				stream.write(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(revision)}));
			}
			else {
				int severity = IConsoleStream.LEVEL_OK;
				String status = null;
				switch (action) {
					case NotifyAction.ADD: 
					case NotifyAction.UPDATE_ADD: 
					case NotifyAction.COMMIT_ADDED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Added");
						break;
					}
					case NotifyAction.DELETE: 
					case NotifyAction.UPDATE_DELETE: 
					case NotifyAction.COMMIT_DELETED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Deleted");
						break;
					}
					case NotifyAction.UPDATE_UPDATE: {
						int resourceState = contentState == NotifyStatus.INAPPLICABLE || contentState == NotifyStatus.UNCHANGED ? propState : contentState;
						severity = 
							contentState == NotifyStatus.CONFLICTED || contentState == NotifyStatus.OBSTRUCTED  || propState == NotifyStatus.CONFLICTED ? 
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						if (resourceState >= 0 && resourceState < NotifyStatus.shortStatusNames.length) {
							status = SVNTeamPlugin.instance().getResource("Console.Update.Status." + NotifyStatus.statusNames[resourceState]);
							if (status.length() > 0) {
								break;
							}
						}
						status = " ";
						break;
					}
					case NotifyAction.COMMIT_MODIFIED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Modified");
						break;
					}
					case NotifyAction.COMMIT_REPLACED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Replaced");
						break;
					}
					case NotifyAction.REVERT: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Reverted");
						break;
					}
					case NotifyAction.RESTORE: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Restored");
						break;
					}
					case NotifyAction.LOCKED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Locked");
						break;
					}
					case NotifyAction.UNLOCKED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Unlocked");
						break;
					}
					default: {
						int resourceState = contentState == Kind.NORMAL ? propState : contentState;
						status = SVNProgressMonitor.getStatus(resourceState);
						severity = 
							resourceState == Kind.CONFLICTED || resourceState == Kind.OBSTRUCTED ?
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						break;
					}
				}
				if (action == NotifyAction.COMMIT_POSTFIX_TXDELTA) {
					String message = SVNTeamPlugin.instance().getResource("Console.TransmittingData");
					stream.write(severity, MessageFormat.format(message, new String[] {path}));
				}
				else if (status != null) {
					String message = SVNTeamPlugin.instance().getResource("Console.Status");
					stream.write(severity, MessageFormat.format(message, new String[] {status, path}));
				}
			}
		}
	}
	
	protected static String getStatus(int resourceState) {
		switch (resourceState) {
			case Kind.ADDED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Added");
			}
			case Kind.MODIFIED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Modified");
			}
			case Kind.DELETED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Deleted");
			}
			case Kind.MISSING: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Missing");
			}
			case Kind.REPLACED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Replaced");
			}
			case Kind.MERGED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Merged");
			}
			case Kind.CONFLICTED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Conflicted");
			}
			case Kind.OBSTRUCTED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Obstructed");
			}
			default: {
				return null;
			}
		}
	}
	
}
