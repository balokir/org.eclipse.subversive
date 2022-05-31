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

package org.eclipse.team.svn.core.operation.remote;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Branch and Tag operation implementation
 * 
 * @author Alexander Gurov
 */
public class BranchTagOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String destinationUrl;
	protected String message;
	protected ArrayList<RevisionPair> revisionsPairs;

	public BranchTagOperation(String operationName, Class<? extends NLS> messagesClass, IRepositoryResource []resources, IRepositoryResource destination, String message) {
		super("Operation_" + operationName, messagesClass, resources); //$NON-NLS-1$
		this.destinationUrl = destination.getUrl();
		this.message = message;
	}

	public RevisionPair []getRevisions() {
		return this.revisionsPairs == null ? null : this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList<RevisionPair>();
		IRepositoryResource []resources = this.operableData();
		
		ProgressMonitorUtility.setTaskInfo(monitor, this, FileUtility.getNamesListAsString(resources));
		final IRepositoryLocation location = resources[0].getRepositoryLocation();
		final ISVNConnector proxy = location.acquireSVNProxy();
		try {
			for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
				final IRepositoryResource current = resources[i];
				final String url2 = SVNUtility.encodeURL(BranchTagOperation.this.destinationUrl);

				ISVNNotificationCallback notify = new ISVNNotificationCallback() {
					public void notify(SVNNotification info) {
						BranchTagOperation.this.revisionsPairs.add(new RevisionPair(info.revision, new String[] {url2}, location));
						String message = SVNMessages.format(SVNMessages.Console_CommittedRevision, new String[] {String.valueOf(info.revision)});
						BranchTagOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
					}
				};
				SVNUtility.addSVNNotifyListener(proxy, notify);
				
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						BranchTagOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + current.getUrl() + "\" \"" + BranchTagOperation.this.destinationUrl + "\" -r " + current.getSelectedRevision() + " -m \"" + BranchTagOperation.this.message + "\"" + ISVNConnector.Options.asCommandLine(ISVNConnector.Options.INTERPRET_AS_CHILD) + FileUtility.getUsernameParam(current.getRepositoryLocation().getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
						SVNEntryRevisionReference []src = new SVNEntryRevisionReference[] {new SVNEntryRevisionReference(SVNUtility.encodeURL(current.getUrl()), current.getPegRevision(), current.getSelectedRevision())};
						proxy.copyRemote(src, url2, BranchTagOperation.this.message, ISVNConnector.Options.INTERPRET_AS_CHILD, null, ISVNConnector.NO_EXTERNALS_TO_PIN, new SVNProgressMonitor(BranchTagOperation.this, monitor, null));
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
