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

package org.eclipse.team.svn.m2e;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.scm.MavenProjectScmInfo;
import org.eclipse.m2e.scm.spi.ScmHandler;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVN-specific source control manager handler
 * 
 * @author Alexander Gurov
 */
@SuppressWarnings("restriction")
public class SVNScmHandler extends ScmHandler {
	public static final String SVN_SCM_ID = "scm:svn:";
	
	public InputStream open(String url, String revision) throws CoreException {
		IRepositoryContainer container = this.getRepositoryContainer(url, revision);
		
		GetFileContentOperation op = new GetFileContentOperation(container.asRepositoryFile(url + "/" + "pom.xml", false));
		
		this.runOperation(op, null);
		
		return op.getContent();
	}

	public void checkoutProject(MavenProjectScmInfo projectInfo, File destination, IProgressMonitor monitor) throws CoreException, InterruptedException {
		IRepositoryContainer container = this.getRepositoryContainer(projectInfo.getFolderUrl(), projectInfo.getRevision());
		
		IActionOperation op = new CheckoutAsOperation(destination, container, SVNDepth.INFINITY, false, true);
		
		String locationId = container.getRepositoryLocation().getId();
		if (SVNRemoteStorage.instance().getRepositoryLocation(locationId) == null) {
			CompositeOperation cOp = new CompositeOperation(op.getOperationName(), op.getMessagesClass());
			cOp.add(op);
			AddRepositoryLocationOperation add = new AddRepositoryLocationOperation(container.getRepositoryLocation());
			cOp.add(add, new IActionOperation[] {op});
			cOp.add(new SaveRepositoryLocationsOperation(), new IActionOperation[] {add});
			op = cOp;
		}
		
		this.runOperation(op, monitor);
	}
	
	protected void runOperation(IActionOperation op, IProgressMonitor monitor) throws CoreException {
		ProgressMonitorUtility.doTaskExternal(op, monitor, ILoggedOperationFactory.EMPTY);
		
		if (op.getExecutionState() != IActionOperation.OK) {
			throw new CoreException(op.getStatus());
		}
	}

	protected IRepositoryContainer getRepositoryContainer(String url, String revision) throws CoreException {
		// SCM id must be always valid if caller works fine
		url = url.substring(SVNScmHandler.SVN_SCM_ID.length());
		
		// Force svn to verify the url
		try {
			SVNUtility.getSVNUrl(url);
		} 
		catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, getClass().getName(), 0, "Invalid url " + url, e));
		}
		
		IRepositoryContainer retVal = (IRepositoryContainer)SVNUtility.asRepositoryResource(url, true);
		//NOTE peg revision is not specified and something will go wrong if URL is not from HEAD revision...
		retVal.setSelectedRevision(SVNRevision.fromString(revision));
		return retVal;
	}
	
}
