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

package org.eclipse.team.svn.core.operation.local;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.EntryReference;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.INotificationCallback;
import org.eclipse.team.svn.core.client.Notification;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Branch and Tag from working copy operation implementation
 *
 * @author Alexey Mikoyan
 *
 */
public class BranchTagOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryResource destination;
	protected String message;
	
	public BranchTagOperation(String operationName, IResource[] resources, IRepositoryResource destination, String message) {
		super("Operation." + operationName, resources);
		this.destination = destination;
		this.message = message;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource[] resources = this.operableData();
		
		ProgressMonitorUtility.setTaskInfo(monitor, this, FileUtility.getNamesListAsString(resources));
		IRepositoryLocation location = this.destination.getRepositoryLocation();
		final ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			final String destinationUrl = SVNUtility.encodeURL(this.destination.getUrl());
			for (int i = 0; i < resources.length; i++) {
				final String wcPath = FileUtility.getWorkingCopyPath(resources[i]);
				
				INotificationCallback notify = new INotificationCallback() {
					public void notify(Notification info) {
						if (info.revision != -1) {
							String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
							BranchTagOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
						}
					}
				};
				SVNUtility.addSVNNotifyListener(proxy, notify);
				
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						BranchTagOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + wcPath + "\" \"" + destinationUrl + "\" -r " + Revision.WORKING + " -m \"" + BranchTagOperation.this.message + "\"" + FileUtility.getUsernameParam(BranchTagOperation.this.destination.getRepositoryLocation().getUsername()) + "\n");
						EntryReference []src = new EntryReference[] {new EntryReference(wcPath, Revision.WORKING, null)};
						proxy.copy(src, destinationUrl, BranchTagOperation.this.message, true, false, true, new SVNProgressMonitor(BranchTagOperation.this, monitor, null));
					}
				}, monitor, resources.length);
				
				SVNUtility.removeSVNNotifyListener(proxy, notify);
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
