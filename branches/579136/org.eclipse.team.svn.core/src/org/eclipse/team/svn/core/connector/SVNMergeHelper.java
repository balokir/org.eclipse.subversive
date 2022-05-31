/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.connector;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.team.svn.core.connector.SVNConflictDescriptor.Action;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Collects merge status information 
 *   
 * @author Alexander Gurov
 */
public class SVNMergeHelper {
	protected ISVNConnector connector;

	public SVNMergeHelper(ISVNConnector connector) {
		this.connector = connector;
	}
	
	public void mergeStatus(SVNEntryReference reference, SVNRevisionRange []revisions, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		this.mergeStatus(reference, null, revisions, path, depth, options, cb, monitor);
	}

	public void mergeStatus(SVNEntryRevisionReference reference1, SVNEntryRevisionReference reference2, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		this.mergeStatus(reference1, reference2, null, path, depth, options, cb, monitor);
	}
	
	public void mergeStatus(SVNEntryReference reference, String mergePath, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		this.mergeStatus(reference, null, null, mergePath, SVNDepth.INFINITY, options, cb, monitor);
	}
	
	protected void mergeStatus(SVNEntryReference reference1, SVNEntryRevisionReference reference2, SVNRevisionRange []revisions, String path, SVNDepth depth, long options, ISVNMergeStatusCallback cb, ISVNProgressMonitor monitor) throws SVNConnectorException {
		final ArrayList<SVNNotification> tmp = new ArrayList<SVNNotification>();
		ISVNNotificationCallback listener = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				tmp.add(info);
			}
		};
		SVNUtility.addSVNNotifyListener(this.connector, listener);
		
		try
		{
			if (reference2 != null) {
				this.connector.mergeTwo((SVNEntryRevisionReference)reference1, reference2, path, depth, options, monitor);
			}
			else if (revisions != null) {
				this.connector.merge(reference1, revisions, path, depth, options, monitor);
			}
			else {
				this.connector.mergeReintegrate(reference1, path, options, monitor);
			}
		}
		finally 
		{
			SVNUtility.removeSVNNotifyListener(this.connector, listener);
		}
			
		SVNRevision from = reference2 == null ? (revisions != null ? revisions[0].from : SVNRevision.fromNumber(1)) : ((SVNEntryRevisionReference)reference1).revision;
		SVNRevision to = reference2 == null ? (revisions != null ? revisions[revisions.length - 1].to : reference1.pegRevision) : reference2.revision;
		if (from.getKind() != SVNRevision.Kind.NUMBER) {
			SVNLogEntry []entries = SVNUtility.logEntries(this.connector, reference1, from, SVNRevision.fromNumber(1), ISVNConnector.Options.NONE, ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, monitor);
			from = SVNRevision.fromNumber(entries[0].revision);
		}
		if (to.getKind() != SVNRevision.Kind.NUMBER) {
			SVNLogEntry []entries = SVNUtility.logEntries(this.connector, reference2 == null ? reference1 : reference2, to, SVNRevision.fromNumber(1), ISVNConnector.Options.NONE, ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, monitor);
			to = SVNRevision.fromNumber(entries[0].revision);
		}
		//tag creation revision greater than last changed revision of CopiedFromURL
		if (reference2 != null) {
			if (from.equals(to)) {
				from = SVNRevision.fromNumber(((SVNRevision.Number)to).getNumber() - 1);
			}
		}
		boolean reversed =
			reference2 == null ? 
			SVNUtility.compareRevisions(from, to, new SVNEntryRevisionReference(reference1.path, reference1.pegRevision, from), new SVNEntryRevisionReference(reference1.path, reference1.pegRevision, to), this.connector) == 1 :
			SVNUtility.compareRevisions(from, to, (SVNEntryRevisionReference)reference1, reference2, this.connector) == 1;
		
		String startUrlPref = reference1.path;
		String endUrlPref = reference2 == null ? reference1.path : reference2.path;
		SVNLogEntry []allMsgs = 
			reversed ? 
			SVNUtility.logEntries(this.connector, reference2 == null ? reference1 : this.getValidReference(reference2, from, monitor), from, to, ISVNConnector.Options.DISCOVER_PATHS, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, 0, monitor) : 
			SVNUtility.logEntries(this.connector, reference2 == null ? reference1 : reference2, to, from, ISVNConnector.Options.DISCOVER_PATHS, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, 0, monitor);
		long minRev = ((SVNRevision.Number)(reversed ? to : from)).getNumber();
		for (Iterator<SVNNotification> it = tmp.iterator(); it.hasNext() && !monitor.isActivityCancelled(); ) {
			SVNNotification state = it.next();
			SVNEntry.Kind kind = state.kind;
			
			String tPath = state.path.substring(path.length());
			String startUrl = SVNUtility.normalizeURL(startUrlPref + tPath);
			String endUrl = SVNUtility.normalizeURL(endUrlPref + tPath);
			boolean skipped = state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.SKIP;							
			org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE;
			org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind pState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE;
							
			boolean hasTreeConflict = state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.TREE_CONFLICT;
			SVNConflictDescriptor treeConflict = null;
			if (hasTreeConflict) {
				SVNEntryInfo[] infos = SVNUtility.info(this.connector, new SVNEntryRevisionReference(state.path), SVNDepth.EMPTY, monitor);
				if (infos.length > 0 && infos[0].treeConflicts != null && infos[0].treeConflicts.length > 0) {
					treeConflict = infos[0].treeConflicts[0];						
					kind = infos[0].kind;
					
					if (treeConflict.conflictKind == SVNConflictDescriptor.Kind.CONTENT) {
						cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED;
					} else {
						pState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED;
					}
				}
				else {
					hasTreeConflict = false; // why is there no info available?
				}
			}
			
			if (state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.UPDATE_ADD) {
				cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.ADDED;
			}
			else if (state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.UPDATE_DELETE) {
				cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.DELETED;
			}
			else if (state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.UPDATE_UPDATE) {
				pState = state.propState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.CHANGED ? org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED :
							(state.propState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.CONFLICTED ? 
							org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED : 
							org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE);
				cState = 
					(state.contentState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.CHANGED || state.contentState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.MERGED) ? 
							org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED : 
							(state.contentState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.CONFLICTED ? 
							org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED : 
							org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE);
			}
			else if (state.action == org.eclipse.team.svn.core.connector.SVNNotification.PerformedAction.SKIP) {
				if (state.contentState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.MISSING) {
					try {
						SVNRevision pegRev = reference1.pegRevision;
						if (reference2 != null) {
							pegRev = reference2.pegRevision;
						}
						SVNUtility.info(this.connector, new SVNEntryRevisionReference(endUrl, pegRev, to), SVNDepth.EMPTY, monitor);
						pState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED;
						cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED;
					}
					catch (Exception ex) {
						cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.DELETED;
					}
				}
				else if (state.contentState == org.eclipse.team.svn.core.connector.SVNNotification.NodeStatus.OBSTRUCTED) {
					cState = org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.ADDED;
				}
			}
			
			if (cState != org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE || pState != org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.NONE || hasTreeConflict) {
				long startRevision = SVNRevision.INVALID_REVISION_NUMBER;
				long endRevision = SVNRevision.INVALID_REVISION_NUMBER;
				long date = 0;
				String author = null;
				String message = null;
				if (cState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.ADDED && !reversed || 
					cState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.DELETED && reversed ||
				    (hasTreeConflict && (treeConflict.action == Action.ADD && !reversed || treeConflict.action == Action.DELETE && reversed))) {
					int idx = this.getLogIndex(allMsgs, endUrl, false);
					if (idx != -1) {
						endRevision = allMsgs[idx].revision;
						date = allMsgs[idx].date;
						author = allMsgs[idx].author;
						message = allMsgs[idx].message;
					}
				}
				else if (cState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED || cState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED || 
						pState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.MODIFIED ||  pState == org.eclipse.team.svn.core.connector.SVNEntryStatus.Kind.CONFLICTED ||
						(hasTreeConflict && treeConflict.action == Action.MODIFY)) {
					int idx = this.getLogIndex(allMsgs, endUrl, false);
					if (idx != -1) {
						endRevision = allMsgs[idx].revision;
						date = allMsgs[idx].date;
						author = allMsgs[idx].author;
						message = allMsgs[idx].message;
					}
					idx = this.getLogIndex(allMsgs, startUrl, true);
					startRevision = idx != -1 ? Math.max(allMsgs[idx].revision, minRev) : minRev;
				}
				else {
					int idx = this.getLogIndex(allMsgs, endUrl, false);
					if (idx != -1) {
						endRevision = allMsgs[idx].revision;
						date = allMsgs[idx].date;
						author = allMsgs[idx].author;
						message = allMsgs[idx].message;
					}
					else {
						idx = this.getLogIndex(allMsgs, startUrl, false);
						if (idx != -1) {
							endUrl = startUrl;
							endRevision = allMsgs[idx].revision;
							date = allMsgs[idx].date;
							author = allMsgs[idx].author;
							message = allMsgs[idx].message;
						}
					}
					idx = this.getLogIndex(allMsgs, startUrl, true);
					startRevision = idx != -1 ? Math.max(allMsgs[idx].revision, minRev) : minRev;
				}
				if (reversed) {
					startRevision = endRevision;
					endRevision = allMsgs[allMsgs.length - 1].revision;
					date = allMsgs[allMsgs.length - 1].date;
					author = allMsgs[allMsgs.length - 1].author;
					message = allMsgs[allMsgs.length - 1].message;
				}
				cb.next(new SVNMergeStatus(startUrl, endUrl, state.path, kind, cState, pState, startRevision, endRevision, date, author, message, skipped, hasTreeConflict, treeConflict));
			}
		}
	}
	
	protected SVNEntryReference getValidReference(SVNEntryReference referenceToExisting, SVNRevision lastRevision, ISVNProgressMonitor monitor) throws SVNConnectorException{
		if (referenceToExisting.pegRevision == null) {
			referenceToExisting = new SVNEntryReference(referenceToExisting.path, SVNRevision.HEAD);
		}
		if (referenceToExisting.pegRevision.getKind() != SVNRevision.Kind.HEAD &&
			referenceToExisting.pegRevision.getKind() != SVNRevision.Kind.NUMBER) {
			throw new RuntimeException("Unexpected revision kind. Kind: " + referenceToExisting.pegRevision.getKind());
		}
		if (lastRevision.getKind() != SVNRevision.Kind.NUMBER) {
			throw new RuntimeException("Unexpected last revision kind. Kind: " + lastRevision.getKind());
		}
			
		if (referenceToExisting.pegRevision.getKind() == SVNRevision.Kind.HEAD) {
			return referenceToExisting;
		}
			
		long start = ((SVNRevision.Number)referenceToExisting.pegRevision).getNumber();
		long end = ((SVNRevision.Number)lastRevision).getNumber();
		while (end > start) {
			referenceToExisting = this.getLastValidReference(referenceToExisting, lastRevision, monitor);
			if (!referenceToExisting.pegRevision.equals(lastRevision)) {
				start = ((SVNRevision.Number)referenceToExisting.pegRevision).getNumber() + 1;
				SVNEntryReference tRef = new SVNEntryReference(referenceToExisting.path, SVNRevision.fromNumber(start));
				while (!this.exists(tRef, monitor)) {
					tRef = new SVNEntryReference(tRef.path.substring(0, tRef.path.lastIndexOf("/")), tRef.pegRevision);
				}
				SVNLogEntry []log = SVNUtility.logEntries(this.connector, tRef, tRef.pegRevision, referenceToExisting.pegRevision, ISVNConnector.Options.DISCOVER_PATHS, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, 0, monitor);
				SVNLogPath []paths = log[0].changedPaths;
				boolean renamed = false;
				if (paths != null) {
					String decodedUrl = SVNUtility.decodeURL(referenceToExisting.path);
					for (int k = 0; k < paths.length; k++) {
						if (paths[k].copiedFromPath != null) {
							int idx = decodedUrl.indexOf(paths[k].copiedFromPath);
							if (idx != -1 && (decodedUrl.charAt(idx + paths[k].copiedFromPath.length()) == '/' || decodedUrl.endsWith(paths[k].copiedFromPath))) {
								decodedUrl = decodedUrl.substring(0, idx) + paths[k].path + decodedUrl.substring(idx + paths[k].copiedFromPath.length());
								tRef = new SVNEntryReference(SVNUtility.encodeURL(decodedUrl), tRef.pegRevision);
								renamed = true;
								break;
							}
						}
					}
				}
				referenceToExisting = tRef;
				if (!renamed) {
					return referenceToExisting;
				}
			}
		} 
		return referenceToExisting;
	}
	
	protected SVNEntryReference getLastValidReference(SVNEntryReference referenceToExisting, SVNRevision lastRevision, ISVNProgressMonitor monitor) {
		long start = ((SVNRevision.Number)referenceToExisting.pegRevision).getNumber();
		long end = ((SVNRevision.Number)lastRevision).getNumber();
		do {
			long middle = end - (end - start) / 2; //long is largest type and (end + start) could out of type ranges
			SVNEntryReference tRef = new SVNEntryReference(referenceToExisting.path, SVNRevision.fromNumber(middle));
			if (this.exists(tRef, monitor)) {
				start = middle;
				referenceToExisting = tRef;
			}
			else {
				if (end - start == 1) {
					break;
				}
				end = middle;
			}
		} 
		while (end > start);
		return referenceToExisting;
	}
	
	protected boolean exists(SVNEntryReference reference, ISVNProgressMonitor monitor) {
		try {
			SVNUtility.logEntries(this.connector, reference, reference.pegRevision, reference.pegRevision, ISVNConnector.Options.NONE, ISVNConnector.EMPTY_LOG_ENTRY_PROPS, 1, monitor);
			return true;
		}
		catch (SVNConnectorException e) {
			return false;
		}
	}
	
	protected int getLogIndex(SVNLogEntry []msgs, String url, boolean last) {
		String decodedUrl = SVNUtility.decodeURL(url);
		int retVal = -1;
		for (int j = 0; j < msgs.length; j++) {
			SVNLogPath []paths = msgs[j].changedPaths;
			if (paths != null) {
				int maxPathIdx = -1, maxPathLen = 0;
				for (int k = 0; k < paths.length; k++) {
					if (paths[k] != null && decodedUrl.endsWith(paths[k].path)) {
						if (last) {
							if (paths[k].copiedFromPath != null) {
								maxPathIdx = k;
								maxPathLen = paths[k].path.length();
							}
							retVal = paths[k].action == SVNLogPath.ChangeType.ADDED ? j : -1;
						}
						else {
							return j;
						}
					}
					else if (paths[k].copiedFromPath != null) {
						int idx = decodedUrl.indexOf(paths[k].path);
						if (idx != -1 && (decodedUrl.charAt(idx + paths[k].path.length()) == '/' || decodedUrl.endsWith(paths[k].path)) && paths[k].path.length() > maxPathLen) {
							maxPathIdx = k;
							maxPathLen = paths[k].path.length();
						}
					}
				}
				if (maxPathIdx != -1) {
					int idx = decodedUrl.indexOf(paths[maxPathIdx].path);
					decodedUrl = decodedUrl.substring(0, idx) + paths[maxPathIdx].copiedFromPath + decodedUrl.substring(idx + paths[maxPathIdx].path.length());
				}
			}
		}
		return retVal;
	}

	
}
