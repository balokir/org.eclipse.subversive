/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import org.eclipse.team.svn.ui.properties.PropertiesEditor;
import org.eclipse.team.svn.ui.properties.PropertiesEditorInput;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

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

	public ShowPropertiesOperation(IWorkbenchPage page, IAdaptable resource, IResourcePropertyProvider propertyProvider) {
		super("Operation.ShowProperties");
		this.resource = resource;
		this.page = page;
		this.propertyProvider = propertyProvider;
	}
	
	public ShowPropertiesOperation(IWorkbenchPage page, IRepositoryResourceProvider provider, IResourcePropertyProvider propertyProvider) {
		this(page, (IRepositoryResource)null, propertyProvider);
		this.provider = provider;
	}
	
	public boolean isEditorOpened() {
		if (this.provider != null) {
			return false;
		}
		
		PropertiesEditorInput input = new PropertiesEditorInput(this.resource, this.propertyProvider);
		final IEditorPart editor = this.page.findEditor(input);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (editor != null) {
					ShowPropertiesOperation.this.page.activate(editor);
				}
			}
		});
		return editor != null;
		
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		if (this.provider != null) {
			this.resource = provider.getRepositoryResources()[0];
		}
		
		final PropertiesEditorInput input = new PropertiesEditorInput(this.resource, this.propertyProvider);
		
		final IEditorPart editor = this.page.findEditor(input);
		
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (editor == null) {
					ShowPropertiesOperation.this.protectStep(new IUnprotectedOperation() {
						public void run(IProgressMonitor monitor) throws Exception {
							ShowPropertiesOperation.this.page.openEditor(input, PropertiesEditor.PROPERTIES_EDITOR_ID);
						}
					}, monitor, 2);
				}
				else {
					ShowPropertiesOperation.this.page.activate(editor);
				}
			}
		});
	}
	
}