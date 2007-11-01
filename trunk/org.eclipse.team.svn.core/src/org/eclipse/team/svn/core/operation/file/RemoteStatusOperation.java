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
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.INotificationCallback;
import org.eclipse.team.svn.core.client.Notification;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.IStatusCallback;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation fetch remote resource statuses
 * 
 * @author Alexander Gurov
 */
public class RemoteStatusOperation extends AbstractStatusOperation implements INotificationCallback {
	protected Map pegRevisions = new HashMap();

	public RemoteStatusOperation(File []files, boolean recursive) {
		super("Operation.UpdateStatusFile", files, recursive);
	}

	public RemoteStatusOperation(IFileProvider provider, boolean recursive) {
		super("Operation.UpdateStatusFile", provider, recursive);
	}
	
	public Revision getPegRevision(File change) {
	    IPath resourcePath = new Path(change.getAbsolutePath());
	    for (Iterator it = this.pegRevisions.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry entry = (Map.Entry)it.next();
	        IPath rootPath = new Path((String)entry.getKey());
	        if (rootPath.isPrefixOf(resourcePath)) {
	            return (Revision)entry.getValue();
	        }
	    }
	    return null;
	}

    public void notify(Notification info) {
    	if (info.revision != Revision.SVN_INVALID_REVNUM) {
    		this.pegRevisions.put(info.path, Revision.fromNumber(info.revision));
    	}
    }
    
	protected void reportStatuses(final ISVNClientWrapper proxy, final IStatusCallback cb, final File current, IProgressMonitor monitor, int tasks) {
		SVNUtility.addSVNNotifyListener(proxy, this);
    	super.reportStatuses(proxy, cb, current, monitor, tasks);
		SVNUtility.removeSVNNotifyListener(proxy, this);
    }
    
    protected boolean isRemote() {
    	return true;
    }
    
}
