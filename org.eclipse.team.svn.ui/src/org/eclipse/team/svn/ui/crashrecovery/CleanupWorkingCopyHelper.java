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

package org.eclipse.team.svn.ui.crashrecovery;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Handle invalid meta-information problem here
 * 
 * @author Alexander Gurov
 */
public class CleanupWorkingCopyHelper implements IResolutionHelper {

	public boolean acquireResolution(ErrorDescription description) {
		if (description.code == ErrorDescription.WORKING_COPY_REQUIRES_CLEANUP) {
			final IProject project = (IProject)description.context;
			ISVNConnector proxy = CoreExtensionsManager.instance().getSVNConnectorFactory().createConnector();
			try {
				proxy.cleanup(FileUtility.getWorkingCopyPath(project), new SVNNullProgressMonitor());
				return true;
			}		
			catch (SVNConnectorException ex) {
				// do nothing
			}
			finally {
				proxy.dispose();
			}
		}
		return false;
	}
	
}
