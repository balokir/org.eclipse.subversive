/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.properties.PropertiesView;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Show properties operation
 *
 * @author Sergiy Logvin
 */
public class ShowPropertiesOperation extends AbstractActionOperation {
	protected IAdaptable resource;
	protected IRepositoryResourceProvider provider;
	protected IWorkbenchPage page;
	protected IResourcePropertyProvider propertyProvider;

	public ShowPropertiesOperation(IWorkbenchPage page, IRepositoryResourceProvider provider, IResourcePropertyProvider propertyProvider) {
		this(page, (IRepositoryResource)null, propertyProvider);
		this.provider = provider;
	}
	
	public ShowPropertiesOperation(IWorkbenchPage page, IAdaptable resource, IResourcePropertyProvider propertyProvider) {
		super("Operation.ShowProperties");
		this.resource = resource;
		this.page = page;
		this.propertyProvider = propertyProvider;
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		if (this.provider != null) {
			this.resource = this.provider.getRepositoryResources()[0];
		}
				
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ShowPropertiesOperation.this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws PartInitException {
						PropertiesView view = (PropertiesView)ShowPropertiesOperation.this.page.showView(PropertiesView.VIEW_ID);
						view.setResource(ShowPropertiesOperation.this.resource, ShowPropertiesOperation.this.propertyProvider, false);
					}
				}, monitor, 1);
				
			}
		});
	}
	
}