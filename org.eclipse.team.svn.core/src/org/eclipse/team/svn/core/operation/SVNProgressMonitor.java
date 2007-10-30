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
import org.eclipse.team.svn.core.client.StatusKind;
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
			if (action == NotifyAction.update_completed || action == NotifyAction.status_completed) {
				String message = SVNTeamPlugin.instance().getResource("Console.AtRevision");
				stream.write(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(revision)}));
			}
			else {
				int severity = IConsoleStream.LEVEL_OK;
				String status = null;
				switch (action) {
					case NotifyAction.add: 
					case NotifyAction.update_add: 
					case NotifyAction.commit_added: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Added");
						break;
					}
					case NotifyAction.delete: 
					case NotifyAction.update_delete: 
					case NotifyAction.commit_deleted: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Deleted");
						break;
					}
					case NotifyAction.update_update: {
						int resourceState = contentState == NotifyStatus.inapplicable || contentState == NotifyStatus.unchanged ? propState : contentState;
						severity = 
							contentState == NotifyStatus.conflicted || contentState == NotifyStatus.conflicted_unresolved || contentState == NotifyStatus.obstructed  ||
							propState == NotifyStatus.conflicted || propState == NotifyStatus.conflicted_unresolved ? 
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
					case NotifyAction.commit_modified: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Modified");
						break;
					}
					case NotifyAction.commit_replaced: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Replaced");
						break;
					}
					case NotifyAction.revert: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Reverted");
						break;
					}
					case NotifyAction.restore: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Restored");
						break;
					}
					case NotifyAction.locked: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Locked");
						break;
					}
					case NotifyAction.unlocked: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Unlocked");
						break;
					}
					default: {
						int resourceState = contentState == StatusKind.normal ? propState : contentState;
						status = SVNProgressMonitor.getStatus(resourceState);
						severity = 
							resourceState == StatusKind.conflicted || resourceState == StatusKind.obstructed ?
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						break;
					}
				}
				if (action == NotifyAction.commit_postfix_txdelta) {
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
			case StatusKind.added: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Added");
			}
			case StatusKind.modified: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Modified");
			}
			case StatusKind.deleted: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Deleted");
			}
			case StatusKind.missing: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Missing");
			}
			case StatusKind.replaced: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Replaced");
			}
			case StatusKind.merged: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Merged");
			}
			case StatusKind.conflicted: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Conflicted");
			}
			case StatusKind.obstructed: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Obstructed");
			}
			default: {
				return null;
			}
		}
	}
	
}
