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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.annotate.AnnotateView;
import org.eclipse.team.svn.ui.annotate.CheckPerspective;

/**
 * The operation shows annotation for repository resource
 * 
 * @author Alexander Gurov
 */
public class RemoteShowAnnotationOperation extends AbstractRepositoryOperation {
	protected IWorkbenchPage page;
	
	public RemoteShowAnnotationOperation(IRepositoryResource resource, IWorkbenchPage page) {
		super("Operation.ShowAnnotationRemote", new IRepositoryResource[] {resource});
		this.page = page;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		CheckPerspective.run(this.page.getWorkbenchWindow());
	    IViewPart viewPart = this.page.showView(AnnotateView.VIEW_ID);
	    if (viewPart != null && viewPart instanceof AnnotateView) {
			((AnnotateView)viewPart).showEditor(this.operableData()[0]);
	    }
	}

}
