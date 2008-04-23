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
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.annotate.BuiltInAnnotate;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The operation shows annotation for repository resource
 * 
 * @author Alexander Gurov
 */
public class RemoteShowAnnotationOperation extends AbstractRepositoryOperation {
	public RemoteShowAnnotationOperation(IRepositoryResource resource) {
		super("Operation.ShowAnnotationRemote", new IRepositoryResource[] {resource});
	}

	public RemoteShowAnnotationOperation(IRepositoryResourceProvider provider) {
		super("Operation.ShowAnnotationRemote", provider);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = UIMonitorUtility.getActivePage();
				
				if (page != null) {
					new BuiltInAnnotate().open(page, RemoteShowAnnotationOperation.this.operableData()[0], null);
				}
			}
		});
	}

}
