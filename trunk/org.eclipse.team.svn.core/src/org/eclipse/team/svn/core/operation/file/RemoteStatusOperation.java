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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.client.ISVNConnector;
import org.eclipse.team.svn.core.client.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.client.ISVNNotificationCallback;
import org.eclipse.team.svn.core.client.SVNNotification;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation fetch remote resource statuses
 * 
 * @author Alexander Gurov
 */
public class RemoteStatusOperation extends AbstractStatusOperation implements ISVNNotificationCallback {
	protected Map pegRevisions = new HashMap();

	public RemoteStatusOperation(File []files, boolean recursive) {
		super("Operation.UpdateStatusFile", files, recursive);
	}

	public RemoteStatusOperation(IFileProvider provider, boolean recursive) {
		super("Operation.UpdateStatusFile", provider, recursive);
	}
	
	public SVNRevision getPegRevision(File change) {
	    IPath resourcePath = new Path(change.getAbsolutePath());
	    for (Iterator it = this.pegRevisions.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry entry = (Map.Entry)it.next();
	        IPath rootPath = new Path((String)entry.getKey());
	        if (rootPath.isPrefixOf(resourcePath)) {
	            return (SVNRevision)entry.getValue();
	        }
	    }
	    return null;
	}

    public void notify(SVNNotification info) {
    	if (info.revision != SVNRevision.INVALID_REVISION_NUMBER) {
    		this.pegRevisions.put(info.path, SVNRevision.fromNumber(info.revision));
    	}
    }
    
	protected void reportStatuses(final ISVNConnector proxy, final ISVNEntryStatusCallback cb, final File current, IProgressMonitor monitor, int tasks) {
		SVNUtility.addSVNNotifyListener(proxy, this);
    	super.reportStatuses(proxy, cb, current, monitor, tasks);
		SVNUtility.removeSVNNotifyListener(proxy, this);
    }
    
    protected boolean isRemote() {
    	return true;
    }
    
}
