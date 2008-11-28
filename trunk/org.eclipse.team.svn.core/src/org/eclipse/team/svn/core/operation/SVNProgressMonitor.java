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
import org.eclipse.team.svn.core.SVNMessages;
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
				String message = SVNMessages.format(SVNMessages.Console_AtRevision, new String[] {String.valueOf(revision)});
				stream.write(IConsoleStream.LEVEL_OK, message);
			}
			else {
				int severity = IConsoleStream.LEVEL_OK;
				String status = null;
				switch (action) {
					case PerformedAction.ADD: 
					case PerformedAction.UPDATE_ADD: 
					case PerformedAction.COMMIT_ADDED: {
						status = SVNMessages.Console_Action_Added;
						break;
					}
					case PerformedAction.DELETE: 
					case PerformedAction.UPDATE_DELETE: 
					case PerformedAction.COMMIT_DELETED: {
						status = SVNMessages.Console_Action_Deleted;
						break;
					}
					case PerformedAction.UPDATE_UPDATE: {
						int resourceState = contentState == NodeStatus.INAPPLICABLE || contentState == NodeStatus.UNCHANGED ? propState : contentState;
						severity = 
							contentState == NodeStatus.CONFLICTED || contentState == NodeStatus.OBSTRUCTED  || propState == NodeStatus.CONFLICTED ? 
							IConsoleStream.LEVEL_WARNING : 
							IConsoleStream.LEVEL_OK;
						if (resourceState >= 0 && resourceState < NodeStatus.shortStatusNames.length) {
							status = SVNMessages.getString("Console_Update_Status_" + NodeStatus.statusNames[resourceState]); //$NON-NLS-1$
							if (status.length() > 0) {
								break;
							}
						}
						status = " "; //$NON-NLS-1$
						break;
					}
					case PerformedAction.COMMIT_MODIFIED: {
						status = SVNMessages.Console_Action_Modified;
						break;
					}
					case PerformedAction.COMMIT_REPLACED: {
						status = SVNMessages.Console_Action_Replaced;
						break;
					}
					case PerformedAction.REVERT: {
						status = SVNMessages.Console_Action_Reverted;
						break;
					}
					case PerformedAction.RESTORE: {
						status = SVNMessages.Console_Action_Restored;
						break;
					}
					case PerformedAction.LOCKED: {
						status = SVNMessages.Console_Action_Locked;
						break;
					}
					case PerformedAction.UNLOCKED: {
						status = SVNMessages.Console_Action_Unlocked;
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
					String message = SVNMessages.format(SVNMessages.Console_TransmittingData, new String[] {path});
					stream.write(severity, message);
				}
				else if (status != null) {
					String message = SVNMessages.format(SVNMessages.Console_Status, new String[] {status, path});
					stream.write(severity, message);
				}
			}
		}
	}
	
	protected static String getStatus(int resourceState) {
		switch (resourceState) {
			case SVNEntryStatus.Kind.ADDED: {
				return SVNMessages.Console_Status_Added;
			}
			case SVNEntryStatus.Kind.MODIFIED: {
				return SVNMessages.Console_Status_Modified;
			}
			case SVNEntryStatus.Kind.DELETED: {
				return SVNMessages.Console_Status_Deleted;
			}
			case SVNEntryStatus.Kind.MISSING: {
				return SVNMessages.Console_Status_Missing;
			}
			case SVNEntryStatus.Kind.REPLACED: {
				return SVNMessages.Console_Status_Replaced;
			}
			case SVNEntryStatus.Kind.MERGED: {
				return SVNMessages.Console_Status_Merged;
			}
			case SVNEntryStatus.Kind.CONFLICTED: {
				return SVNMessages.Console_Status_Conflicted;
			}
			case SVNEntryStatus.Kind.OBSTRUCTED: {
				return SVNMessages.Console_Status_Obstructed;
			}
			default: {
				return null;
			}
		}
	}
	
}
