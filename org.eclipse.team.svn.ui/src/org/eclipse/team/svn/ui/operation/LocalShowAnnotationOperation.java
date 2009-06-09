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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.annotate.BuiltInAnnotate;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.ShowAnnotationPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The operation shows annotation for the local resource.
 * 
 * @author Alexander Gurov
 */
public class LocalShowAnnotationOperation extends AbstractWorkingCopyOperation {
	
	public LocalShowAnnotationOperation(IResource resource) {
		super("Operation_ShowAnnotation", new IResource[] {resource}); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final IResource resource = this.operableData()[0];
    	ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(resource);
    	boolean notExists = IStateFilter.SF_NOTEXISTS.accept(local);
    	final SVNRevision revision = notExists || local.getRevision() == SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.HEAD : SVNRevision.fromNumber(local.getRevision());    	    	
    	final IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
	    remote.setSelectedRevision(revision);
	    
		final CorrectRevisionOperation correctOp = new CorrectRevisionOperation(null, remote, local.getRevision(), resource);
				
		this.protectStep(new IUnprotectedOperation() {
			public void run(IProgressMonitor monitor) throws Exception {
				correctOp.run(monitor);
			}
		}, monitor, 1);				
		if (correctOp.isCancel()) {
			return;
		}						
		if (!correctOp.hasWarning()) {
			remote.setSelectedRevision(SVNRevision.HEAD);
		}
								
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {		    				
				ShowAnnotationPanel panel = new ShowAnnotationPanel(remote);
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
				if (dialog.open() == 0) {
					IWorkbenchPage page = UIMonitorUtility.getActivePage();
					if (page != null) {
					    new BuiltInAnnotate().open(page, remote, (IFile)resource, panel.getRevisions());
					}									
				} 				
			}
		});	
	}  					
	
}
