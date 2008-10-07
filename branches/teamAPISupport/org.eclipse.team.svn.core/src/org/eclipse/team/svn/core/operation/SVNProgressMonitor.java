/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus;
import org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Default progress monitor implementation
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
			if (action == PerformedAction.UPDATE_COMPLETED || action == PerformedAction.STATUS_COMPLETED) {
				String message = SVNTeamPlugin.instance().getResource("Console.AtRevision", new String[] {String.valueOf(revision)});
				stream.write(IConsoleStream.LEVEL_OK, message);
			}
			else {
				int severity = IConsoleStream.LEVEL_OK;
				String status = null;
				switch (action) {
					case PerformedAction.ADD: 
					case PerformedAction.UPDATE_ADD: 
					case PerformedAction.COMMIT_ADDED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Added");
						break;
					}
					case PerformedAction.DELETE: 
					case PerformedAction.UPDATE_DELETE: 
					case PerformedAction.COMMIT_DELETED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Deleted");
						break;
					}
					case PerformedAction.UPDATE_UPDATE: {
						int resourceState = contentState == NodeStatus.INAPPLICABLE || contentState == NodeStatus.UNCHANGED ? propState : contentState;
						severity = 
							contentState == NodeStatus.CONFLICTED || contentState == NodeStatus.OBSTRUCTED  || propState == NodeStatus.CONFLICTED ? 
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						if (resourceState >= 0 && resourceState < NodeStatus.shortStatusNames.length) {
							status = SVNTeamPlugin.instance().getResource("Console.Update.Status." + NodeStatus.statusNames[resourceState]);
							if (status.length() > 0) {
								break;
							}
						}
						status = " ";
						break;
					}
					case PerformedAction.COMMIT_MODIFIED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Modified");
						break;
					}
					case PerformedAction.COMMIT_REPLACED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Replaced");
						break;
					}
					case PerformedAction.REVERT: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Reverted");
						break;
					}
					case PerformedAction.RESTORE: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Restored");
						break;
					}
					case PerformedAction.LOCKED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Locked");
						break;
					}
					case PerformedAction.UNLOCKED: {
						status = SVNTeamPlugin.instance().getResource("Console.Action.Unlocked");
						break;
					}
					default: {
						int resourceState = contentState == SVNEntryStatus.Kind.NORMAL ? propState : contentState;
						status = SVNProgressMonitor.getStatus(resourceState);
						severity = 
							resourceState == SVNEntryStatus.Kind.CONFLICTED || resourceState == SVNEntryStatus.Kind.OBSTRUCTED ?
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						break;
					}
				}
				if (action == PerformedAction.COMMIT_POSTFIX_TXDELTA) {
					String message = SVNTeamPlugin.instance().getResource("Console.TransmittingData", new String[] {path});
					stream.write(severity, message);
				}
				else if (status != null) {
					String message = SVNTeamPlugin.instance().getResource("Console.Status", new String[] {status, path});
					stream.write(severity, message);
				}
			}
		}
	}
	
	protected static String getStatus(int resourceState) {
		switch (resourceState) {
			case SVNEntryStatus.Kind.ADDED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Added");
			}
			case SVNEntryStatus.Kind.MODIFIED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Modified");
			}
			case SVNEntryStatus.Kind.DELETED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Deleted");
			}
			case SVNEntryStatus.Kind.MISSING: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Missing");
			}
			case SVNEntryStatus.Kind.REPLACED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Replaced");
			}
			case SVNEntryStatus.Kind.MERGED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Merged");
			}
			case SVNEntryStatus.Kind.CONFLICTED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Conflicted");
			}
			case SVNEntryStatus.Kind.OBSTRUCTED: {
				return SVNTeamPlugin.instance().getResource("Console.Status.Obstructed");
			}
			default: {
				return null;
			}
		}
	}
	
}
