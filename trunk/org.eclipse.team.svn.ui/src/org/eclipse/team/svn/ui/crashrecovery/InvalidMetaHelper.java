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

package org.eclipse.team.svn.ui.crashrecovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.extension.factory.ISVNClientWrapperFactory;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.crashrecovery.invalidmeta.ValidClientsSelectionPanel;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Handle invalid meta-information problem here
 * 
 * @author Alexander Gurov
 */
public class InvalidMetaHelper implements IResolutionHelper {

	public boolean acquireResolution(ErrorDescription description) {
		if (description.code == ErrorDescription.CANNOT_READ_PROJECT_METAINFORMATION) {
			final IProject project = (IProject)description.context;
			IPath location = project.getLocation();
			if (location == null || !location.append(SVNUtility.getSVNFolderName()).toFile().exists()) {
				return false;
			}
			ISVNClientWrapperFactory current = CoreExtensionsManager.instance().getSVNClientWrapperFactory();
			String path = location.toString();
			// check if already handled for any other project
			if (this.isValid(current, path)) {
				return true;
			}
			Collection clients = CoreExtensionsManager.instance().getAccessibleClients();
			final ArrayList valid = new ArrayList();
			for (Iterator it = clients.iterator(); it.hasNext(); ) {
				ISVNClientWrapperFactory factory = (ISVNClientWrapperFactory)it.next();
				if (this.isValid(factory, path)) {
					valid.add(factory);
				}
			}
			if (valid.size() == 0) {
				return false;
			}

			final boolean []solved = new boolean[] {false};
			UIMonitorUtility.parallelSyncExec(new Runnable() {
				public void run() {
					DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), new ValidClientsSelectionPanel(project, valid));
					solved[0] = dialog.open() == 0;
				}
			});
			// if user pressed "Ok" the project can be recovered
			return solved[0];
		}
		return false;
	}

	protected boolean isValid(ISVNClientWrapperFactory factory, String path) {
		try {
			ISVNClientWrapper proxy = factory.newInstance();
			try {
				Status []st = proxy.status(path, false, false, true, false, new SVNNullProgressMonitor());
				return st != null && st.length > 0;
			}
			finally {
				proxy.dispose();
			}
		}
		catch (Throwable ex) {
			// any exception including instantiation problems...
			return false;
		}
	}
	
}
