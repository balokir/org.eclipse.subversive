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
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.annotate.BuiltInAnnotate;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The operation shows annotation for the local resource.
 * 
 * @author Alexander Gurov
 * @deprecated use BuiltInAnnotate.open() instead
 */
public class LocalShowAnnotationOperation extends AbstractWorkingCopyOperation {
	
	public LocalShowAnnotationOperation(IResource resource) {
		super("Operation_ShowAnnotation", SVNUIMessages.class, new IResource[] {resource}); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IWorkbenchPage page = UIMonitorUtility.getActivePage();
		if (page != null) {
			new BuiltInAnnotate().open(UIMonitorUtility.getActivePage(), (IFile)this.operableData()[0], UIMonitorUtility.getShell());
		}
	}  					
	
}
